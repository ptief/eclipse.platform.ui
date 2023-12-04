/*******************************************************************************
 * Copyright (c) 2014, 2017 TwelveTone LLC and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation, Bug 424730
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs.BundleImageCache;
import org.eclipse.e4.tools.emf.ui.internal.common.component.tabs.empty.TitleAreaFilterDialog;
import org.eclipse.e4.tools.emf.ui.internal.common.resourcelocator.Messages;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class ReferencedProjectPickerDialog extends TitleAreaFilterDialog {

	private IProject project;
	private BundleImageCache imageCache;
	static protected Image imgProject;

	protected ReferencedProjectPickerDialog(final Shell parentShell, IProject project) {
		super(parentShell, new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IProject project = (IProject) element;
				return project.getName();
			}

			@Override
			public Image getImage(Object element) {
				return imgProject;
			}
		});
		this.project = project;
	}

	@Override
	protected Control createContents(Composite parent) {
		imageCache = new BundleImageCache(parent.getDisplay(), getClass().getClassLoader());
		getShell().addDisposeListener(e -> {
			imageCache.dispose();
			imgProject = null;
		});
		imgProject = imageCache.create("/icons/full/obj16/projects.png"); //$NON-NLS-1$

		Control ret = super.createContents(parent);
		try {
			setElements(project.getReferencedProjects());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		String message = Messages.ReferencedProjectPickerDialog_selectReferencedProject;
		setMessage(message);
		getShell().setText(message);
		setTitle(message);
		return ret;
	}

}
