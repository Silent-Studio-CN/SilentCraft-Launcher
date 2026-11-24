// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.setting;

/// Selects the implementation used for file downloads.
public enum DownloadEngine {
    /// HMCL's original concurrent small-file engine.
    HMCL,

    /// SilentStudio's range-based engine with adaptive segments.
    SILENT_ENGINE
}
