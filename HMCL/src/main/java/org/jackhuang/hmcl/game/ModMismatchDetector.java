// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.game;

import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.construct.MessageDialogPane;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Detects generic FML/NeoForge mod dependency and version mismatches from game output.
@NotNullByDefault
public final class ModMismatchDetector {
    /// Matches "Mod <id> requires <dependency> <version>".
    private static final Pattern REQUIRES = Pattern.compile(
            "Mod (\\S+) requires ([\\w-]+) ([\\w.+-]+)");

    /// Matches "Currently, <dependency> is <version>".
    private static final Pattern CURRENT = Pattern.compile(
            "Currently, ([\\w-]+) is ([\\w.+-]+)");

    /// Matches the crash-report form "Mod ID: 'x', Requested by: 'y', Expected range: ..., Actual version: 'z'".
    private static final Pattern CRASH_REPORT = Pattern.compile(
            "Mod ID: '([^']+)', Requested by: '([^']+)', Expected range: (?:\\[)?'?([^'\\],]+)'?.*?Actual version: '([^']+)'",
            Pattern.DOTALL);

    private ModMismatchDetector() {
    }

    /// Returns a mismatch description when both lines are present, otherwise null.
    public static @Nullable Mismatch detect(String text) {
        Matcher report = CRASH_REPORT.matcher(text);
        if (report.find()) {
            String expected = report.group(3).replaceAll("^[\\[']+", "").replaceAll("[\\]']+$", "");
            return new Mismatch(report.group(2), report.group(1), expected, report.group(4));
        }

        Matcher requires = REQUIRES.matcher(text);
        if (!requires.find()) {
            return null;
        }
        Matcher current = CURRENT.matcher(text);
        if (!current.find()) {
            return null;
        }
        return new Mismatch(requires.group(1), requires.group(2), requires.group(3), current.group(2));
    }

    /// Shows the mismatch dialog.
    public static void show(HMCLGameInstance instance, Mismatch mismatch) {
        MessageDialogPane.Builder builder = new MessageDialogPane.Builder(
                i18n("game.mod_mismatch.message", mismatch.mod(), mismatch.dependency(),
                        mismatch.required(), mismatch.current()),
                i18n("game.mod_mismatch.title"),
                MessageDialogPane.MessageType.ERROR)
                .addAction(i18n("game.mod_mismatch.open_mods"),
                        () -> FXUtils.openFolder(instance.getRunDirectory().resolve("mods")))
                .addCancel(i18n("button.close"), null);
        Controllers.dialog(builder.build());
    }

    /// Reads recent logs and crash reports from the instance and detects a mismatch.
    public static @Nullable Mismatch detectFromInstance(HMCLGameInstance instance) {
        StringBuilder text = new StringBuilder();
        try {
            Path latest = instance.getRunDirectory().resolve("logs/latest.log");
            if (Files.isReadable(latest)) {
                text.append(Files.readString(latest));
            }
        } catch (Exception ignored) {
        }
        try {
            Path dir = instance.getRunDirectory().resolve("crash-reports");
            if (Files.isDirectory(dir)) {
                try (var stream = Files.list(dir)) {
                    for (Path report : stream.filter(path -> path.getFileName().toString().endsWith(".txt"))
                            .sorted(java.util.Comparator.comparing((Path path) -> {
                                try {
                                    return Files.getLastModifiedTime(path).toMillis();
                                } catch (Exception e) {
                                    return 0L;
                                }
                            }).reversed()).limit(2).toList()) {
                        text.append(Files.readString(report));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return detect(text.toString());
    }

    /// Installs a known fix, or opens Modrinth for an unknown mod.
    public static void installFix(HMCLGameInstance instance, Mismatch mismatch) {
        if (mismatch.mod().toLowerCase(java.util.Locale.ROOT).contains("neoloadingvulkanfix")) {
            org.jackhuang.hmcl.ui.VulkanErrorDialog.installLatest(instance);
        } else {
            FXUtils.openLink("https://modrinth.com/mods?q="
                    + java.net.URLEncoder.encode(mismatch.mod(), java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /// One detected mod dependency mismatch.
    public record Mismatch(String mod, String dependency, String required, String current) {
    }
}
