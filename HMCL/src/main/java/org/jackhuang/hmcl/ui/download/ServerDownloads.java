// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.stage.DirectoryChooser;
import org.glavo.url.WebURL;
import org.jackhuang.hmcl.download.ComponentRemoteVersion;
import org.jackhuang.hmcl.task.FileDownloadTask;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.util.TaskCancellationAction;
import org.jackhuang.hmcl.util.gson.JsonUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Downloads the official server jar of a game version.
public final class ServerDownloads {
    private static final String OFFICIAL_HOST = "piston-data.mojang.com";
    private static final String MIRROR_HOST = "bmclapi2.bangbang93.com";

    private ServerDownloads() {
    }

    public static void downloadServer(ComponentRemoteVersion version) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n("download.server.choose"));
        Path directory = Controllers.showDialog(chooser);
        if (directory == null) {
            return;
        }

        Path target = directory.resolve("server-" + version.getSelfVersion() + ".jar");

        Task<String> metadataTask = new GetTask(WebURL.parse(version.getUrls().get(0)));
        Task<?> task = metadataTask.thenComposeAsync(json -> {
            VersionMetadata metadata = JsonUtils.fromNonNullJson(json, VersionMetadata.class);
            ServerDownload download = metadata.downloads() != null ? metadata.downloads().server() : null;
            if (download == null) {
                throw new IOException("No server download available for " + version.getSelfVersion());
            }

            List<WebURL> candidates = new ArrayList<>(2);
            candidates.add(WebURL.parse(download.url()));
            if (download.url().contains(OFFICIAL_HOST)) {
                candidates.add(WebURL.parse(download.url().replace(OFFICIAL_HOST, MIRROR_HOST)));
            }

            return new FileDownloadTask(candidates, target,
                    FileDownloadTask.IntegrityCheck.of("SHA-1", download.sha1()));
        });

        org.jackhuang.hmcl.task.Task<Void> dialogTask =
                task.whenComplete(org.jackhuang.hmcl.task.Schedulers.javafx(), (result, exception) -> {
            if (exception == null) {
                org.jackhuang.hmcl.setting.SettingsManager.settings()
                        .setLastServerType(org.jackhuang.hmcl.setting.ServerType.VANILLA);
                org.jackhuang.hmcl.setting.SettingsManager.settings()
                        .setLastServerVersion(version.getSelfVersion());
            }
        });

        Controllers.taskDialog(dialogTask,
                i18n("message.downloading.file", target.getFileName().toString()),
                TaskCancellationAction.NORMAL);
    }

    private record VersionMetadata(Downloads downloads) {
    }

    private record Downloads(ServerDownload server) {
    }

    private record ServerDownload(String url, String sha1) {
    }
}
