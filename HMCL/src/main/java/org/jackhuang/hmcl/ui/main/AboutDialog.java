// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.main;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.jackhuang.hmcl.Metadata;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jetbrains.annotations.NotNullByDefault;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Small About window used on macOS when no app bundle is present.
@NotNullByDefault
public final class AboutDialog {
    private AboutDialog() {
    }

    /// Shows the About window.
    public static void show() {
        Stage stage = new Stage(StageStyle.DECORATED);
        stage.setTitle(Metadata.FULL_NAME);
        stage.setResizable(true);

        ImageView logo = new ImageView(FXUtils.newBuiltinImage("/assets/img/logo.png"));
        logo.setFitWidth(240);
        logo.setPreserveRatio(true);

        Label name = new Label(Metadata.FULL_NAME);
        name.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label version = new Label(i18n("about.based_on_version", "3.18"));
        version.getStyleClass().add("subtitle");

        Hyperlink ownGithub = new Hyperlink(i18n("about.visit_our_github.link"));
        ownGithub.setOnAction(event -> FXUtils.openLink(Metadata.REPOSITORY_URL));
        HBox ownLine = new HBox(0, new Label(i18n("about.visit_our_github.prefix")), ownGithub);
        ownLine.setAlignment(Pos.CENTER);

        Hyperlink hmclGithub = new Hyperlink(i18n("about.recommend_hmcl.link"));
        hmclGithub.setOnAction(event -> FXUtils.openLink("https://github.com/HMCL-dev/HMCL"));
        HBox hmclLine = new HBox(0,
                new Label(i18n("about.recommend_hmcl.prefix")),
                hmclGithub,
                new Label(i18n("about.recommend_hmcl.suffix")));
        hmclLine.setAlignment(Pos.CENTER);

        Label copyright = new Label("Copyright (C) 2013-2026 The HMCL Authors\nCopyright (C) 2026 SilentStudio");
        copyright.getStyleClass().add("subtitle");
        copyright.setWrapText(true);

        Button close = new Button("OK");
        close.setDefaultButton(true);
        close.setOnAction(event -> stage.close());

        VBox root = new VBox(10, logo, name, version, ownLine, hmclLine, copyright, close);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(24));
        root.setPrefWidth(420);
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");

        Scene scene = new Scene(root);
        StyleSheets.init(scene);
        stage.setScene(scene);
        stage.show();
    }
}
