// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.application.Platform;
import javafx.beans.value.WritableValue;
import javafx.scene.image.Image;
import org.jackhuang.hmcl.addon.RemoteAddon;
import org.jackhuang.hmcl.image.ImageLoaders;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Resolves plugin icons with Hangar detail, CDN, then Modrinth fallbacks.
@NotNullByDefault
public final class PluginIconLoader {
    /// Decoded icons keyed by addon id.
    private static final Map<String, Image> CACHE = new ConcurrentHashMap<>();
    /// Addon ids with an in-flight resolve request.
    private static final Set<String> LOADING = ConcurrentHashMap.newKeySet();

    private PluginIconLoader() {
    }

    /// Starts resolving an icon and assigns it to the target on the JavaFX thread.
    public static void load(RemoteAddon addon, WritableValue<Image> target) {
        Image cached = CACHE.get(addon.slug());
        if (cached != null) {
            target.setValue(cached);
            return;
        }

        target.setValue(null);

        if (!LOADING.add(addon.slug())) {
            return;
        }

        new Thread(() -> {
            Image image = resolve(addon);
            if (image != null) {
                CACHE.put(addon.slug(), image);
            }
            LOADING.remove(addon.slug());

            if (image != null) {
                Platform.runLater(() -> target.setValue(image));
            }
        }, "plugin-icon").start();
    }

    /// Tries the Hangar detail avatar, then the CDN URL, then a Modrinth match.
    private static @Nullable Image resolve(RemoteAddon addon) {
        List<String> candidates = new ArrayList<>();

        if (addon.source() == RemoteAddon.Source.HANGAR) {
            String detail = fetchHangarAvatar(addon.slug());
            if (detail != null) {
                candidates.add(detail);
            }
        }

        if (addon.iconUrl() != null && !addon.iconUrl().isBlank()) {
            candidates.add(addon.iconUrl());
        }

        if (addon.source() == RemoteAddon.Source.HANGAR) {
            String modrinth = fetchModrinthIcon(addon.title());
            if (modrinth != null) {
                candidates.add(modrinth);
            }
        }

        for (String candidate : candidates) {
            Image image = loadImage(candidate);
            if (image != null) {
                return image;
            }
        }
        return null;
    }

    /// Reads the avatar of a Hangar project detail response.
    private static @Nullable String fetchHangarAvatar(String id) {
        try {
            String json = get("https://hangar.papermc.io/api/v1/projects/" + id);
            return JsonUtils.fromNonNullJson(json, HangarProject.class).avatarUrl();
        } catch (Exception e) {
            return null;
        }
    }

    /// Finds the first Modrinth hit for a project name and returns its icon URL.
    private static @Nullable String fetchModrinthIcon(String title) {
        try {
            String query = URLEncoder.encode(title, StandardCharsets.UTF_8);
            String json = get("https://api.modrinth.com/v2/search?limit=1&query=" + query);
            ModrinthSearch result = JsonUtils.fromNonNullJson(json, ModrinthSearch.class);
            return result.hits() == null || result.hits().isEmpty() ? null : result.hits().get(0).icon_url();
        } catch (Exception e) {
            return null;
        }
    }

    /// Downloads and decodes one image, returning null on any failure.
    private static @Nullable Image loadImage(String url) {
        try {
            byte[] data = getBytes(url);
            return ImageLoaders.DEFAULT.load(new ByteArrayInputStream(data), url, 40, 40, true, true);
        } catch (Exception e) {
            return null;
        }
    }

    private static String get(String url) throws Exception {
        return new String(getBytes(url), StandardCharsets.UTF_8);
    }

    private static byte[] getBytes(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(15_000);
        connection.setReadTimeout(20_000);
        connection.setRequestProperty("User-Agent", "HMCL/3.18");
        try (InputStream in = connection.getInputStream()) {
            return in.readAllBytes();
        } finally {
            connection.disconnect();
        }
    }

    private record HangarProject(String avatarUrl) {
    }

    private record ModrinthHit(String icon_url) {
    }

    private record ModrinthSearch(List<ModrinthHit> hits) {
    }
}
