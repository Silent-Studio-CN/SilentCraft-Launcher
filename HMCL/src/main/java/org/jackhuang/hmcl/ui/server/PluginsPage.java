// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.construct.ComponentList;
import org.jackhuang.hmcl.ui.construct.LinePane;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Lists and manages plugin jars in a server's plugins directory.
@NotNullByDefault
public final class PluginsPage extends DecoratorAnimatedPage implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();
    private final Path pluginsDirectory;
    private final ComponentList list = new ComponentList();
    private final Label empty = new Label(i18n("server.plugins.empty"));

    /// Creates the plugin manager for one server directory.
    public PluginsPage(Path serverDirectory) {
        this.pluginsDirectory = serverDirectory.resolve("plugins");
        getStyleClass().add("gray-background");
        state.set(State.fromTitle(i18n("server.plugins")));

        JFXButton add = FXUtils.newRaisedButton(i18n("server.plugins.add"));
        add.setOnAction(event -> addPlugins());
        JFXButton open = new JFXButton(i18n("button.reveal_dir"));
        open.setOnAction(event -> FXUtils.openFolder(pluginsDirectory));

        HBox toolbar = new HBox(8, add, open);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 12, 0));

        empty.setPadding(new Insets(24));

        VBox content = new VBox(toolbar, list, empty);
        content.setPadding(new Insets(16));
        setContent(content);

        reload();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Reloads plugin jars from disk.
    private void reload() {
        list.getContent().clear();
        try {
            Files.createDirectories(pluginsDirectory);
            List<Path> jars;
            try (var stream = Files.list(pluginsDirectory)) {
                jars = stream.filter(path -> path.getFileName().toString().endsWith(".jar"))
                        .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                        .toList();
            }

            for (Path jar : jars) {
                LinePane row = new LinePane();
                row.setTitle(jar.getFileName().toString());
                try {
                    row.setSubtitle(String.format("%.1f KB", Files.size(jar) / 1024.0));
                } catch (IOException ignored) {
                }
                JFXButton delete = new JFXButton(i18n("button.delete"));
                delete.setOnAction(event -> Controllers.confirm(
                        i18n("server.plugins.delete.confirm", jar.getFileName()),
                        i18n("server.plugins.delete"), () -> {
                            try {
                                Files.deleteIfExists(jar);
                                reload();
                            } catch (IOException e) {
                                LOG.warning("Failed to delete plugin", e);
                            }
                        }, null));
                row.setRight(delete);
                list.getContent().add(row);
            }
        } catch (IOException e) {
            LOG.warning("Failed to list plugins", e);
        }

        boolean isEmpty = list.getContent().isEmpty();
        empty.setVisible(isEmpty);
        empty.setManaged(isEmpty);
    }

    /// Copies selected jars into the plugins directory.
    private void addPlugins() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(i18n("server.plugins.add"));
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("*.jar", "*.jar"));
        List<java.io.File> files = chooser.showOpenMultipleDialog(Controllers.getStage());
        if (files == null) {
            return;
        }
        try {
            Files.createDirectories(pluginsDirectory);
            for (java.io.File file : files) {
                Files.copy(file.toPath(), pluginsDirectory.resolve(file.getName()),
                        StandardCopyOption.REPLACE_EXISTING);
            }
            reload();
        } catch (IOException e) {
            LOG.warning("Failed to add plugins", e);
        }
    }
}
