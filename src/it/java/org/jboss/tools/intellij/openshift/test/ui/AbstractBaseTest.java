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
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.toolwindowspane.ToolWindowsPane;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;
import org.jboss.tools.intellij.openshift.test.ui.annotations.UITest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ProjectStructureDialog;
import org.jboss.tools.intellij.openshift.test.ui.junit.TestRunnerExtension;
import org.jboss.tools.intellij.openshift.test.ui.runner.IdeaRunner;
import org.jboss.tools.intellij.openshift.test.ui.utils.ProjectUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;

import static org.jboss.tools.intellij.openshift.test.ui.runner.IdeaRunner.getInstance;

/**
 * @author Ondrej Dockal, odockal@redhat.com
 */
@ExtendWith(TestRunnerExtension.class)
@UITest
abstract public class AbstractBaseTest {
    protected static RemoteRobot robot;
    private static boolean hasConnectedToTestIDE = false;

    @BeforeAll
    public static void connect() {
        if (!hasConnectedToTestIDE) {
            robot = getInstance().getRemoteRobot();
            ProjectUtility.createEmptyProject(robot, "test-project");
            ProjectUtility.closeTipDialogIfItAppears(robot);
            ProjectStructureDialog.cancelProjectStructureDialogIfItAppears(robot);
            ProjectUtility.closeGotItPopup(robot);
            hasConnectedToTestIDE = true;
        }
        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        ideStatusBar.waitUntilAllBgTasksFinish();
    }

    protected static void restart(IntelliJVersion ideaVersion, int portNumber) {
        robot = IdeaRunner.getInstance().restartIDE(ideaVersion, portNumber);
        hasConnectedToTestIDE = true;

        ProjectUtility.closeTipDialogIfItAppears(robot);

        IdeStatusBar ideStatusBar = robot.find(IdeStatusBar.class, Duration.ofSeconds(5));
        ideStatusBar.waitUntilAllBgTasksFinish();
    }

    public RemoteRobot getRobotReference() {
        return robot;
    }

    public boolean isStripeButtonAvailable(String label) {
        try {
            ToolWindowsPane toolWindowsPane = robot.find(ToolWindowsPane.class);
            toolWindowsPane.stripeButton(label, false);
        } catch (WaitForConditionTimeoutException e) {
            return false;
        }
        return true;
    }
}
