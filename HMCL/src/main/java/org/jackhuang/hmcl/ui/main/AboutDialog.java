// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jetbrains.annotations.NotNullByDefault;

/// Small native-style About window used on macOS when no app bundle is present.
@NotNullByDefault
public final class AboutDialog {
    private AboutDialog() {
    }

    /// Shows the About window.
    public static void show() {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.setTitle(Metadata.FULL_NAME);
        stage.setResizable(false);

        ImageView logo = new ImageView(FXUtils.newBuiltinImage("/assets/img/logo.png"));
        logo.setFitWidth(240);
        logo.setPreserveRatio(true);

        Label name = new Label(Metadata.FULL_NAME);
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label version = new Label(Metadata.VERSION);
        version.getStyleClass().add("subtitle");

        Label basedOn = new Label("based on HMCL");
        basedOn.getStyleClass().add("subtitle");

        Label copyright = new Label("Copyright (C) 2013-2026 The HMCL Authors\nCopyright (C) 2026 SilentStudio");
        copyright.getStyleClass().add("subtitle");
        copyright.setWrapText(true);

        Button close = new Button("OK");
        close.setDefaultButton(true);
        close.setOnAction(event -> stage.close());

        VBox root = new VBox(10, logo, name, version, basedOn, copyright, close);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setPrefWidth(360);
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(0,0,0,0.15);");

        Scene scene = new Scene(root);
        StyleSheets.init(scene);
        stage.setScene(scene);
        stage.show();
    }
}
