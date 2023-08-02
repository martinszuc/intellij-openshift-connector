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
import com.redhat.devtools.intellij.commonuitest.UITestRunner;
import com.redhat.devtools.intellij.commonuitest.utils.runner.IntelliJVersion;

/**
 * Idea Runner singleton to keep track of running IDE
 * @author Ondrej Dockal
 *
 */
public class IdeaRunner {

	private static IdeaRunner ideaRunner = null;
	private static boolean ideaIsStarted = false;
	private static int port = 8580;

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
			robot = UITestRunner.runIde(ideaVersion, port);
			port = portNumber; //TODO ask about order
			System.out.println("IDEA port for remote robot: " + port);
			ideaIsStarted = true;
		}
	}

	public void stopIDE() {
		if (ideaIsStarted) {
			System.out.println("Stopping IDE, setting ideaIsStarted to false");
			UITestRunner.closeIde();
			ideaIsStarted = false;
		}
	}

	public void restartIDE(IntelliJVersion ideaVersion, int portNumber) {
		stopIDE();
		//here we need to wait
		startIDE(ideaVersion, portNumber);
	}

	public boolean getIdeaIsStarted() {
		//TODO maybe check if its started for real
		return ideaIsStarted;
	}

	public RemoteRobot getRemoteRobot() {
		return robot;
	}



}
