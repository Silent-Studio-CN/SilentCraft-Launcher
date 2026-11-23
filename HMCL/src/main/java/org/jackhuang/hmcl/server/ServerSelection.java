// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Holds the selected server instance and restores it across restarts.
@NotNullByDefault
public final class ServerSelection {
    /// File storing the selected server directory.
    private static final Path FILE = Paths.get(System.getProperty("user.home"), ".hmcl", "server-selection.txt");

    /// Matches the Minecraft version line in a Paper/Vanilla log.
    private static final Pattern LOG_VERSION = Pattern.compile(
            "(?:Starting minecraft server version|for Minecraft|Minecraft version)\\s+(\\S+)",
            Pattern.CASE_INSENSITIVE);

    private static final ObjectProperty<ServerRepository.ServerInstance> selected = new SimpleObjectProperty<>();
    private static boolean restored;

    private ServerSelection() {
    }

    /// Returns the selected server property.
    public static ReadOnlyObjectProperty<ServerRepository.ServerInstance> selectedProperty() {
        return selected;
    }

    /// Returns the selected server, restoring it from disk on first use.
    public static @Nullable ServerRepository.ServerInstance getSelected() {
        restore();
        return selected.get();
    }

    /// Selects a server instance and persists its directory.
    public static void select(@Nullable ServerRepository.ServerInstance server) {
        restored = true;
        selected.set(server);
        try {
            Files.createDirectories(FILE.getParent());
            if (server == null) {
                Files.deleteIfExists(FILE);
            } else {
                Files.writeString(FILE, server.directory().toAbsolutePath().toString(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.warning("Failed to save selected server", e);
        }
    }

    /// Picks the instance matching logs/latest.log, falling back to the newest jar.
    public static @Nullable ServerRepository.ServerInstance defaultForRoot(Path root) {
        List<ServerRepository.ServerInstance> instances = ServerRepository.listInstances(root);
        if (instances.isEmpty()) {
            return null;
        }

        Path log = root.resolve("logs").resolve("latest.log");
        if (Files.isRegularFile(log)) {
            try (var reader = Files.newBufferedReader(log, StandardCharsets.UTF_8)) {
                String line;
                int checked = 0;
                while ((line = reader.readLine()) != null && checked++ < 300) {
                    Matcher matcher = LOG_VERSION.matcher(line);
                    if (matcher.find()) {
                        String version = matcher.group(1);
                        for (ServerRepository.ServerInstance instance : instances) {
                            if (instance.metadata().gameVersion().equals(version)) {
                                return instance;
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return instances.stream()
                .max(Comparator.comparing(instance -> {
                    try {
                        Path jar = instance.directory().resolve(instance.metadata().jar());
                        return Files.getLastModifiedTime(jar).toMillis();
                    } catch (Exception e) {
                        return 0L;
                    }
                }))
                .orElse(instances.get(0));
    }

    /// Loads the persisted selection once.
    private static void restore() {
        if (restored) {
            return;
        }
        restored = true;

        if (Files.isRegularFile(FILE)) {
            try {
                Path path = Paths.get(Files.readString(FILE, StandardCharsets.UTF_8).strip());
                for (ServerRepository.ServerInstance instance : ServerRepository.listInstances()) {
                    if (instance.directory().toAbsolutePath().normalize()
                            .equals(path.toAbsolutePath().normalize())) {
                        selected.set(instance);
                        return;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        ServerRepository.ServerInstance fallback = defaultForRoot(ServerRepository.rootDirectory());
        if (fallback != null) {
            selected.set(fallback);
        }
    }
}
