package org.jboss.tools.intellij.openshift.test.ui.tests_public;

import com.intellij.remoterobot.utils.WaitForConditionTimeoutException;
import org.jboss.tools.intellij.openshift.test.ui.AbstractBaseTest;
import org.jboss.tools.intellij.openshift.test.ui.dialogs.component.CreateComponentDialog;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CreateComponentTest extends AbstractBaseTest {

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
}
