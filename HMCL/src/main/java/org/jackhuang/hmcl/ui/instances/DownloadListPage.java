// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.instances;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import org.jackhuang.hmcl.addon.RemoteAddon;
import org.jackhuang.hmcl.addon.AddonProjectInfo;
import org.jackhuang.hmcl.addon.AddonProjectInfoRegistry;
import org.jackhuang.hmcl.addon.RemoteAddonRepository;
import org.jackhuang.hmcl.addon.repository.ModrinthRemoteAddonRepository;
import org.jackhuang.hmcl.download.DownloadCandidates;
import org.jackhuang.hmcl.download.DownloadProvider;
import org.jackhuang.hmcl.game.*;
import org.jackhuang.hmcl.setting.DownloadProviders;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.task.Task;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.WeakListenerHolder;
import org.jackhuang.hmcl.ui.construct.*;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jackhuang.hmcl.ui.download.PluginIconLoader;
import org.jackhuang.hmcl.util.*;
import org.jackhuang.hmcl.util.i18n.I18n;
import org.jackhuang.hmcl.util.javafx.BindingMapping;
import org.jackhuang.hmcl.util.versioning.GameVersionNumber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static org.jackhuang.hmcl.ui.FXUtils.ignoreEvent;
import static org.jackhuang.hmcl.ui.FXUtils.stringConverter;
import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.javafx.ExtendedProperties.selectedItemPropertyFor;

public class DownloadListPage extends Control implements DecoratorPage {
    protected final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final BooleanProperty failed = new SimpleBooleanProperty(false);
    private final boolean instanceSelection;
    private final ObjectProperty<HMCLGameInstance.Optional> instanceReference = new SimpleObjectProperty<>();
    private final IntegerProperty pageOffset = new SimpleIntegerProperty(0);
    private final IntegerProperty pageCount = new SimpleIntegerProperty(-1);
    private final ListProperty<RemoteAddon> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());
    private final ObservableList<GameInstanceID> instances = FXCollections.observableArrayList();
    private final ObjectProperty<GameInstanceID> selectedInstance = new SimpleObjectProperty<>();
    private final DownloadPage.DownloadCallback callback;
    private boolean searchInitialized = false;
    protected final BooleanProperty supportChinese = new SimpleBooleanProperty();
    private final ObservableList<Node> actions = FXCollections.observableArrayList();
    protected final ListProperty<String> downloadSources = new SimpleListProperty<>(this, "downloadSources", FXCollections.observableArrayList());
    protected final StringProperty downloadSource = new SimpleStringProperty();
    private final WeakListenerHolder listenerHolder = new WeakListenerHolder();
    private int searchID = 0;
    protected final RemoteAddonRepository repository;
    protected final @NotNull RemoteAddon.Type type;
    private final DownloadProvider downloadProvider;

    private Runnable retrySearch;

    public DownloadListPage(@NotNull RemoteAddon.Type type, RemoteAddonRepository repository) {
        this(type, repository, null, false);
    }

    public DownloadListPage(@NotNull RemoteAddon.Type type, RemoteAddonRepository repository, DownloadPage.DownloadCallback callback, boolean instanceSelection) {
        this.type = Objects.requireNonNull(type);
        this.repository = repository;
        this.callback = callback;
        this.instanceSelection = instanceSelection;
        this.downloadProvider = DownloadProviders.getDownloadProvider();
    }

    public DownloadProvider getDownloadProvider() {
        return downloadProvider;
    }

    public ObservableList<Node> getActions() {
        return actions;
    }

    public void loadInstance(HMCLGameInstance.Optional instance) {
        this.instanceReference.set(instance);

        setLoading(false);
        setFailed(false);

        if (!searchInitialized) {
            searchInitialized = true;
            search("", null, 0, "", RemoteAddonRepository.SortType.RELEVANCY);
        }

        if (instanceSelection) {
            HMCLGameRepository repository = instance.repository();
            instances.setAll(repository.getDisplayInstances()
                    .map(DefaultGameInstance::getId)
                    .toList());
            @Nullable HMCLGameInstance repositorySelection = repository.getSelectedInstance();
            selectedInstance.set(repositorySelection != null ? repositorySelection.getId() : null);
        }
    }

    public boolean isFailed() {
        return failed.get();
    }

    public BooleanProperty failedProperty() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed.set(failed);
    }

    public boolean isLoading() {
        return loading.get();
    }

    public BooleanProperty loadingProperty() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading.set(loading);
    }

    public void selectInstance(GameInstanceID instanceId) {
        FXUtils.runInFX(() -> selectedInstance.set(instanceId));
    }

    public DownloadPage.DownloadCallback getCallback() {
        return callback;
    }

    private void search(String userGameVersion, RemoteAddonRepository.Category category, int pageOffset, String searchFilter, RemoteAddonRepository.SortType sort) {
        retrySearch = null;
        setLoading(true);
        setFailed(false);

        int currentSearchID = searchID = searchID + 1;
        Task.supplyAsync(() -> {
            HMCLGameInstance.Optional instanceReference = this.instanceReference.get();
            @Nullable HMCLGameInstance instance = instanceReference.instance();
            if (instance == null) {
                return userGameVersion;
            } else {
                GameVersionNumber version = instance.getVersion();
                return version != GameVersionNumber.unknown() ? version.toString() : "";
            }
        }).thenApplyAsync(
                gameVersion -> repository.search(downloadProvider, type, gameVersion, category, pageOffset, 50, searchFilter, sort, RemoteAddonRepository.SortOrder.DESC)
        ).whenComplete(Schedulers.javafx(), (result, exception) -> {
            if (searchID != currentSearchID) {
                return;
            }

            setLoading(false);
            if (exception == null) {
                items.setAll(result.results().collect(Collectors.toList()));
                pageCount.set(result.totalPages());
                failed.set(false);
            } else {
                failed.set(true);
                pageCount.set(-1);
                retrySearch = () -> search(userGameVersion, category, pageOffset, searchFilter, sort);
            }
        }).executor(true);
    }

    protected String getLocalizedCategory(String category, Object self) {
        try {
            return repository instanceof ModrinthRemoteAddonRepository
                    ? i18n("modrinth.category." + category)
                    : i18n("curse.category." + category);
        } catch (java.util.MissingResourceException e) {
            // Plugin sources expose loaders (paper, spigot, ...) without a CurseForge translation.
            return category;
        }
    }

    protected boolean shouldDisplayCategory(String category) {
        return !"minecraft".equals(category);
    }

    private String getLocalizedCategoryIndent(ModDownloadListPageSkin.CategoryIndented category) {
        return StringUtils.repeats(' ', category.indent * 4) +
                (category.category() == null
                        ? i18n("curse.category.0")
                        : getLocalizedCategory(category.category().id(), category.category().self()));
    }

    protected String getLocalizedOfficialPage() {
        if (repository instanceof ModrinthRemoteAddonRepository) {
            return i18n("addon.modrinth");
        } else {
            return i18n("addon.curseforge");
        }
    }

    protected HMCLGameInstance.Optional getInstanceOptional() {
        if (instanceSelection) {
            @Nullable GameInstanceID instanceId = selectedInstance.get();
            return HMCLGameInstance.Optional.of(instanceReference.get().repository(), instanceId);
        } else {
            return instanceReference.get();
        }
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ModDownloadListPageSkin(this);
    }

    private static class ModDownloadListPageSkin extends SkinBase<DownloadListPage> {
        private final JFXListView<RemoteAddon> listView = new JFXListView<>();
        private final RemoteImageLoader iconLoader;

        protected ModDownloadListPageSkin(DownloadListPage control) {
            super(control);

            listView.getStyleClass().add("no-horizontal-scrollbar");

            iconLoader = new RemoteImageLoader(control.downloadProvider) {
                @Override
                protected @NotNull Task<Image> createLoadTask(@NotNull DownloadCandidates urls) {
                    return FXUtils.getRemoteImageTask(urls, 80, 80, true, true);
                }
            };

            BorderPane pane = new BorderPane();

            GridPane searchPane = new GridPane();
            pane.setTop(searchPane);
            searchPane.getStyleClass().addAll("card");
            BorderPane.setMargin(searchPane, new Insets(10, 10, 0, 10));

            ColumnConstraints nameColumn = new ColumnConstraints();
            nameColumn.setMinWidth(USE_PREF_SIZE);
            ColumnConstraints column1 = new ColumnConstraints();
            column1.setHgrow(Priority.ALWAYS);
            ColumnConstraints column2 = new ColumnConstraints();
            column2.setHgrow(Priority.ALWAYS);
            searchPane.getColumnConstraints().setAll(nameColumn, column1, nameColumn, column2);

            searchPane.setHgap(16);
            searchPane.setVgap(10);

            {
                int rowIndex = 0;

                if (control.instanceSelection || !control.downloadSources.isEmpty()) {
                    searchPane.addRow(rowIndex);
                    int columns = 0;
                    Node lastNode = null;
                    if (control.instanceSelection) {
                        JFXComboBox<GameInstanceID> instancesComboBox = new JFXComboBox<>();
                        instancesComboBox.setMaxWidth(Double.MAX_VALUE);
                        Bindings.bindContent(instancesComboBox.getItems(), control.instances);
                        selectedItemPropertyFor(instancesComboBox).bindBidirectional(control.selectedInstance);

                        searchPane.add(new Label(i18n("instance")), columns++, rowIndex);
                        searchPane.add(lastNode = instancesComboBox, columns++, rowIndex);
                    }

                    if (control.downloadSources.getSize() > 1) {
                        JFXComboBox<String> downloadSourceComboBox = new JFXComboBox<>();
                        downloadSourceComboBox.setMaxWidth(Double.MAX_VALUE);
                        downloadSourceComboBox.getItems().setAll(control.downloadSources.get());
                        downloadSourceComboBox.setConverter(stringConverter(I18n::i18n));
                        selectedItemPropertyFor(downloadSourceComboBox).bindBidirectional(control.downloadSource);

                        searchPane.add(new Label(i18n("settings.launcher.download_source")), columns++, rowIndex);
                        searchPane.add(lastNode = downloadSourceComboBox, columns++, rowIndex);
                    }

                    if (columns == 2) {
                        GridPane.setColumnSpan(lastNode, 3);
                    }

                    rowIndex++;
                }

                JFXTextField nameField = new JFXTextField();
                nameField.setPromptText(getSkinnable().supportChinese.get() ? i18n("search.hint.chinese") : i18n("search.hint.english"));
                if (getSkinnable().supportChinese.get()) {
                    FXUtils.installFastTooltip(nameField, i18n("search.hint.chinese"));
                } else {
                    FXUtils.installFastTooltip(nameField, i18n("search.hint.english"));
                }

                JFXComboBox<String> gameVersionField = new JFXComboBox<>();
                gameVersionField.setMaxWidth(Double.MAX_VALUE);
                gameVersionField.setEditable(true);
                gameVersionField.getItems().setAll(GameVersionNumber.getDefaultGameVersions());
                Label lblGameVersion = new Label(i18n("world.game_version"));
                final @Nullable JFXComboBox<String> coreBox;
                final @Nullable HBox sourceBox;
                final @Nullable javafx.scene.control.CheckBox modrinthSource;
                final @Nullable javafx.scene.control.CheckBox spigotSource;
                final @Nullable javafx.scene.control.CheckBox hangarSource;
                if (control.type == RemoteAddon.Type.PLUGIN) {
                    modrinthSource = new javafx.scene.control.CheckBox("Modrinth");
                    modrinthSource.setSelected(true);
                    spigotSource = new javafx.scene.control.CheckBox("SpigotMC");
                    spigotSource.setSelected(false);
                    spigotSource.setDisable(true);
                    spigotSource.setTooltip(new javafx.scene.control.Tooltip(i18n("server.local.plugins.spigot_unavailable")));
                    hangarSource = new javafx.scene.control.CheckBox("Hangar");
                    hangarSource.setSelected(true);
                    sourceBox = new HBox(12, modrinthSource, spigotSource, hangarSource);
                    sourceBox.setAlignment(Pos.CENTER_LEFT);

                    coreBox = new JFXComboBox<>();
                    coreBox.getItems().setAll("paper", "spigot");
                    coreBox.setValue("paper");
                    coreBox.setMaxWidth(Double.MAX_VALUE);

                    searchPane.add(new Label(i18n("server.local.plugins.search_label")), 0, rowIndex);
                    nameField.setMaxWidth(Double.MAX_VALUE);
                    searchPane.add(nameField, 1, rowIndex, 3, 1);
                    GridPane.setHgrow(nameField, Priority.ALWAYS);
                    GridPane.setFillWidth(nameField, true);
                    rowIndex++;
                    searchPane.addRow(rowIndex++, new Label(i18n("server.local.plugins.core")), coreBox,
                            new Label(i18n("server.local.plugins.core_version")), gameVersionField);

                    String lastVersion = org.jackhuang.hmcl.setting.SettingsManager.settings().getLastServerVersion();
                    if (lastVersion != null && !lastVersion.isBlank()) {
                        gameVersionField.setValue(lastVersion);
                    }
                } else {
                    coreBox = null;
                    sourceBox = null;
                    modrinthSource = null;
                    spigotSource = null;
                    hangarSource = null;
                    searchPane.addRow(rowIndex++, new Label(i18n("mods.name")), nameField, lblGameVersion, gameVersionField);
                }

                ObjectBinding<Boolean> hasVersion = BindingMapping.of(getSkinnable().instanceReference)
                        .map(instanceReference -> instanceReference.instanceId() == null);
                lblGameVersion.managedProperty().bind(hasVersion);
                lblGameVersion.visibleProperty().bind(hasVersion);
                gameVersionField.managedProperty().bind(hasVersion);
                gameVersionField.visibleProperty().bind(hasVersion);
                FXUtils.installFastTooltip(gameVersionField, i18n("search.enter"));

                FXUtils.onChangeAndOperate(getSkinnable().instanceReference, instanceReference -> {
                    if (getSkinnable().type == RemoteAddon.Type.PLUGIN || instanceReference.instanceId() != null) {
                        GridPane.setColumnSpan(nameField, 3);
                    } else {
                        GridPane.setColumnSpan(nameField, 1);
                    }
                });

                StackPane categoryStackPane = new StackPane();
                JFXComboBox<CategoryIndented> categoryComboBox = new JFXComboBox<>();
                categoryComboBox.getItems().setAll(CategoryIndented.ALL);
                categoryStackPane.getChildren().setAll(categoryComboBox);
                categoryComboBox.prefWidthProperty().bind(categoryStackPane.widthProperty());
                categoryComboBox.getStyleClass().add("fit-width");
                categoryComboBox.setPromptText(i18n("addon.category"));
                categoryComboBox.getSelectionModel().select(0);
                categoryComboBox.setConverter(stringConverter(getSkinnable()::getLocalizedCategoryIndent));
                FXUtils.onChangeAndOperate(getSkinnable().downloadSource, downloadSource -> {
                    categoryComboBox.getItems().setAll(CategoryIndented.ALL);
                    categoryComboBox.getSelectionModel().select(0);

                    Task.supplyAsync(() -> getSkinnable().repository.getCategories(getSkinnable().type))
                            .thenAcceptAsync(Schedulers.javafx(), categories -> {
                                if (!Objects.equals(getSkinnable().downloadSource.get(), downloadSource)) {
                                    return;
                                }

                                List<CategoryIndented> result = new ArrayList<>();
                                result.add(CategoryIndented.ALL);
                                for (RemoteAddonRepository.Category category : Lang.toIterable(categories)) {
                                    resolveCategory(category, 0, result);
                                }
                                categoryComboBox.getItems().setAll(result);
                                categoryComboBox.getSelectionModel().select(0);
                            }).start();
                });

                StackPane sortStackPane = new StackPane();
                JFXComboBox<RemoteAddonRepository.SortType> sortComboBox = new JFXComboBox<>();
                sortStackPane.getChildren().setAll(sortComboBox);
                sortComboBox.prefWidthProperty().bind(sortStackPane.widthProperty());
                sortComboBox.getStyleClass().add("fit-width");
                sortComboBox.setConverter(stringConverter(sortType -> i18n("addon.sort." + sortType.name().toLowerCase(Locale.ROOT))));
                sortComboBox.getItems().setAll(RemoteAddonRepository.SortType.values());
                sortComboBox.getSelectionModel().select(0);
                Node categoryNode = sourceBox != null ? sourceBox : categoryStackPane;
                Label categoryLabel = sourceBox != null
                        ? new Label(i18n("server.local.plugins.sources"))
                        : new Label(i18n("addon.category"));
                searchPane.addRow(rowIndex++, categoryLabel, categoryNode, new Label(i18n("search.sort")), sortStackPane);

                IntegerProperty filterID = new SimpleIntegerProperty(this, "Filter ID", 0);
                IntegerProperty currentFilterID = new SimpleIntegerProperty(this, "Current Filter ID", -1);
                EventHandler<ActionEvent> searchAction = e -> {
                    iconLoader.clearInvalidCache();
                    if (currentFilterID.get() != -1 && currentFilterID.get() != filterID.get()) {
                        control.pageOffset.set(0);
                    }
                    currentFilterID.set(filterID.get());

                    int pageOffset = control.pageOffset.get();
                    RemoteAddonRepository.Category selectedCategory = sourceBox != null
                            ? null
                            : Optional.ofNullable(categoryComboBox.getSelectionModel().getSelectedItem())
                                    .map(CategoryIndented::category)
                                    .orElse(null);
                    if (selectedCategory == null && getSkinnable().type == RemoteAddon.Type.PLUGIN
                            && coreBox != null && coreBox.getValue() != null) {
                        selectedCategory = new RemoteAddonRepository.Category(null, coreBox.getValue(), java.util.List.of());
                    }

                    getSkinnable().search(gameVersionField.getSelectionModel().getSelectedItem(),
                            selectedCategory,
                            pageOffset == -1 ? 0 : pageOffset,
                            nameField.getText(),
                            sortComboBox.getSelectionModel().getSelectedItem());
                };

                if (coreBox != null) {
                    control.listenerHolder.add(FXUtils.observeWeak(
                            () -> filterID.set(filterID.get() + 1),
                            coreBox.getSelectionModel().selectedItemProperty()));
                }

                if (modrinthSource != null && getSkinnable() instanceof org.jackhuang.hmcl.ui.download.PluginDownloadPage pluginPage) {
                    modrinthSource.setOnAction(event -> {
                        pluginPage.setSourceEnabled(org.jackhuang.hmcl.ui.download.PluginRepository.Source.MODRINTH, modrinthSource.isSelected());
                        control.pageOffset.set(0);
                        searchAction.handle(event);
                    });
                    hangarSource.setOnAction(event -> {
                        pluginPage.setSourceEnabled(org.jackhuang.hmcl.ui.download.PluginRepository.Source.HANGAR, hangarSource.isSelected());
                        control.pageOffset.set(0);
                        searchAction.handle(event);
                    });
                    spigotSource.setOnAction(event -> {
                        pluginPage.setSourceEnabled(org.jackhuang.hmcl.ui.download.PluginRepository.Source.SPIGOT, spigotSource.isSelected());
                        control.pageOffset.set(0);
                        searchAction.handle(event);
                    });
                }

                control.listenerHolder.add(FXUtils.observeWeak(
                        () -> filterID.set(filterID.get() + 1),

                        control.downloadSource,
                        gameVersionField.getSelectionModel().selectedItemProperty(),
                        categoryComboBox.getSelectionModel().selectedItemProperty(),
                        nameField.textProperty(),
                        sortComboBox.getSelectionModel().selectedItemProperty()
                ));

                HBox actionsBox = new HBox(8);
                GridPane.setColumnSpan(actionsBox, 4);
                actionsBox.setAlignment(Pos.CENTER);
                {
                    AggregatedObservableList<Node> actions = new AggregatedObservableList<>();

                    Holder<Runnable> changeButton = new Holder<>();

                    JFXButton firstPageButton = FXUtils.newBorderButton(i18n("search.first_page"));
                    firstPageButton.setOnAction(event -> {
                        control.pageOffset.set(0);
                        searchAction.handle(event);
                        changeButton.value.run();
                    });

                    JFXButton previousPageButton = FXUtils.newBorderButton(i18n("search.previous_page"));
                    previousPageButton.setOnAction(event -> {
                        int pageOffset = control.pageOffset.get();
                        if (pageOffset > 0) {
                            control.pageOffset.set(pageOffset - 1);
                            searchAction.handle(event);
                            changeButton.value.run();
                        }
                    });

                    Label pageDescription = new Label();
                    pageDescription.textProperty().bind(Bindings.createStringBinding(() -> {
                        int pageCount = control.pageCount.get();
                        return i18n("search.page_n", control.pageOffset.get() + 1, pageCount == -1 ? "-" : String.valueOf(pageCount));
                    }, control.pageOffset, control.pageCount));

                    JFXButton nextPageButton = FXUtils.newBorderButton(i18n("search.next_page"));
                    nextPageButton.setOnAction(event -> {
                        int nv = control.pageOffset.get() + 1;
                        if (nv < control.pageCount.get()) {
                            control.pageOffset.set(nv);
                            searchAction.handle(event);
                            changeButton.value.run();
                        }
                    });

                    JFXButton lastPageButton = FXUtils.newBorderButton(i18n("search.last_page"));
                    lastPageButton.setOnAction(event -> {
                        control.pageOffset.set(control.pageCount.get() - 1);
                        searchAction.handle(event);
                        changeButton.value.run();
                    });

                    firstPageButton.setDisable(true);
                    previousPageButton.setDisable(true);
                    lastPageButton.setDisable(true);
                    nextPageButton.setDisable(true);

                    changeButton.value = () -> {
                        int pageOffset = control.pageOffset.get();
                        int pageCount = control.pageCount.get();

                        boolean disableAll = pageCount >= -1 && pageCount <= 1;

                        boolean disablePrevious = disableAll || pageOffset == 0;
                        firstPageButton.setDisable(disablePrevious);
                        previousPageButton.setDisable(disablePrevious);

                        boolean disableNext = disableAll || pageOffset == pageCount - 1;
                        nextPageButton.setDisable(disableNext);
                        lastPageButton.setDisable(disableNext);

                        listView.scrollTo(0);
                    };

                    FXUtils.onChange(control.pageCount, pageCountN -> {
                        int pageCount = pageCountN.intValue();

                        if (pageCount != -1) {
                            if (control.pageOffset.get() + 1 >= pageCount) {
                                control.pageOffset.set(pageCount - 1);
                            }
                        }

                        changeButton.value.run();
                    });

                    FXUtils.onChange(control.pageOffset, pageOffsetN -> {
                        changeButton.value.run();
                    });

                    Pane placeholder = new Pane();
                    HBox.setHgrow(placeholder, Priority.SOMETIMES);

                    JFXButton searchButton = FXUtils.newRaisedButton(i18n("search"));
                    searchButton.setOnAction(searchAction);

                    actions.appendList(FXCollections.observableArrayList(firstPageButton, previousPageButton, pageDescription, nextPageButton, lastPageButton, placeholder, searchButton));
                    actions.appendList(control.actions);
                    Bindings.bindContent(actionsBox.getChildren(), actions.getAggregatedList());
                }

                searchPane.addRow(rowIndex++, actionsBox);

                FXUtils.onChange(control.downloadSource, v -> searchAction.handle(null));
                nameField.setOnAction(searchAction);
                gameVersionField.setOnAction(searchAction);
                categoryComboBox.setOnAction(searchAction);
                sortComboBox.setOnAction(searchAction);
            }

            SpinnerPane spinnerPane = new SpinnerPane();
            pane.setCenter(spinnerPane);
            {
                spinnerPane.loadingProperty().bind(getSkinnable().loadingProperty());
                spinnerPane.failedReasonProperty().bind(
                    Bindings.createStringBinding(() -> {
                        if (getSkinnable().isFailed()) {
                            return i18n("download.failed.refresh");
                        } else if (!getSkinnable().isLoading() && getSkinnable().pageCount.get() >= 0 && getSkinnable().items.isEmpty()) {
                            return i18n("search.no_results_found");
                        } else {
                            return null;
                        }
                    },
                    getSkinnable().failedProperty(),
                    getSkinnable().loadingProperty(),
                    getSkinnable().pageCount,
                    getSkinnable().items)
                );
                spinnerPane.setOnFailedAction(e -> {
                    if (getSkinnable().isFailed() && getSkinnable().retrySearch != null) {
                        getSkinnable().retrySearch.run();
                    }
                });

                spinnerPane.setContent(listView);
                Bindings.bindContent(listView.getItems(), getSkinnable().items);
                listView.setSelectionModel(new NoneMultipleSelectionModel<>());
                // ListViewBehavior would consume ESC pressed event, preventing us from handling it, so we ignore it here
                ignoreEvent(listView, KeyEvent.KEY_PRESSED, e -> e.getCode() == KeyCode.ESCAPE);
                listView.setCellFactory(x -> new DownloadListCell(getSkinnable(), listView, iconLoader));
            }

            this.getChildren().setAll(pane);
        }

        private record CategoryIndented(int indent, RemoteAddonRepository.Category category) {
            private static final CategoryIndented ALL = new CategoryIndented(0, null);
        }

        private static void resolveCategory(RemoteAddonRepository.Category category, int indent, List<CategoryIndented> result) {
            result.add(new CategoryIndented(indent, category));
            for (RemoteAddonRepository.Category subcategory : category.subcategories()) {
                resolveCategory(subcategory, indent + 1, result);
            }
        }
    }

    private static final class DownloadListCell extends ListCell<RemoteAddon> {
        private final DownloadListPage page;
        private final JFXListView<RemoteAddon> listView;
        private final RemoteImageLoader iconLoader;

        private static final Insets PADDING = new Insets(9, 9, 0, 9);
        private static final Insets LAST_PADDING = new Insets(9, 9, 9, 9);

        private final RipplerContainer graphic;
        private final StackPane wrapper = new StackPane();

        private final TwoLineListItem content = new TwoLineListItem();
        private final ImageContainer imageContainer = new ImageContainer(40);

        private final HBox pluginGraphic = new HBox(10);
        private final ImageContainer pluginIcon = new ImageContainer(40);
        private final VBox pluginText = new VBox(2);
        private final Label pluginTitle = new Label();
        private final Label pluginDescription = new Label();
        private final GridPane pluginFooter = new GridPane();
        private final Label pluginSource = new Label();
        private final Label pluginDate = new Label();
        private final Label pluginDownloads = new Label();
        private final Label pluginVersions = new Label();

        /// Creates a download card with its icon and text laid out by the same row.
        private DownloadListCell(DownloadListPage page, JFXListView<RemoteAddon> listView, RemoteImageLoader iconLoader) {
            this.page = page;
            this.listView = listView;
            this.iconLoader = iconLoader;

            setPadding(PADDING);

            content.setPadding(new Insets(8));
            content.setCursor(Cursor.HAND);
            TwoLineListItem.setMargin(imageContainer, new Insets(0, 8, 0, 0));
            imageContainer.setMouseTransparent(true);
            content.setLeading(imageContainer);

            pluginTitle.getStyleClass().add("title");
            pluginDescription.getStyleClass().add("subtitle");
            for (Label label : new Label[]{pluginSource, pluginDate, pluginDownloads, pluginVersions}) {
                label.getStyleClass().add("subtitle");
            }
            pluginFooter.setHgap(12);
            for (int i = 0; i < 4; i++) {
                ColumnConstraints constraints = new ColumnConstraints();
                constraints.setPercentWidth(25);
                pluginFooter.getColumnConstraints().add(constraints);
            }
            pluginFooter.addRow(0, pluginSource, pluginDate, pluginDownloads, pluginVersions);
            pluginText.getChildren().addAll(pluginTitle, pluginDescription, pluginFooter);
            HBox.setHgrow(pluginText, Priority.ALWAYS);
            pluginGraphic.setPadding(new Insets(8));
            pluginGraphic.getChildren().addAll(pluginIcon, pluginText);
            pluginIcon.setMouseTransparent(true);

            this.graphic = new RipplerContainer(content);
            wrapper.getChildren().setAll(this.graphic);
            wrapper.getStyleClass().add("card-no-padding");

            FXUtils.onClicked(wrapper, () -> {
                RemoteAddon item = getItem();
                if (item != null) {
                    Controllers.navigate(new DownloadPage(page, item, page.getInstanceOptional(), page.callback));
                }
            });

            setPrefWidth(0);

            FXUtils.limitCellWidth(listView, this);
        }

        /// Returns the display name of the plugin source.
        private static String sourceName(RemoteAddon item) {
            return item.source() == null ? "—" : switch (item.source()) {
                case MODRINTH -> "Modrinth";
                case HANGAR -> "Hangar";
                case SPIGOT -> "SpigotMC";
                case CURSEFORGE -> "CurseForge";
            };
        }

        /// Formats the supported game versions as a minimum-to-maximum range.
        private static String versionRange(@Nullable List<String> versions) {
            if (versions == null || versions.isEmpty()) {
                return "—";
            }
            try {
                List<String> sorted = versions.stream()
                        .filter(version -> version != null && !version.isBlank())
                        .sorted(java.util.Comparator.comparing(org.jackhuang.hmcl.util.versioning.GameVersionNumber::asGameVersion))
                        .toList();
                if (sorted.isEmpty()) {
                    return "—";
                }
                String min = sorted.get(0);
                String max = sorted.get(sorted.size() - 1);
                return min.equals(max) ? min : min + " ~ " + max;
            } catch (Exception e) {
                return String.join(", ", versions);
            }
        }

        /// Updates addon text and replaces category tags together so their labels can be reused.
        @Override
        protected void updateItem(@Nullable RemoteAddon item, boolean empty) {
            @Nullable RemoteAddon oldItem = getItem();
            boolean oldEmpty = isEmpty();

            super.updateItem(item, empty);

            if (oldItem == item && oldEmpty == empty) return;

            this.graphic.resetFeedback();

            if (empty || item == null) {
                setGraphic(null);
            } else if (page.type == RemoteAddon.Type.PLUGIN) {
                setPadding(
                    getIndex() == listView.getItems().size() - 1
                        ? LAST_PADDING
                        : PADDING
                );

                pluginTitle.setText(item.title());
                @Nullable String pluginDescriptionText = item.description();
                pluginDescription.setText(pluginDescriptionText == null
                        ? ""
                        : pluginDescriptionText.replaceAll("\\R", " "));
                pluginSource.setText(i18n("server.local.plugins.source_label") + ": " + sourceName(item));
                pluginDate.setText(i18n("server.local.plugins.updated_label") + ": "
                        + (item.updatedAt() == null ? "—" : I18n.formatDateTime(item.updatedAt())));
                pluginDownloads.setText(i18n("server.local.plugins.downloads_label") + ": "
                        + (item.downloads() > 0 ? String.format("%,d", item.downloads()) : "—"));
                pluginVersions.setText(i18n("server.local.plugins.versions_label") + ": " + versionRange(item.gameVersions()));

                if (wrapper.getChildren().isEmpty() || wrapper.getChildren().get(0) != pluginGraphic) {
                    wrapper.getChildren().setAll(pluginGraphic);
                }
                PluginIconLoader.load(item, pluginIcon.imageProperty());
                setGraphic(wrapper);
            } else {
                setPadding(
                    getIndex() == listView.getItems().size() - 1
                        ? LAST_PADDING
                        : PADDING
                );

                @Nullable AddonProjectInfoRegistry<?> registry = AddonProjectInfoRegistry.getByAddonType(page.type);
                @Nullable AddonProjectInfo mod = registry != null ? registry.getInfoByCurseForgeId(item.slug()) : null;
                content.setTitle(mod != null && I18n.isUseChinese() ? mod.displayName() : item.title());
                @Nullable String description = item.description();
                if (description != null) {
                    description = description.replaceAll("\\R", " ");
                }
                content.setSubtitle(description);
                content.getTags().setAll(item.categories().stream()
                        .filter(page::shouldDisplayCategory)
                        .map(category -> new Tag(page.getLocalizedCategory(category, null)))
                        .toList());
                iconLoader.load(imageContainer.imageProperty(), item.iconUrl());
                setGraphic(wrapper);
            }
        }
    }
}
