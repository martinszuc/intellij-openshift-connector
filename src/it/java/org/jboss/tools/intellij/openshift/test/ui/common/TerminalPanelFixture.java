package org.jboss.tools.intellij.openshift.test.ui.common;

import com.intellij.remoterobot.RemoteRobot;
import com.intellij.remoterobot.data.RemoteComponent;
import com.intellij.remoterobot.fixtures.ComponentFixture;
import com.intellij.remoterobot.fixtures.DefaultXpath;
import com.intellij.remoterobot.fixtures.FixtureName;
import org.jetbrains.annotations.NotNull;

import static com.intellij.remoterobot.stepsProcessing.StepWorkerKt.step;

/**
 * Fixture for interacting with the terminal panel in the IDE.
 */
@DefaultXpath(by = "TerminalPanelFixture type", xpath = "//div[@class='JBTerminalPanel']")
@FixtureName(name = "Terminal Panel")
public class TerminalPanelFixture extends ComponentFixture {

    public TerminalPanelFixture(@NotNull RemoteRobot remoteRobot, @NotNull RemoteComponent remoteComponent) {
        super(remoteRobot, remoteComponent);
    }

    /**
     * Retrieves the text from the terminal panel using callJs.
     *
     * @return the text content of the terminal
     */
    public String getTerminalText() {
        return step("Get terminal text", () -> {
            String result = callJs("component.getText();");
            return result != null ? result : "No content found";
        });
    }
}