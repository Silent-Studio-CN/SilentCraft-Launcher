// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jackhuang.hmcl.server.ServerConsole;
import org.jackhuang.hmcl.server.ServerRepository;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Standalone themed window showing the running server console.
@NotNullByDefault
public final class ServerConsoleWindow {
    private static @Nullable Stage stage;

    private ServerConsoleWindow() {
    }

    /// Opens the console window for the given server, reusing an existing one.
    public static void show(ServerRepository.ServerInstance server) {
        if (stage != null && stage.isShowing()) {
            stage.toFront();
            stage.requestFocus();
            return;
        }

        Stage window = new Stage();
        window.setTitle(server.metadata().type() + " " + server.metadata().gameVersion()
                + " - " + i18n("server.console"));

        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(false);
        console.getStyleClass().add("server-console");
        console.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace;");
        VBox.setVgrow(console, Priority.ALWAYS);

        JFXTextField command = new JFXTextField();
        command.setPromptText(i18n("server.console.command"));
        HBox.setHgrow(command, Priority.ALWAYS);
        JFXButton send = new JFXButton(i18n("server.console.send"));
        send.setMinWidth(80);
        send.setOnAction(event -> {
            ServerConsole.sendCommand(command.getText());
            command.clear();
        });
        JFXButton stop = new JFXButton(i18n("server.console.stop"));
        stop.setOnAction(event -> ServerConsole.stop());

        HBox input = new HBox(8, command, send, stop);
        input.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(server.metadata().type() + " " + server.metadata().gameVersion()
                + " " + server.metadata().build());
        title.getStyleClass().add("subtitle");

        VBox root = new VBox(10, title, console, input);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");

        for (String line : ServerConsole.getLines()) {
            console.appendText(line + System.lineSeparator());
        }
        ServerConsole.getLines().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String line : change.getAddedSubList()) {
                        console.appendText(line + System.lineSeparator());
                    }
                }
            }
        });

        Scene scene = new Scene(root, 760, 480);
        StyleSheets.init(scene);
        window.setScene(scene);
        window.setOnHidden(event -> stage = null);
        stage = window;
        window.show();

        Platform.runLater(() -> console.setScrollTop(Double.MAX_VALUE));
    }
}
