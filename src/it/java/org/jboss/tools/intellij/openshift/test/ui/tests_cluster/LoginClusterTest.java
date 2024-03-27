package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.ContainerFixture;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.jboss.tools.intellij.openshift.test.ui.views.GettingStartedView;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;

public class LoginClusterTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginClusterTest.class);
    private static String currentClusterUrl = DEFAULT_CLUSTER_URL;

    @BeforeAll
    public static void setUp() {
        KubeConfigUtility.backupKubeConfig();
        KubeConfigUtility.removeKubeConfig();

        try {
            GettingStartedView gettingStartedView = robot.find(GettingStartedView.class, byXpath("//div[@accessiblename='Getting Started' and @class='BaseLabel' and @text='Getting Started']"));
            gettingStartedView.closeView();
            LOGGER.info("Before test setup: Getting started view closed.");

        } catch (Exception ignored) {
            LOGGER.info("Before test setup: Getting Started view not found, ignored.");
        }
    }

    @AfterAll
    public static void tearDown() {
        KubeConfigUtility.restoreKubeConfig();
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
    public void usernameLoginTest() {
        LOGGER.info("usernameLoginTest: Start");
        logOut();
        loginWithUsername();
        verifyClusterLogin(currentClusterUrl);
        logOut();
        LOGGER.info("usernameLoginTest: End");
    }
}
