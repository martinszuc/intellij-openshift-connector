/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors: Martin Szuc
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.jboss.tools.intellij.openshift.test.ui.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.JB_TERMINAL_PANEL;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.SELECT_ALL;

/**
 * Fixture for interacting with the terminal panel in the IDE.
 */
@DefaultXpath(by = "TerminalPanelFixture type", xpath = XPathConstants.JB_TERMINAL_PANEL)
@FixtureName(name = "Terminal Panel")
public class TerminalPanelFixture extends ComponentFixture {

    public TerminalPanelFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    /**
     * Performs a right-click action on a the terminal panel
     * and selects an option from the context menu based on the provided XPath locator.
     *
     * @param robot an instance of the RemoteRobot used for interacting with the UI
     * @param xpath the XPath locator for the component to select from the context menu
     */
    public void rightClickSelect(RemoteRobot robot, String xpathString) {
        Point linkPosition = new Point(20, 20);
        TerminalPanelFixture terminalPanel = robot.find(TerminalPanelFixture.class, byXpath(JB_TERMINAL_PANEL));
        terminalPanel.rightClick(linkPosition);
        waitForComponentByXpath(robot, 20, 1, byXpath(xpathString));
        robot.find(ComponentFixture.class, byXpath(xpathString))
                .click();
    }
}