/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui.views;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.utils.Keyboard;
import com.redhat.devtools.intellij.commonuitest.UITestRunner;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowLeftToolbar;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowRightToolbar;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.*;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants.*;

/**
 * @author Richard Kocian
 */
@DefaultXpath(by = "Getting Started type", xpath = IDE_FRAME_IMPL)
@FixtureName(name = "GettingStarted View")
public class GettingStartedView extends ContainerFixture {

    private static final Logger LOGGER = LoggerFactory.getLogger(GettingStartedView.class);

    public GettingStartedView(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public void openView() {
        if (!isViewOpened()) {
            if(UITestRunner.getIdeaVersion().toInt() > 20242) {
                final ToolWindowRightToolbar toolWindowRightToolbar = find(ToolWindowRightToolbar.class);
                toolWindowRightToolbar.clickStripeButton(GETTING_STARTED);
            }
            final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
            toolWindowPane.button(byXpath(getToolWindowButton(GETTING_STARTED)), Duration.ofSeconds(2)).click();
            LOGGER.info("Getting Started view opened");
        }
    }

    public void closeView() {
        if (isViewOpened()) {
            if(UITestRunner.getIdeaVersion().toInt() > 20242) {
                final ToolWindowRightToolbar toolWindowRightToolbar = find(ToolWindowRightToolbar.class);
                toolWindowRightToolbar.clickStripeButton(GETTING_STARTED);
            }
            final ToolWindowPane toolWindowPane = find(ToolWindowPane.class);
            toolWindowPane.button(byXpath(getToolWindowButton(GETTING_STARTED)), Duration.ofSeconds(2)).click();
            LOGGER.info("Getting Started view closed");
        }
    }

    public void waitForTreeItem(String itemText, int duration, int interval) {
        waitFor(Duration.ofSeconds(duration),
                Duration.ofSeconds(interval),
                "The " + itemText + " project is still not available.",
                () -> getGettingStartedTree().hasText(itemText));
    }

    public JTreeFixture getGettingStartedTree() {
        return find(JTreeFixture.class, byXpath(TREE_CLASS), Duration.ofSeconds(30));
    }

    public ComponentFixture findEditorPaneFixture() {
        return getRemoteRobot().findAll(GettingStartedView.class, byXpath(JEDITOR_PANE)).get(0);
    }

    public ComponentFixture findBackToMainButton() {
        return getRemoteRobot().find(GettingStartedView.class, byXpath(XPathConstants.BACK_BUTTON_GETTING_STARTED));
    }

    public void maximalizeToolWindow(RemoteRobot robot) {
        Keyboard keyboard = new Keyboard(robot);
        // Other OS may have different keyboard shortcut for maximizing
        keyboard.hotKey(KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_QUOTE);
    }

    private boolean isViewOpened() {
        try {
            if (UITestRunner.getIdeaVersionInt() >= 20242) {
                // For IntelliJ IDEA 2024.2 and newer
                find(ComponentFixture.class, byXpath(GETTING_STARTED_BASELABEL), Duration.ofSeconds(5));
            } else {
                // For older versions
                ToolWindowPane toolWindowPane = find(ToolWindowPane.class, Duration.ofSeconds(5));
                toolWindowPane.find(ComponentFixture.class, byXpath(GETTING_STARTED_BASELABEL));
            }
            LOGGER.info("Getting Started view: View is already opened");
            return true;
        } catch (Exception ignored) {
            LOGGER.info("Getting Started view: View is not opened");
            return false;
        }
    }
}
