// SPDX-FileCopyrightText: Copyright (C) 2021 The HMCL Authors
// SPDX-License-Identifier: GPL-3.0-or-later WITH AdditionRef-HMCL
package org.jackhuang.hmcl.ui.main;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import org.jackhuang.hmcl.theme.Themes;
import org.jackhuang.hmcl.ui.FXUtils;
import org.jackhuang.hmcl.ui.ScrollUtils;
import org.jackhuang.hmcl.ui.WeakListenerHolder;
import org.jackhuang.hmcl.ui.construct.ComponentList;
import org.jackhuang.hmcl.ui.construct.LineButton;
import org.jackhuang.hmcl.ui.construct.SpinnerPane;

import static org.jackhuang.hmcl.util.i18n.I18n.i18n;

import org.jackhuang.hmcl.Metadata;

public class FeedbackPage extends SpinnerPane {

    private final WeakListenerHolder holder = new WeakListenerHolder();

    public FeedbackPage() {
        VBox content = new VBox();
        content.getStyleClass().add("spinner-pane-content");
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        ScrollUtils.installSmoothScrolling(scrollPane);
        setContent(scrollPane);

        ComponentList groups = new ComponentList();
        {
            var users = LineButton.createExternalLinkButton(Metadata.GROUPS_URL);
            users.setLargeTitle(true);
            javafx.scene.image.ImageView hmclIcon = new javafx.scene.image.ImageView(
                    FXUtils.newBuiltinImage("/assets/img/hmcl.png"));
            hmclIcon.setFitWidth(36);
            hmclIcon.setFitHeight(36);
            hmclIcon.setPreserveRatio(true);
            users.setLeading(hmclIcon);
            users.setTitle(i18n("contact.chat.qq_group"));
            users.setSubtitle(i18n("contact.chat.qq_group.statement"));

            var discord = LineButton.createExternalLinkButton("https://discord.gg/jVvC7HfM6U");
            discord.setLargeTitle(true);
            discord.setLeading(FXUtils.newBuiltinImage("/assets/img/discord.png"));
            discord.setTitle(i18n("contact.chat.discord"));
            discord.setSubtitle(i18n("contact.chat.discord.statement"));

            groups.getContent().setAll(users, discord);
        }

        ComponentList feedback = new ComponentList();
        {
            var ownGitHub = LineButton.createExternalLinkButton(Metadata.REPOSITORY_URL + "/issues/new/choose");
            ownGitHub.setLargeTitle(true);
            ownGitHub.setTitle(i18n("contact.feedback.silentcraft.github"));
            ownGitHub.setSubtitle(i18n("contact.feedback.silentcraft.github.statement"));

            var hmclGitHub = LineButton.createExternalLinkButton("https://github.com/HMCL-dev/HMCL/issues/new/choose");
            hmclGitHub.setLargeTitle(true);
            hmclGitHub.setTitle(i18n("contact.feedback.github"));
            hmclGitHub.setSubtitle(i18n("contact.feedback.github.statement"));

            holder.add(FXUtils.onWeakChangeAndOperate(Themes.darkModeProperty(), darkMode -> {
                var icon = darkMode
                        ? FXUtils.newBuiltinImage("/assets/img/github-white.png")
                        : FXUtils.newBuiltinImage("/assets/img/github.png");
                ownGitHub.setLeading(icon);
                hmclGitHub.setLeading(icon);
            }));

            feedback.getContent().setAll(ownGitHub, hmclGitHub);
        }

        content.getChildren().addAll(
                ComponentList.createComponentListTitle(i18n("contact.chat")),
                groups,
                ComponentList.createComponentListTitle(i18n("contact.feedback")),
                feedback
        );
    }
}
