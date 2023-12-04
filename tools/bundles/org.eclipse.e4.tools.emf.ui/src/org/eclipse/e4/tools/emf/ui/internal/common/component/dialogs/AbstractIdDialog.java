/*******************************************************************************
 * Copyright (c) 2013, 2017 fhv.at and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Nicolaj Hoess <nicohoess@gmail.com> - initial implementation (Bug 396975)
 * Andrej Brummelhuis <andrejbrummelhuis@gmail.com> - Bug 396975, 395283
 * Adrian Alcaide - initial implementation (Bug 396975)
 * Simon Scholz <simon.scholz@vogella.com> - Bug 475365
 *******************************************************************************/
package org.eclipse.e4.tools.emf.ui.internal.common.component.dialogs;

import java.util.List;

import org.eclipse.e4.tools.emf.ui.common.IModelResource;
import org.eclipse.e4.tools.emf.ui.internal.Messages;
import org.eclipse.e4.tools.emf.ui.internal.common.component.ControlFactory;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractIdDialog<ContributionClass, ElementClass extends MApplicationElement> extends
SaveDialogBoundsSettingsDialog {

	protected EModelService modelService;

	protected TableViewer viewer;
	protected EditingDomain domain;
	protected IModelResource resource;
	protected ContributionClass contribution;
	protected Messages messages;

	public AbstractIdDialog(Shell parentShell, IModelResource resource, ContributionClass toolbarContribution,
			EditingDomain domain, EModelService modelService, Messages Messages) {
		super(parentShell);
		this.resource = resource;
		this.modelService = modelService;
		this.messages = Messages;
		this.domain = domain;
		this.contribution = toolbarContribution;
	}

	protected abstract String getShellTitle();

	protected abstract String getDialogTitle();

	protected abstract String getDialogMessage();

	protected abstract String getLabelText();

	protected abstract List<ElementClass> getViewerInput();

	protected abstract EAttribute getFeatureLiteral();

	protected abstract String getListItemInformation(ElementClass listItem);

	protected IBaseLabelProvider getLabelProvider() {
		return new DelegatingStyledCellLabelProvider(new DefaultStyledLabelProvider());
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(getShellTitle());
		setTitle(getDialogTitle());
		setMessage(getDialogMessage());
		final Composite comp = (Composite) super.createDialogArea(parent);
		final Composite container = new Composite(comp, SWT.NONE);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		container.setLayout(new GridLayout(2, false));

		Label l = new Label(container, SWT.NONE);
		l.setText(getLabelText());

		final Text idField = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH);
		idField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final PatternFilter filter = new PatternFilter(true) {
			@Override
			protected boolean isParentMatch(Viewer viewer, Object element) {
				return viewer instanceof AbstractTreeViewer && super.isParentMatch(viewer, element);
			}
		};

		l = new Label(container, SWT.NONE);
		viewer = new TableViewer(container);
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(getLabelProvider());
		viewer.addFilter(filter);
		viewer.addDoubleClickListener(event -> okPressed());

		ControlFactory.attachFiltering(idField, viewer, filter);

		viewer.setInput(getViewerInput());

		return comp;
	}

	@Override
	protected void okPressed() {
		if (!viewer.getSelection().isEmpty()) {
			@SuppressWarnings("unchecked")
			final ElementClass el = (ElementClass) ((IStructuredSelection) viewer.getSelection()).getFirstElement();
			final Command cmd = SetCommand.create(domain, contribution, getFeatureLiteral(), el.getElementId());
			if (cmd.canExecute()) {
				domain.getCommandStack().execute(cmd);
				super.okPressed();
			}
		}
	}

	private class DefaultStyledLabelProvider extends BaseLabelProvider implements IStyledLabelProvider {

		@Override
		public StyledString getStyledText(Object element) {
			@SuppressWarnings("unchecked")
			final ElementClass el = (ElementClass) element;
			final String elementId = el.getElementId() != null && el.getElementId().trim().length() > 0
					? el.getElementId() : "(Id missing)"; //$NON-NLS-1$
					final StyledString str = new StyledString(elementId);

					final String infoString = getListItemInformation(el);
					if (infoString != null && infoString.trim().length() > 0) {
						str.append(" - " + getListItemInformation(el), StyledString.DECORATIONS_STYLER); //$NON-NLS-1$
					}

					return str;
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}
	}
}
