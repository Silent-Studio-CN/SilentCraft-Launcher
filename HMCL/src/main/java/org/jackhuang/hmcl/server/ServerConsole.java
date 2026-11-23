// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Owns the running server process and exposes its console output to the UI.
@NotNullByDefault
public final class ServerConsole {
    /// Maximum number of retained console lines.
    private static final int MAX_LINES = 3000;

    /// Console lines shown by the management page.
    private static final ObservableList<String> lines = FXCollections.observableArrayList();

    /// Whether a server process is currently running.
    private static final ReadOnlyBooleanWrapper running = new ReadOnlyBooleanWrapper(false);

    /// Current process, or null.
    private static @Nullable Process process;

    /// Writer used to send commands to the server.
    private static @Nullable BufferedWriter input;

    private ServerConsole() {
    }

    /// Reads the required Java major from version.json, or falls back to Java 8.
    private static int requiredJava(Path directory) {
        Path metadata = directory.resolve("version.json");
        if (Files.isRegularFile(metadata)) {
            try {
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("\"java_version\"\\s*:\\s*(\\d+)")
                        .matcher(Files.readString(metadata, StandardCharsets.UTF_8));
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                }
            } catch (Exception ignored) {
            }
        }
        return 8;
    }

    /// Chooses the smallest installed Java that satisfies the requirement, then the current JVM.
    private static @Nullable Path findJava(int required) {
        try {
            java.util.List<org.jackhuang.hmcl.java.JavaRuntime> runtimes =
                    new java.util.ArrayList<>(org.jackhuang.hmcl.java.JavaManager.getAllJava());
            runtimes.sort(java.util.Comparator.comparingInt(org.jackhuang.hmcl.java.JavaRuntime::getParsedVersion));
            for (org.jackhuang.hmcl.java.JavaRuntime runtime : runtimes) {
                if (runtime.getParsedVersion() >= required) {
                    return runtime.getBinary();
                }
            }
        } catch (Exception ignored) {
        }

        try {
            int current = Integer.parseInt(System.getProperty("java.specification.version"));
            if (current >= required) {
                return Path.of(System.getProperty("java.home"), "bin", "java");
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /// Returns the live console lines.
    public static ObservableList<String> getLines() {
        return lines;
    }

    /// Returns whether a server is running.
    public static ReadOnlyBooleanProperty runningProperty() {
        return running.getReadOnlyProperty();
    }

    /// Starts the given server and streams its output.
    public static void start(ServerRepository.ServerInstance server) {
        stop();

        Path directory = server.directory();
        Path jar = directory.resolve(server.metadata().jar());
        try {
            Path logDirectory = directory.resolve("logs");
            Files.createDirectories(logDirectory);
            Path logFile = logDirectory.resolve("hmcl-server.log");

            int requiredJava = requiredJava(directory);
            Path java = findJava(requiredJava);
            if (java == null) {
                org.jackhuang.hmcl.ui.Controllers.dialog(
                        i18n("server.java.missing.message", requiredJava),
                        i18n("server.java.missing.title"),
                        org.jackhuang.hmcl.ui.construct.MessageDialogPane.MessageType.WARNING);
                return;
            }

            ProcessBuilder builder = new ProcessBuilder(
                    java.toString(), "-Xmx2G", "-jar", jar.toString(), "nogui");
            builder.directory(directory.toFile());
            builder.redirectErrorStream(true);

            Process started = builder.start();
            process = started;
            input = new BufferedWriter(new OutputStreamWriter(started.getOutputStream(), StandardCharsets.UTF_8));
            running.set(true);

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(started.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String value = line;
                        Files.writeString(logFile, value + System.lineSeparator(),
                                StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        Platform.runLater(() -> {
                            lines.add(value);
                            if (lines.size() > MAX_LINES) {
                                lines.remove(0, lines.size() - MAX_LINES);
                            }
                        });
                    }
                } catch (IOException e) {
                    LOG.warning("Server console reader stopped", e);
                } finally {
                    Platform.runLater(() -> running.set(false));
                }
            }, "server-console").start();
        } catch (Exception e) {
            LOG.warning("Failed to start server", e);
        }
    }

    /// Sends one command line to the running server.
    public static void sendCommand(String command) {
        if (input == null || command.isBlank()) {
            return;
        }
        try {
            input.write(command);
            input.newLine();
            input.flush();
        } catch (IOException e) {
            LOG.warning("Failed to send server command", e);
        }
    }

    /// Stops the running server.
    public static void stop() {
        if (process != null) {
            process.destroy();
            process = null;
        }
        input = null;
        running.set(false);
    }
}
