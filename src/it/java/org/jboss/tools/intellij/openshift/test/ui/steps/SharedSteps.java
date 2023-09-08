package org.jboss.tools.intellij.openshift.test.ui.steps;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.search.locators.Locator;
import org.jetbrains.annotations.NotNull;
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

public class SharedSteps {
//    final private RemoteRobot robot;
    private static final String USER_HOME = System.getProperty("user.home");
    private static final Path CONFIG_FILE_PATH = getKubeConfigPath();
    private static final Path BACKUP_FILE_PATH = Paths.get(USER_HOME, ".kube", "config.bak");
    private static final Logger LOGGER = LoggerFactory.getLogger(SharedSteps.class);

    public void waitForComponentByXpath(RemoteRobot robot, int duration, int interval, Locator xpath) {
        waitFor(Duration.ofSeconds(duration), Duration.ofSeconds(interval), () -> robot.findAll(ComponentFixture.class, xpath)
                .stream()
                .anyMatch(ComponentFixture::isShowing));
    }

    public void removeKubeConfig() {
        try {
            LOGGER.info("Attempting to delete kube config file");
            Files.deleteIfExists(CONFIG_FILE_PATH);
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
    @NotNull
    public static Path getKubeConfigPath() {
        Path configFilePath;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            configFilePath = Paths.get(System.getenv("USERPROFILE"), ".kube", "config");
        } else if (os.contains("mac")) {
            configFilePath = Paths.get(System.getProperty("user.home"), ".kube", "config");
        } else {
            configFilePath = Paths.get(USER_HOME, ".kube", "config");
        }
        return configFilePath;
    }

}
