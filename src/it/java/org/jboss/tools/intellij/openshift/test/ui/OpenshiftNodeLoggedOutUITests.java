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
import org.intellij.lang.annotations.Language;
import org.jboss.tools.intellij.openshift.test.ui.steps.SharedSteps;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenshiftNodeLoggedOutUITests extends AbstractBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftNodeLoggedOutUITests.class);
    private static final SharedSteps sharedSteps = new SharedSteps(robot);
    private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
    private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
    private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");
    private static final String CLUSTER_TOKEN = System.getenv("CLUSTER_TOKEN");
    private static final String DEFAULT_CLUSTER_URL = "https://kubernetes.default.svc/";
    private static String currentClusterUrl = DEFAULT_CLUSTER_URL;

    @BeforeAll
    public static void setUp() {
        sharedSteps.backupKubeConfig();
        sharedSteps.removeKubeConfig();
    }

    @AfterClass
    public static void tearDown() {
        sharedSteps.restoreKubeConfig();
    }

    @AfterEach
    public void afterEachCleanUp() {
        try {
            LOGGER.info("After test cleanup: Checking for any opened dialog window");
            ContainerFixture dialogWindow = robot.find(ContainerFixture.class, byXpath("//div[@class='MyDialog']"));
            dialogWindow.find(ComponentFixture.class, byXpath("//div[@class='JButton']"))
                    .click();
            currentClusterUrl = DEFAULT_CLUSTER_URL;
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No dialog window opened");
        }

        try {
            LOGGER.info("After test cleanup: Checking for any opened run window");
            robot.find(ComponentFixture.class, byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                    .click();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: No run window opened");
        }

        try {
            LOGGER.info("After test cleanup: Checking for opened Openshift view");
            OpenshiftView view = robot.find(OpenshiftView.class);
            robot.find(ComponentFixture.class, byXpath("//div[@class='BaseLabel']"), Duration.ofSeconds(2));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: Openshift view is not opened");
        }
    }

    @Test
    public void defaultNodeTest() {
        logOut();
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        waitFor(Duration.ofSeconds(20), () -> !view.getOpenshiftConnectorTree().findAllText(DEFAULT_CLUSTER_URL).isEmpty());
        view.getOpenshiftConnectorTree().expand(DEFAULT_CLUSTER_URL);
        view.waitForTreeItem("Please log in to the cluster", 60, 1);

        assertFalse(view.getOpenshiftConnectorTree().findAllText("Please log in to the cluster").isEmpty());

        view.closeView();
    }

    @Test
    public void clusterLoginDialogTest() {
        // Open the Openshift view
        OpenshiftView view = robot.find(OpenshiftView.class);
        sharedSteps.openLoginDialog(view);

        sharedSteps.waitForComponentToAppear(20, 1, byXpath("//div[@class='MyDialog']"));
        assertTrue(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        // Close the dialog window
        ContainerFixture dialogWindow = robot.find(ContainerFixture.class, byXpath("//div[@class='MyDialog']"));
        dialogWindow.find(ComponentFixture.class, byXpath("//div[@text.key='button.cancel']"))
                .click();
        assertFalse(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        view.closeView();
    }

    @Test
    public void tokenLoginTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        logOut();
        loginWithToken(view);
        sharedSteps.verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void pasteLoginTest() {
        logOut();
        loginWithPasteCommand();
        sharedSteps.verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void usernameLoginTest() {
        logOut();
        loginWithUsername();
        sharedSteps.verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void aboutLoggedOutTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        logOut();
        view.openView();

        sharedSteps.menuRightClickAndSelect(view, 0, byXpath("//div[@text='About']"));
        sharedSteps.waitForComponentToAppear(20, 1, byXpath("//div[@class='JBTerminalPanel']"));

        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"));

//        aboutTerminalRightClickSelect(terminalPanel, "//div[@text.key='action.$SelectAll.text']");
//        aboutTerminalRightClickSelect(terminalPanel, "//div[@text.key='action.$Copy.text']");
        aboutTerminalRightClickSelect(terminalPanel, "//div[contains(@text.key, 'action.$SelectAll.text')]");
        aboutTerminalRightClickSelect(terminalPanel, "//div[contains(@text.key, 'action.$Copy.text')]");

        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                assert clipboardContents.contains("odo version");
                assert clipboardContents.contains("unable to fetch the cluster server version");
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("aboutLoggedOutTest failed: Copied text is not string!");
            }
        }

        robot.find(ComponentFixture.class,
                        byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                .click();
        view.closeView();
    }

    @Test
    public void aboutLoggedInTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        loginWithToken(view);
        sharedSteps.verifyClusterLogin(currentClusterUrl);
        view.openView();

        sharedSteps.menuRightClickAndSelect(view, 0, byXpath("//div[@text='About']"));
        sharedSteps.waitForComponentToAppear(20, 1, byXpath("//div[@class='JBTerminalPanel']"));

        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
        try {
            aboutTerminalRightClickSelect(terminalPanel, "//div[contains(@text.key, 'action.$SelectAll.text')]");
            aboutTerminalRightClickSelect(terminalPanel, "//div[contains(@text.key, 'action.$Copy.text')]");
        } catch (Exception e) {
            LOGGER.error("An error occurred while selecting and copying text from the terminal: {}", e.getMessage());
            fail("Test failed due to an error while selecting and copying text from the terminal");
        }

        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                assert clipboardContents.contains("odo version");
                assert clipboardContents.contains("Server:");
                assert clipboardContents.contains("Kubernetes:");
                assert clipboardContents.contains("Podman Client:");
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("aboutLoggedInTest failed: Copied text is not string!");
            }
        }

        robot.find(ComponentFixture.class, byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                .click();

        logOut();
        view.closeView();
    }

    private void loginWithPasteCommand() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        sharedSteps.openLoginDialog(view);

        // Locate the fields
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"), Duration.ofSeconds(5));
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);

        // Set the contents of the clipboard to the oc login command
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("oc login --token=" + CLUSTER_TOKEN + " --server=" + CLUSTER_URL);
        clipboard.setContents(selection, null);

        // Locate the "Paste Login Command" button and click on it
        assertTrue(tokenField.getText().isEmpty());
        robot.find(JButtonFixture.class, byXpath("//div[@text='Paste Login Command']"))
                .click();

        assertFalse(urlField.getText().isEmpty());
        currentClusterUrl = urlField.getText();
        checkUrlFormat();

        assertFalse(tokenField.getText().isEmpty());

        // Locate the OK button and click on it
        robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"))
                .click();

        view.closeView();
    }

    private void loginWithUsername() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        sharedSteps.openLoginDialog(view);

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
        robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"))
                .click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();

        view.closeView();
    }

    private void loginWithToken(OpenshiftView view) {
        sharedSteps.openLoginDialog(view);
        // Locate the Cluster URL JTextField
        JTextFieldFixture urlField = robot.find(JTextFieldFixture.class, byXpath("//div[@visible_text='" + currentClusterUrl + "']"));
        urlField.click();
        urlField.setText(CLUSTER_URL);

        // Locate all JPasswordField objects (token and password field)
        List<JTextFieldFixture> passwordFields = robot.findAll(JTextFieldFixture.class, byXpath("//div[@class='JPasswordField']"));
        JTextFieldFixture tokenField = passwordFields.get(0);
        tokenField.click();
        tokenField.setText(CLUSTER_TOKEN);

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();

        // Locate the OK button and click on it
        robot.find(JButtonFixture.class, byXpath("//div[@visible_text='OK']"))
                .click();

        view.closeView();
    }

    private void aboutTerminalRightClickSelect(ComponentFixture terminalPanel, @Language("XPath") String xpath) {
        Point linkPosition = new Point(20, 20);
        terminalPanel.rightClick(linkPosition);
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath(xpath))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        robot.find(ComponentFixture.class, byXpath(xpath))
                .click();
    }

    private void logOut() {
        sharedSteps.removeKubeConfig();
        currentClusterUrl = DEFAULT_CLUSTER_URL;
    }

    private void checkUrlFormat() {
        if (!currentClusterUrl.endsWith("/")) {
            currentClusterUrl = currentClusterUrl + "/";
        }
    }
}
