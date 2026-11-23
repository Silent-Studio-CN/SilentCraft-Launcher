// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui;

import com.jfoenix.controls.JFXButton;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jackhuang.hmcl.game.HMCLGameInstance;
import org.jackhuang.hmcl.game.ModMismatchDetector;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.task.Schedulers;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.CompletableFuture;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Separate window attached to the right edge of the game crash window.
@NotNullByDefault
public final class ModFixWindow {
    private ModFixWindow() {
    }

    /// Shows the repair panel next to the crash window.
    public static void show(Stage crashWindow, HMCLGameInstance instance, ModMismatchDetector.Mismatch mismatch) {
        Stage panel = new Stage(StageStyle.UNDECORATED);
        panel.initOwner(crashWindow);

        Label title = new Label(i18n("game.mod_mismatch.detected"));
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label fix = new Label(i18n("game.mod_mismatch.fix", mismatch.mod()));
        fix.setWrapText(true);

        JFXButton install = FXUtils.newRaisedButton(i18n("game.mod_mismatch.install"));
        install.setMaxWidth(Double.MAX_VALUE);
        install.setOnAction(event -> {
            install.setDisable(true);
            install.setText(i18n("game.vulkan.installing"));
            CompletableFuture.runAsync(() -> ModMismatchDetector.installFix(instance, mismatch))
                    .whenComplete((ignored, throwable) -> Schedulers.javafx().execute(() -> {
                        if (throwable == null) {
                            Controllers.showToast(i18n("install.success"));
                            install.setDisable(false);
                            install.setText(i18n("game.mod_mismatch.install"));
                        } else {
                            install.setDisable(false);
                            install.setText(i18n("game.mod_mismatch.install"));
                            Controllers.dialog(throwable.getMessage(), i18n("install.failed"),
                                    org.jackhuang.hmcl.ui.construct.MessageDialogPane.MessageType.ERROR);
                        }
                    }));
        });

        JFXButton close = new JFXButton(i18n("button.close"));
        close.setMaxWidth(Double.MAX_VALUE);
        close.setStyle("-fx-background-color: -monet-surface-variant; -fx-text-fill: -monet-on-surface;");
        close.setOnAction(event -> panel.close());

        VBox root = new VBox(12, title, fix, install, close);
        root.setPadding(new Insets(16));
        root.setStyle("-fx-background-color: -monet-surface; -fx-border-color: -monet-outline-variant; -fx-border-width: 1 1 1 1;");
        VBox.setVgrow(fix, Priority.ALWAYS);

        Scene scene = new Scene(root, 320, Math.max(400, crashWindow.getHeight()));
        StyleSheets.init(scene);
        panel.setScene(scene);
        position(panel, crashWindow);

        ChangeListener<Number> follow = (observable, oldValue, newValue) -> position(panel, crashWindow);
        crashWindow.xProperty().addListener(follow);
        crashWindow.yProperty().addListener(follow);
        crashWindow.widthProperty().addListener(follow);
        crashWindow.heightProperty().addListener(follow);
        crashWindow.setOnHidden(event -> panel.close());

        panel.show();
    }

    /// Places the panel flush against the crash window's right edge.
    private static void position(Stage panel, Stage crashWindow) {
        panel.setX(crashWindow.getX() + crashWindow.getWidth());
        panel.setY(crashWindow.getY());
        panel.setHeight(Math.max(400, crashWindow.getHeight()));
    }
}
