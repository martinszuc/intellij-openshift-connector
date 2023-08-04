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
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenshiftNodeLoggedOutUITests extends AbstractBaseTest {

    private static final String DEFAULT_CLUSTER_URL = "https://kubernetes.default.svc/";
    private static final String CLUSTER_URL_TO_LOGIN = "https://api.ocp2.adapters-crs.ccitredhat.com:6443/";
    private static final String CLUSTER_USERNAME = "developer";
    private static final String CLUSTER_PASSWD = "developer";
    // When defining your CLUSTER_URL_TO_LOGIN and CLUSTER_PASTE_LOGIN remember the '/' at the end as it is required to function properly
    private static final String CLUSTER_PASTE_LOGIN = "oc login --token=sha256~DpgF-1l5jBeVC2WMGsQZsAPDNCcR_yNu1F_FQJdE4tw --server=https://api.ocp2.adapters-crs.ccitredhat.com:6443/";
    private static final String CLUSTER_TOKEN = "sha256~DpgF-1l5jBeVC2WMGsQZsAPDNCcR_yNu1F_FQJdE4tw";
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String MY_TEST_TREEITEM = "my-test";
    private static String currentClusterUrl = DEFAULT_CLUSTER_URL;
    private static final IntelliJVersion INTELLI_J_VERSION = IntelliJVersion.ULTIMATE_V_2021_2;
    private static final Integer INTELLI_J_PORT = 8580;

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
        view.waitForTreeItem(currentClusterUrl, 10, 1);
        view.waitForTreeItem("Devfile registries", 10, 1);
        view.closeView();
    }

    @Test
    public void defaultNodeAndUserLoggedOutTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        logOut();
        view.openView();

        // Wait for the "https://kubernetes.default.svc/" TreeItem to be available
        waitFor(Duration.ofSeconds(10), () -> !view.getOpenshiftConnectorTree().findAllText(DEFAULT_CLUSTER_URL).isEmpty());

        if (view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").isEmpty()) {
            // If the "Please log in to the cluster" item is not present, double-click on the TreeItem to expand it
            view.getOpenshiftConnectorTree().findText(DEFAULT_CLUSTER_URL).doubleClick();
        }
        // Verify that the "Please log in to the cluster" item is present
        assertTrue(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").size() > 0);

        view.closeView();
    }

    @Test
    public void clusterLoginDialogTest() {
        // Open the Openshift view
        OpenshiftView view = robot.find(OpenshiftView.class);

        openClusterLoginDialog(view);

        // Wait for the login window to appear
        waitFor(Duration.ofSeconds(10), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Verify that the login window is showing
        assertTrue(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        closeDialogWindows();

        // Verify that the login window is not present
        assertFalse(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        view.closeView();
    }

    @Test
    public void pasteLoginCommandTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the fields by default node
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);

        // Set the contents of the clipboard
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection(CLUSTER_PASTE_LOGIN);
        clipboard.setContents(selection, null);

        assertTrue(tokenField.getText().isEmpty());

        // Locate the "Paste Login Command" button and click on it
        JButtonFixture pasteLoginCommandButton = robot.find(JButtonFixture.class, byXpath("//div[@text='Paste Login Command']"));
        pasteLoginCommandButton.click();

        assertFalse(urlField.getText().isEmpty());
        currentClusterUrl = urlField.getText();
        assertFalse(tokenField.getText().isEmpty());

        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();

        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }


    @Test
    public void usernameLoginToClusterTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the Cluster URL JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL_TO_LOGIN);

        // Locate the username JTextField
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        usernameField.click();
        usernameField.setText(CLUSTER_USERNAME);

        // Locate all JPasswordField objects (token and password field)
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText(CLUSTER_PASSWD);

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();
        currentClusterUrl = CLUSTER_URL_TO_LOGIN;

        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }
    @Test
    public void tokenLoginToClusterTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the Cluster URL JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL_TO_LOGIN);


        // Locate all JPasswordField objects (token and password field)
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText(CLUSTER_TOKEN);

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();
        currentClusterUrl = CLUSTER_URL_TO_LOGIN;

        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }

    private void checkIfLoggedIn(){
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(currentClusterUrl, 10, 1);

        if (view.getOpenshiftConnectorTree().findAllText(MY_TEST_TREEITEM).isEmpty()) {
            view.getOpenshiftConnectorTree().findText(currentClusterUrl).doubleClick();
        }
        view.waitForTreeItem(MY_TEST_TREEITEM, 10, 1);

        assertFalse(view.getOpenshiftConnectorTree().findAllText(MY_TEST_TREEITEM).isEmpty());

        view.closeView();
        logOut();
    }

    private static void closeDialogWindows() {
        Keyboard keyboard = new Keyboard(robot);
        keyboard.escape();
        keyboard.escape();

    }

    private void openClusterLoginDialog(OpenshiftView view) {
        view.openView();

        // Wait for the cluster URL TreeItem to be available
        waitFor(Duration.ofSeconds(10), () -> !view.getOpenshiftConnectorTree().findAllText(currentClusterUrl).isEmpty());

        // Access the context menu by right-clicking the TreeItem
        view.getOpenshiftConnectorTree().findText(currentClusterUrl).rightClick();

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

    private void logOut() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        removeKubeConfig();
        currentClusterUrl = DEFAULT_CLUSTER_URL;

        if (view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").isEmpty()) {
            // If the "Please log in to the cluster" item is not present, double-click on the TreeItem to expand it
            view.getOpenshiftConnectorTree().findText(currentClusterUrl).doubleClick();
        }

        view.waitForTreeItem(currentClusterUrl, 10, 1);
        view.waitForTreeItem("Please log in to the cluster", 10, 1);
        assertTrue(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").size() > 0);


        view.closeView();
    }

    private void removeKubeConfig() {
        Path configFilePath = Paths.get(USER_HOME, ".kube", "config");
        try {
            Files.deleteIfExists(configFilePath);
        } catch (IOException e) {
            // Handle exception
        }
    }
}
