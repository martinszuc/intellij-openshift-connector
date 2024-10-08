/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jboss.tools.intellij.openshift.telemetry.TelemetrySender;
import org.jboss.tools.intellij.openshift.utils.odo.OdoFacade;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.tree.TreePath;

import static org.jboss.tools.intellij.openshift.telemetry.TelemetryService.PREFIX_ACTION;

public abstract class OdoAction extends TelemetryAction {

  private static final Logger LOGGER = LoggerFactory.getLogger(OdoAction.class);

  protected OdoAction(Class... filters) {
    super(filters);
  }

  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected) {
    setTelemetrySender(new TelemetrySender(PREFIX_ACTION + getTelemetryActionName()));
    OdoFacade odo = getOdo(anActionEvent);
    if (odo == null) {
      return;
    }
    this.actionPerformedOnSelectedObject(anActionEvent, getElement(selected), odo);
  }

  protected OdoFacade getOdo(AnActionEvent anActionEvent) {
    try {
      return ActionUtils.getApplicationRootNode(anActionEvent).getOdo().getNow(null);
    } catch (Exception e) {
      LOGGER.warn("Could not get odo: {}", e.getMessage(), e);
      return null;
    }
  }

  public abstract void actionPerformedOnSelectedObject(AnActionEvent anActionEvent, Object selected, @NotNull OdoFacade odo);
}
