// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/// Detects a server jar's type, Minecraft version, and build from the jar itself rather than the folder name.
@NotNullByDefault
public final class ServerJarInspector {
    /// Minecraft version and build pattern used by Paper and Spigot artifact names.
    private static final Pattern PAPER_NAME = Pattern.compile("^paper-(.+?)-(\\d+)\\.jar$", Pattern.CASE_INSENSITIVE);
    private static final Pattern SPIGOT_NAME = Pattern.compile("^spigot-(.+?)-(\\d+)\\.jar$", Pattern.CASE_INSENSITIVE);
    private static final Pattern VERSION_JSON_ID = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");

    private ServerJarInspector() {
    }

    /// Detected metadata for one server jar.
    public record Info(String type, String gameVersion, @Nullable String build, String jar, @Nullable String channel) {
    }

    /// Inspects a server directory and returns metadata, or null when no jar is present.
    public static @Nullable Info inspect(Path directory) {
        Path jar = findJar(directory);
        return jar == null ? null : inspect(directory, jar);
    }

    /// Inspects a specific jar inside a server directory.
    public static Info inspect(Path directory, Path jar) {
        String fileName = jar.getFileName().toString();
        String lower = fileName.toLowerCase(Locale.ROOT);
        String type = lower.startsWith("paper") ? "Paper" : lower.startsWith("spigot") ? "Spigot" : "Vanilla";
        String gameVersion = null;
        String build = null;

        Matcher paper = PAPER_NAME.matcher(fileName);
        Matcher spigot = SPIGOT_NAME.matcher(fileName);
        if (paper.matches()) {
            type = "Paper";
            gameVersion = paper.group(1);
            build = paper.group(2);
        } else if (spigot.matches()) {
            type = "Spigot";
            gameVersion = spigot.group(1);
            build = spigot.group(2);
        }

        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry manifestEntry = zip.getEntry("META-INF/MANIFEST.MF");
            if (manifestEntry != null) {
                try (InputStream in = zip.getInputStream(manifestEntry)) {
                    Manifest manifest = new Manifest(in);
                    String mainClass = manifest.getMainAttributes().getValue("Main-Class");
                    if (mainClass != null && mainClass.toLowerCase(Locale.ROOT).contains("paperclip")) {
                        type = "Paper";
                    }
                    String implementationVersion = manifest.getMainAttributes().getValue("Implementation-Version");
                    if (gameVersion == null && implementationVersion != null) {
                        gameVersion = implementationVersion;
                    }
                }
            }

            // Some Paper builds ship a version.json describing the Minecraft version.
            ZipEntry versionEntry = zip.getEntry("version.json");
            if (versionEntry != null && gameVersion == null) {
                try (InputStream in = zip.getInputStream(versionEntry)) {
                    Matcher matcher = VERSION_JSON_ID.matcher(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                    if (matcher.find()) {
                        gameVersion = matcher.group(1);
                    }
                }
            }
        } catch (Exception ignored) {
            // A corrupt or non-zip jar still yields filename-based metadata below.
        }

        Path siblingVersion = directory.resolve("version.json");
        if (gameVersion == null && Files.isRegularFile(siblingVersion)) {
            try {
                Matcher matcher = VERSION_JSON_ID.matcher(Files.readString(siblingVersion, StandardCharsets.UTF_8));
                if (matcher.find()) {
                    gameVersion = matcher.group(1);
                }
            } catch (IOException ignored) {
            }
        }

        return new Info(type, gameVersion == null ? "unknown" : gameVersion,
                build == null ? null : "#" + build, fileName, null);
    }

    /// Returns the first jar file in a server directory, preferring names that contain paper or server.
    private static @Nullable Path findJar(Path directory) {
        try (Stream<Path> files = Files.list(directory)) {
            return files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted((a, b) -> Boolean.compare(
                            !a.getFileName().toString().toLowerCase(Locale.ROOT).contains("paper"),
                            !b.getFileName().toString().toLowerCase(Locale.ROOT).contains("paper")))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
}
