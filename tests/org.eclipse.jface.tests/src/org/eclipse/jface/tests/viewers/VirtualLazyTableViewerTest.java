/*******************************************************************************
 * Copyright (c) 2005, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - bug 170381
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

/**
 * The VirtualLazyTableViewerTest is a test of table viewers with lazy
 * population.
 */
public class VirtualLazyTableViewerTest extends VirtualTableViewerTest {

	private List<Integer> updatedElements;
	// by default, no failure is triggered when updateElement is called
	int updatedElementFailureTriggerIndex = -1;

	@Override
	protected TestModelContentProvider getContentProvider() {
		return new TestLazyModelContentProvider(this);
	}

	@Override
	public void setUp() {
		updatedElements = new ArrayList<>();
		super.setUp();
		processEvents();
	}

	@Override
	protected void setUpModel() {
		fRootElement = TestElement.createModel(2, 100);
		fModel = fRootElement.getModel();
	}

	@After
	@Override
	public void tearDown() {
		super.tearDown();
		updatedElements = null;
	}

	// this method is called from TestLazyModelContentProvider
	public void updateElementCalled(int index) {
		updatedElements.add(Integer.valueOf(index));
		if (updatedElementFailureTriggerIndex != -1 && updatedElements.size() >= updatedElementFailureTriggerIndex) {
			fail("unexpected call to updateElement, this is the " + updatedElements.size() + "th call");
		}
	}

	/**
	 * Test selecting all elements.
	 */
	@Test
	public void testSetIndexedSelection() {
		TestElement[] children = fRootElement.getChildren();
		int selectionSize = children.length / 2;
		int[] indices = new int[selectionSize];
		for (int i = 0; i < indices.length; i++) {
			indices[i] = i * 2;
		}

		Table table = ((TableViewer) fViewer).getTable();
		table.setSelection(indices);

		// we are virtual, so not all indices we requested to select will be selected.
		indices = table.getSelectionIndices();
		selectionSize = indices.length;
		assertTrue("Expected at least one selected element", selectionSize > 0);

		table.showSelection();

		IStructuredSelection result = fViewer.getStructuredSelection();
		assertEquals(selectionSize, result.size());
		assertEquals("First elements do not match ", result.getFirstElement(), children[indices[0]]);
		int lastIndex = indices[indices.length - 1];
		assertEquals("Last elements do not match ", result.toArray()[result.size() - 1], children[lastIndex]);

	}

	@Test
	public void testSetInputDoesNotMaterializeEverything() {
		fViewer.setInput(null);
		updatedElements.clear();
		// Assume something is wrong if all TableItems are materialized:
		updatedElementFailureTriggerIndex = fRootElement.getChildCount();
		fViewer.setInput(fRootElement);

		int materializedSize = updatedElements.size();
		assertTrue("Expected less than " + fRootElement.getChildCount() + ", actual " + materializedSize,
				materializedSize < fRootElement.getChildCount());
		// create a new model and check if we get an equal number of calls to
		// updateElement
		setUpModel();
		updatedElements.clear();
		fViewer.setInput(fRootElement);
		assertEquals(materializedSize, updatedElements.size());
	}

	@Test
	public void testBug160153() {
		int childCount = fRootElement.getChildCount();
		TestElement lastChild = fRootElement.getChildAt(childCount - 1);
		// materialize last child
		fViewer.setSelection(new StructuredSelection(lastChild));
		processEvents();
		assertNotNull("last Child should be in the map", fViewer.testFindItem(lastChild));
		((TableViewer) fViewer).setItemCount(childCount - 1);
		assertNull("last Child should no longer be in the map", fViewer.testFindItem(lastChild));
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testSorter() {
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testRenameWithSorter() {
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testSetFilters() {
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testFilter() {
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testRenameWithFilter() {
	}

	@Ignore("This test is no use here as it is based on the assumption that all items are created.")
	@Override
	public void testContains() {
	}
}
