// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import org.jackhuang.hmcl.setting.SettingsManager;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/// Stores every downloaded server in its own directory under the configured root.
@NotNullByDefault
public final class ServerRepository {
    private static final String METADATA_FILE = "server.json";

    private ServerRepository() {
    }

    /// Returns the root directory that holds all server instances.
    public static Path rootDirectory() {
        String configured = SettingsManager.settings().getServerDirectory();
        if (configured == null || configured.isBlank()) {
            return Paths.get(System.getProperty("user.home"), ".hmcl", "servers");
        }
        return Paths.get(configured);
    }

    /// Creates a unique directory for a new server and returns it.
    public static Path createInstanceDirectory(String type, String gameVersion, String build) throws IOException {
        Path root = rootDirectory();
        Files.createDirectories(root);

        String baseName = type.toLowerCase(java.util.Locale.ROOT) + "-" + gameVersion + "-" + build;
        Path directory = root.resolve(baseName);
        int suffix = 2;
        while (Files.exists(directory)) {
            directory = root.resolve(baseName + "-" + suffix++);
        }
        Files.createDirectories(directory);
        return directory;
    }

    /// Writes the metadata file describing a downloaded server.
    public static void writeMetadata(Path directory, ServerMetadata metadata) throws IOException {
        Files.writeString(directory.resolve(METADATA_FILE), JsonUtils.GSON.toJson(metadata), StandardCharsets.UTF_8);
    }

    /// Returns every configured server root, with the primary root first.
    public static List<Path> getDirectories() {
        java.util.LinkedHashSet<Path> directories = new java.util.LinkedHashSet<>();
        directories.add(rootDirectory());

        Path file = directoriesFile();
        if (Files.isRegularFile(file)) {
            try {
                String[] stored = JsonUtils.fromJsonFile(file, String[].class);
                if (stored != null) {
                    for (String value : stored) {
                        if (value != null && !value.isBlank()) {
                            directories.add(Paths.get(value));
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return List.copyOf(directories);
    }

    /// Adds a server root without removing the existing ones.
    public static void addDirectory(Path directory) {
        List<Path> directories = new ArrayList<>(getDirectories());
        Path absolute = directory.toAbsolutePath().normalize();
        if (!directories.contains(absolute)) {
            directories.add(absolute);
            saveDirectories(directories);
        }
    }

    /// Removes a server root and optionally deletes its files.
    public static void removeDirectory(Path directory, boolean deleteFiles) {
        Path absolute = directory.toAbsolutePath().normalize();
        List<Path> directories = new ArrayList<>(getDirectories());
        directories.remove(absolute);
        saveDirectories(directories);

        String primary = SettingsManager.settings().getServerDirectory();
        if (primary != null && Paths.get(primary).toAbsolutePath().normalize().equals(absolute)) {
            SettingsManager.settings().setServerDirectory(
                    directories.isEmpty()
                            ? Paths.get(System.getProperty("user.home"), ".hmcl", "servers").toString()
                            : directories.get(0).toString());
        }

        if (deleteFiles) {
            try (Stream<Path> stream = Files.walk(absolute)) {
                for (Path path : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            } catch (IOException ignored) {
            }
        }
    }

    /// Persists the server root list.
    private static void saveDirectories(List<Path> directories) {
        try {
            Path file = directoriesFile();
            Files.createDirectories(file.getParent());
            Files.writeString(file, JsonUtils.GSON.toJson(directories.stream().map(Path::toString).toList()),
                    StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    /// Returns the file storing additional server roots.
    private static Path directoriesFile() {
        return Paths.get(System.getProperty("user.home"), ".hmcl", "server-directories.json");
    }

    /// Lists all server instances under every configured root.
    public static List<ServerInstance> listInstances() {
        List<ServerInstance> instances = new ArrayList<>();
        java.util.Set<Path> seen = new java.util.HashSet<>();

        for (Path root : getDirectories()) {
            if (!Files.isDirectory(root)) {
                continue;
            }

            try (Stream<Path> stream = Files.list(root)) {
                stream.filter(Files::isDirectory).forEach(directory -> {
                    if (!seen.add(directory.toAbsolutePath().normalize()) || isSupportDirectory(directory)) {
                        return;
                    }

                    Path metadataFile = directory.resolve(METADATA_FILE);
                    if (Files.isRegularFile(metadataFile)) {
                        try {
                            ServerMetadata metadata = JsonUtils.fromJsonFile(metadataFile, ServerMetadata.class);
                            if (metadata != null) {
                                instances.add(new ServerInstance(directory, metadata));
                                return;
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    // Older downloads predate server.json; inspect the jar instead of trusting the folder name.
                    ServerJarInspector.Info info = ServerJarInspector.inspect(directory);
                    if (info != null) {
                        instances.add(new ServerInstance(directory, new ServerMetadata(
                                info.type(), info.gameVersion(),
                                info.build() == null ? "" : info.build(),
                                info.jar(), info.channel())));
                    }
                });
            } catch (IOException ignored) {
            }

            // The root itself may be a server directory containing several version jars.
            addRootJars(root, instances);
        }
        return instances;
    }

    /// Adds one entry per version jar found directly under a root directory.
    private static void addRootJars(Path root, List<ServerInstance> instances) {
        try (Stream<Path> files = Files.list(root)) {
            List<Path> jars = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .sorted()
                    .toList();
            for (Path jar : jars) {
                ServerJarInspector.Info info = ServerJarInspector.inspect(root, jar);
                if (info != null) {
                    instances.add(new ServerInstance(root, new ServerMetadata(
                            info.type(), info.gameVersion(),
                            info.build() == null ? "" : info.build(),
                            info.jar(), info.channel())));
                }
            }
        } catch (IOException ignored) {
        }
    }

    /// Returns whether a directory is server support data rather than a server instance.
    private static boolean isSupportDirectory(Path directory) {
        String name = directory.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.equals("cache") || name.equals("libraries") || name.equals("versions")
                || name.equals("logs") || name.equals("plugins") || name.equals("config")
                || name.equals("crash-reports") || name.equals("backups")
                || name.startsWith("world");
    }

    /// A downloaded server directory.
    public record ServerInstance(Path directory, ServerMetadata metadata) {
    }

    /// Persisted metadata for one server download.
    public record ServerMetadata(String type, String gameVersion, String build, String jar,
                                 @Nullable String channel) {
        /// Creates metadata without a channel for older records and non-Paper sources.
        public ServerMetadata(String type, String gameVersion, String build, String jar) {
            this(type, gameVersion, build, jar, null);
        }
    }
}
