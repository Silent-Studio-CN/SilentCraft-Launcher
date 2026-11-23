// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.download;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.glavo.url.WebURL;
import org.jackhuang.hmcl.task.GetTask;
import org.jackhuang.hmcl.task.Schedulers;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jackhuang.hmcl.util.gson.JsonUtils;
import org.jetbrains.annotations.NotNullByDefault;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Searches Modrinth for plugins (never mods) and records the user's selection.
@NotNullByDefault
public final class PluginSelectPage extends StackPane implements DecoratorPage {
    private static final String SEARCH_API = "https://api.modrinth.com/v2/search?limit=30&facets=%5B%5B%22project_type%3Aplugin%22%5D%5D&query=";

    /// A plugin selected for pre-installation.
    public record PluginRef(String slug, String title) {
    }

    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>(State.fromTitle(i18n("server.local.plugins")));
    private final ObservableList<PluginRef> selected;
    private final ListView<Hit> results = new ListView<>();

    /// Creates the plugin search page sharing {@code selected} with the installer page.
    public PluginSelectPage(ObservableList<PluginRef> selected) {
        this.selected = selected;

        TextField query = new TextField();
        query.setPromptText(i18n("server.local.plugins.search"));
        HBox.setHgrow(query, Priority.ALWAYS);

        Button search = new Button(i18n("button.search"));
        search.setOnAction(event -> load(query.getText()));

        HBox toolbar = new HBox(8, query, search);
        toolbar.setPadding(new Insets(16));
        toolbar.setAlignment(Pos.CENTER_LEFT);

        results.setCellFactory(list -> new HitCell());
        VBox root = new VBox(toolbar, results);
        VBox.setVgrow(results, Priority.ALWAYS);
        getChildren().add(root);

        load("utility");
    }

    private void load(String query) {
        String encoded = URLEncoder.encode(query == null ? "" : query, StandardCharsets.UTF_8);
        new GetTask(WebURL.parse(SEARCH_API + encoded))
                .thenApplyAsync(json -> JsonUtils.fromNonNullJson(json, SearchResult.class))
                .whenComplete(Schedulers.javafx(), (result, exception) -> {
                    if (exception != null) {
                        LOG.warning("Failed to search Modrinth plugins", exception);
                        return;
                    }
                    results.getItems().setAll(result.hits());
                })
                .start();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    private final class HitCell extends ListCell<Hit> {
        private final CheckBox check = new CheckBox();
        private final Label title = new Label();
        private final Label description = new Label();
        private final VBox texts = new VBox(2, title, description);
        private final HBox box = new HBox(10, check, texts, new Region());

        private HitCell() {
            HBox.setHgrow(box.getChildren().get(2), Priority.ALWAYS);
            description.getStyleClass().add("subtitle");
            check.setOnAction(event -> {
                Hit hit = getItem();
                if (hit == null) {
                    return;
                }
                PluginRef ref = new PluginRef(hit.slug(), hit.title());
                if (check.isSelected()) {
                    if (!selected.contains(ref)) {
                        selected.add(ref);
                    }
                } else {
                    selected.remove(ref);
                }
            });
        }

        @Override
        protected void updateItem(Hit item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            title.setText(item.title());
            description.setText(item.description());
            check.setSelected(selected.contains(new PluginRef(item.slug(), item.title())));
            setGraphic(box);
        }
    }

    private record SearchResult(List<Hit> hits) {
    }

    private record Hit(String project_id, String slug, String title, String description) {
    }
}
