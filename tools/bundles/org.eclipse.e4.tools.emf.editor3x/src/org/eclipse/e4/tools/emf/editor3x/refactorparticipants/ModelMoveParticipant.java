/*******************************************************************************
 * Copyright (c) 2013 Remain Software, Industrial-TSI and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Wim Jongman <wim.jongman@remainsoftware.com> - Bug 395174: e4xmi should participate in package renaming
 * Bug 432892: Eclipse 4 Application does not work after renaming the project name
 ******************************************************************************/
package org.eclipse.e4.tools.emf.editor3x.refactorparticipants;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.e4.tools.emf.editor3x.Messages;
import org.eclipse.e4.tools.emf.editor3x.RefactorModel;
import org.eclipse.e4.tools.emf.editor3x.extension.Util;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;

public class ModelMoveParticipant extends MoveParticipant {
	private IType fType;
	private IPackageFragment fPckage;
	private IFile fFile;
	private RefactorModel fModel;

	@Override
	protected boolean initialize(Object pElement) {

		fModel = RefactorModel.getModel(this);

		if (pElement instanceof IType) {
			fType = (IType) pElement;
			return true;
		}

		if (pElement instanceof IPackageFragment) {
			fPckage = (IPackageFragment) pElement;
			return true;
		}

		if (pElement instanceof IFile) {
			fFile = (IFile) pElement;
			return true;
		}

		return false;
	}

	@Override
	public String getName() {
		return "Workbench Model Contribution Participant"; //$NON-NLS-1$
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pMonitor,
		CheckConditionsContext pContext) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pMonitor) throws CoreException,
	OperationCanceledException {

		pMonitor.beginTask(Messages.ModelMoveParticipant_CreatingChange, IProgressMonitor.UNKNOWN);

		Change change = null;

		if (fType != null) {
			change = createClassChange(pMonitor, fType);
		}

		else if (fPckage != null) {
			change = createPackageChange(pMonitor, fPckage);
		}

		else if (fFile != null) {
			change = createFileChange(pMonitor, fFile);
		}

		pMonitor.done();

		return change;
	}

	private Change createFileChange(IProgressMonitor pMonitor, IFile file)
		throws CoreException {

		String newUrl = "platform:/plugin/"; //$NON-NLS-1$
		if (getArguments().getDestination() instanceof IFolder) {
			final IFolder folder = (IFolder) getArguments().getDestination();
			newUrl += folder.getProject().getName() + "/" //$NON-NLS-1$
				+ folder.getProjectRelativePath().toString() + "/" //$NON-NLS-1$
				+ file.getName();
		} else {
			final IProject project = (IProject) getArguments().getDestination();
			newUrl += project.getName() + "/" + file.getName(); //$NON-NLS-1$

		}

		final String oldUrl = "platform:/plugin" + file.getFullPath(); //$NON-NLS-1$

		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pMonitor, fModel);
	}

	private Change createPackageChange(IProgressMonitor pMonitor,
		IPackageFragment pPckage) throws CoreException,
		OperationCanceledException {
		final String fromBundle = Util.getBundleSymbolicName(pPckage.getJavaProject()
			.getProject());

		final IPackageFragmentRoot fragmentRoot = (IPackageFragmentRoot) getArguments()
			.getDestination();
		final String toBundle = Util.getBundleSymbolicName(fragmentRoot
			.getJavaProject().getProject());

		final String newUrl = "bundleclass://" + toBundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ pPckage.getElementName();

		final String oldUrl = "bundleclass://" + fromBundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
			+ pPckage.getElementName();

		fModel.addTextRename(oldUrl, newUrl);

		return RefactorParticipantDelegate.createChange(pMonitor, fModel);
	}

	private Change createClassChange(IProgressMonitor pMonitor, IType pType)
		throws CoreException, OperationCanceledException {
		final String fromBundle = Util.getBundleSymbolicName(fType.getJavaProject()
			.getProject());
		final String fromClassname = pType.getFullyQualifiedName();

		final IPackageFragment fragment = (IPackageFragment) getArguments()
			.getDestination();
		final String toBundle = Util.getBundleSymbolicName(fragment.getJavaProject()
			.getProject());
		final String toClassName = fragment.getElementName().length() == 0 ? pType
			.getElementName() : fragment.getElementName() + "." //$NON-NLS-1$
			+ pType.getElementName();

		return RefactorParticipantDelegate.createChange(
				pMonitor,
				fModel.addTextRename("bundleclass://" + fromBundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
					+ fromClassname, "bundleclass://" + toBundle + "/" //$NON-NLS-1$ //$NON-NLS-2$
					+ toClassName));
	}

}
