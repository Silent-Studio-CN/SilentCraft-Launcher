// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import org.jackhuang.hmcl.addon.RemoteAddon;
import org.jackhuang.hmcl.addon.repository.ModrinthRemoteAddonRepository;
import org.jackhuang.hmcl.game.HMCLGameInstance;
import org.jackhuang.hmcl.setting.GameDirectoryManager;
import org.jackhuang.hmcl.task.FileDownloadTask;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.instances.DownloadListPage;
import org.jackhuang.hmcl.util.TaskCancellationAction;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import javafx.stage.DirectoryChooser;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Server plugin list built on the exact mod download page, restricted to Modrinth plugins.
@NotNullByDefault
public final class PluginDownloadPage extends DownloadListPage {
    /// Shared repository facade that merges Modrinth and Hangar.
    private static final PluginRepository REPOSITORY = new PluginRepository();

    /// A plugin file selected for installation.
    public record SelectedPlugin(String title, String url, String filename) {
    }

    /// Creates the plugin page; a null {@code onSelect} downloads plugins to a chosen directory.
    public PluginDownloadPage(@Nullable Consumer<List<SelectedPlugin>> onSelect) {
        super(RemoteAddon.Type.PLUGIN, REPOSITORY,
                (provider, repository, instanceId, addon, version) -> {
                    SelectedPlugin plugin = new SelectedPlugin(
                            addon.title(), version.file().url(), version.file().filename());
                    if (onSelect != null) {
                        onSelect.accept(List.of(plugin));
                    } else {
                        downloadToDirectory(plugin);
                    }
                }, false);
        loadInstance(HMCLGameInstance.Optional.empty(GameDirectoryManager.getSelectedRepository()));
    }

    /// Enables or disables one plugin source and re-runs the current search.
    public void setSourceEnabled(PluginRepository.Source source, boolean enabled) {
        REPOSITORY.setEnabled(source, enabled);
        refresh();
    }

    /// Downloads a plugin into a user-selected directory.
    private static void downloadToDirectory(SelectedPlugin plugin) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n("download.server.plugins"));
        Path directory = Controllers.showDialog(chooser);
        if (directory == null) {
            return;
        }

        Controllers.taskDialog(new PaperDownloadTask(plugin.url(), directory.resolve(plugin.filename()), 0, null),
                i18n("message.downloading.file", plugin.filename()),
                TaskCancellationAction.NORMAL);
    }
}
