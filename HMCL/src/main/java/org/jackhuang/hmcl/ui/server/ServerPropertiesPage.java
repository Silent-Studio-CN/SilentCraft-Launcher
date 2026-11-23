// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.construct.AdvancedListBox;
import org.jackhuang.hmcl.ui.construct.AdvancedListItem;
import org.jackhuang.hmcl.ui.construct.ComponentList;
import org.jackhuang.hmcl.ui.construct.LinePane;
import org.jackhuang.hmcl.ui.construct.LineSwitch;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Form-based editor for server.properties with a category sidebar.
@NotNullByDefault
public final class ServerPropertiesPage extends DecoratorAnimatedPage implements DecoratorPage {
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();

    /// Server directory being edited.
    private final Path directory;

    /// All properties, including unknown keys kept from the original file.
    private final Map<String, String> properties = new LinkedHashMap<>();

    /// Editors that write back into [#properties].
    private final Map<String, Supplier<String>> editors = new LinkedHashMap<>();

    /// Current MOTD string edited by the popup editor.
    private String motdValue = "";

    /// Creates the form editor for one server directory.
    public ServerPropertiesPage(Path directory) {
        this.directory = directory;
        getStyleClass().add("gray-background");
        String title = i18n("server.local.config");
        for (org.jackhuang.hmcl.server.ServerRepository.ServerInstance server
                : org.jackhuang.hmcl.server.ServerRepository.listInstances()) {
            if (server.directory().toAbsolutePath().normalize()
                    .equals(directory.toAbsolutePath().normalize())) {
                title = server.metadata().type() + " " + server.metadata().gameVersion()
                        + " - " + i18n("server.local.config");
                break;
            }
        }
        state.set(State.fromTitle(title));
        loadProperties();

        AdvancedListItem basic = item(i18n("server.config.basic"), SVG.SETTINGS, this::showBasic);
        AdvancedListItem gameplay = item(i18n("server.config.gameplay"), SVG.STADIA_CONTROLLER, this::showGameplay);
        AdvancedListItem world = item(i18n("server.config.world"), SVG.PUBLIC, this::showWorld);
        AdvancedListItem security = item(i18n("server.config.security"), SVG.PERSON, this::showSecurity);
        AdvancedListItem advanced = item(i18n("server.config.advanced"), SVG.SCRIPT, this::showAdvanced);
        AdvancedListItem plugins = item(i18n("server.plugins"), SVG.EXTENSION, () ->
                Controllers.navigate(new PluginsPage(directory)));
        AdvancedListItem openFolder = item(i18n("button.reveal_dir"), SVG.FOLDER, () ->
                FXUtils.openFolder(directory));

        AdvancedListBox sidebar = new AdvancedListBox();
        sidebar.add(basic).add(gameplay).add(world).add(security).add(advanced).add(plugins).add(openFolder);
        getSidebar().add(sidebar);

        JFXButton save = FXUtils.newRaisedButton(i18n("button.save"));
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(event -> save());
        VBox bottom = new VBox(save);
        bottom.setPadding(new Insets(0, 12, 12, 12));
        setSidebarBottom(bottom);

        basic.setActive(true);
        showBasic();
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    private AdvancedListItem item(String title, SVG icon, Runnable action) {
        AdvancedListItem item = new AdvancedListItem();
        item.setTitle(title);
        item.setLeftIcon(icon);
        item.setOnAction(event -> action.run());
        return item;
    }

    private void loadProperties() {
        Path file = directory.resolve("server.properties");
        String content = null;
        try {
            if (Files.isRegularFile(file)) {
                content = Files.readString(file, StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            LOG.warning("Failed to read server.properties", e);
        }

        if (content == null) {
            try (InputStream in = ServerPropertiesPage.class.getResourceAsStream("/assets/server/server.properties")) {
                content = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException ignored) {
            }
        }

        for (String line : content.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }
            int index = trimmed.indexOf('=');
            if (index > 0) {
                properties.put(trimmed.substring(0, index), trimmed.substring(index + 1));
            }
        }
    }

    private void showBasic() {
        ComponentList list = new ComponentList();
        list.getContent().addAll(
                text("server-port", i18n("server.config.port"), "25565"),
                motd(),
                text("max-players", i18n("server.config.max_players"), "20"),
                toggle("online-mode", i18n("server.config.online_mode"), i18n("server.config.online_mode.hint")),
                text("level-name", i18n("server.config.level_name"), "world"),
                text("level-seed", i18n("server.config.level_seed"), ""));
        setContent(new VBox(list));
    }

    private void showGameplay() {
        ComponentList list = new ComponentList();
        list.getContent().addAll(
                choice("gamemode", i18n("server.config.gamemode"), List.of("survival", "creative", "adventure", "spectator")),
                choice("difficulty", i18n("server.config.difficulty"), List.of("peaceful", "easy", "normal", "hard")),
                toggle("pvp", i18n("server.config.pvp"), null),
                toggle("hardcore", i18n("server.config.hardcore"), null),
                toggle("allow-flight", i18n("server.config.allow_flight"), null),
                text("spawn-protection", i18n("server.config.spawn_protection"), "16"));
        setContent(new VBox(list));
    }

    private void showWorld() {
        ComponentList list = new ComponentList();
        list.getContent().addAll(
                text("view-distance", i18n("server.config.view_distance"), "10"),
                text("simulation-distance", i18n("server.config.simulation_distance"), "10"),
                toggle("generate-structures", i18n("server.config.generate_structures"), null),
                toggle("allow-nether", i18n("server.config.allow_nether"), null),
                text("max-world-size", i18n("server.config.max_world_size"), "29999984"));
        setContent(new VBox(list));
    }

    private void showSecurity() {
        ComponentList list = new ComponentList();
        list.getContent().addAll(
                toggle("white-list", i18n("server.config.white_list"), null),
                toggle("enforce-whitelist", i18n("server.config.enforce_whitelist"), null),
                toggle("enforce-secure-profile", i18n("server.config.enforce_secure_profile"), null),
                toggle("enable-command-block", i18n("server.config.command_block"), null),
                text("player-idle-timeout", i18n("server.config.idle_timeout"), "0"),
                text("op-permission-level", i18n("server.config.op_level"), "4"));
        setContent(new VBox(list));
    }

    private void showAdvanced() {
        ComponentList list = new ComponentList();
        list.getContent().addAll(
                text("max-tick-time", i18n("server.config.max_tick_time"), "60000"),
                text("network-compression-threshold", i18n("server.config.compression"), "256"),
                text("entity-broadcast-range-percentage", i18n("server.config.entity_range"), "100"),
                toggle("sync-chunk-writes", i18n("server.config.sync_chunks"), null),
                toggle("enable-rcon", i18n("server.config.rcon"), null),
                text("rcon.port", i18n("server.config.rcon_port"), "25575"));
        setContent(new VBox(list));
    }

    /// Adds a text editor for a property.
    private LinePane text(String key, String title, String hint) {
        JFXTextField field = new JFXTextField();
        field.setText(properties.getOrDefault(key, hint == null ? "" : hint));
        field.setPrefWidth(320);
        field.setMaxWidth(320);
        if (hint != null && !hint.isBlank()) {
            field.setPromptText(hint);
        }
        editors.put(key, field::getText);

        LinePane pane = new LinePane();
        pane.setTitle(title);
        pane.setRight(field);
        return pane;
    }

    /// Adds the MOTD preview row with a popup editor button.
    private LinePane motd() {
        motdValue = properties.getOrDefault("motd", "");

        javafx.scene.text.TextFlow preview = new javafx.scene.text.TextFlow();
        preview.setMaxWidth(300);
        preview.setPadding(new Insets(4, 8, 4, 8));
        preview.setStyle("-fx-background-color: #1b1b1f; -fx-background-radius: 4;");
        MotdEditorDialog.render(preview, motdValue);

        JFXButton edit = new JFXButton(i18n("server.config.motd.edit"));
        edit.setOnAction(event -> {
            String value = MotdEditorDialog.show(motdValue);
            if (value != null) {
                motdValue = value;
                MotdEditorDialog.render(preview, value);
            }
        });

        javafx.scene.layout.HBox right = new javafx.scene.layout.HBox(8, preview, edit);
        right.setAlignment(Pos.CENTER_RIGHT);

        LinePane pane = new LinePane();
        pane.setTitle(i18n("server.config.motd"));
        pane.setRight(right);
        editors.put("motd", () -> motdValue);
        return pane;
    }

    /// Adds a boolean switch for a property.
    private LineSwitch toggle(String key, String title, String subtitle) {
        LineSwitch toggle = new LineSwitch();
        toggle.setTitle(title);
        if (subtitle != null) {
            toggle.setSubtitle(subtitle);
        }
        toggle.setSelected(Boolean.parseBoolean(properties.getOrDefault(key, "false")));
        editors.put(key, () -> Boolean.toString(toggle.isSelected()));
        return toggle;
    }

    /// Adds a choice editor for a property.
    private LinePane choice(String key, String title, List<String> values) {
        JFXComboBox<String> box = new JFXComboBox<>();
        box.getItems().setAll(values);
        box.setValue(properties.getOrDefault(key, values.get(0)));
        box.setPrefWidth(320);
        box.setMaxWidth(320);
        editors.put(key, box::getValue);

        LinePane pane = new LinePane();
        pane.setTitle(title);
        pane.setRight(box);
        return pane;
    }

    /// Writes edited values back while preserving unknown keys.
    private void save() {
        editors.forEach((key, supplier) -> properties.put(key, supplier.get()));

        StringBuilder builder = new StringBuilder();
        properties.forEach((key, value) -> builder.append(key).append('=').append(value).append('\n'));
        try {
            Files.createDirectories(directory);
            Files.writeString(directory.resolve("server.properties"), builder.toString(), StandardCharsets.UTF_8);
            Controllers.showToast(i18n("server.local.config.saved"));
        } catch (IOException e) {
            LOG.warning("Failed to save server.properties", e);
        }
    }
}
