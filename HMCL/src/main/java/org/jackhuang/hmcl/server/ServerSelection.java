// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

/// Holds the server instance selected for the home-page launch button.
@NotNullByDefault
public final class ServerSelection {
    private static final ObjectProperty<ServerRepository.ServerInstance> selected =
            new SimpleObjectProperty<>();

    private ServerSelection() {
    }

    /// Returns the selected server property.
    public static ReadOnlyObjectProperty<ServerRepository.ServerInstance> selectedProperty() {
        return selected;
    }

    /// Returns the selected server, or null.
    public static ServerRepository.@Nullable ServerInstance getSelected() {
        return selected.get();
    }

    /// Selects a server instance.
    public static void select(@Nullable ServerRepository.ServerInstance server) {
        selected.set(server);
    }
}
