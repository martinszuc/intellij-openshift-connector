package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.common.TerminalPanelFixture;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest;
import org.jboss.tools.intellij.openshift.test.ui.utils.constants.XPathConstants;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComponentNodeTest extends AbstractClusterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentNodeTest.class);

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
    public void startDevModeOnClusterComponentTest() {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.openView();
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.menuRightClickAndSelect(robot, 2, "Start dev on Cluster");


        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        assertDevModeStarted();
    }

    @Test
    @Order(4)
    public void stopDevModeOnClusterComponentTest() {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.openView();
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.menuRightClickAndSelect(robot, 2, "Stop dev on Cluster");
        
        robot.find(IdeStatusBar.class).waitUntilAllBgTasksFinish();
        assertDevModeStopped();
    }

    private void assertDevModeStopped() {
        // Verify Run tool window with terminal is opened
        TerminalPanelFixture terminalPanel = robot.find(TerminalPanelFixture.class, Duration.ofSeconds(30));
        terminalPanel.rightClickSelect(robot, byXpath(XPathConstants.SELECT_ALL));
        terminalPanel.rightClickSelect(robot, byXpath(XPathConstants.COPY));

        AboutPublicTest.verifyClipboardContent("Cleaning resources, please wait");

        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.waitForTreeItem("locally created", 240, 10);
    }


    private void assertDevModeStarted() {
        // Verify Run tool window with terminal is opened
        TerminalPanelFixture terminalPanel = robot.find(TerminalPanelFixture.class, Duration.ofSeconds(30));
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.waitForTreeItem("debug, dev", 240, 10);

        openshiftView.expandOpenshiftExceptDevfile();

        openshiftView.waitForTreeItem("runtime (3000)", 240, 10);
    }




}
