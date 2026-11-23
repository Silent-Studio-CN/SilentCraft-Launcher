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

/// SpigotMC plugin repository backed by the community Spiget API.
///
/// @see <a href="https://spiget.org/documentation">Spiget API</a>
public final class SpigotRemoteAddonRepository implements RemoteAddonRepository {
    private static final SpigotRemoteAddonRepository INSTANCE = new SpigotRemoteAddonRepository();

    public static SpigotRemoteAddonRepository getInstance() {
        return INSTANCE;
    }

    private static final String PREFIX = "https://api.spiget.org/v2";
    private static final String BASE = "https://www.spigotmc.org";
    private static final String ID_PREFIX = "spigot:";

    private SpigotRemoteAddonRepository() {
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
        String url;
        if (searchFilter == null || searchFilter.isBlank()) {
            url = PREFIX + "/resources?size=" + pageSize + "&page=" + pageOffset + "&sort=-downloads";
        } else {
            url = PREFIX + "/search/resources/" + encode(searchFilter)
                    + "?field=name&size=" + pageSize + "&page=" + pageOffset;
        }

        Resource[] resources = JsonUtils.fromNonNullJson(HttpRequest.GET(url).getString(), Resource[].class);
        List<RemoteAddon> result = new ArrayList<>();
        if (resources != null) {
            for (Resource resource : resources) {
                result.add(new RemoteSpigotAddon(resource).toAddon());
            }
        }
        return new SearchResult(result.stream(), pageOffset + 1);
    }

    @Override
    public Optional<RemoteAddon.Version> getRemoteVersionByLocalFile(Path file) {
        return Optional.empty();
    }

    @Override
    public RemoteAddon getAddonById(DownloadProvider downloadProvider, String id) throws IOException {
        String numeric = strip(id);
        Resource resource = JsonUtils.fromNonNullJson(
                HttpRequest.GET(PREFIX + "/resources/" + numeric).getString(), Resource.class);
        if (resource == null) {
            throw new IOException("Spigot resource not found: " + id);
        }
        return new RemoteSpigotAddon(resource).toAddon();
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
        return getRemoteVersionsById(null, projectId)
                .filter(version -> version.versionId().equals(fileId) || version.version().equals(fileId))
                .map(RemoteAddon.Version::file)
                .findFirst()
                .orElseThrow(() -> new IOException("Spigot version not found: " + projectId + " / " + fileId));
    }

    @Override
    public Stream<RemoteAddon.Version> getRemoteVersionsById(DownloadProvider downloadProvider, String id) throws IOException {
        String numeric = strip(id);
        Resource resource = JsonUtils.fromNonNullJson(
                HttpRequest.GET(PREFIX + "/resources/" + numeric).getString(), Resource.class);
        Version[] versions = JsonUtils.fromNonNullJson(
                HttpRequest.GET(PREFIX + "/resources/" + numeric + "/versions?size=50&sort=-releaseDate").getString(),
                Version[].class);

        String downloadUrl = resource != null && resource.file() != null && resource.file().externalUrl() != null
                ? resource.file().externalUrl()
                : PREFIX + "/resources/" + numeric + "/download";
        String baseName = resource == null ? "plugin" : resource.name().replaceAll("[^A-Za-z0-9._-]+", "-");

        if (versions == null || versions.length == 0) {
            return Stream.of(new RemoteAddon.Version(RemoteAddon.Source.SPIGOT, "latest", id,
                    "", "latest", Instant.EPOCH, RemoteAddon.VersionType.Release,
                    new RemoteAddon.File(Map.of(), downloadUrl, baseName + ".jar"),
                    List.of(), resource == null ? List.of() : resource.testedVersions(), List.of()));
        }

        return Stream.of(versions)
                .sorted(Comparator.comparingLong(Version::releaseDate).reversed())
                .map(version -> new RemoteAddon.Version(RemoteAddon.Source.SPIGOT,
                        String.valueOf(version.id()), id, "", version.name(),
                        Instant.ofEpochSecond(version.releaseDate()), RemoteAddon.VersionType.Release,
                        new RemoteAddon.File(Map.of(), downloadUrl, baseName + "-" + version.name() + ".jar"),
                        List.of(), resource == null ? List.of() : resource.testedVersions(), List.of()));
    }

    @Override
    public @Nullable String getAddonChangelog(DownloadProvider downloadProvider, String addonId, String versionId) {
        return null;
    }

    @Override
    public @NotNull String getVersionPageUrl(RemoteAddon.Version version) {
        return BASE + "/resources/" + strip(version.projectId()) + "/";
    }

    @Override
    public Stream<Category> getCategories(RemoteAddon.Type type) {
        return Stream.of(new Category(null, "spigot", List.of()));
    }

    /// Addon handle bound to one Spigot resource.
    private final class RemoteSpigotAddon implements RemoteAddon.IAddon {
        private final Resource resource;

        private RemoteSpigotAddon(Resource resource) {
            this.resource = resource;
        }

        @Override
        public List<RemoteAddon> loadDependencies(RemoteAddonRepository repo, DownloadProvider downloadProvider) {
            return List.of();
        }

        @Override
        public Stream<RemoteAddon.Version> loadVersions(RemoteAddonRepository repo, DownloadProvider downloadProvider) throws IOException {
            return getRemoteVersionsById(downloadProvider, ID_PREFIX + resource.id());
        }

        private RemoteAddon toAddon() {
            String id = ID_PREFIX + resource.id();
            return new RemoteAddon(id, "", resource.name(),
                    resource.tag() == null ? "" : resource.tag(),
                    List.of("spigot"),
                    BASE + "/resources/" + resource.id() + "/",
                    PREFIX + "/resources/" + resource.id() + "/icon",
                    this, RemoteAddon.Type.PLUGIN,
                    resource.downloads(), java.time.Instant.ofEpochSecond(resource.updateDate()),
                    resource.testedVersions() == null ? List.of() : resource.testedVersions(),
                    RemoteAddon.Source.SPIGOT);
        }
    }

    private static String strip(String id) {
        return id.startsWith(ID_PREFIX) ? id.substring(ID_PREFIX.length()) : id;
    }

    private static String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private record Resource(int id, String name, String tag,
                            @SerializedName("testedVersions") List<String> testedVersions,
                            @SerializedName("updateDate") long updateDate,
                            @SerializedName("downloads") long downloads,
                            @SerializedName("file") FileInfo file) {
    }

    private record FileInfo(String type, long size, String url, @SerializedName("externalUrl") String externalUrl) {
    }

    private record Version(int id, String name, @SerializedName("releaseDate") long releaseDate) {
    }
}
