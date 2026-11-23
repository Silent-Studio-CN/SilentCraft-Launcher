// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.jackhuang.hmcl.task.FetchTask;
import org.jackhuang.hmcl.task.Task;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

/// Downloads a file with parallel HTTP range requests.
///
/// The Paper CDN limits a single connection, so splitting the file into chunks and
/// fetching them concurrently is noticeably faster than a sequential download.
public final class SilentStudioDownloadTask extends Task<Void> {
    private static final int SEGMENTS = 4;

    private final String url;
    private final Path target;
    private final long size;
    private final @Nullable String sha256;

    public SilentStudioDownloadTask(String url, Path target, long size, @Nullable String sha256) {
        this.url = url;
        this.target = target;
        this.size = size;
        this.sha256 = sha256;
        setName(target.getFileName().toString());
    }

    @Override
    public void execute() throws Exception {
        Files.createDirectories(target.getParent());

        long total = size > 0 ? size : contentLength(url);
        if (total <= 0) {
            downloadWhole(url, target);
            verify();
            return;
        }

        List<Path> parts = new ArrayList<>(SEGMENTS);
        AtomicLong downloaded = new AtomicLong();

        Thread speedReporter = startSpeedReporter(downloaded);

        try {
            downloadSegments(parts, downloaded, total);
        } catch (IOException e) {
            for (Path part : parts) {
                Files.deleteIfExists(part);
            }
            downloadWhole(url, target);
            verify();
            return;
        }

        try (OutputStream out = Files.newOutputStream(target)) {
            for (Path part : parts) {
                Files.copy(part, out);
            }
        } finally {
            stopSpeedReporter(speedReporter);
            for (Path part : parts) {
                Files.deleteIfExists(part);
            }
        }

        verify();
    }

    /// Reports aggregate download speed once per second through the shared fetch event.
    private static Thread startSpeedReporter(AtomicLong downloaded) {
        Thread thread = new Thread(() -> {
            long last = 0;
            try {
                while (true) {
                    Thread.sleep(1000);
                    long now = downloaded.get();
                    FetchTask.SPEED_EVENT.fireEvent(new FetchTask.SpeedEvent(FetchTask.SPEED_EVENT, now - last));
                    last = now;
                }
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }, "paper-download-speed");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    /// Stops the speed reporter thread.
    private static void stopSpeedReporter(Thread thread) {
        thread.interrupt();
    }

    private void downloadSegments(List<Path> parts, AtomicLong downloaded, long total) throws Exception {
        long chunkSize = (total + SEGMENTS - 1) / SEGMENTS;
        ExecutorService pool = Executors.newFixedThreadPool(SEGMENTS);
        List<Future<?>> futures = new ArrayList<>(SEGMENTS);

        try {
            for (int i = 0; i < SEGMENTS; i++) {
                long start = i * chunkSize;
                if (start >= total) {
                    break;
                }
                long end = Math.min(total - 1, start + chunkSize - 1);
                Path part = target.resolveSibling(target.getFileName() + ".part" + i);
                parts.add(part);

                futures.add(pool.submit(() -> {
                    downloadRange(part, start, end, downloaded);
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } catch (Exception e) {
            for (Future<?> future : futures) {
                future.cancel(true);
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("分段下载失败", e);
        } finally {
            pool.shutdownNow();
        }
    }

    private void downloadRange(Path part, long start, long end, AtomicLong downloaded) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("Range", "bytes=" + start + "-" + end);
        connection.setRequestProperty("User-Agent", "HMCL/3.18");
        connection.setInstanceFollowRedirects(true);

        try {
            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException("服务器不支持分段下载: HTTP " + code);
            }

            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(part)) {
                byte[] buffer = new byte[128 * 1024];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    out.write(buffer, 0, read);
                    updateProgress(downloaded.addAndGet(read), size);
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    /// Reads Content-Length for servers that support range requests.
    private static long contentLength(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            long length = connection.getContentLengthLong();
            connection.disconnect();
            return length;
        } catch (Exception e) {
            return -1;
        }
    }

    private void downloadWhole(String url, Path target) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(30_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("User-Agent", "HMCL/3.18");
        connection.setInstanceFollowRedirects(true);

        try (InputStream in = connection.getInputStream();
             OutputStream out = Files.newOutputStream(target)) {
            byte[] buffer = new byte[128 * 1024];
            int read;
            while ((read = in.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
        } finally {
            connection.disconnect();
        }
    }

    private void verify() throws IOException {
        if (sha256 == null || sha256.isBlank()) {
            return;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(target)) {
                byte[] buffer = new byte[128 * 1024];
                int read;
                while ((read = in.read(buffer)) > 0) {
                    digest.update(buffer, 0, read);
                }
            }

            StringBuilder hex = new StringBuilder();
            for (byte b : digest.digest()) {
                hex.append(String.format("%02x", b));
            }

            if (!hex.toString().equalsIgnoreCase(sha256)) {
                Files.deleteIfExists(target);
                throw new IOException("SHA-256 校验失败");
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}
