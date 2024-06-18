/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.test.ui;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowPane;
import com.redhat.devtools.intellij.commonuitest.utils.project.CreateCloseUtils;
import com.redhat.devtools.intellij.commonuitest.utils.screenshot.ScreenshotUtils;
import org.jboss.tools.intellij.openshift.test.ui.annotations.UITest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ProjectStructureDialog;
import org.jboss.tools.intellij.openshift.test.ui.junit.TestRunnerExtension;
import org.jboss.tools.intellij.openshift.test.ui.runner.IdeaRunner;
import org.jboss.tools.intellij.openshift.test.ui.utils.CleanUpUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.ProjectUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.LabelConstants;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Duration;
import java.util.Optional;

/**
 * @author Ondrej Dockal, odockal@redhat.com
 */
@ExtendWith({TestRunnerExtension.class, AbstractBaseTest.ScreenshotTestWatcher.class})
@UITest
public abstract class AbstractBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBaseTest.class);

    protected static RemoteRobot robot;
    private static boolean isProjectCreatedAndOpened = false;
    public static final String DEFAULT_CLUSTER_URL = "no (current) context/cluster set";
    protected static String currentClusterUrl = DEFAULT_CLUSTER_URL;

    @BeforeAll
    public static void setUpProject() {

        robot = IdeaRunner.getInstance().getRemoteRobot();

        if (!isProjectCreatedAndOpened) {
            FlatWelcomeFrame flatWelcomeFrame = robot.find(FlatWelcomeFrame.class, Duration.ofSeconds(10));
            flatWelcomeFrame.disableNotifications();
            flatWelcomeFrame.preventTipDialogFromOpening();

            CreateCloseUtils.createNewProject(robot, "test-project", CreateCloseUtils.NewProjectType.PLAIN_JAVA);
            ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
            ProjectUtility.closeGotItPopup(robot);

            IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
            ideStatusBar.waitUntilAllBgTasksFinish();

            isProjectCreatedAndOpened = true;
        }
    }

    @AfterEach
    public void afterEachCleanUp() {
        CleanUpUtility.cleanUpAll(robot);
    }

    protected static void logOut() {
        LOGGER.info("Starting logout process...");

        // Remove KubeConfig
        LOGGER.info("Removing KubeConfig...");
        KubeConfigUtility.removeKubeConfig();
        LOGGER.info("KubeConfig removed.");

        // Sleep to ensure the removal is processed
        LOGGER.info("Sleeping for 10 seconds to ensure KubeConfig removal is processed...");
        sleep(10000);

        currentClusterUrl = DEFAULT_CLUSTER_URL;

        OpenshiftView view = robot.find(OpenshiftView.class);
        LOGGER.info("Found OpenshiftView component.");

        // Open Openshift view
        view.openView();
        LOGGER.info("Opened Openshift view.");

        // Wait for the default cluster URL to appear
        LOGGER.info("Waiting for DEFAULT_CLUSTER_URL to appear...");
        view.waitForTreeItem(DEFAULT_CLUSTER_URL, 300, 5); // Wait for "loading..." to finish
        LOGGER.info("DEFAULT_CLUSTER_URL appeared.");

        // Refresh the Openshift view tree
        LOGGER.info("Refreshing Openshift view tree...");
        view.refreshTree(robot);
        LOGGER.info("Openshift view tree refreshed.");

        // Wait until all background tasks finish
        LOGGER.info("Waiting for all background tasks to finish...");
        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class);
        ideStatusBar.waitUntilAllBgTasksFinish();
        LOGGER.info("All background tasks finished.");

        // Close Openshift view
        view.closeView();
        LOGGER.info("Closed Openshift view.");

        LOGGER.info("Logout process completed.");
    }


    public RemoteRobot getRobotReference() {
        return robot;
    }

    public boolean isStripeButtonAvailable(String label) {
        try {
            ToolWindowPane toolWindowPane = robot.find(ToolWindowPane.class);
            toolWindowPane.stripeButton(label, false);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }

    protected static void sleep(long ms) {
        System.out.println("Putting thread into sleep for: " + ms + " ms");
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static class ScreenshotTestWatcher implements TestWatcher {
        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            File screenshot = ScreenshotUtils.takeScreenshot(robot);
            if (screenshot != null) {
                LOGGER.info("Screenshot saved: " + screenshot.getAbsolutePath());
            } else {
                LOGGER.error("Failed to take screenshot");
            }
        }

        @Override
        public void testSuccessful(ExtensionContext context) {
            // No-op
        }

        @Override
        public void testAborted(ExtensionContext context, Throwable cause) {
            // No-op
        }

        @Override
        public void testDisabled(ExtensionContext context, Optional<String> reason) {
            // No-op
        }
    }
}
