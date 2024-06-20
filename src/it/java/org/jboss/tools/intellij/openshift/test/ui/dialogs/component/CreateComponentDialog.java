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
package org.jboss.tools.intellij.openshift.test.ui.dialogs.component;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.*;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

/**
 * Create Component dialog fixture
 */
@DefaultXpath(by = "MyDialog type", xpath = XPathConstants.CREATE_COMPONENT_DIALOG)
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
        JTextFieldFixture nameField = find(JTextFieldFixture.class, byXpath("//div[@class='JTextField' and @visible_text='untitled1']"));
        nameField.click();
        nameField.setText(name);
    }

    public void selectModule() {
        find(ComponentFixture.class, byXpath("//div[@text='Select module']")).click();
    }

    public void selectFolder() {
        find(ComponentFixture.class, byXpath("//div[@text='Select folder']")).click();
    }

    public void selectComponentType(String type) {
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
