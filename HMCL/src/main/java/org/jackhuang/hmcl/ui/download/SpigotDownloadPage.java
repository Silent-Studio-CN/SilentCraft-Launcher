// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Placeholder for Spigot BuildTools integration.
public final class SpigotDownloadPage extends VBox {
    public SpigotDownloadPage() {
        setAlignment(Pos.CENTER);
        setSpacing(8);
        setPadding(new Insets(32));

        Label title = new Label(i18n("download.server.spigot"));
        Label description = new Label(i18n("download.server.spigot.placeholder"));
        description.setWrapText(true);

        getChildren().addAll(title, description);
    }
}
