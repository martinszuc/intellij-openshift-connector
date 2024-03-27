package org.jboss.tools.intellij.openshift.test.ui.tests_cluster;

import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.ClusterLoginDialog;
import org.jboss.tools.intellij.openshift.test.ui.utils.KubeConfigUtility;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.redhat.devtools.intellij.commonuitest.utils.steps.SharedSteps.waitForComponentByXpath;

public abstract class AbstractClusterTest extends AbstractBaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractClusterTest.class);
    protected static String currentClusterUrl = DEFAULT_CLUSTER_URL;
    private static final String CLUSTER_URL = System.getenv("CLUSTER_URL");
    private static final String CLUSTER_USER = System.getenv("CLUSTER_USER");
    private static final String CLUSTER_PASSWORD = System.getenv("CLUSTER_PASSWORD");


    protected void logOut() {
        KubeConfigUtility.removeKubeConfig();
        currentClusterUrl = DEFAULT_CLUSTER_URL;
    }

    protected void loginWithUsername() {
        OpenshiftView view = robot.find(OpenshiftView.class);

        LOGGER.info("Opening cluster login dialog");
        ClusterLoginDialog clusterLoginDialog = ClusterLoginDialog.open(robot);

        clusterLoginDialog.insertURL(CLUSTER_URL);
        clusterLoginDialog.insertUsername(CLUSTER_USER);
        clusterLoginDialog.insertPassword(CLUSTER_PASSWORD);
        clusterLoginDialog.button("OK").click();

        currentClusterUrl = CLUSTER_URL;
        checkUrlFormat();
        view.closeView();
    }

    private void checkUrlFormat() {
        if (!currentClusterUrl.endsWith("/")) {
            currentClusterUrl += "/";
        }
    }

    protected void verifyClusterLogin(String expectedURL) {
        LOGGER.info("Verifying login");
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        view.menuRightClickAndSelect(robot, 0, "Refresh");
        LOGGER.info("Waiting for '" + expectedURL + "' to appear.");

        view.waitForTreeItem(expectedURL, 120, 1);
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
        waitForComponentByXpath(robot, 60, 1, byXpath("//div[@text='Open Console Dashboard']"));
        LOGGER.info("Login successfully verified");

        view.closeView();
    }
}