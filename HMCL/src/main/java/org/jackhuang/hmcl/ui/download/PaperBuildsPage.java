// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import org.glavo.url.WebURL;
import org.jackhuang.hmcl.download.ComponentRemoteVersion;
import org.jackhuang.hmcl.game.GameComponentType;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jackhuang.hmcl.util.TaskCancellationAction;
import org.jackhuang.hmcl.util.gson.JsonUtils;

import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Full-page Paper build list for one Minecraft version.
@org.jetbrains.annotations.NotNullByDefault
public final class PaperBuildsPage extends StackPane implements DecoratorPage {
    private static final String PROJECT_API = "https://fill.papermc.io/v3/projects/paper";

    private final ReadOnlyObjectWrapper<State> state;
    private final VersionsPage buildsPage;

    /// Creates the build page and starts loading builds for {@code version}.
    public PaperBuildsPage(ComponentRemoteVersion version) {
        this.state = new ReadOnlyObjectWrapper<>(State.fromTitle(
                i18n("download.server.paper.builds", version.getSelfVersion())));

        this.buildsPage = new VersionsPage(
                i18n("download.server.paper.builds", version.getSelfVersion()),
                GameComponentType.GAME,
                this::downloadBuild);
        buildsPage.setPaperBuildList(true);
        buildsPage.setVersionTitlePrefix("");
        getChildren().add(buildsPage);

        new GetTask(WebURL.parse(PROJECT_API + "/versions/" + version.getSelfVersion() + "/builds"))
                .thenApplyAsync(json -> JsonUtils.<List<Build>>fromNonNullJson(json, JsonUtils.listTypeOf(Build.class)))
                .whenComplete(Schedulers.javafx(), (builds, exception) -> {
                    if (exception != null) {
                        LOG.warning("Failed to fetch Paper builds", exception);
                        return;
                    }

                    List<ComponentRemoteVersion> items = new ArrayList<>(builds.size());
                    for (Build build : builds) {
                        Artifact artifact = build.downloads() != null ? build.downloads().get("server:default") : null;
                        if (artifact == null) {
                            continue;
                        }
                        String sha256 = artifact.checksums() != null ? artifact.checksums().get("sha256") : null;
                        items.add(new PaperBuildRemoteVersion(
                                version.getGameVersion(), build.id(), build.channel(),
                                parseTime(build.time()), artifact.url(), sha256, artifact.size(),
                                version.getUrls().get(0), artifact.name()));
                    }
                    buildsPage.setVersions(items);
                })
                .start();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Opens the install options page for the selected build.
    private void downloadBuild(ComponentRemoteVersion remoteVersion) {
        if (remoteVersion instanceof PaperBuildRemoteVersion build) {
            Controllers.navigate(new PaperInstallPage(build));
        }
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
