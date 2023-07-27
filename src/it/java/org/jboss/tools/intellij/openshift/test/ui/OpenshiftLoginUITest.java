/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.JButtonFixture;
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import com.intellij.remoterobot.search.locators.Locator;
import com.intellij.remoterobot.utils.Keyboard;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenshiftLoginUITest extends AbstractBaseTest {

    @Test
    public void openshiftExtensionTest() {
        waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'OpenShift' stripe button is not available.", () -> isStripeButtonAvailable("OpenShift"));
        waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Kubernetes' stripe button is not available.", () -> isStripeButtonAvailable("Kubernetes"));
        waitFor(Duration.ofSeconds(10), Duration.ofSeconds(1), "The 'Getting Started' stripe button is not available.", () -> isStripeButtonAvailable("Getting Started"));
    }

    @Test
    public void openshiftViewTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem("https://kubernetes.default.svc/", 10, 1);
        view.waitForTreeItem("Devfile registries", 10, 1);
        view.closeView();
    }

    @Test
    public void openshiftDefaultNodeAndUserLoggedOutTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        // Wait for the "https://kubernetes.default.svc/" TreeItem to be available
        waitFor(Duration.ofSeconds(10), () -> !view.getOpenshiftConnectorTree().findAllText("https://kubernetes.default.svc/").isEmpty());

        if (view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").isEmpty()) {
            // If the "Please log in to the cluster" item is not present, double-click on the TreeItem to expand it
            view.getOpenshiftConnectorTree().findText("https://kubernetes.default.svc/").doubleClick();
        }
        // Verify that the "Please log in to the cluster" item is present
        assertTrue(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").size() > 0);

        view.closeView();
    }


    @Test
    public void openshiftLoginDialogShowingTest() {
        // Open the Openshift view
        OpenshiftView view = robot.find(OpenshiftView.class);

        openClusterLoginDialog(view);

        // Wait for the login window to appear
        waitFor(Duration.ofSeconds(10) , () -> robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Verify that the login window is showing
        assertTrue(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        closeClusterLoginDialog();

        // Verify that the login window is not present
        assertFalse(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Close the Openshift view
        view.closeView();
    }


    @Test
    public void usernameLoginToClusterTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the Cluster URL JTextField by default node
        JTextFieldFixture textField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='https://kubernetes.default.svc/']"));
        textField.click();
        textField.setText("https://api.ocp2.adapters-crs.ccitredhat.com:6443");

        // Locate the username JTextField
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        usernameField.click();
        usernameField.setText("developer");

        // Locate all JPasswordField objects
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText("developer");

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();

        closeClusterLoginDialog();

        //TODO: check if logged in

//        // Wait for the specified element to appear
//        String xpath = "//div[@visible_text='https://api.ocp2.adapters-crs.ccitredhat.com:6443']";
//        Duration timeout = Duration.ofSeconds(15);
//        waitFor(timeout, () -> !robot.findAll(ComponentFixture.class, byXpath(xpath)).isEmpty());

        view.closeView();
    }
    @Test
    public void tokenLoginToClusterTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate all JPasswordField objects
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        // Access the token password field and fill it out
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText("sample-text");

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();

        //TODO: check if logged in

        closeClusterLoginDialog();
        view.closeView();
    }


    private static void closeClusterLoginDialog() {
        Keyboard keyboard = new Keyboard(robot);
        keyboard.escape();
        keyboard.escape();

    }

    private void openClusterLoginDialog(OpenshiftView view){
        view.openView();

        // Wait for the "https://kubernetes.default.svc/" TreeItem to be available
        waitFor(Duration.ofSeconds(10), () -> !view.getOpenshiftConnectorTree().findAllText("https://kubernetes.default.svc/").isEmpty());

        // Access the context menu by right-clicking the TreeItem
        view.getOpenshiftConnectorTree().findText("https://kubernetes.default.svc/").rightClick();

        // Wait for the context menu to appear
        waitFor(Duration.ofSeconds(10), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Log in to cluster']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Select "Log in to cluster" from the context menu
        Locator locator = byXpath("//div[@text='Log in to cluster']");
        ComponentFixture menuItem = robot.find(ComponentFixture.class, locator);
        menuItem.click();

        // Wait for the login window to appear
        waitFor(Duration.ofSeconds(10), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Cluster URL:']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
    }
}
