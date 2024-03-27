package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.jayway.jsonpath.internal.path.PathCompiler.fail;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;
import static org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest.aboutTerminalRightClickSelect;
import static org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest.verifyClipboardContent;

public class AboutClusterTest extends AbstractClusterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AboutClusterTest.class);

    @Test
    public void aboutLoggedInTest() {
        LOGGER.info("aboutLoggedInTest: Start");

        OpenshiftView view = robot.find(OpenshiftView.class);

        try {
            loginWithUsername();
            verifyClusterLogin(currentClusterUrl);
        } catch (Exception e) {
            LOGGER.error("AboutLoggedInTest: Failed to login!");
            fail("AboutLoggedInTest: Failed to login!");
        }
        view.openView();

        view.menuRightClickAndSelect(robot, 0, "About");
        waitForComponentByXpath(robot, 20, 1, byXpath("//div[@class='JBTerminalPanel']"));


        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        // Copy contents of terminal inside Run panel to clipboard.
        try {
            aboutTerminalRightClickSelect(robot, byXpath("//div[contains(@text.key, 'action.$SelectAll.text')]"));
            aboutTerminalRightClickSelect(robot, byXpath("//div[contains(@text.key, 'action.$Copy.text')]"));
        } catch (Exception e) {
            LOGGER.error("An error occurred while selecting and copying text from the terminal: {}", e.getMessage());
            fail("Test failed due to an error while selecting and copying text from the terminal");
        }

        verifyClipboardContent("odo version", "Server:");

        // Close the "Run" tool window // TODO ask xpathdefinitions
        robot.find(ComponentFixture.class, byXpath("//div[@class='ToolWindowHeader'][.//div[@class='ContentTabLabel']]//div[@myaction.key='tool.window.hide.action.name']"))
                .click();

        logOut();
        view.closeView();
        LOGGER.info("aboutLoggedInTest: End");

    }
}
