// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXPopup;
import com.jfoenix.controls.JFXTextField;
import javafx.animation.PauseTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import org.jackhuang.hmcl.server.ServerRepository;
import org.jackhuang.hmcl.setting.SettingsManager;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.ListPageBase;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.ToolbarListPageSkin;
import org.jackhuang.hmcl.ui.animation.ContainerAnimations;
import org.jackhuang.hmcl.ui.animation.TransitionPane;
import org.jackhuang.hmcl.ui.construct.AdvancedListBox;
import org.jackhuang.hmcl.ui.construct.AdvancedListItem;
import org.jackhuang.hmcl.ui.construct.IconedMenuItem;
import org.jackhuang.hmcl.ui.construct.MenuSeparator;
import org.jackhuang.hmcl.ui.construct.PopupMenu;
import org.jackhuang.hmcl.ui.construct.RipplerContainer;
import org.jackhuang.hmcl.ui.construct.Tag;
import org.jackhuang.hmcl.ui.construct.TwoLineListItem;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jackhuang.hmcl.ui.download.DownloadPage;
import org.jackhuang.hmcl.util.StringUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Lists downloaded servers with the instance-list toolbar, search, and management menu.
@NotNullByDefault
public final class ServerListPage extends DecoratorAnimatedPage implements DecoratorPage {
    /// Current page title and navigation state.
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(i18n("server.local.list")));

    /// The list control holding the loaded server entries.
    private final ServerList list = new ServerList();

    /// Sidebar container rebuilt when server roots change.
    private final VBox rootItems = new VBox();

    /// Creates the page, its sidebar, and loads the current server entries.
    public ServerListPage() {
        getStyleClass().add("gray-background");

        AdvancedListItem addItem = new AdvancedListItem();
        addItem.setTitle(i18n("server.local.add_directory"));
        addItem.setLeftIcon(SVG.ADD_CIRCLE);
        addItem.setOnAction(event -> addDirectory());

        AdvancedListBox sidebar = new AdvancedListBox();
        sidebar.add(rootItems).add(addItem);
        getSidebar().add(sidebar);
        refreshSidebar();

        setContent(list);

        // Refresh when the page is shown again, so returning from an install shows the new server immediately.
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                list.refreshList();
            }
        });
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Reloads server entries from disk.
    @Override
    public void refresh() {
        list.refreshList();
    }

    /// Rebuilds the sidebar list from the configured roots.
    private void refreshSidebar() {
        rootItems.getChildren().clear();

        for (Path root : ServerRepository.getDirectories()) {
            AdvancedListItem item = new AdvancedListItem();
            item.setTitle(root.getFileName() == null ? root.toString() : root.getFileName().toString());
            item.setSubtitle(root.toString());
            item.setLeftIcon(SVG.FOLDER);
            item.setActive(root.toAbsolutePath().normalize()
                    .equals(ServerRepository.rootDirectory().toAbsolutePath().normalize()));
            item.setOnAction(event -> {
                SettingsManager.settings().setServerDirectory(root.toString());
                refreshSidebar();
                list.setRoot(root);
            });
            item.setRightAction(SVG.CLOSE, () -> removeDirectory(root));
            rootItems.getChildren().add(item);
        }
    }

    /// Adds a server root without replacing the existing ones.
    private void addDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(i18n("server.local.add_directory"));
        Path current = ServerRepository.rootDirectory();
        if (Files.isDirectory(current)) {
            chooser.setInitialDirectory(current.toFile());
        }
        Path selected = Controllers.showDialog(chooser);
        if (selected != null) {
            ServerRepository.addDirectory(selected);
            SettingsManager.settings().setServerDirectory(selected.toString());
            refreshSidebar();
            list.setRoot(selected);
        }
    }

    /// Removes a server root; yes deletes its files, no only removes the HMCL record.
    private void removeDirectory(Path root) {
        Controllers.confirm(i18n("server.local.remove_directory.message"), i18n("server.local.remove_directory"),
                () -> removeRoot(root, true),
                () -> removeRoot(root, false));
    }

    /// Removes a root and refreshes the visible list.
    private void removeRoot(Path root, boolean deleteFiles) {
        ServerRepository.removeDirectory(root, deleteFiles);
        refreshSidebar();
        list.setRoot(ServerRepository.rootDirectory());
    }

    /// One server entry with display properties derived from its metadata.
    private static final class ServerItem {
        /// Filesystem record backing this row.
        private final ServerRepository.ServerInstance instance;

        /// Displayed row title.
        private final StringProperty title = new SimpleStringProperty();

        /// Displayed row subtitle.
        private final StringProperty subtitle = new SimpleStringProperty();

        /// Displayed source tag text.
        private final StringProperty tag = new SimpleStringProperty();

        /// Optional release channel tag.
        private final @Nullable String channel;

        private ServerItem(ServerRepository.ServerInstance instance) {
            this.instance = instance;
            ServerRepository.ServerMetadata metadata = instance.metadata();
            String type = "Vanilla".equalsIgnoreCase(metadata.type())
                    ? i18n("server.local.type.vanilla")
                    : metadata.type();
            title.set(type + " " + metadata.gameVersion() + " " + metadata.build());
            subtitle.set(instance.directory().toString());
            tag.set(metadata.type());
            this.channel = metadata.channel();
        }

        /// Returns the backing server record.
        private ServerRepository.ServerInstance instance() {
            return instance;
        }
    }

    /// List control with search filtering and server management.
    private static final class ServerList extends ListPageBase<ServerItem> {
        /// Observable source list before filtering.
        private final ObservableList<ServerItem> sourceList = FXCollections.observableArrayList();

        /// Filtered list displayed by the skin.
        private final FilteredList<ServerItem> filteredList = new FilteredList<>(sourceList);

        /// Currently selected entry, if any.
        private final ObjectProperty<@Nullable ServerItem> selected = new SimpleObjectProperty<>();

        /// Root shown by this list.
        private Path root = ServerRepository.rootDirectory();

        /// Switches the displayed root and reloads its instances.
        private void setRoot(Path root) {
            this.root = root;
            refreshList();
        }

        private ServerList() {
            setItems(filteredList);
            setOnFailedAction(event -> Controllers.navigate(new DownloadPage()));
            refreshList();
        }

        /// Reloads all server entries from the repository.
        private void refreshList() {
            setLoading(true);
            setFailedReason(null);

            List<ServerRepository.ServerInstance> instances = ServerRepository.listInstances(root);
            sourceList.setAll(instances.stream().map(ServerItem::new).toList());

            if (instances.isEmpty()) {
                setFailedReason(i18n("server.local.empty"));
            }

            setLoading(false);
        }

        /// Builds a case-insensitive substring or regex predicate over title and path.
        private Predicate<ServerItem> createPredicate(@Nullable String searchText) {
            if (searchText == null || searchText.isEmpty()) {
                return item -> true;
            }
            if (searchText.startsWith("regex:")) {
                try {
                    Pattern pattern = Pattern.compile(searchText.substring("regex:".length()), Pattern.CASE_INSENSITIVE);
                    return item -> pattern.matcher(item.title.get() + " " + item.subtitle.get()).find();
                } catch (PatternSyntaxException e) {
                    return item -> false;
                }
            }
            String lower = searchText.toLowerCase(Locale.ROOT);
            return item -> (item.title.get() + " " + item.subtitle.get()).toLowerCase(Locale.ROOT).contains(lower);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ServerListSkin(this);
        }
    }

    /// Toolbar and list skin with the same normal/search transition as the game list.
    private static final class ServerListSkin extends ToolbarListPageSkin<ServerItem, ServerList> {
        /// Toolbar container that switches between the normal and search rows.
        private @Nullable TransitionPane toolbarPane;

        /// Normal toolbar with refresh, download, and search actions.
        private @Nullable HBox toolbarNormal;

        /// Search toolbar with the filter field and close action.
        private @Nullable HBox searchBar;

        /// Search filter field.
        private @Nullable JFXTextField searchField;

        private ServerListSkin(ServerList control) {
            super(control);
        }

        @Override
        protected List<Node> initializeToolbar(ServerList skinnable) {
            // The skin base calls this from its constructor, before subclass fields would be initialized.
            TransitionPane toolbarPane = new TransitionPane();
            HBox toolbarNormal = new HBox();
            HBox searchBar = new HBox();
            JFXTextField searchField = new JFXTextField();
            this.toolbarPane = toolbarPane;
            this.toolbarNormal = toolbarNormal;
            this.searchBar = searchBar;
            this.searchField = searchField;

            toolbarNormal.setAlignment(Pos.CENTER_LEFT);

            JFXButton refresh = ToolbarListPageSkin.createToolbarButton2(i18n("button.refresh"), SVG.REFRESH,
                    skinnable::refreshList);
            JFXButton download = ToolbarListPageSkin.createToolbarButton2(i18n("server.local.new"), SVG.DOWNLOAD,
                    () -> {
                        DownloadPage page = new DownloadPage();
                        Controllers.navigate(page);
                        page.selectPaperServerTab();
                    });
            JFXButton search = ToolbarListPageSkin.createToolbarButton2(i18n("search"), SVG.SEARCH,
                    () -> changeToolbar(searchBar));
            toolbarNormal.getChildren().setAll(refresh, download, search);

            searchBar.setAlignment(Pos.CENTER_LEFT);
            searchBar.setPadding(new Insets(0, 5, 0, 5));
            searchField.setPromptText(i18n("search"));
            HBox.setHgrow(searchField, Priority.ALWAYS);

            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> skinnable.filteredList.setPredicate(skinnable.createPredicate(searchField.getText())));
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                pause.setRate(1);
                pause.playFromStart();
            });

            JFXButton close = ToolbarListPageSkin.createToolbarButton2(null, SVG.CLOSE, () -> {
                changeToolbar(toolbarNormal);
                searchField.clear();
                pause.stop();
                skinnable.filteredList.setPredicate(null);
            });
            FXUtils.onEscPressed(searchField, close::fire);
            searchBar.getChildren().setAll(searchField, close);

            toolbarPane.setContent(toolbarNormal, ContainerAnimations.FADE);
            FXUtils.setOverflowHidden(toolbarPane, 8);
            return List.of(toolbarPane);
        }

        @Override
        protected ListCell<ServerItem> createListCell(JFXListView<ServerItem> listView) {
            return new ServerCell(getSkinnable(), listView);
        }

        /// Switches the toolbar with a fade animation and focuses the search field.
        private void changeToolbar(HBox newToolbar) {
            if (toolbarPane != null && toolbarPane.getCurrentNode() != newToolbar) {
                toolbarPane.setContent(newToolbar, ContainerAnimations.FADE);
                if (newToolbar == searchBar && searchField != null) {
                    FXUtils.runInFX(searchField::requestFocus);
                }
            }
        }
    }

    /// One server card with icon, text, tag, and management actions.
    private static final class ServerCell extends ListCell<ServerItem> {
        /// Page-level list used for selection and refresh.
        private final ServerList list;

        /// Row content with leading icon and trailing buttons.
        private final TwoLineListItem content = new TwoLineListItem();

        /// Selection indicator shown before the icon.
        private final com.jfoenix.controls.JFXRadioButton radio = new com.jfoenix.controls.JFXRadioButton();

        /// Server icon shown next to the selection indicator.
        private final javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView();

        private ServerCell(ServerList list, JFXListView<ServerItem> listView) {
            this.list = list;
            FXUtils.limitCellWidth(listView, this);
            content.setMaxWidth(Double.MAX_VALUE);
            content.setPadding(new Insets(8, 8, 8, 0));

            icon.setFitWidth(32);
            icon.setFitHeight(32);
            icon.setPreserveRatio(true);

            HBox leading = new HBox(8, radio, icon);
            leading.setAlignment(Pos.CENTER_LEFT);
            // Keep the radio-to-logo gap equal to the logo-to-text gap.
            TwoLineListItem.setMargin(leading, new Insets(0, 8, 0, 0));
            content.setLeading(leading);

            list.selected.addListener((observable, oldValue, newValue) -> {
                ServerItem current = getItem();
                radio.setSelected(current != null && current == newValue);
            });
        }

        @Override
        protected void updateItem(@Nullable ServerItem item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }

            content.setTitle(item.title.get());
            content.setSubtitle(item.subtitle.get());
            if (item.channel == null || item.channel.isBlank()) {
                content.getTags().setAll(new Tag(item.tag.get()));
            } else {
                content.getTags().setAll(new Tag(item.tag.get()),
                        new Tag(i18n("download.server.channel." + item.channel.toLowerCase(Locale.ROOT))));
            }

            radio.setSelected(list.selected.get() == item);
            radio.setOnAction(event -> {
                list.selected.set(item);
                org.jackhuang.hmcl.server.ServerSelection.select(item.instance());
            });

            org.jackhuang.hmcl.setting.GameInstanceIconType iconType =
                    "Paper".equalsIgnoreCase(item.instance().metadata().type())
                            ? org.jackhuang.hmcl.setting.GameInstanceIconType.PAPER
                            : org.jackhuang.hmcl.setting.GameInstanceIconType.GRASS;
            icon.setImage(iconType.getIcon());

            JFXButton open = FXUtils.newToggleButton4(SVG.FOLDER_OPEN);
            FXUtils.installFastTooltip(open, i18n("button.reveal_dir"));
            open.setOnAction(event -> FXUtils.openFolder(item.instance().directory()));

            JFXButton more = FXUtils.newToggleButton4(SVG.MORE_VERT);
            FXUtils.installFastTooltip(more, i18n("settings.game.management"));
            more.setOnAction(event -> showMenu(item, more));

            JFXButton remove = FXUtils.newToggleButton4(SVG.CLOSE);
            FXUtils.installFastTooltip(remove, i18n("instance.manage.remove"));
            remove.setOnAction(event -> this.remove(item));

            HBox trailing = new HBox(4, open, remove, more);
            trailing.setAlignment(Pos.CENTER_RIGHT);
            content.setTrailing(trailing);

            content.getStyleClass().add("md-list-cell");
            content.setPadding(new Insets(8, 8, 8, 0));

            content.setOnMouseClicked(event -> {
                if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    list.selected.set(item);
                    org.jackhuang.hmcl.server.ServerSelection.select(item.instance());
                } else if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                    showMenu(item, content);
                }
            });

            setGraphic(content);
        }

        /// Opens the management menu for one server entry.
        private void showMenu(ServerItem item, Node owner) {
            PopupMenu menu = new PopupMenu();
            JFXPopup popup = new JFXPopup(menu);
            menu.getContent().setAll(
                    new IconedMenuItem(SVG.EDIT, i18n("server.local.config"), () -> openConfig(item), popup),
                    new IconedMenuItem(SVG.FOLDER_COPY, i18n("instance.manage.duplicate"), () -> duplicate(item), popup),
                    new IconedMenuItem(SVG.EDIT, i18n("instance.manage.rename"), () -> rename(item), popup),
                    new MenuSeparator(),
                    new IconedMenuItem(SVG.DELETE, i18n("instance.manage.remove"), () -> remove(item), popup),
                    new IconedMenuItem(SVG.FOLDER_OPEN, i18n("button.reveal_dir"), () -> FXUtils.openFolder(item.instance().directory()), popup));
            popup.show(owner, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.RIGHT, -120, 0);
        }

        /// Opens the in-app server.properties editor.
        private void openConfig(ServerItem item) {
            Controllers.navigate(new ServerPropertiesPage(item.instance().directory()));
        }

        /// Renames the server directory.
        private void rename(ServerItem item) {
            TextInputDialog dialog = new TextInputDialog(item.instance().directory().getFileName().toString());
            dialog.setTitle(i18n("instance.manage.rename"));
            dialog.setHeaderText(null);
            dialog.setContentText(i18n("instance.manage.rename"));
            dialog.showAndWait().ifPresent(name -> {
                if (StringUtils.isBlank(name)) {
                    return;
                }
                try {
                    Files.move(item.instance().directory(), item.instance().directory().resolveSibling(name));
                    list.refreshList();
                } catch (IOException e) {
                    LOG.warning("Failed to rename server directory", e);
                }
            });
        }

        /// Copies the server directory next to the original.
        private void duplicate(ServerItem item) {
            Path source = item.instance().directory();
            Path target = source.resolveSibling(source.getFileName() + "-copy");
            try {
                copyDirectory(source, target);
                list.refreshList();
            } catch (IOException e) {
                LOG.warning("Failed to duplicate server directory", e);
            }
        }

        /// Deletes the server directory after confirmation.
        private void remove(ServerItem item) {
            Controllers.confirm(i18n("instance.manage.remove"), item.title.get(), () -> {
                try {
                    deleteDirectory(item.instance().directory());
                    list.refreshList();
                } catch (IOException e) {
                    LOG.warning("Failed to delete server directory", e);
                }
            }, null);
        }

        /// Recursively copies a directory.
        private static void copyDirectory(Path source, Path target) throws IOException {
            try (Stream<Path> stream = Files.walk(source)) {
                for (Path path : stream.toList()) {
                    Path destination = target.resolve(source.relativize(path).toString());
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(destination);
                    } else {
                        Files.copy(path, destination);
                    }
                }
            }
        }

        /// Recursively deletes a directory.
        private static void deleteDirectory(Path directory) throws IOException {
            try (Stream<Path> stream = Files.walk(directory)) {
                for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
