// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.jackhuang.hmcl.download.ComponentRemoteVersion;
import org.jackhuang.hmcl.game.GameComponentType;
import org.jackhuang.hmcl.game.GameInstanceManifest;
import org.jackhuang.hmcl.game.GameInstancePatch;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.download.DefaultDependencyManager;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

/// A single Paper build exposed as a {@link ComponentRemoteVersion} so the shared version list can render it.
public final class PaperBuildRemoteVersion extends ComponentRemoteVersion {
    private final int buildId;
    private final String channel;
    private final String url;
    private final @Nullable String sha256;
    private final long size;
    private final String metaUrl;
    private final String fileName;

    public PaperBuildRemoteVersion(GameVersionNumber gameVersion, int buildId, String channel,
                                   @Nullable Instant releaseDate, String url, @Nullable String sha256, long size, String metaUrl, String fileName) {
        super(GameComponentType.GAME, gameVersion, "Paper " + gameVersion + " #" + buildId,
                releaseDate, mapType(channel), List.of(url));
        this.buildId = buildId;
        this.channel = channel;
        this.url = url;
        this.sha256 = sha256;
        this.size = size;
        this.metaUrl = metaUrl;
        this.fileName = fileName;
    }

    public int getBuildId() {
        return buildId;
    }

    public String channel() {
        return channel;
    }

    public String url() {
        return url;
    }

    public @Nullable String sha256() {
        return sha256;
    }

    public long size() {
        return size;
    }

    public String metaUrl() {
        return metaUrl;
    }

    public String fileName() {
        return fileName;
    }

    private static Type mapType(String channel) {
        return switch (channel) {
            case "STABLE" -> Type.RELEASE;
            case "BETA" -> Type.SNAPSHOT;
            default -> Type.UNCATEGORIZED;
        };
    }

    @Override
    public Task<GameInstancePatch> getInstallTask(DefaultDependencyManager dependencyManager,
                                                  GameInstanceManifest baseManifest,
                                                  Path modsDirectory) {
        throw new UnsupportedOperationException("Paper builds are downloaded, not installed as a game instance");
    }
}
