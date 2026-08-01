/*******************************************************************************
 *  Copyright (c) 2008, 2026 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sonatype, Inc. - ongoing development
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.dialogs;

import java.util.List;
import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.internal.p2.ui.model.ElementUtils;
import org.eclipse.equinox.internal.p2.ui.viewers.IUColumnConfig;
import org.eclipse.equinox.internal.p2.ui.viewers.IUComparator;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.ui.Policy;
import org.eclipse.equinox.p2.ui.ProvisioningUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

/**
 * A StructuredIUGroup is a reusable UI component that displays a
 * structured view of IU's driven by some queries.
 *
 * @since 3.4
 */
public abstract class StructuredIUGroup {

	private final FontMetrics fm;
	protected StructuredViewer viewer;
	private Composite composite;
	private final ProvisioningUI ui;
	private IUColumnConfig[] columnConfig;

	/**
	 * Create a group that represents the available IU's.
	 *
	 * @param ui The application policy to use in the group
	 * @param parent the parent composite for the group
	 * to retrieve elements in the viewer.
	 * @param font The font to use for calculating pixel sizes.  This font is
	 * not managed by the receiver.
	 * @param columnConfig the columns to be shown
	 */
	protected StructuredIUGroup(ProvisioningUI ui, Composite parent, Font font, IUColumnConfig[] columnConfig) {
		this.ui = ui;
		if (columnConfig == null) {
			this.columnConfig = ProvUI.getIUColumnConfig();
		} else {
			this.columnConfig = columnConfig;
		}

		// Set up a fontmetrics for calculations
		GC gc = new GC(parent);
		gc.setFont(font);
		fm = gc.getFontMetrics();
		gc.dispose();
	}

	protected void createGroupComposite(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		viewer = createViewer(composite);
		viewer.getControl().setLayoutData(getViewerGridData());
	}

	protected GridData getViewerGridData() {
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		return data;
	}

	protected abstract StructuredViewer createViewer(Composite parent);

	protected Composite getComposite() {
		return composite;
	}

	protected Shell getShell() {
		return composite.getShell();
	}

	protected StructuredViewer getStructuredViewer() {
		return viewer;
	}

	protected IUColumnConfig[] getColumnConfig() {
		return columnConfig;
	}

	public List<IInstallableUnit> getSelectedIUs() {
		return ElementUtils.elementsToIUs(getSelectedIUElements());
	}

	public Object[] getSelectedIUElements() {
		return viewer.getStructuredSelection().toArray();
	}

	protected int convertHorizontalDLUsToPixels(int dlus) {
		return Dialog.convertHorizontalDLUsToPixels(fm, dlus);
	}

	protected int convertWidthInCharsToPixels(int dlus) {
		return Dialog.convertWidthInCharsToPixels(fm, dlus);
	}

	protected int convertVerticalDLUsToPixels(int dlus) {
		return Dialog.convertVerticalDLUsToPixels(fm, dlus);
	}

	protected int convertHeightInCharsToPixels(int dlus) {
		return Dialog.convertHeightInCharsToPixels(fm, dlus);
	}

	protected void createSortableTreeColumns(Tree tree, TreeViewer treeViewer, IUComparator comparator) {
		tree.setHeaderVisible(true);
		IUColumnConfig[] cols = getColumnConfig();
		for (int i = 0; i < cols.length; i++) {
			TreeColumn tc = new TreeColumn(tree, SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(cols[i].getColumnTitle());
			tc.setWidth(cols[i].getWidthInPixels(tree));
			int sortKey = toSortKey(cols[i].getColumnType());
			tc.addSelectionListener(
					SelectionListener.widgetSelectedAdapter(e -> columnSelected(tree, treeViewer, comparator, (TreeColumn) e.widget, sortKey)));
		}
		if (cols.length > 0) {
			tree.setSortColumn(tree.getColumn(0));
			tree.setSortDirection(comparator.isAscending() ? SWT.UP : SWT.DOWN);
		}
	}

	private static int toSortKey(int columnType) {
		return switch (columnType) {
			case IUColumnConfig.COLUMN_ID -> IUComparator.IU_ID;
			case IUColumnConfig.COLUMN_VERSION, IUColumnConfig.OLD_COLUMN_VERSION, IUColumnConfig.NEW_COLUMN_VERSION -> IUComparator.IU_VERSION;
			case IUColumnConfig.COLUMN_PROVIDER -> IUComparator.IU_PROVIDER;
			default -> IUComparator.IU_NAME;
		};
	}

	private void columnSelected(Tree tree, TreeViewer treeViewer, IUComparator comparator, TreeColumn tc, int sortKey) {
		if (sortKey != comparator.getSortKey()) {
			comparator.setSortKey(sortKey);
			comparator.sortAscending();
			tree.setSortDirection(SWT.UP);
		} else if (comparator.isAscending()) {
			comparator.sortDescending();
			tree.setSortDirection(SWT.DOWN);
		} else {
			comparator.sortAscending();
			tree.setSortDirection(SWT.UP);
		}
		tree.setSortColumn(tc);
		treeViewer.refresh();
	}

	protected Policy getPolicy() {
		return ui.getPolicy();
	}

	protected ProvisioningSession getSession() {
		return ui.getSession();
	}

	protected ProvisioningUI getProvisioningUI() {
		return ui;
	}

	protected Control getDefaultFocusControl() {
		if (viewer != null) {
			return viewer.getControl();
		}
		return null;
	}
}
