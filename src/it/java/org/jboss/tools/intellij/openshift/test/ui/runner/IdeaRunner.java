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
package org.jboss.tools.intellij.openshift.test.ui.runner;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.stepsProcessing.StepLogger;
import com.intellij.remoterobot.stepsProcessing.StepWorker;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.UITestRunner;
import com.redhat.devtools.intellij.commonuitest.exceptions.UITestException;
import com.redhat.devtools.intellij.commonuitest.fixtures.dialogs.FlatWelcomeFrame;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.MainIdeWindow;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;

/**
 * Idea Runner singleton to keep track of running IDE
 * @author Ondrej Dockal
 *
 */
public class IdeaRunner {

	private static IdeaRunner ideaRunner = null;
	private static boolean ideaIsStarted = false;


	private RemoteRobot robot;

	private IdeaRunner() {}

	public static IdeaRunner getInstance() {
		if (ideaRunner == null) {
			ideaRunner = new IdeaRunner();
		}
		return ideaRunner;
	}

	public void startIDE(IntelliJVersion ideaVersion, int portNumber) {
		if (!ideaIsStarted) {
			System.out.println("Starting IDE, setting ideaIsStarted to true");
			robot = UITestRunner.runIde(ideaVersion, portNumber);
			System.out.println("IDEA port for remote robot: " + portNumber);
			ideaIsStarted = true;
		}
	}

	public RemoteRobot restartIDE(IntelliJVersion ideaVersion, int portNumber) {
		return UITestRunner.restartIde(ideaVersion,portNumber);
	}

	public RemoteRobot getRemoteRobot() {
		return robot;
	}
}

