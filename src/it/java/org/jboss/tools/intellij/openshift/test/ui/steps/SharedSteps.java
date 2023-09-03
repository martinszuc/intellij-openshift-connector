package org.jboss.tools.intellij.openshift.test.ui.steps;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.utils.Keyboard;
import org.jboss.tools.intellij.openshift.test.ui.views.OpenshiftView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

import static com.intellij.remoterobot.search.locators.Locators.byXpath;
import static com.intellij.remoterobot.utils.RepeatUtilsKt.waitFor;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class SharedSteps {
    final private RemoteRobot robot;
    final private Keyboard keyboard;
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path CONFIG_FILE_PATH = Paths.get(USER_HOME, ".kube", "config");
    private static final Path BACKUP_FILE_PATH = Paths.get(USER_HOME, ".kube", "config.bak");
    private static final Logger LOGGER = LoggerFactory.getLogger(SharedSteps.class);

    public SharedSteps(RemoteRobot robot) {
        this.robot = robot;
        this.keyboard = new Keyboard(robot);
    }

    public void openLoginDialog(OpenshiftView view) {
        view.openView();
        rightClickAndSelect(view, 0, "//div[@text='Log in to cluster']");
        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Cluster URL:']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
    }

    public void verifyClusterLogin(String expectedURL) {
        OpenshiftView view = robot.find(OpenshiftView.class);
        view.openView();

        rightClickAndSelect(view, 0, "//div[@text='Refresh']");
        LOGGER.info("Waiting for '" + expectedURL + "' to appear.");

//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }


        view.waitForTreeItem(expectedURL, 60, 1);
        try {
            view.getOpenshiftConnectorTree().expandAllExcept("Devfile registries");
        } catch (Exception e) {
            view.closeView();  //TODO check if works
            view.openView();   //TODO check if works
            try {
                view.getOpenshiftConnectorTree().expandAllExcept("Devfile registries");
            } catch (Exception ex) {
                LOGGER.error("Expanding Openshift tree failed!");
            }
        }

        rightClickAndSelect(view, 0, "//div[@text='Refresh']");
        view.getOpenshiftConnectorTree().rightClickRow(0);
        waitFor(Duration.ofSeconds(60), () -> robot.findAll(ComponentFixture.class, byXpath("//div[@text='Open Console Dashboard']"))
                .stream()
                .anyMatch(ComponentFixture::isShowing));

        view.closeView();
    }

    public void rightClickAndSelect(OpenshiftView view, int row, String xpath) {
        view.getOpenshiftConnectorTree().clickRow(0);
        view.getOpenshiftConnectorTree().rightClickRow(row);

        waitFor(Duration.ofSeconds(20), () -> robot.findAll(ComponentFixture.class, byXpath(xpath))
                .stream()
                .anyMatch(ComponentFixture::isShowing));
        robot.find(ComponentFixture.class, byXpath(xpath))
                .click();
    }

    public void removeKubeConfig() {
        Path configFilePath = Paths.get(USER_HOME, ".kube", "config");
        try {
            LOGGER.info("Attempting to delete kube config file");
            Files.deleteIfExists(configFilePath);
        } catch (IOException e) {
            LOGGER.error("Failed to delete kube config file: {}", e.getMessage());
        }
    }

    public void backupKubeConfig() {
        try {
            LOGGER.info("Attempting to backup kube config file");
            Files.copy(CONFIG_FILE_PATH, BACKUP_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to backup kube config file: {}", e.getMessage());
        }
    }

    public void restoreKubeConfig() {
        try {
            LOGGER.info("Attempting to restore kube config file");
            Files.copy(BACKUP_FILE_PATH, CONFIG_FILE_PATH, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("Failed to restore kube config file: {}", e.getMessage());
        }
    }
}
