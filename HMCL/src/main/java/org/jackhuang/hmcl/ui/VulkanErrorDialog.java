// SPDX-FileCopyrightText: Copyright (C) 2026 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jackhuang.hmcl.game.HMCLGameInstance;
import org.jackhuang.hmcl.setting.StyleSheets;
import org.jetbrains.annotations.NotNullByDefault;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;
import static org.jackhuang.hmcl.util.logging.Logger.LOG;

/// Unified dialog for NeoForge Vulkan early-loading crashes, with one-click mod install.
@NotNullByDefault
public final class VulkanErrorDialog {
    /// Modrinth project id of NeoLoading-Vulkan-Fix.
    private static final String PROJECT_ID = "2XDbg6Zz";

    private final HMCLGameInstance instance;

    /// Creates the dialog for the crashed instance.
    public VulkanErrorDialog(HMCLGameInstance instance) {
        this.instance = instance;
    }

    /// Returns whether the log contains a real Vulkan failure, not just normal backend selection.
    public static boolean matchesError(String text) {
        return text.contains("VK_ERROR") || text.contains("VulkanCommandEncoder")
                || text.contains("VK semaphore") || text.contains("Failed to create Vulkan")
                || text.contains("Device lost") || text.contains("vkCreate") || text.contains("vkQueue")
                || text.contains("Window surface creation") || text.contains("GLFW_NO_API")
                || text.contains("Failed to create window");
    }

    /// Returns whether the log contains a Vulkan crash signature.
    public static boolean matches(String text) {
        return text.contains("Vulkan") || text.contains("VK_ERROR") || text.contains("MoltenVK")
                || text.contains("vkCreate") || text.contains("vkQueue") || text.contains("VulkanCommandEncoder");
    }

    /// Shows the themed error window.
    public void show() {
        Stage stage = new Stage();
        stage.setTitle(i18n("game.vulkan.title"));
        Stage owner = Controllers.getStage();
        if (owner != null) {
            stage.initOwner(owner);
        }

        Label title = new Label(i18n("game.vulkan.title"));
        title.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");

        Label body = new Label(i18n("game.vulkan.message"));
        body.setWrapText(true);

        JFXButton install = FXUtils.newRaisedButton(i18n("game.vulkan.install"));
        install.setOnAction(event -> {
            install.setDisable(true);
            install.setText(i18n("game.vulkan.installing"));
            CompletableFuture.runAsync(() -> installMod())
                    .thenRun(() -> Platform.runLater(() -> {
                        Controllers.showToast(i18n("install.success"));
                        stage.close();
                    }))
                    .exceptionally(throwable -> {
                        LOG.warning("Failed to install NeoLoading-Vulkan-Fix", throwable);
                        Platform.runLater(() -> {
                            install.setDisable(false);
                            install.setText(i18n("game.vulkan.install"));
                            Controllers.dialog(throwable.getMessage(), i18n("install.failed"),
                                    org.jackhuang.hmcl.ui.construct.MessageDialogPane.MessageType.ERROR);
                        });
                        return null;
                    });
        });
        JFXButton close = new JFXButton(i18n("button.close"));
        close.setOnAction(event -> stage.close());

        HBox actions = new HBox(8, install, close);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(12, title, body, actions);
        root.setPadding(new Insets(18));
        root.setPrefWidth(560);
        root.getStyleClass().add("gray-background");
        root.setStyle("-fx-background-color: -monet-surface;");

        Scene scene = new Scene(root);
        StyleSheets.init(scene);
        stage.setScene(scene);
        stage.setOnShown(event -> {
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });
        stage.show();
    }

    /// Downloads the latest NeoLoading-Vulkan-Fix jar into the instance mods folder.
    private void installMod() {
        try {
            String body = fetch("https://api.modrinth.com/v2/project/" + PROJECT_ID + "/version");
            JsonArray versions = JsonParser.parseString(body).getAsJsonArray();
            for (var element : versions) {
                JsonObject version = element.getAsJsonObject();
                JsonArray files = version.getAsJsonArray("files");
                if (files == null || files.isEmpty()) {
                    continue;
                }
                JsonObject file = files.get(0).getAsJsonObject();
                String url = file.get("url").getAsString();
                String filename = file.get("filename").getAsString();

                Path mods = instance.getRunDirectory().resolve("mods");
                Files.createDirectories(mods);
                try (InputStream in = new URL(url).openStream()) {
                    Files.copy(in, mods.resolve(filename), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
                return;
            }
            throw new IllegalStateException(i18n("game.vulkan.not_found"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /// Fetches a URL as UTF-8 text.
    private static String fetch(String url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(8000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty("User-Agent", "SilentCraft-Launcher");
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } finally {
            connection.disconnect();
        }
    }
}
