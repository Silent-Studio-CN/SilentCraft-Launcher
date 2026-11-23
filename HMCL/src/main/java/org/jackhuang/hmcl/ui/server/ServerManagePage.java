// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.setting.ServerType;
import org.jackhuang.hmcl.setting.SettingsManager;
import org.jetbrains.annotations.NotNullByDefault;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Summarizes the most recently downloaded server and provides entry points for it.
@NotNullByDefault
public final class ServerManagePage extends VBox {
    /// Creates the page and keeps its summary in sync with the launcher settings.
    public ServerManagePage() {
        setPadding(new Insets(32));
        setSpacing(12);
        setAlignment(Pos.TOP_LEFT);

        Label title = new Label(i18n("server.local.manage"));
        title.setStyle("-fx-font-size: 24px;");

        Label summary = new Label();
        javafx.scene.control.Hyperlink getServer = new javafx.scene.control.Hyperlink(i18n("server.local.get"));
        getServer.setOnAction(event -> org.jackhuang.hmcl.ui.Controllers.navigate(
                org.jackhuang.hmcl.ui.Controllers.getDownloadPage()));
        getServer.setVisible(false);
        getServer.setManaged(false);
        Runnable update = () -> {
            ServerType type = SettingsManager.settings().getLastServerType();
            String version = SettingsManager.settings().getLastServerVersion();
            boolean none = version == null || version.isBlank();
            summary.setText(none ? i18n("server.local.none") : type.getDisplayName() + " " + version);
            getServer.setVisible(none);
            getServer.setManaged(none);
        };
        update.run();

        SettingsManager.settings().lastServerTypeProperty().addListener((observable, oldValue, newValue) -> update.run());
        SettingsManager.settings().lastServerVersionProperty().addListener((observable, oldValue, newValue) -> update.run());

        getChildren().addAll(title, summary, getServer);
    }
}
