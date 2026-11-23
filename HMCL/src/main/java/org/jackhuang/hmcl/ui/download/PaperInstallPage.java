// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.jackhuang.hmcl.server.ServerRepository;
import org.jackhuang.hmcl.setting.ServerType;
import org.jackhuang.hmcl.setting.SettingsManager;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.construct.AdvancedListItem;
import org.jackhuang.hmcl.ui.construct.AdvancedListBox;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Third-level Paper install page: left navigation selects the options shown on the right.
@NotNullByDefault
public final class PaperInstallPage extends DecoratorAnimatedPage implements DecoratorPage {
    /// A plugin queued for pre-installation.
    private record PluginRef(String title, String url, String filename) {
    }

    private final ReadOnlyObjectWrapper<State> state;
    private final PaperBuildRemoteVersion build;
    private final ObservableList<PluginRef> plugins = FXCollections.observableArrayList();

    private final AdvancedListItem pluginsItem = new AdvancedListItem();
    private final AdvancedListItem runItem = new AdvancedListItem();
    private final AdvancedListItem directoryItem = new AdvancedListItem();

    private final CheckBox runAfter = new CheckBox(i18n("server.local.run_after"));
    private final Label directoryLabel = new Label();
    private final ListView<String> selectedPlugins = new ListView<>();
    private @Nullable PluginDownloadPage pluginPage;

    /// Creates the install page for one Paper build.
    public PaperInstallPage(PaperBuildRemoteVersion build) {
        this.build = build;
        this.state = new ReadOnlyObjectWrapper<>(State.fromTitle(i18n(
                "server.local.install_title", "Paper", build.getGameVersion() + " #" + build.getBuildId())));

        getStyleClass().add("gray-background");

        pluginsItem.setLeftIcon(SVG.EXTENSION);
        pluginsItem.setTitle(i18n("server.local.plugins"));
        pluginsItem.setOnAction(event -> openPluginPage());

        runItem.setLeftIcon(SVG.ROCKET_LAUNCH);
        runItem.setTitle(i18n("server.local.run_after"));
        runItem.setOnAction(event -> showRun());

        directoryItem.setLeftIcon(SVG.FOLDER);
        directoryItem.setTitle(i18n("server.local.directory"));
        directoryItem.setOnAction(event -> showDirectory());

        getSidebar().add(new AdvancedListBox()
                .startCategory(i18n("server.local.options").toUpperCase(java.util.Locale.ROOT))
                .add(pluginsItem)
                .add(runItem)
                .add(directoryItem));

        Button install = new Button(i18n("server.local.install"));
        install.setMaxWidth(Double.MAX_VALUE);
        install.setOnAction(event -> install());
        VBox bottom = new VBox(install);
        bottom.setPadding(new Insets(0, 12, 12, 12));
        setSidebarBottom(bottom);

        plugins.addListener((javafx.collections.ListChangeListener<PluginRef>) change -> {
            selectedPlugins.getItems().setAll(plugins.stream().map(PluginRef::title).toList());
            pluginsItem.setSubtitle(i18n("server.local.plugins.count", plugins.size()));
        });
        pluginsItem.setSubtitle(i18n("server.local.plugins.count", 0));

        directoryLabel.setText(ServerRepository.rootDirectory().toString());
        openPluginPage();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    private void select(AdvancedListItem item) {
        pluginsItem.setActive(item == pluginsItem);
        runItem.setActive(item == runItem);
        directoryItem.setActive(item == directoryItem);
    }

    /// Shows the plugin browser inside the install page so the left navigation stays visible.
    private void openPluginPage() {
        select(pluginsItem);
        if (pluginPage == null) {
            pluginPage = new PluginDownloadPage(selected -> {
                for (PluginDownloadPage.SelectedPlugin plugin : selected) {
                    if (plugins.stream().noneMatch(existing -> existing.filename().equals(plugin.filename()))) {
                        plugins.add(new PluginRef(plugin.title(), plugin.url(), plugin.filename()));
                    }
                }
            });
        }
        setContent(pluginPage);
    }

    private void showPluginsSummary() {
        select(pluginsItem);
        Label hint = new Label(i18n("server.local.plugins.hint"));
        VBox content = new VBox(12, hint, selectedPlugins);
        content.setPadding(new Insets(24));
        VBox.setVgrow(selectedPlugins, javafx.scene.layout.Priority.ALWAYS);
        setContent(content);
    }

    private void showRun() {
        select(runItem);
        Label hint = new Label(i18n("server.local.run_hint"));
        VBox content = new VBox(12, runAfter, hint);
        content.setPadding(new Insets(24));
        setContent(content);
    }

    private void showDirectory() {
        select(directoryItem);
        Button choose = new Button(i18n("server.local.directory.choose"));
        choose.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle(i18n("server.local.directory"));
            Path selected = Controllers.showDialog(chooser);
            if (selected != null) {
                SettingsManager.settings().setServerDirectory(selected.toString());
                directoryLabel.setText(selected.toString());
            }
        });
        VBox content = new VBox(12, directoryLabel, choose);
        content.setPadding(new Insets(24));
        setContent(content);
    }

    /// Downloads the jar into its own directory, installs selected plugins, then optionally starts the server.
    private void install() {
        Path directory;
        try {
            directory = ServerRepository.createInstanceDirectory(
                    "paper", build.getGameVersion().toString(), String.valueOf(build.getBuildId()));
        } catch (Exception e) {
            LOG.warning("Failed to create server directory", e);
            return;
        }

        String fileName = build.fileName() == null || build.fileName().isBlank()
                ? "paper-" + build.getGameVersion() + "-" + build.getBuildId() + ".jar"
                : build.fileName();
        Path target = directory.resolve(fileName);

        PaperDownloadTask jarTask = new PaperDownloadTask(build.url(), target, build.size(), build.sha256());

        Task<Void> prepare = jarTask.thenComposeAsync(Schedulers.io(), ignored -> Task.runAsync(Schedulers.io(),
                () -> {
                    // Persist metadata as soon as the jar is complete so the server list sees it even if later steps fail.
                    try {
                        ServerRepository.writeMetadata(directory, new ServerRepository.ServerMetadata(
                                "Paper", build.getGameVersion().toString(), "#" + build.getBuildId(),
                                target.getFileName().toString(), build.channel()));
                    } catch (Exception e) {
                        LOG.warning("Failed to write server metadata", e);
                    }
                    installPlugins(directory);
                }));

        Task<Void> finished = prepare.whenComplete(Schedulers.javafx(), (result, exception) -> {
            if (exception != null) {
                return;
            }

            SettingsManager.settings().setLastServerType(ServerType.PAPER);
            SettingsManager.settings().setLastServerVersion(build.getGameVersion().toString());

            if (runAfter.isSelected()) {
                ServerLaunch.offerRun(target, directory, build.metaUrl());
            } else {
                Controllers.showToast(i18n("server.local.installed", directory));
            }
        });

        Controllers.taskDialog(finished,
                i18n("message.downloading.file", target.getFileName().toString()),
                org.jackhuang.hmcl.util.TaskCancellationAction.NORMAL);
    }

    private void installPlugins(Path directory) {
        if (plugins.isEmpty()) {
            return;
        }
        try {
            Path pluginsDirectory = directory.resolve("plugins");
            Files.createDirectories(pluginsDirectory);
            for (PluginRef plugin : plugins) {
                download(plugin.url(), pluginsDirectory.resolve(plugin.filename()));
            }
        } catch (Exception e) {
            LOG.warning("Failed to install plugins", e);
        }
    }

    private static void download(String url, Path target) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(20_000);
        connection.setReadTimeout(60_000);
        connection.setRequestProperty("User-Agent", "HMCL/3.18");
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } finally {
            connection.disconnect();
        }
    }
}
