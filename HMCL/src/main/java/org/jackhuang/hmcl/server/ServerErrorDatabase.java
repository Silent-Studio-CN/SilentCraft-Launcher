// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.server;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Downloads and matches the community-maintained server error database.
@NotNullByDefault
public final class ServerErrorDatabase {
    /// Error entry from the database.
    public record Entry(String id, List<String> patterns, String title, String message) {
    }

    /// Primary database URL; changes go through pull requests.
    private static final String PRIMARY =
            "https://raw.githubusercontent.com/Silent-Studio-CN/SilentCraft-Launcher/main/server-errors.json";

    /// Mirror URLs used when GitHub is slow or unreachable.
    private static final List<String> MIRRORS = List.of(
            "https://gh-proxy.com/" + PRIMARY,
            "https://ghproxy.net/" + PRIMARY
    );

    /// Local cache of the last successful download.
    private static final Path CACHE = Paths.get(System.getProperty("user.home"),
            ".hmcl", "cache", "server-errors.json");

    private ServerErrorDatabase() {
    }

    /// Finds the first entry whose pattern appears in the console text.
    public static @Nullable Entry find(String text) {
        String body = download();
        if (body == null) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            JsonArray errors = root.getAsJsonArray("errors");
            if (errors == null) {
                return null;
            }
            for (JsonElement element : errors) {
                JsonObject object = element.getAsJsonObject();
                List<String> patterns = new ArrayList<>();
                for (JsonElement pattern : object.getAsJsonArray("patterns")) {
                    patterns.add(pattern.getAsString());
                }
                for (String pattern : patterns) {
                    if (!pattern.isBlank() && text.contains(pattern)) {
                        return new Entry(
                                object.get("id").getAsString(),
                                List.copyOf(patterns),
                                object.get("title").getAsString(),
                                object.get("message").getAsString());
                    }
                }
            }
        } catch (Exception e) {
            LOG.warning("Failed to parse server error database", e);
        }
        return null;
    }

    /// Tries the cache, GitHub, then mirrors.
    private static @Nullable String download() {
        if (Files.isRegularFile(CACHE)) {
            try {
                return Files.readString(CACHE, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }

        List<String> urls = new ArrayList<>();
        urls.add(PRIMARY);
        urls.addAll(MIRRORS);
        for (String url : urls) {
            try {
                String body = fetch(url);
                if (body != null && !body.isBlank()) {
                    Files.createDirectories(CACHE.getParent());
                    Files.writeString(CACHE, body, StandardCharsets.UTF_8);
                    return body;
                }
            } catch (Exception e) {
                LOG.warning("Failed to download error database from " + url, e);
            }
        }
        return null;
    }

    /// Fetches one URL with a short timeout.
    private static @Nullable String fetch(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(8000);
        connection.setRequestProperty("User-Agent", "SilentCraft-Launcher");
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }
}
