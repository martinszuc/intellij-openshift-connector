/*******************************************************************************
 * Copyright (c) 2019-2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import org.jboss.tools.intellij.openshift.actions.ActionUtils;
import org.jboss.tools.intellij.openshift.tree.application.ComponentNode;
import org.jboss.tools.intellij.openshift.tree.application.NamespaceNode;
import org.jboss.tools.intellij.openshift.utils.odo.Component;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.DebugComponentFeature;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Consumer;

import static org.jboss.tools.intellij.openshift.actions.ActionUtils.runWithProgress;
import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.TelemetryResult;

public abstract class FeatureComponentAction extends ContextAwareComponentAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureComponentAction.class);

    protected final ComponentFeature feature;

    protected DebugComponentFeature debugFeature;

    protected FeatureComponentAction(ComponentFeature feature) {
        this.feature = feature;
    }

    @Override
    public boolean isVisible(Object selected) {
        boolean visible = super.isVisible(selected);
        if (visible && selected instanceof ComponentNode) {
            Component component = ((ComponentNode) selected).getComponent();
            visible = component.getInfo().getSupportedFeatures().contains(feature.getMode());
        }
        return visible;
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        if (e.getPresentation().isVisible() && needCustomizedPresentation()) {
            Object node = adjust(getSelected(getTree(e)));
            if (!(node instanceof ComponentNode)) {
                return;
            }
            ComponentNode componentNode = (ComponentNode) node;
            Component component = componentNode.getComponent();
            try {
                Odo odo = componentNode.getRoot().getOdo().getNow(null);
                if (odo == null) {
                    return;
                }
                e.getPresentation().setText(getCustomizedPresentation(component));
            } catch (Exception ex) {
                LOGGER.warn("Could not update {}", componentNode.getName(), e);
            }
        }
    }

    protected abstract String getCustomizedPresentation(Component component);

    protected boolean needCustomizedPresentation() {
        return false;
    }

    protected String getActionName() {
        return feature.getLabel();
    }

    @Override
    public String getTelemetryActionName() {
        return feature.getLabel() + " component";
    }

    @Override
    public void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull Odo odo) {
        ComponentNode componentNode = (ComponentNode) selected;
        Component component = componentNode.getComponent();
        NamespaceNode namespaceNode = componentNode.getParent();
        runWithProgress(
            (ProgressIndicator progress) -> {
                try {
                    ComponentFeature feat = getComponentFeature();
                    process(odo,
                        namespaceNode.getName(),
                        component,
                        feat,
                        b1 -> {
                            component.getLiveFeatures().addFeature(feature);
                            if (feat.getMode().equals(ComponentFeature.Mode.DEBUG_MODE)) {
                                component.getLiveFeatures().addFeature(debugFeature);
                            }
                            ActionUtils.getApplicationTreeStructure(anActionEvent).fireModified(componentNode);
                        }, b2 -> {
                            component.getLiveFeatures().removeFeature(feature);
                            if (feat.getMode().equals(ComponentFeature.Mode.DEBUG_MODE)) {
                                component.getLiveFeatures().removeFeature(debugFeature);
                            }
                            ActionUtils.getApplicationTreeStructure(anActionEvent).fireModified(componentNode);
                        });
                    sendTelemetryResults(TelemetryResult.SUCCESS);
                } catch (IOException e) {
                    sendTelemetryError(e);
                    UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), getActionName()));
                }
            },
            getActionName() + component.getName() + "...",
            getEventProject(anActionEvent));
    }

    private ComponentFeature getComponentFeature() {
        // for now, always start dev in debug mode. see #571 for use of a preference
        if (debugFeature == null)
            debugFeature = new DebugComponentFeature(feature);
        return debugFeature;
    }

    protected void process(Odo odo, String project, Component component,
                           ComponentFeature feat, Consumer<Boolean> callback, Consumer<Boolean> processTerminatedCallback) throws IOException {
        if (odo.isStarted(project, component.getPath(), component.getName(), feat)) {
            odo.stop(project, component.getPath(), component.getName(), feat);
        } else {
            odo.start(project, component.getPath(), component.getName(), feat, callback, processTerminatedCallback);
        }
    }
}
