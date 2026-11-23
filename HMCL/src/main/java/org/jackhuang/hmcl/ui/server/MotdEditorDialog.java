// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.server;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

/// Modal MOTD editor with live preview and Minecraft section-code palette.
@NotNullByDefault
public final class MotdEditorDialog {
    /// Minecraft color codes and their preview colors.
    private static final Map<Character, String> COLORS = new LinkedHashMap<>();

    static {
        COLORS.put('0', "#000000");
        COLORS.put('1', "#0000AA");
        COLORS.put('2', "#00AA00");
        COLORS.put('3', "#00AAAA");
        COLORS.put('4', "#AA0000");
        COLORS.put('5', "#AA00AA");
        COLORS.put('6', "#FFAA00");
        COLORS.put('7', "#AAAAAA");
        COLORS.put('8', "#555555");
        COLORS.put('9', "#5555FF");
        COLORS.put('a', "#55FF55");
        COLORS.put('b', "#55FFFF");
        COLORS.put('c', "#FF5555");
        COLORS.put('d', "#FF55FF");
        COLORS.put('e', "#FFFF55");
        COLORS.put('f', "#FFFFFF");
    }

    private MotdEditorDialog() {
    }

    /// Shows the editor and returns the edited MOTD, or null when cancelled.
    public static @Nullable String show(String initial) {
        Stage stage = new Stage(javafx.stage.StageStyle.DECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(i18n("server.config.motd.editor"));

        JFXTextArea input = new JFXTextArea(initial);
        input.setPrefRowCount(3);
        input.setWrapText(true);

        TextFlow preview = new TextFlow();
        preview.setPadding(new Insets(10));
        preview.setStyle("-fx-background-color: #1b1b1f; -fx-background-radius: 4;");
        preview.setPrefHeight(74);

        Runnable refresh = () -> render(preview, input.getText());
        input.textProperty().addListener((observable, oldValue, newValue) -> refresh.run());
        refresh.run();

        FlowPane palette = new FlowPane(6, 6);
        COLORS.forEach((code, color) -> {
            JFXButton button = new JFXButton("\u00a7" + code);
            button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: "
                    + (code == '0' || code == '1' || code == '4' || code == '8' ? "white" : "black") + ";");
            button.setOnAction(event -> insert(input, "\u00a7" + code, ""));
            palette.getChildren().add(button);
        });

        FlowPane formats = new FlowPane(6, 6);
        addFormat(formats, input, 'l', "B");
        addFormat(formats, input, 'o', "I");
        addFormat(formats, input, 'n', "U");
        addFormat(formats, input, 'm', "S");
        addFormat(formats, input, 'k', "?");
        addFormat(formats, input, 'r', "R");

        JFXButton ok = FXUtils.newRaisedButton(i18n("button.ok"));
        JFXButton cancel = new JFXButton(i18n("button.cancel"));
        cancel.setOnAction(event -> stage.close());
        String[] result = new String[1];
        ok.setOnAction(event -> {
            result[0] = input.getText();
            stage.close();
        });

        Label colorsLabel = new Label(i18n("server.config.motd.colors"));
        Label formatsLabel = new Label(i18n("server.config.motd.formats"));
        Label previewLabel = new Label(i18n("server.config.motd.preview"));

        HBox actions = new HBox(8, ok, cancel);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(10,
                new Label(i18n("server.config.motd.hint")),
                input,
                previewLabel, preview,
                colorsLabel, palette,
                formatsLabel, formats,
                actions);
        root.setPadding(new Insets(16));
        root.setPrefWidth(560);
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");

        Scene scene = new Scene(root);
        StyleSheets.init(scene);
        stage.setScene(scene);
        stage.showAndWait();
        return result[0];
    }

    /// Inserts a code, wrapping the current selection and resetting after it.
    private static void insert(JFXTextArea input, String prefix, String ignored) {
        int start = input.getSelection().getStart();
        int end = input.getSelection().getEnd();
        String selected = input.getSelectedText();
        String replacement = prefix + selected + (selected.isEmpty() ? "" : "\u00a7r");
        input.replaceText(start, end, replacement);
        input.positionCaret(start + replacement.length());
    }

    /// Adds one format toggle button.
    private static void addFormat(FlowPane pane, JFXTextArea input, char code, String label) {
        JFXButton button = new JFXButton(label);
        button.setOnAction(event -> insert(input, "\u00a7" + code, ""));
        pane.getChildren().add(button);
    }

    /// Renders section codes into a styled text flow.
    public static void render(TextFlow flow, String motd) {
        flow.getChildren().clear();
        String text = motd.replace("\\n", "\n");
        Color color = Color.WHITE;
        boolean bold = false, italic = false, underline = false, strike = false;
        StringBuilder run = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\u00a7' && i + 1 < text.length()) {
                flush(flow, run, color, bold, italic, underline, strike);
                char code = Character.toLowerCase(text.charAt(++i));
                if (COLORS.containsKey(code)) {
                    color = Color.web(COLORS.get(code));
                } else {
                    switch (code) {
                        case 'l' -> bold = true;
                        case 'o' -> italic = true;
                        case 'n' -> underline = true;
                        case 'm' -> strike = true;
                        case 'r' -> {
                            color = Color.WHITE;
                            bold = italic = underline = strike = false;
                        }
                        default -> {
                        }
                    }
                }
            } else {
                run.append(ch);
            }
        }
        flush(flow, run, color, bold, italic, underline, strike);
    }

    private static void flush(TextFlow flow, StringBuilder run, Color color,
                              boolean bold, boolean italic, boolean underline, boolean strike) {
        if (run.isEmpty()) {
            return;
        }
        Text text = new Text(run.toString());
        text.setFill(color);
        StringBuilder style = new StringBuilder();
        if (bold) style.append("-fx-font-weight: bold;");
        if (italic) style.append("-fx-font-style: italic;");
        if (underline) style.append("-fx-underline: true;");
        if (strike) style.append("-fx-strikethrough: true;");
        text.setStyle(style.toString());
        flow.getChildren().add(text);
        run.setLength(0);
    }
}
