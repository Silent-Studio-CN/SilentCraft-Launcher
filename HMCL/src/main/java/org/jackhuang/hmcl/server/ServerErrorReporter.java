// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jetbrains.annotations.NotNullByDefault;

import java.awt.Desktop;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Classifies captured server console errors and shows a concise diagnostic dialog.
@NotNullByDefault
public final class ServerErrorReporter {
    private ServerErrorReporter() {
    }

    /// Reports a failed server run, ignoring clean exits without errors.
    public static void report(List<String> lines, int exitCode, Path logFile) {
        String text = String.join(System.lineSeparator(), lines);
        boolean hasError = text.contains("Exception") || text.contains("ERROR]")
                || text.contains("Failed to") || text.contains("Caused by:");
        if (exitCode == 0 && !hasError) {
            return;
        }

        String title;
        String message;
        if (text.contains("No key dimensions") || text.contains("Failed to load datapacks")) {
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

        String excerpt = tail(text, 1800);
        MessageDialogPane.Builder builder = new MessageDialogPane.Builder(
                message + System.lineSeparator() + System.lineSeparator() + excerpt,
                title, MessageDialogPane.MessageType.ERROR)
                .addAction(i18n("server.error.open_log"), () -> openLog(logFile))
                .addAction(i18n("server.error.copy"), () -> org.jackhuang.hmcl.ui.FXUtils.copyText(text))
                .addCancel(i18n("button.ok"), null);
        Controllers.dialog(builder.build());
    }

    /// Returns the last [max] characters of the text.
    private static String tail(String text, int max) {
        return text.length() <= max ? text : text.substring(text.length() - max);
    }

    /// Opens the server log with the system handler.
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
