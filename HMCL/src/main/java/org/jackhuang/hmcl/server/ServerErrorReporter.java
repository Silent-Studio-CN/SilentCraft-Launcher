// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import com.jfoenix.controls.JFXButton;
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
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Shows an immediate "analyzing" window and then a database-backed diagnosis.
@NotNullByDefault
public final class ServerErrorReporter {
    private ServerErrorReporter() {
    }

    /// Reports a failed server run, ignoring clean shutdowns.
    public static void report(List<String> lines, int exitCode, Path logFile) {
        String text = String.join(System.lineSeparator(), lines);
        if (exitCode == 0 && (text.contains("Stopping server") || text.contains("Closing Server"))) {
            return;
        }

        Stage stage = new Stage();
        stage.setTitle(i18n("server.error.title"));
        Label status = new Label(i18n("server.error.analyzing"));
        status.setStyle("-fx-font-size: 14px;");
        VBox root = new VBox(12, status);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");
        Scene scene = new Scene(root, 520, 160);
        StyleSheets.init(scene);
        stage.setScene(scene);
        stage.show();

        CompletableFuture.supplyAsync(() -> ServerErrorDatabase.find(text))
                .thenAccept(entry -> Platform.runLater(() -> fill(stage, entry, text, logFile)));
    }

    /// Replaces the analyzing content with the diagnosis.
    private static void fill(Stage stage, @Nullable ServerErrorDatabase.Entry entry,
                             String text, Path logFile) {
        String title;
        String message;
        if (entry != null) {
            title = entry.title();
            message = entry.message();
        } else if (text.contains("No key dimensions") || text.contains("Failed to load datapacks")) {
            title = i18n("server.error.world_corrupted");
            message = i18n("server.error.world_corrupted.message");
        } else if (text.contains("UnsupportedClassVersionError")
                || text.contains("has been compiled by a more recent version")) {
            title = i18n("server.error.java_version");
            message = i18n("server.error.java_version.message");
        } else if (text.contains("Address already in use")) {
            title = i18n("server.error.port");
            message = i18n("server.error.port.message");
        } else if (text.contains("You need to agree to the EULA")) {
            title = i18n("server.error.eula");
            message = i18n("server.error.eula.message");
        } else if (text.contains("OutOfMemoryError")) {
            title = i18n("server.error.memory");
            message = i18n("server.error.memory.message");
        } else {
            title = i18n("server.error.unknown");
            message = i18n("server.error.unknown.message");
        }

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);

        TextArea excerpt = new TextArea(tail(text, 2000));
        excerpt.setEditable(false);
        excerpt.setWrapText(false);
        excerpt.getStyleClass().add("server-console");
        excerpt.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace;");
        VBox.setVgrow(excerpt, Priority.ALWAYS);

        JFXButton openLog = new JFXButton(i18n("server.error.open_log"));
        openLog.setOnAction(event -> openLog(logFile));
        JFXButton copy = new JFXButton(i18n("server.error.copy"));
        copy.setOnAction(event -> FXUtils.copyText(text));
        JFXButton close = new JFXButton(i18n("button.ok"));
        close.setOnAction(event -> stage.close());
        HBox actions = new HBox(8, openLog, copy, close);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10, titleLabel, messageLabel, excerpt, actions);
        root.setPadding(new Insets(16));
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");
        stage.setScene(new Scene(root, 720, 520));
        StyleSheets.init(stage.getScene());
    }

    /// Returns the last [max] characters.
    private static String tail(String text, int max) {
        return text.length() <= max ? text : text.substring(text.length() - max);
    }

    /// Opens the server log.
    private static void openLog(Path logFile) {
        try {
            if (Files.isRegularFile(logFile)) {
                Desktop.getDesktop().open(logFile.toFile());
            }
        } catch (Exception e) {
            LOG.warning("Failed to open server log", e);
        }
    }
}
