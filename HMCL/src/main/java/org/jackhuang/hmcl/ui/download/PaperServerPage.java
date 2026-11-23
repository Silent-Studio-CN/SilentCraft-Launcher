// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.glavo.url.WebURL;
import org.jackhuang.hmcl.download.ComponentRemoteVersion;
import org.jackhuang.hmcl.game.GameComponentType;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.util.TaskCancellationAction;
import org.jackhuang.hmcl.util.gson.JsonUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Two-level Paper browser: game versions, then all builds of the selected version.
public final class PaperServerPage extends VBox {
    private static final String PROJECT_API = "https://fill.papermc.io/v3/projects/paper";

    private final VersionsPage versionsPage;

    public PaperServerPage() {
        setSpacing(0);
        versionsPage = new VersionsPage(
                i18n("download.server.paper"),
                org.jackhuang.hmcl.setting.DownloadProviders.getDownloadProvider(),
                GameComponentType.GAME,
                this::showBuilds);
        versionsPage.setVersionTitlePrefix("Paper ");
        versionsPage.setIconOverride(org.jackhuang.hmcl.setting.GameInstanceIconType.PAPER.getIcon());
        VBox.setVgrow(versionsPage, Priority.ALWAYS);
        getChildren().add(versionsPage);
    }

    private void showBuilds(ComponentRemoteVersion version) {
        Controllers.navigate(new PaperBuildsPage(version));
    }

    private void downloadBuild(ComponentRemoteVersion remoteVersion) {
        if (!(remoteVersion instanceof PaperBuildRemoteVersion build)) {
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n("download.server.choose"));
        Path directory = Controllers.showDialog(chooser);
        if (directory == null) {
            return;
        }

        Path target = directory.resolve("paper-" + remoteVersion.getGameVersion() + "-" + build.getBuildId() + ".jar");
        SilentStudioDownloadTask task = new SilentStudioDownloadTask(build.url(), target, build.size(), build.sha256());
        Task<Void> dialogTask = task.whenComplete(Schedulers.javafx(), (result, exception) -> {
            if (exception == null) {
                org.jackhuang.hmcl.setting.SettingsManager.settings()
                        .setLastServerType(org.jackhuang.hmcl.setting.ServerType.PAPER);
                org.jackhuang.hmcl.setting.SettingsManager.settings()
                        .setLastServerVersion(remoteVersion.getGameVersion().toString());
                ServerLaunch.offerRun(target, directory, build.metaUrl());
                }
        });

        Controllers.taskDialog(dialogTask,
                i18n("message.downloading.file", target.getFileName().toString()),
                TaskCancellationAction.NORMAL);
    }

    private static Instant parseTime(String time) {
        try {
            return time == null ? null : Instant.parse(time);
        } catch (Exception e) {
            return null;
        }
    }

    private record Build(int id, String channel, String time, Map<String, Artifact> downloads) {
    }

    private record Artifact(String name, String url, Map<String, String> checksums, long size) {
    }
}
