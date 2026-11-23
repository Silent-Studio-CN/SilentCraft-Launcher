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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jackhuang.hmcl.game.HMCLGameInstance;
import org.jackhuang.hmcl.game.ModMismatchDetector;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.task.Schedulers;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.concurrent.CompletableFuture;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Separate rounded assistant window attached to the right edge of the crash window.
@NotNullByDefault
public final class ModFixWindow {
    private ModFixWindow() {
    }

    /// Shows the repair assistant next to the crash window.
    public static void show(Stage crashWindow, HMCLGameInstance instance, ModMismatchDetector.Mismatch mismatch) {
        Stage panel = new Stage(StageStyle.TRANSPARENT);
        panel.initOwner(crashWindow);

        Label title = new Label(i18n("game.mod_mismatch.assistant"));
        title.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #e6e6e6;");
        JFXButton titleClose = new JFXButton("×");
        titleClose.setStyle("-fx-text-fill: #e6e6e6; -fx-font-size: 15px;");
        titleClose.setOnAction(event -> panel.close());
        HBox titleBar = new HBox(title, new javafx.scene.layout.Region(), titleClose);
        HBox.setHgrow(titleBar.getChildren().get(1), Priority.ALWAYS);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(6, 10, 6, 14));
        titleBar.setStyle("-fx-background-color: #3a3a3a; -fx-background-radius: 10 10 0 0;");

        Label detection = new Label(i18n("game.mod_mismatch.detected"));
        detection.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
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

        VBox body = new VBox(12, detection, fix, install, close);
        body.setPadding(new Insets(16));
        body.setStyle("-fx-background-color: -monet-surface; -fx-background-radius: 0 0 10 10;");
        VBox.setVgrow(fix, Priority.ALWAYS);

        VBox card = new VBox(titleBar, body);
        card.setStyle("-fx-background-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.2, 0, 4);");

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(root, 340, Math.max(420, crashWindow.getHeight()));
        scene.setFill(Color.TRANSPARENT);
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

    /// Places the assistant flush against the crash window's right edge.
    private static void position(Stage panel, Stage crashWindow) {
        panel.setX(crashWindow.getX() + crashWindow.getWidth() - 8);
        panel.setY(crashWindow.getY());
        panel.setHeight(Math.max(420, crashWindow.getHeight()));
    }
}
