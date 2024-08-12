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
package org.jboss.tools.intellij.openshift.test.ui.dialogs.component;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import com.intellij.remoterobot.utils.Keyboard;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;

import java.awt.event.KeyEvent;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create Component dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.MYDIALOG_CLASS)
@FixtureName(name = "Create Component Dialog")
public class CreateComponentDialog extends CommonContainerFixture {

    public CreateComponentDialog(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    public static CreateComponentDialog open(RemoteRobot robot) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.expandOpenshiftExceptDevfile();
        view.menuRightClickAndSelect(robot, 1, LabelConstants.NEW_COMPONENT);
        return robot.find(CreateComponentDialog.class, Duration.ofSeconds(60));
    }

    public void close() {
        findText(LabelConstants.CANCEL).click();
    }

    public void setName(String name) {
        JTextFieldFixture nameField = findAll(JTextFieldFixture.class, byXpath(XPathConstants.JTEXT_FIELD)).get(0);
        nameField.click();
        nameField.setText(name);
    }

    /**
     * Selects a component type from the list within the Create Component dialog.
     *
     * Attempts to select the specified component type.
     * If the component type is not immediately visible, it scrolls down the list
     * by sending the Down arrow key.
     *
     * @param type the name of the component type to select
     * @param remoteRobot an instance of the RemoteRobot
     */
    public void selectComponentType(String type, RemoteRobot remoteRobot) {
        JListFixture jList = find(JListFixture.class, byXpath(XPathConstants.JLIST));
        jList.click();
        Keyboard keyboard = new Keyboard(remoteRobot);
        keyboard.key(KeyEvent.VK_HOME);         // Use RemoteRobot's Keyboard to go to the top

        boolean itemFound = false;
        int attempts = 0;

        while (!itemFound && attempts < 30) { // Limit attempts to prevent infinite loop
            try {
                jList.clickItem(type, false);
                itemFound = true;
            } catch (WaitForConditionTimeoutException e) {
                // Scroll down by pressing the Down key
                keyboard.key(KeyEvent.VK_DOWN);
            }
            attempts++;
        }

        if (!itemFound) {
            throw new RuntimeException("Component type not found: " + type);
        }
    }

    /**
     * Selects a project starter from the combo box.
     * @param starterName the name of the project starter to select
     */
    public void selectProjectStarter(String starterName) {
        ComboBoxFixture comboBox = find(ComboBoxFixture.class, byXpath(XPathConstants.JCOMBOBOX));
        comboBox.click();
        comboBox.selectItem(starterName);
    }

    public void setStartDevMode(boolean start) {
        JCheckboxFixture checkBox = find(JCheckboxFixture.class, byXpath(XPathConstants.JCHECKBOX));
        if (start) {
            if (!checkBox.isSelected()) {
                checkBox.select();
            }
        } else {
            if (checkBox.isSelected()) {
                checkBox.select();
            }
        }
    }

    public void clickPrevious() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_PREVIOUS)).click();
    }

    public void clickNext() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_NEXT)).click();
    }

    public void clickCreate() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CREATE)).click();
    }

    public void clickCancel() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_CANCEL)).click();
    }

    public void clickHelp() {
        find(ComponentFixture.class, byXpath(XPathConstants.BUTTON_HELP)).click();
    }
}
