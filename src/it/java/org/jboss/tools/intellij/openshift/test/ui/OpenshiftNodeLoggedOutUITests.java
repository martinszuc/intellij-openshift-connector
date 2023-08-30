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
import com.intellij.remoterobot.fixtures.ContainerFixture;
import com.intellij.remoterobot.fixtures.JButtonFixture;
import com.intellij.remoterobot.fixtures.JTextFieldFixture;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jboss.tools.intellij.openshift.ui.sandbox.SandboxLoginPage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.*;

public class OpenshiftNodeLoggedOutUITests extends AbstractBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftNodeLoggedOutUITests.class);

    private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
    private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
    private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");
    private static final String CLUSTER_TOKEN = System.getenv("CLUSTER_TOKEN");
    private static final String DEFAULT_CLUSTER_URL = "https://kubernetes.default.svc/";
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path CONFIG_FILE_PATH = Paths.get(USER_HOME, ".kube", "config");
    private static final Path BACKUP_FILE_PATH = Paths.get(USER_HOME, ".kube", "config.bak");
    private static String currentClusterUrl = DEFAULT_CLUSTER_URL;
    private static final IntelliJVersion INTELLI_J_VERSION = IntelliJVersion.ULTIMATE_V_2021_2;
    private static final Integer INTELLI_J_PORT = 8580;

    @BeforeAll
    public static void setUp() {
        backupKubeConfig();
        removeKubeConfig();
    }

    @AfterAll
    public static void tearDown() {
        restoreKubeConfig();
    }

    @AfterEach
    public void afterEachCleanUp() {
        // Check if the Openshift view is open and if close
        try {
            OpenshiftView view = robot.find(OpenshiftView.class);
            robot.find(ComponentFixture.class, byXpath("//div[@class='BaseLabel']"), Duration.ofSeconds(2));
            view.closeView();
        } catch (Exception e) {
            // The Openshift view is not open
        }
        // If any of the login tests failed there will be a dialog window telling us so
        try {
            ContainerFixture dialogWindow = robot.find(ContainerFixture.class, byXpath("//div[@class='MyDialog']"));
            dialogWindow.find(ComponentFixture.class, byXpath("//div[@class='JButton']")).click();
            currentClusterUrl = DEFAULT_CLUSTER_URL;
        } catch (Exception e) {
            // No dialog windows found
        }

    }

    @Test
    public void openshiftExtensionTest() {
        waitFor(Duration.ofSeconds(20), Duration.ofSeconds(1), "The 'OpenShift' stripe button is not available.", () -> isStripeButtonAvailable("OpenShift"));
        waitFor(Duration.ofSeconds(20), Duration.ofSeconds(1), "The 'Kubernetes' stripe button is not available.", () -> isStripeButtonAvailable("Kubernetes"));
        waitFor(Duration.ofSeconds(20), Duration.ofSeconds(1), "The 'Getting Started' stripe button is not available.", () -> isStripeButtonAvailable("Getting Started"));
    }

    @Test
    public void openshiftViewTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        view.waitForTreeItem(currentClusterUrl, 20, 1);
        view.waitForTreeItem("Devfile registries", 20, 1);
        view.closeView();
    }

    @Test
    public void defaultNodeTest() {
        logOut();
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        // Wait for the default cluster URL TreeItem to be available
        waitFor(Duration.ofSeconds(20), () -> !view.getOpenshiftConnectorTree().findAllText(DEFAULT_CLUSTER_URL).isEmpty());
        view.getOpenshiftConnectorTree().expand(DEFAULT_CLUSTER_URL);
        view.waitForTreeItem("Please log in to the cluster", 20, 1);

        // Verify that the "Please log in to the cluster" item is present
        assertFalse(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").isEmpty());

        view.closeView();
    }

    @Test
    public void clusterLoginDialogTest() {
        // Open the Openshift view
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        // Wait for the login window to appear and verify
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        assertTrue(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Close the dialog window
        ContainerFixture dialogWindow = robot.find(ContainerFixture.class, byXpath("//div[@class='MyDialog']"));
        dialogWindow.find(ComponentFixture.class, byXpath("//div[@text.key='button.cancel']")).click();

        // Verify that the login window is not present
        assertFalse(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        view.closeView();
    }

    @Test
    public void pasteLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);
        logOut();

        // Locate the fields
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);

        // Set the contents of the clipboard to the oc login command
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("oc login --token=" + CLUSTER_TOKEN + " --server=" + CLUSTER_URL);
        clipboard.setContents(selection, null);

        // Locate the "Paste Login Command" button and click on it
        assertTrue(tokenField.getText().isEmpty());
        JButtonFixture pasteLoginCommandButton = robot.find(JButtonFixture.class, byXpath("//div[@text='Paste Login Command']"));
        pasteLoginCommandButton.click();

        assertFalse(urlField.getText().isEmpty());
        currentClusterUrl = urlField.getText();
        checkUrlFormat();

        assertFalse(tokenField.getText().isEmpty());

        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();

        view.closeView();
        //restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }


    @Test
    public void usernameLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);
        logOut();

        // Locate the Cluster URL, username JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL);
        JTextFieldFixture usernameField = robot.find(JTextFieldFixture.class, byXpath("//div[@text='Username:']/following-sibling::div[@class='JTextField']"));
        usernameField.click();
        usernameField.setText(CLUSTER_USER);

        // Locate all JPasswordField objects (token and password field)
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture passwordField = passwordFields.get(1);
        passwordField.click();
        passwordField.setText(CLUSTER_PASSWORD);

        // OK button
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();

        view.closeView();
//        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }

    @Test
    public void tokenLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);
        logOut();

        // Locate the Cluster URL JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL);

        // Locate all JPasswordField objects (token and password field)
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText(CLUSTER_TOKEN);

        // Locate the OK button and click on it
        JButtonFixture okButton = robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"));
        okButton.click();
        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();
        view.closeView();
//        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }

    //    @Test
    public void restartTest() {
        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
    }

    //    @Test
    public void aboutLoggedOutTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();
        logOut();

        waitFor(Duration.ofSeconds(20), () -> !view.getOpenshiftConnectorTree().findAllText(currentClusterUrl).isEmpty());
        view.getOpenshiftConnectorTree().findText(currentClusterUrl).rightClick();

        // Wait for the "About" option to become visible and click on it
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='About']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        robot.find(ComponentFixture.class, byXpath("//div[@text='About']"))
                .click();

        // Wait for the JBTerminalPanel to become visible
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"));

    }

    private void checkIfLoggedIn() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.getOpenshiftConnectorTree().rightClick();
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Refresh']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        robot.find(ComponentFixture.class, byXpath("//div[@text='Refresh']"))
                .click();

        LOGGER.info("Waiting for '" + currentClusterUrl + "' to appear.");
        try {
            view.waitForTreeItem(currentClusterUrl, 60, 1);
            view.getOpenshiftConnectorTree().expandAllExcept("Devfile registries");
            view.waitForTreeItem("No deployments, click here to create one.", 60, 1);
        } catch (Exception e) {
            LOGGER.error("Waiting for '" + currentClusterUrl + "' has failed, login could not be verified!");
            fail("Login could not be verified!");
        }
        view.closeView();
        logOut();
    }

    private void openClusterLoginDialog(OpenshiftView view) {
        view.openView();

        // Wait for the cluster URL TreeItem and "Log in to cluster"
        waitFor(Duration.ofSeconds(20), () -> !view.getOpenshiftConnectorTree().findAllText(currentClusterUrl).isEmpty());
        view.getOpenshiftConnectorTree().findText(currentClusterUrl).rightClick();
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Log in to cluster']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Select "Log in to cluster" from the context menu
        ComponentFixture menuItem = robot.find(ComponentFixture.class, byXpath("//div[@text='Log in to cluster']"));
        menuItem.click();

        // Wait for the login window to appear
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Cluster URL:']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
    }

    private void logOut() {
        removeKubeConfig();
        currentClusterUrl = DEFAULT_CLUSTER_URL;
    }

    private static void removeKubeConfig() {
        Path configFilePath = Paths.get(USER_HOME, ".kube", "config");
        try {
            LOGGER.info("Attempting to delete kube config file");
            Files.deleteIfExists(configFilePath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete kube config file: {}", e.getMessage());
        }
    }

    private static void backupKubeConfig() {
        try {
            LOGGER.info("Attempting to backup kube config file");
            Files.copy(CONFIG_FILE_PATH, BACKUP_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to backup kube config file: {}", e.getMessage());
        }
    }

    private static void restoreKubeConfig() {
        try {
            LOGGER.info("Attempting to restore kube config file");
            Files.copy(BACKUP_FILE_PATH, CONFIG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to restore kube config file: {}", e.getMessage());
        }
    }

    private void checkUrlFormat() {
        if (!currentClusterUrl.endsWith("/")) {
            currentClusterUrl = currentClusterUrl + "/";
        }
    }
}
