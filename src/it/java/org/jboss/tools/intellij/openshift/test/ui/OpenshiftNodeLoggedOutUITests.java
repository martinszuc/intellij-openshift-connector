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
import com.intellij.remoterobot.utils.Keyboard;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenshiftNodeLoggedOutUITests extends AbstractBaseTest {
    
    private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
    private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
    private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");
    private static final String CLUSTER_TOKEN = System.getenv("CLUSTER_TOKEN");
    private static final String DEFAULT_CLUSTER_URL = "https://kubernetes.default.svc/";
    private static final String DEFAULT_NAMESPACE = "my-test";
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
        assertTrue(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").size() > 0);

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
        Keyboard keyboard = new Keyboard(robot);
        keyboard.escape();
        keyboard.escape();

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
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //TODO maybe add a case for login failed window that would appear here

        view.closeView();
        //restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }


    @Test
    public void usernameLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        view.closeView();
        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }

    @Test
    public void tokenLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

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

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        view.closeView();
        restart(INTELLI_J_VERSION, INTELLI_J_PORT);
        checkIfLoggedIn();
    }

    //@Test
    public void devSandboxTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        openClusterLoginDialog(view);

        ContainerFixture loginDialog = robot.find(ContainerFixture.class, byXpath("//div[@class='MyDialog']"));

        // Find the JEditorPane component containing the desired text
        ComponentFixture jEditorPane = loginDialog.find(ComponentFixture.class, byXpath("//div[@class='JEditorPane']"));

        // Specify the position of the hyperlink within the JEditorPane component
        int x = 213; // x coordinate of the hyperlink
        int y = 41; // y coordinate of the hyperlink

        Point linkPosition = new Point(x, y);

        // Simulate a click on the hyperlink
        jEditorPane.click(linkPosition);

    }


    private void checkIfLoggedIn() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.waitForTreeItem(currentClusterUrl, 20, 1);
        view.getOpenshiftConnectorTree().expand(currentClusterUrl);
        view.waitForTreeItem(DEFAULT_NAMESPACE, 20, 1);
        assertFalse(view.getOpenshiftConnectorTree().findAllText(DEFAULT_NAMESPACE).isEmpty());

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
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        removeKubeConfig();
        currentClusterUrl = DEFAULT_CLUSTER_URL;
        view.waitForTreeItem(currentClusterUrl, 20, 1);
        view.getOpenshiftConnectorTree().expand(currentClusterUrl);
        view.waitForTreeItem("Please log in to the cluster", 20, 1);
        assertTrue(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").size() > 0);

        view.closeView();
    }

    private static void removeKubeConfig() {
        Path configFilePath = Paths.get(USER_HOME, ".kube", "config");
        try {
            Files.deleteIfExists(configFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to delete kube config file: " + e.getMessage());
        }
    }

    private static void backupKubeConfig() {
        //LOGGER.info("Backing up kube config file");
        try {
            Files.copy(CONFIG_FILE_PATH, BACKUP_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
            //LOGGER.info("Kube config file backed up successfully");S
        } catch (IOException e) {
            //LOGGER.severe("Failed to backup kube config file: " + e.getMessage());
        }
    }

    private static void restoreKubeConfig() {
        try {
            Files.copy(BACKUP_FILE_PATH, CONFIG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to restore kube config file: " + e.getMessage());
        }
    }

    private void checkUrlFormat() {
        if (!currentClusterUrl.endsWith("/")) {
            currentClusterUrl = currentClusterUrl + "/";
        }
    }
}
