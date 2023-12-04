/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 424730
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.swt.widgets.Shell;

public class ToolItemIconDialogEditor extends AbstractIconDialog {

	public ToolItemIconDialogEditor(Shell parentShell, IEclipseContext context, IProject project, EditingDomain editingDomain, MToolItem element, Messages Messages) {
		super(parentShell, context, project, editingDomain, element, UiPackageImpl.Literals.UI_LABEL__ICON_URI, Messages);
	}

	@Override
	protected String getShellTitle() {
		return Messages.ToolItemIconDialogEditor_ShellTitle;
	}

	@Override
	protected String getDialogTitle() {
		return Messages.ToolItemIconDialogEditor_DialogTitle;
	}

	@Override
	protected String getDialogMessage() {
		return Messages.ToolItemIconDialogEditor_DialogMessage;
	}

}
