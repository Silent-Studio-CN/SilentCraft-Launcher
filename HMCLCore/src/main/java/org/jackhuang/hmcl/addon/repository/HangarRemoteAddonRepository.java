// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.addon.repository;

import com.google.gson.annotations.SerializedName;
import org.jackhuang.hmcl.addon.RemoteAddon;
import org.jackhuang.hmcl.addon.RemoteAddonRepository;
import org.jackhuang.hmcl.download.DownloadProvider;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jackhuang.hmcl.util.io.HttpRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Hangar (PaperMC) plugin repository.
///
/// @see <a href="https://hangar.papermc.io/api/v1">Hangar API</a>
public final class HangarRemoteAddonRepository implements RemoteAddonRepository {
    private static final HangarRemoteAddonRepository INSTANCE = new HangarRemoteAddonRepository();

    public static HangarRemoteAddonRepository getInstance() {
        return INSTANCE;
    }

    private static final String PREFIX = "https://hangar.papermc.io/api/v1";
    private static final String BASE = "https://hangar.papermc.io";

    private HangarRemoteAddonRepository() {
    }

    @Override
    public String getApiBaseUrl() {
        return PREFIX;
    }

    @Override
    public String getBaseUrl() {
        return BASE;
    }

    @Override
    public SearchResult search(DownloadProvider downloadProvider, RemoteAddon.Type type, @Nullable String gameVersion,
                               @Nullable Category category, int pageOffset, int pageSize, String searchFilter,
                               SortType sortType, SortOrder sortOrder) throws IOException {
        StringBuilder url = new StringBuilder(PREFIX)
                .append("/projects?limit=").append(pageSize)
                .append("&offset=").append(pageOffset * pageSize)
                .append("&q=").append(encode(searchFilter))
                .append("&platform=PAPER");
        if (gameVersion != null && !gameVersion.isBlank()) {
            url.append("&version=").append(encode(gameVersion));
        }

        HangarProjects projects = JsonUtils.fromNonNullJson(HttpRequest.GET(url.toString()).getString(), HangarProjects.class);

        List<RemoteAddon> result = new ArrayList<>();
        if (projects != null && projects.result() != null) {
            for (Project project : projects.result()) {
                result.add(toAddon(project));
            }
        }
        int totalPages = projects == null || projects.pagination() == null
                ? 1
                : Math.max(1, (projects.pagination().count() + pageSize - 1) / pageSize);
        return new SearchResult(result.stream(), totalPages);
    }

    @Override
    public Optional<RemoteAddon.Version> getRemoteVersionByLocalFile(Path file) {
        return Optional.empty();
    }

    @Override
    public RemoteAddon getAddonById(DownloadProvider downloadProvider, String id) throws IOException {
        Project project = JsonUtils.fromNonNullJson(
                HttpRequest.GET(PREFIX + "/projects/" + id).getString(), Project.class);
        if (project == null) {
            throw new IOException("Hangar project not found: " + id);
        }
        return toAddon(project);
    }

    @Override
    public RemoteAddon resolveDependency(DownloadProvider downloadProvider, String id) throws IOException {
        try {
            return getAddonById(downloadProvider, id);
        } catch (IOException e) {
            return RemoteAddon.BROKEN;
        }
    }

    @Override
    public RemoteAddon.File getAddonFile(String projectId, String fileId) throws IOException {
        List<RemoteAddon.Version> versions = getRemoteVersionsById(null, projectId).toList();
        for (RemoteAddon.Version version : versions) {
            if (version.version().equals(fileId) || version.versionId().equals(fileId)) {
                return version.file();
            }
        }
        throw new IOException("Hangar version not found: " + projectId + " / " + fileId);
    }

    @Override
    public Stream<RemoteAddon.Version> getRemoteVersionsById(DownloadProvider downloadProvider, String id) throws IOException {
        HangarVersions versions = JsonUtils.fromNonNullJson(
                HttpRequest.GET(PREFIX + "/projects/" + id + "/versions?limit=50").getString(),
                HangarVersions.class);
        if (versions == null || versions.result() == null) {
            return Stream.empty();
        }
        return versions.result().stream()
                .sorted(Comparator.comparing((Version version) -> version.createdAt() == null ? Instant.EPOCH : version.createdAt()).reversed())
                .map(version -> toVersion(id, version));
    }

    @Override
    public @Nullable String getAddonChangelog(DownloadProvider downloadProvider, String addonId, String versionId) throws IOException {
        return getRemoteVersionsById(downloadProvider, addonId)
                .filter(version -> version.versionId().equals(versionId) || version.version().equals(versionId))
                .map(RemoteAddon.Version::name)
                .findFirst().orElse(null);
    }

    @Override
    public @NotNull String getVersionPageUrl(RemoteAddon.Version version) {
        return BASE + "/" + version.projectId() + "/versions/" + version.version();
    }

    @Override
    public Stream<Category> getCategories(RemoteAddon.Type type) {
        return Stream.of(
                new Category(null, "paper", List.of()),
                new Category(null, "spigot", List.of()),
                new Category(null, "bukkit", List.of()));
    }

    /// Addon handle that resolves versions for a single Hangar project.
    private final class HangarAddon implements RemoteAddon.IAddon {
        private final String projectId;

        private HangarAddon(String projectId) {
            this.projectId = projectId;
        }

        @Override
        public List<RemoteAddon> loadDependencies(RemoteAddonRepository repo, DownloadProvider downloadProvider) {
            return List.of();
        }

        @Override
        public Stream<RemoteAddon.Version> loadVersions(RemoteAddonRepository repo, DownloadProvider downloadProvider) throws IOException {
            return getRemoteVersionsById(downloadProvider, projectId);
        }
    }

    private RemoteAddon toAddon(Project project) {
        String id = project.namespace() == null ? project.name() : project.namespace().owner() + "/" + project.namespace().slug();
        // Search results omit avatarUrl; the CDN path is predictable from the numeric project id.
        String icon = project.avatarUrl() != null && !project.avatarUrl().isBlank()
                ? project.avatarUrl()
                : "https://hangarcdn.papermc.io/avatars/project/" + project.id() + ".webp";
        return new RemoteAddon(id, "", project.name(), project.description(),
                List.of(project.category() == null ? "paper" : project.category()),
                BASE + "/" + id, icon, new HangarAddon(id), RemoteAddon.Type.PLUGIN,
                project.stats() == null ? 0L : project.stats().downloads(), project.lastUpdated(),
                List.of(), RemoteAddon.Source.HANGAR);
    }

    private RemoteAddon.Version toVersion(String projectId, Version version) {
        Map<String, String> hashes = Map.of();
        String fileName = "plugin.jar";
        String url = BASE + "/api/v1/projects/" + projectId + "/versions/" + version.name() + "/PAPER/download";

        PlatformDownload download = version.downloads() == null ? null : version.downloads().get("PAPER");
        FileInfo info = download == null ? null : download.fileInfo();
        if (info != null) {
            fileName = info.name();
            if (info.sha256Hash() != null) {
                hashes = Map.of("sha256", info.sha256Hash());
            }
        }

        return new RemoteAddon.Version(RemoteAddon.Source.HANGAR, String.valueOf(version.id()), projectId,
                version.description() == null ? version.name() : version.description(), version.name(),
                version.createdAt(), RemoteAddon.VersionType.Release,
                new RemoteAddon.File(hashes, url, fileName),
                List.of(), List.of(), List.of());
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value == null ? "" : value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private record Pagination(int count, int limit, int offset) {
    }

    private record Namespace(String owner, String slug) {
    }

    private record Stats(int downloads) {
    }

    private record Project(int id, String name, Namespace namespace, String description,
                           @SerializedName("avatarUrl") String avatarUrl, String category, Stats stats,
                           @SerializedName("lastUpdated") Instant lastUpdated) {
    }

    private record HangarProjects(Pagination pagination, List<Project> result) {
    }

    private record FileInfo(String name, long sizeBytes, String sha256Hash) {
    }

    private record PlatformDownload(FileInfo fileInfo) {
    }

    private record Version(int id, String name, String description, @SerializedName("createdAt") Instant createdAt,
                           Map<String, PlatformDownload> downloads) {
    }

    private record HangarVersions(Pagination pagination, List<Version> result) {
    }
}
