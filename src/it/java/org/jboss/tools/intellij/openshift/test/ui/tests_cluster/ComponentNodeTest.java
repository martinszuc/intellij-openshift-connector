package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import com.intellij.remoterobot.fixtures.dataExtractor.RemoteText;
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import com.redhat.devtools.intellij.commonuitest.fixtures.mainidewindow.idestatusbar.IdeStatusBar;
import org.jboss.tools.intellij.openshift.test.ui.common.TerminalPanelFixture;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.jboss.tools.intellij.openshift.test.ui.tests_public.AboutPublicTest;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        createComponentDialog.selectComponentType("Node.js Runtime", robot);
        createComponentDialog.setStartDevMode(true);
        createComponentDialog.selectProjectStarter("nodejs-starter");
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

    private void assertDevModeStarted() {
        assertTextInOpenshiftTree("debug, dev", Duration.ofSeconds(240), Duration.ofSeconds(10));

        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));
        openshiftView.expandOpenshiftExceptDevfile();
        openshiftView.waitForTreeItem("runtime (3000)", 240, 10);
    }

    private void assertDevModeStopped() {
        assertTextInOpenshiftTree("locally created", Duration.ofSeconds(240), Duration.ofSeconds(10));
    }

    private void assertTextInOpenshiftTree(String text, Duration timeout, Duration interval) {
        OpenshiftView openshiftView = robot.find(OpenshiftView.class, Duration.ofSeconds(2));

        waitFor(
                timeout,
                interval,
                "Waiting for text: " + text,
                "Expected text not found: " + text,
                () -> {
                    List<String> renderedText = openshiftView.findAllText().stream()
                            .map(RemoteText::getText)
                            .toList();
                    return renderedText.contains(text);
                }
        );
    }
}
