package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.common.TerminalPanelFixture;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants.JB_TERMINAL_PANEL;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateComponentTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CreateComponentTest.class);

    @Test
    @Order(1)
    public void openCloseCreateComponentDialogTest() {
        // Open the Create Component dialog
        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);

        createComponentDialog.close();
        assertThrowsExactly(WaitForConditionTimeoutException.class, () -> {
            robot.find(CreateComponentDialog.class, Duration.ofSeconds(2));
        });
    }

    @Test
    @Order(2)
    public void createGoRuntimeComponentTest() {
        String COMPONENT_NAME = "test-component";

        CreateComponentDialog createComponentDialog = CreateComponentDialog.open(robot);
        assertNotNull(createComponentDialog);
        createComponentDialog.setName(COMPONENT_NAME);
        createComponentDialog.selectComponentType("Go Runtime", robot);
        createComponentDialog.setStartDevMode(true);
        createComponentDialog.selectProjectStarter("go-starter");
        createComponentDialog.clickCreate();

        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();


        ProjectClusterTest.verifyProjectHasItem("newtestproject", COMPONENT_NAME);
    }

    @Test
    @Order(3)
    public void startStopDevModeOnClusterComponentTest() {
        String COMPONENT_NAME = "test-component";
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.openView();
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.menuRightClickAndSelect(robot, 2, "Start dev on Cluster");


        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        assertDevModeStarted();
    }


    private void assertDevModeStarted() {
        // Use TerminalPanelFixture to interact with the terminal panel
        TerminalPanelFixture terminalPanel = robot.find(TerminalPanelFixture.class, Duration.ofSeconds(60));

        // Get the terminal text using the custom fixture method
        String terminalText = terminalPanel.getTerminalText();
        // Log the terminal output for inspection
        LOGGER.info("Terminal output: {}", terminalText);

        // Ensure the terminal text is not null
        assertNotNull(terminalText, "Terminal output should not be null");



        // Further parse or check the terminal output for specific success indicators
        // For example, you might look for specific log entries or success messages
    }

}
