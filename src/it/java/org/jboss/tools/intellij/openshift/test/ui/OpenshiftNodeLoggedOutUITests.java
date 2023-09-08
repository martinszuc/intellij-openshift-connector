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
import com.intellij.remoterobot.search.locators.Locator;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ClusterLoginDialog;
import org.jboss.tools.intellij.openshift.test.ui.steps.SharedSteps;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OpenshiftNodeLoggedOutUITests extends AbstractBaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenshiftNodeLoggedOutUITests.class);
    private static final SharedSteps sharedSteps = new SharedSteps();
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

    @AfterAll
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
            robot.find(ComponentFixture.class, byXpath("//div[@class='BaseLabel']"));
            view.closeView();
        } catch (Exception e) {
            LOGGER.info("After test cleanup: Openshift view is not opened");
        }
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
        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        assertTrue(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));   // TODO these asserts may be redundant

        LOGGER.info("Closing cluster login dialog");
        clusterLoginDialog.close(robot);

        assertFalse(robot.findAll(ComponentFixture.class, byXpath("//div[@class='MyDialog']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
    }

    @Test
    public void tokenLoginTest() {
        logOut();
        loginWithToken();
        verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void pasteLoginTest() {
        logOut();
        loginWithPasteCommand();
        verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void usernameLoginTest() {
        logOut();
        loginWithUsername();
        verifyClusterLogin(currentClusterUrl);
        logOut();
    }

    @Test
    public void aboutLoggedOutTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);
        logOut();
        view.openView();

        view.menuRightClickAndSelect(robot, 0, "About");
        sharedSteps.waitForComponentByXpath(robot, 20, 1, byXpath("//div[@class='JBTerminalPanel']"));

        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        try {
            aboutTerminalRightClickSelect(terminalPanel, byXpath("//div[contains(@text.key, 'action.$SelectAll.text')]"));
            aboutTerminalRightClickSelect(terminalPanel, byXpath("//div[contains(@text.key, 'action.$Copy.text')]"));
        } catch (Exception e) {
            LOGGER.error("An error occurred while selecting and copying text from the terminal: {}", e.getMessage());
            fail("Test failed due to an error while selecting and copying text from the terminal");
        }

        verifyClipboardContent("odo version", "unable to fetch the cluster server version");

        robot.find(ComponentFixture.class, byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                .click();
        view.closeView();
    }

    @Test
    public void aboutLoggedInTest() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        loginWithUsername();
        verifyClusterLogin(currentClusterUrl);
        view.openView();

        view.menuRightClickAndSelect(robot, 0, "About");
        sharedSteps.waitForComponentByXpath(robot, 20, 1, byXpath("//div[@class='JBTerminalPanel']"));

        ComponentFixture terminalPanel = robot.find(ComponentFixture.class, byXpath("//div[@class='JBTerminalPanel']"));

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        // Copy contents of terminal inside Run panel to clipboard.
        try {
            aboutTerminalRightClickSelect(terminalPanel, byXpath("//div[contains(@text.key, 'action.$SelectAll.text')]"));
            aboutTerminalRightClickSelect(terminalPanel, byXpath("//div[contains(@text.key, 'action.$Copy.text')]"));
        } catch (Exception e) {
            LOGGER.error("An error occurred while selecting and copying text from the terminal: {}", e.getMessage());
            fail("Test failed due to an error while selecting and copying text from the terminal");
        }

        verifyClipboardContent("odo version", "Server:");

        robot.find(ComponentFixture.class, byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                .click();
        logOut();
        view.closeView();
    }

    private static void verifyClipboardContent(String... expectedContents) {
        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            try {
                String clipboardContents = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                for (String expectedContent : expectedContents) {
                    assert clipboardContents.contains(expectedContent);
                }
            } catch (UnsupportedFlavorException | IOException e) {
                LOGGER.error("Test failed: Copied text is not string!");
            } catch (AssertionError e) {
                LOGGER.error("Test failed: Contents were not able to verify!");
                fail("Test failed due to failed assertions");
            }
        }
    }


    private void loginWithPasteCommand() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        // Set the contents of the clipboard to the oc login command
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringSelection selection = new StringSelection("oc login --token=" + CLUSTER_TOKEN + " --server=" + CLUSTER_URL);
        clipboard.setContents(selection, null);

        clusterLoginDialog.button("Paste Login Command").click();
        clusterLoginDialog.button("OK").click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();

        view.closeView();
    }

    private void loginWithUsername() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        clusterLoginDialog.insertURL(robot, CLUSTER_URL);
        clusterLoginDialog.insertUsername(robot, CLUSTER_USER);
        clusterLoginDialog.insertPassword(robot, CLUSTER_PASSWORD);
        clusterLoginDialog.button("OK").click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();
        view.closeView();
    }

    private void loginWithToken() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        clusterLoginDialog.insertURL(robot, CLUSTER_URL);
        clusterLoginDialog.insertToken(robot, CLUSTER_TOKEN);
        clusterLoginDialog.button("OK").click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();
        view.closeView();
    }

    private void verifyClusterLogin(String expectedURL) {
        LOGGER.info("Verifying login");
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.menuRightClickAndSelect(robot, 0, "Refresh");
        LOGGER.info("Waiting for '" + expectedURL + "' to appear.");

        view.waitForTreeItem(expectedURL, 60, 1);
        try {
            view.getOpenshiftConnectorTree().expandAllExcept("Devfile registries");
        } catch (Exception e) {
            view.closeView();
            view.openView();
            try {
                view.getOpenshiftConnectorTree().expandAllExcept("Devfile registries");
            } catch (Exception ex) {
                LOGGER.error("Expanding Openshift tree failed!");
            }
        }

        view.menuRightClickAndSelect(robot, 0, "Refresh");
        view.getOpenshiftConnectorTree().rightClickRow(0);
        sharedSteps.waitForComponentByXpath(robot, 60, 1, byXpath("//div[@text='Open Console Dashboard']"));
        LOGGER.info("Login successfully verified");

        view.closeView();
    }

    private void aboutTerminalRightClickSelect(ComponentFixture terminalPanel, Locator xpath) {
        Point linkPosition = new Point(20, 20);
        terminalPanel.rightClick(linkPosition);
        sharedSteps.waitForComponentByXpath(robot, 20, 1, xpath);
        robot.find(ComponentFixture.class, xpath)
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
