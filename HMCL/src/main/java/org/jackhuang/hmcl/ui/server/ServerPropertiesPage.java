// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// In-app editor for a server's server.properties file.
@NotNullByDefault
public final class ServerPropertiesPage extends DecoratorAnimatedPage implements DecoratorPage {
    /// Page title state.
    private final ReadOnlyObjectWrapper<State> state =
            new ReadOnlyObjectWrapper<>(State.fromTitle(i18n("server.local.config")));

    /// Server directory whose server.properties is edited.
    private final Path directory;

    /// Editor showing the raw properties text.
    private final TextArea editor = new TextArea();

    /// Creates the editor for one server directory.
    public ServerPropertiesPage(Path directory) {
        this.directory = directory;
        getStyleClass().add("gray-background");

        Label hint = new Label(i18n("server.local.config.editor_hint"));
        hint.getStyleClass().add("subtitle");

        editor.setWrapText(false);
        editor.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace;");
        VBox.setVgrow(editor, Priority.ALWAYS);

        JFXButton save = FXUtils.newRaisedButton(i18n("button.save"));
        save.setOnAction(event -> save());

        VBox content = new VBox(12, hint, editor, save);
        content.setPadding(new Insets(20));
        setContent(content);

        load();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Loads server.properties, falling back to the bundled template before the first start.
    private void load() {
        Path file = directory.resolve("server.properties");
        try {
            if (Files.isRegularFile(file)) {
                editor.setText(Files.readString(file, StandardCharsets.UTF_8));
                return;
            }
        } catch (IOException e) {
            LOG.warning("Failed to read server.properties", e);
        }

        try (InputStream in = ServerPropertiesPage.class.getResourceAsStream("/assets/server/server.properties")) {
            editor.setText(in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            editor.setText("");
        }
    }

    /// Writes the edited text back to server.properties.
    private void save() {
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("server.properties"), editor.getText(), StandardCharsets.UTF_8);
            Controllers.showToast(i18n("server.local.config.saved"));
        } catch (IOException e) {
            LOG.warning("Failed to save server.properties", e);
        }
    }
}
