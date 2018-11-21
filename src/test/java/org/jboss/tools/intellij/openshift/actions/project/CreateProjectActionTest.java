package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationNode;
import org.jboss.tools.intellij.openshift.tree.application.ApplicationsRootNode;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.PersistentVolumeClaimNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.openshift.actions.ActionTest;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateProjectActionTest extends ActionTest {
  @Override
  public AnAction getAction() {
    return new CreateProjectAction();
  }

  public void testThatActionIsEnabledOnLoggedInCluster() {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    when(applicationsRootNode.isLogged()).thenReturn(true);
    AnActionEvent event = createEvent(applicationsRootNode);
    AnAction action = getAction();
    action.update(event);
    assertTrue(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnLoggedOutCluster() {
    ApplicationsRootNode applicationsRootNode = mock(ApplicationsRootNode.class);
    when(applicationsRootNode.isLogged()).thenReturn(false);
    AnActionEvent event = createEvent(applicationsRootNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnProject() {
    ProjectNode projectNode = mock(ProjectNode.class);
    AnActionEvent event = createEvent(projectNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnApplication() {
    ApplicationNode applicationNode = mock(ApplicationNode.class);
    AnActionEvent event = createEvent(applicationNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnComponent() {
    ComponentNode componentNode = mock(ComponentNode.class);
    AnActionEvent event = createEvent(componentNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

  public void testThatActionIsDisabledOnStorage() {
    PersistentVolumeClaimNode storageNode = mock(PersistentVolumeClaimNode.class);
    AnActionEvent event = createEvent(storageNode);
    AnAction action = getAction();
    action.update(event);
    assertFalse(event.getPresentation().isVisible());
  }

}
