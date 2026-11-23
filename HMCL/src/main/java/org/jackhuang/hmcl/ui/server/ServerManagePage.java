// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.server.ServerConsole;
import org.jackhuang.hmcl.server.ServerRepository;
import org.jackhuang.hmcl.server.ServerSelection;
import org.jackhuang.hmcl.ui.Controllers;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.SVG;
import org.jackhuang.hmcl.ui.construct.AdvancedListBox;
import org.jackhuang.hmcl.ui.construct.AdvancedListItem;
import org.jackhuang.hmcl.ui.decorator.DecoratorAnimatedPage;
import org.jackhuang.hmcl.ui.decorator.DecoratorPage;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Server management page with a function sidebar and a live console.
@NotNullByDefault
public final class ServerManagePage extends DecoratorAnimatedPage implements DecoratorPage {
    /// Page title state.
    private final ReadOnlyObjectWrapper<State> state = new ReadOnlyObjectWrapper<>();

    /// Server shown by this page, or null when none is available.
    private final @Nullable ServerRepository.ServerInstance server;

    /// Live console output.
    private final TextArea console = new TextArea();

    /// Command input.
    private final JFXTextField command = new JFXTextField();

    /// Creates a page for the given server, or the selected/first server when null.
    public ServerManagePage(@Nullable ServerRepository.ServerInstance server) {
        getStyleClass().add("gray-background");
        this.server = server != null ? server : currentServer();

        state.set(State.fromTitle(this.server == null
                ? i18n("server.local.manage")
                : this.server.metadata().type() + " " + this.server.metadata().gameVersion()
                  + " " + this.server.metadata().build()));

        AdvancedListItem consoleItem = new AdvancedListItem();
        consoleItem.setTitle(i18n("server.console"));
        consoleItem.setLeftIcon(SVG.SCRIPT);
        consoleItem.setActive(true);

        AdvancedListItem configItem = new AdvancedListItem();
        configItem.setTitle(i18n("server.local.config"));
        configItem.setLeftIcon(SVG.EDIT);
        configItem.setOnAction(event -> {
            if (this.server != null) {
                Controllers.navigate(new ServerPropertiesPage(this.server.directory()));
            }
        });

        AdvancedListItem folderItem = new AdvancedListItem();
        folderItem.setTitle(i18n("button.reveal_dir"));
        folderItem.setLeftIcon(SVG.FOLDER);
        folderItem.setOnAction(event -> {
            if (this.server != null) {
                FXUtils.openFolder(this.server.directory());
            }
        });

        AdvancedListBox sidebar = new AdvancedListBox();
        sidebar.add(consoleItem).add(configItem).add(folderItem);
        getSidebar().add(sidebar);

        console.setEditable(false);
        console.setWrapText(false);
        console.setStyle("-fx-font-family: 'Menlo', 'Consolas', monospace;");
        VBox.setVgrow(console, Priority.ALWAYS);

        command.setPromptText(i18n("server.console.command"));
        HBox.setHgrow(command, Priority.ALWAYS);
        JFXButton send = new JFXButton(i18n("server.console.send"));
        send.setOnAction(event -> {
            ServerConsole.sendCommand(command.getText());
            command.clear();
        });
        HBox inputRow = new HBox(8, command, send);
        inputRow.setPadding(new Insets(8, 0, 0, 0));

        BorderPane content = new BorderPane();
        content.setCenter(console);
        content.setBottom(inputRow);
        content.setPadding(new Insets(16));
        setContent(content);

        ServerConsole.getLines().addListener((javafx.collections.ListChangeListener<String>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (String line : change.getAddedSubList()) {
                        console.appendText(line + System.lineSeparator());
                    }
                }
            }
        });
        console.setText(String.join(System.lineSeparator(), ServerConsole.getLines())
                + (ServerConsole.getLines().isEmpty() ? "" : System.lineSeparator()));

        if (this.server != null && !ServerConsole.runningProperty().get()) {
            ServerConsole.start(this.server);
        }
    }

    /// Creates a page for the selected or first server.
    public ServerManagePage() {
        this(ServerSelection.getSelected());
    }

    @Override
    public ReadOnlyObjectProperty<State> stateProperty() {
        return state.getReadOnlyProperty();
    }

    /// Returns the selected server, or the first server of the root.
    private static @Nullable ServerRepository.ServerInstance currentServer() {
        ServerRepository.ServerInstance selected = ServerSelection.getSelected();
        if (selected != null) {
            return selected;
        }
        List<ServerRepository.ServerInstance> instances =
                ServerRepository.listInstances(ServerRepository.rootDirectory());
        return instances.isEmpty() ? null : instances.get(0);
    }
}
