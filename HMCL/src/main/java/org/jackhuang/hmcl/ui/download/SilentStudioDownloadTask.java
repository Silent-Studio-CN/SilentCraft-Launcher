// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.jackhuang.hmcl.task.FetchTask;
import org.jackhuang.hmcl.task.Task;
import org.jetbrains.annotations.Nullable;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

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
    private static final int MAX_SEGMENTS = 4;
    private static final long SMALL_FILE = 1024L * 1024;
    private static final long MEDIUM_FILE = 8L * 1024 * 1024;

    private final String url;
    private final Path target;
    private final long size;
    private final @Nullable String sha256;
    private final long limitBytesPerSecond;
    private long throttleStartNanos;
    private long throttledBytes;

    public SilentStudioDownloadTask(String url, Path target, long size, @Nullable String sha256) {
        this.url = url;
        this.target = target;
        this.size = size;
        this.sha256 = sha256;
        this.limitBytesPerSecond = org.jackhuang.hmcl.setting.SettingsManager.settings()
                .downloadSpeedLimitProperty().get() * 1024L;
        setName(target.getFileName().toString());
    }

    @Override
    public void execute() throws Exception {
        long startedNanos = System.nanoTime();
        Files.createDirectories(target.getParent());
        throttleStartNanos = System.nanoTime();

        Path temp = target.resolveSibling(target.getFileName() + ".download");
        try {
            long total = size > 0 ? size : contentLength(url);

            if (total <= 0 || total < SMALL_FILE) {
                downloadWhole(url, temp);
                verify(temp);
                Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return;
            }

            int segments = total < MEDIUM_FILE ? 2 : MAX_SEGMENTS;
            List<Path> parts = new ArrayList<>(segments);
            AtomicLong downloaded = new AtomicLong();
            Thread speedReporter = startSpeedReporter(downloaded);

            try {
                try {
                    downloadSegments(parts, downloaded, total, segments);
                } catch (java.util.concurrent.CancellationException e) {
                    throw e;
                } catch (IOException e) {
                    downloadWhole(url, temp);
                }

                if (!Files.exists(temp)) {
                    try (OutputStream out = Files.newOutputStream(temp)) {
                        for (Path part : parts) {
                            Files.copy(part, out);
                        }
                    }
                }

                verify(temp);
                Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } finally {
                stopSpeedReporter(speedReporter);
                for (Path part : parts) {
                    Files.deleteIfExists(part);
                }
            }
        } finally {
            Files.deleteIfExists(temp);
            try {
                long elapsedMillis = (System.nanoTime() - startedNanos) / 1_000_000L;
                long bytes = Files.exists(target) ? Files.size(target) : 0;
                double speed = elapsedMillis > 0 ? bytes / 1024.0 / (elapsedMillis / 1000.0) : 0;
                LOG.info(String.format("SILENTENGINE Successfully Downloaded in %.1f Seconds",
                        elapsedMillis / 1000.0));
            } catch (Exception ignored) {
            }
        }
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

    private void downloadSegments(List<Path> parts, AtomicLong downloaded, long total, int segments) throws Exception {
        long chunkSize = (total + segments - 1) / segments;
        ExecutorService pool = Executors.newFixedThreadPool(segments);
        List<Future<?>> futures = new ArrayList<>(segments);

        try {
            for (int i = 0; i < segments; i++) {
                long start = i * chunkSize;
                if (start >= total) {
                    break;
                }
                long end = Math.min(total - 1, start + chunkSize - 1);
                Path part = target.resolveSibling(target.getFileName() + ".part" + i);
                parts.add(part);

                futures.add(pool.submit(() -> {
                    downloadRange(part, start, end, downloaded, total);
                    return null;
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
        } catch (java.util.concurrent.CancellationException e) {
            for (Future<?> future : futures) {
                future.cancel(true);
            }
            throw e;
        } catch (Exception e) {
            for (Future<?> future : futures) {
                future.cancel(true);
            }
            Throwable cause = e.getCause();
            if (cause instanceof java.util.concurrent.CancellationException cancellation) {
                throw cancellation;
            }
            if (e instanceof IOException ioException) {
                throw ioException;
            }
            throw new IOException("分段下载失败", e);
        } finally {
            pool.shutdownNow();
        }
    }

    private void downloadRange(Path part, long start, long end, AtomicLong downloaded, long total) throws IOException {
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
                    checkCancelled();
                    out.write(buffer, 0, read);
                    throttle(read);
                    updateProgress(downloaded.addAndGet(read), total);
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
                checkCancelled();
                out.write(buffer, 0, read);
                throttle(read);
            }
        } finally {
            connection.disconnect();
        }
    }

    /// Throws when the owning task has been cancelled.
    private void checkCancelled() {
        if (isCancelled()) {
            throw new java.util.concurrent.CancellationException("Download cancelled");
        }
    }

    /// Limits throughput by sleeping until the configured budget catches up.
    private void throttle(long read) {
        if (limitBytesPerSecond <= 0) {
            return;
        }
        throttledBytes += read;
        long expectedNanos = throttledBytes * 1_000_000_000L / limitBytesPerSecond;
        long actualNanos = System.nanoTime() - throttleStartNanos;
        long sleepNanos = expectedNanos - actualNanos;
        if (sleepNanos > 0) {
            try {
                Thread.sleep(sleepNanos / 1_000_000L, (int) (sleepNanos % 1_000_000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void verify(Path file) throws IOException {
        if (sha256 == null || sha256.isBlank()) {
            return;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream in = Files.newInputStream(file)) {
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
                Files.deleteIfExists(file);
                throw new IOException("SHA-256 校验失败");
            }
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }
}
