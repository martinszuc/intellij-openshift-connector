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
import com.thaiopensource.xml.dtd.om.Def;
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

public class OpenshiftLoginUITest extends AbstractBaseTest {

    private static final String DEFAULT_CLUSTER_URL = "https://kubernetes.default.svc/";
    private static final String CLUSTER_URL_TO_LOGIN = "https://api.ocp2.adapters-crs.ccitredhat.com:6443/";
    private static final String USER_HOME = System.getProperty("user.home");
    private static final String USERNAME = "developer";
    private static final String PASSWD = "developer";
    private static String clusterUrl = DEFAULT_CLUSTER_URL;


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
        view.waitForTreeItem(clusterUrl, 10, 1);
        view.waitForTreeItem("Devfile registries", 10, 1);
        view.closeView();
    }

    @Test
    public void openshiftDefaultNodeAndUserLoggedOutTest() {
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
    public void openshiftLoginDialogShowingTest() {
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

        // Close the Openshift view
        view.closeView();
    }

    //@Test
    public void pasteLoginCommandTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the fields by default node
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + clusterUrl + "']"));
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);

        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        //TODO paste command pastes url and token
        //oc login --token=sha256~i7-K7krCd8Y58zOBIs0Tph8sDa86OQxmPJ0msap6l6A --server=https://api.ocp2.adapters-crs.ccitredhat.com:6443


        // Set the contents of the clipboard
        String text = "oc login --token=sha256~i7-K7krCd8Y58zOBIs0Tph8sDa86OQxmPJ0msap6l6A --server=https://api.ocp2.adapters-crs.ccitredhat.com:6443";
        StringSelection selection = new StringSelection(text);
        clipboard.setContents(selection, null);

        // Assert that the URL, username, and password fields are empty
        assertTrue(usernameField.getText().isEmpty());
        assertTrue(passwordField.getText().isEmpty());

        // Locate the "Paste Login Command" button and click on it
        JButtonFixture pasteLoginCommandButton = robot.find(JButtonFixture.class, byXpath("//div[@text='Paste Login Command']"));
        pasteLoginCommandButton.click();

        // Assert that the URL, username, and password fields contain the text "sample-text"
        assertEquals("https://sample-text:6443", urlField.getText());
        assertEquals("sample-text", usernameField.getText());
        assertEquals("sample-text", passwordField.getText());

        closeDialogWindows();
        view.closeView();
    }


    @Test
    public void usernameLoginToClusterTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Locate the Cluster URL JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + clusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL_TO_LOGIN);

        // Locate the username JTextField
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        usernameField.click();
        usernameField.setText(USERNAME);

        // Locate all JPasswordField objects
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText(PASSWD);

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();
        clusterUrl = CLUSTER_URL_TO_LOGIN;

        closeDialogWindows();

        restart(IntelliJVersion.ULTIMATE_V_2021_2, 8580);

        // Check if logged in
        view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(clusterUrl, 10, 1);

        if (view.getOpenshiftConnectorTree().findAllText("my-test").isEmpty()) {
            view.getOpenshiftConnectorTree().findText(clusterUrl).doubleClick();
        }
        view.waitForTreeItem("my-test", 10, 1);

        assertFalse(view.getOpenshiftConnectorTree().findAllText("my-test").isEmpty());

        view.closeView();
        logOut();
    }

    private void logOut() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        removeKubeConfig();
        clusterUrl=DEFAULT_CLUSTER_URL;

        view.waitForTreeItem(clusterUrl, 10, 1);
        view.closeView();
    }

    //@Test
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

        closeDialogWindows();
        view.closeView();
    }


    private static void closeDialogWindows() {
        Keyboard keyboard = new Keyboard(robot);
        keyboard.escape();
        keyboard.escape();

    }

    private void openClusterLoginDialog(OpenshiftView view) {
        view.openView();

        // Wait for the cluster URL TreeItem to be available
        waitFor(Duration.ofSeconds(10), () -> !view.getOpenshiftConnectorTree().findAllText(clusterUrl).isEmpty());

        // Access the context menu by right-clicking the TreeItem
        view.getOpenshiftConnectorTree().findText(clusterUrl).rightClick();

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
    private void removeKubeConfig(){
        Path configFilePath = Paths.get(USER_HOME, ".kube", "config");
        try {
            Files.deleteIfExists(configFilePath);
        } catch (IOException e) {
            // Handle exception
        }
    }
}
