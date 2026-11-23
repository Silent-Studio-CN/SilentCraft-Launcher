// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.glavo.url.WebURL;
import org.jackhuang.hmcl.java.JavaManager;
import org.jackhuang.hmcl.java.JavaRuntime;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jackhuang.hmcl.ui.main.JavaDownloadDialog;
import org.jackhuang.hmcl.util.gson.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Resolves the Java runtime required by a server version and starts the server.
public final class ServerLaunch {
    private ServerLaunch() {
    }

    /// Asks the user whether to start the downloaded server, then checks its Java runtime.
    public static void offerRun(Path jar, Path runDirectory, String versionMetaUrl) {
        Controllers.confirm(i18n("server.run.message"), i18n("server.run.title"),
                () -> launch(jar, runDirectory, versionMetaUrl), null);
    }

    private static void launch(Path jar, Path runDirectory, String versionMetaUrl) {
        new GetTask(WebURL.parse(versionMetaUrl))
                .thenApplyAsync(json -> JsonUtils.fromNonNullJson(json, VersionMetadata.class))
                .whenComplete(Schedulers.javafx(), (metadata, exception) -> {
                    if (exception != null) {
                        LOG.warning("Failed to read server version metadata", exception);
                        start(null, jar, runDirectory, "");
                        return;
                    }

                    int requiredJava = metadata.javaVersion() != null ? metadata.javaVersion().majorVersion() : 0;

                    Task.supplyAsync("Scan Java", JavaManager::getAllJava)
                            .whenComplete(Schedulers.javafx(), (runtimes, scanException) -> {
                                if (scanException != null) {
                                    LOG.warning("Failed to scan Java runtimes", scanException);
                                }
                                resolve(requiredJava, runtimes == null ? List.of() : runtimes, jar, runDirectory);
                            })
                            .start();
                })
                .start();
    }

    private static void resolve(int requiredJava, Collection<JavaRuntime> runtimes, Path jar, Path runDirectory) {
        JavaRuntime matching = runtimes.stream()
                .filter(runtime -> runtime.getInfo().getParsedVersion() == requiredJava)
                .findFirst()
                .orElse(null);

        if (matching != null) {
            start(matching, jar, runDirectory, "");
            return;
        }

        JavaRuntime fallback = runtimes.stream()
                .max(Comparator.comparingInt(runtime -> runtime.getInfo().getParsedVersion()))
                .orElse(null);

        Runnable downloadAction = JavaDownloadDialog.showDialogAction(DownloadProviders.getDownloadProvider());

        if (fallback == null) {
            Controllers.dialog(new MessageDialogPane.Builder(
                    i18n("server.java.missing.message", requiredJava),
                    i18n("server.java.missing.title"),
                    MessageDialogPane.MessageType.WARNING)
                    .addAction(i18n("server.java.download", requiredJava), downloadAction)
                    .addAction(i18n("button.cancel"), null)
                    .build());
            return;
        }

        Controllers.dialog(new MessageDialogPane.Builder(
                i18n("server.java.wrong.message"),
                i18n("server.java.wrong.title"),
                MessageDialogPane.MessageType.WARNING)
                .addAction(i18n("server.java.continue"), () -> start(fallback, jar, runDirectory, ""))
                .addAction(i18n("server.java.download", requiredJava), downloadAction)
                .addAction(i18n("button.cancel"), null)
                .build());
    }

    private static void start(JavaRuntime runtime, Path jar, Path runDirectory, String extraArgs) {
        if (runtime == null) {
            Controllers.dialog(i18n("server.java.missing.message", 0), i18n("server.java.missing.title"), MessageDialogPane.MessageType.WARNING);
            return;
        }

        try {
            Path logDirectory = runDirectory.resolve("logs");
            Files.createDirectories(logDirectory);
            Path logFile = logDirectory.resolve("hmcl-server.log");

            ProcessBuilder builder = new ProcessBuilder(
                    runtime.getBinary().toString(),
                    "-Xmx2G",
                    "-jar",
                    jar.toString(),
                    "nogui");
            if (extraArgs != null && !extraArgs.isBlank()) {
                builder.command().addAll(List.of(extraArgs.split("\\s+")));
            }
            builder.directory(runDirectory.toFile());
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile.toFile()));
            builder.start();

            Controllers.showToast(i18n("server.run.started", runDirectory));
        } catch (IOException e) {
            LOG.warning("Failed to start server", e);
            Controllers.dialog(org.jackhuang.hmcl.util.StringUtils.getStackTrace(e), i18n("server.run.failed"), MessageDialogPane.MessageType.ERROR);
        }
    }

    private record VersionMetadata(JavaVersion javaVersion) {
    }

    private record JavaVersion(int majorVersion) {
    }
}
