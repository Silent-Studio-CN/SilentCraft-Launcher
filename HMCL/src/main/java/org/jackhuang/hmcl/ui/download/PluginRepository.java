// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.jackhuang.hmcl.addon.RemoteAddon;
import org.jackhuang.hmcl.addon.RemoteAddonRepository;
import org.jackhuang.hmcl.addon.repository.HangarRemoteAddonRepository;
import org.jackhuang.hmcl.addon.repository.ModrinthRemoteAddonRepository;
import org.jackhuang.hmcl.download.DownloadProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Merges the enabled plugin sources behind one repository facade.
public final class PluginRepository implements RemoteAddonRepository {
    /// Sources that can be enabled independently.
    public enum Source {
        MODRINTH,
        HANGAR,
        SPIGOT
    }

    private final Set<Source> enabled = EnumSet.of(Source.MODRINTH, Source.HANGAR);

    /// Enables or disables a source; callers should trigger a new search afterwards.
    public void setEnabled(Source source, boolean value) {
        if (value) {
            enabled.add(source);
        } else {
            enabled.remove(source);
        }
    }

    /// Returns whether a source is currently enabled.
    public boolean isEnabled(Source source) {
        return enabled.contains(source);
    }

    @Override
    public String getApiBaseUrl() {
        return ModrinthRemoteAddonRepository.getInstance().getApiBaseUrl();
    }

    @Override
    public String getBaseUrl() {
        return ModrinthRemoteAddonRepository.getInstance().getBaseUrl();
    }

    @Override
    public SearchResult search(DownloadProvider downloadProvider, RemoteAddon.Type type, @Nullable String gameVersion,
                               @Nullable Category category, int pageOffset, int pageSize, String searchFilter,
                               SortType sortType, SortOrder sortOrder) {
        List<RemoteAddon> merged = new ArrayList<>();
        int pages = 0;

        if (enabled.contains(Source.MODRINTH)) {
            try {
                SearchResult result = ModrinthRemoteAddonRepository.getInstance()
                        .search(downloadProvider, type, gameVersion, category, pageOffset, pageSize, searchFilter, sortType, sortOrder);
                result.results().forEach(merged::add);
                pages = Math.max(pages, result.totalPages());
            } catch (Exception e) {
                LOG.warning("Modrinth plugin search failed", e);
            }
        }

        if (enabled.contains(Source.HANGAR)) {
            try {
                SearchResult result = HangarRemoteAddonRepository.getInstance()
                        .search(downloadProvider, type, gameVersion, category, pageOffset, pageSize, searchFilter, sortType, sortOrder);
                result.results().forEach(merged::add);
                pages = Math.max(pages, result.totalPages());
            } catch (Exception e) {
                LOG.warning("Hangar plugin search failed", e);
            }
        }

        if (enabled.contains(Source.SPIGOT)) {
            try {
                SearchResult result = org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                        .search(downloadProvider, type, gameVersion, category, pageOffset, pageSize, searchFilter, sortType, sortOrder);
                result.results().forEach(merged::add);
                pages = Math.max(pages, result.totalPages());
            } catch (Exception e) {
                LOG.warning("Spigot plugin search failed", e);
            }
        }

        return new SearchResult(merged.stream(), pages);
    }

    private static boolean isHangar(String id) {
        return id != null && id.contains("/");
    }

    private static boolean isSpigot(String id) {
        return id != null && id.startsWith("spigot:");
    }

    @Override
    public Optional<RemoteAddon.Version> getRemoteVersionByLocalFile(Path file) throws IOException {
        return ModrinthRemoteAddonRepository.getInstance().getRemoteVersionByLocalFile(file);
    }

    @Override
    public RemoteAddon getAddonById(DownloadProvider downloadProvider, String id) throws IOException {
        if (isSpigot(id)) {
            return org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                    .getAddonById(downloadProvider, id);
        }
        return isHangar(id)
                ? HangarRemoteAddonRepository.getInstance().getAddonById(downloadProvider, id)
                : ModrinthRemoteAddonRepository.getInstance().getAddonById(downloadProvider, id);
    }

    @Override
    public RemoteAddon resolveDependency(DownloadProvider downloadProvider, String id) throws IOException {
        if (isSpigot(id)) {
            return org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                    .resolveDependency(downloadProvider, id);
        }
        return isHangar(id)
                ? HangarRemoteAddonRepository.getInstance().resolveDependency(downloadProvider, id)
                : ModrinthRemoteAddonRepository.getInstance().resolveDependency(downloadProvider, id);
    }

    @Override
    public RemoteAddon.File getAddonFile(String projectId, String fileId) throws IOException {
        if (isSpigot(projectId)) {
            return org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                    .getAddonFile(projectId, fileId);
        }
        return isHangar(projectId)
                ? HangarRemoteAddonRepository.getInstance().getAddonFile(projectId, fileId)
                : ModrinthRemoteAddonRepository.getInstance().getAddonFile(projectId, fileId);
    }

    @Override
    public Stream<RemoteAddon.Version> getRemoteVersionsById(DownloadProvider downloadProvider, String id) throws IOException {
        if (isSpigot(id)) {
            return org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                    .getRemoteVersionsById(downloadProvider, id);
        }
        return isHangar(id)
                ? HangarRemoteAddonRepository.getInstance().getRemoteVersionsById(downloadProvider, id)
                : ModrinthRemoteAddonRepository.getInstance().getRemoteVersionsById(downloadProvider, id);
    }

    @Override
    public @Nullable String getAddonChangelog(DownloadProvider downloadProvider, String addonId, String versionId) throws IOException {
        if (isSpigot(addonId)) {
            return org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance()
                    .getAddonChangelog(downloadProvider, addonId, versionId);
        }
        return isHangar(addonId)
                ? HangarRemoteAddonRepository.getInstance().getAddonChangelog(downloadProvider, addonId, versionId)
                : ModrinthRemoteAddonRepository.getInstance().getAddonChangelog(downloadProvider, addonId, versionId);
    }

    @Override
    public @NotNull String getVersionPageUrl(RemoteAddon.Version version) throws IOException {
        return switch (version.source()) {
            case HANGAR -> HangarRemoteAddonRepository.getInstance().getVersionPageUrl(version);
            case SPIGOT -> org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance().getVersionPageUrl(version);
            default -> ModrinthRemoteAddonRepository.getInstance().getVersionPageUrl(version);
        };
    }

    @Override
    public Stream<Category> getCategories(RemoteAddon.Type type) throws IOException {
        return Stream.of(
                        ModrinthRemoteAddonRepository.getInstance().getCategories(type),
                        HangarRemoteAddonRepository.getInstance().getCategories(type),
                        org.jackhuang.hmcl.addon.repository.SpigotRemoteAddonRepository.getInstance().getCategories(type))
                .flatMap(stream -> stream);
    }
}
