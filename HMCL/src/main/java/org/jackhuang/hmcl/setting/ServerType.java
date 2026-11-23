// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.setting;

import org.jetbrains.annotations.NotNullByDefault;

/// The server software whose download was completed most recently.
@NotNullByDefault
public enum ServerType {
    /// The official Mojang server.
    VANILLA("Vanilla"),

    /// PaperMC server builds.
    PAPER("Paper"),

    /// Spigot builds produced by BuildTools.
    SPIGOT("Spigot");

    /// The name shown in the launcher sidebar.
    private final String displayName;

    ServerType(String displayName) {
        this.displayName = displayName;
    }

    /// Returns the user-facing name of this server type.
    public String getDisplayName() {
        return displayName;
    }
}
