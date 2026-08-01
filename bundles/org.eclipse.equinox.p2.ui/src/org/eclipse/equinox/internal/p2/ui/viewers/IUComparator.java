/*******************************************************************************
 *  Copyright (c) 2000, 2026 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.equinox.internal.p2.ui.viewers;

import org.eclipse.equinox.internal.p2.ui.ProvUI;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class IUComparator extends ViewerComparator {
	public static final int IU_NAME = 0;
	public static final int IU_ID = 1;

	public static final int IU_VERSION = 2;
	public static final int IU_PROVIDER = 3;
	private static final int ASCENDING = 1;
	private static final int DESCENDING = -1;
	private int key;
	private int direction = ASCENDING;
	private boolean showingId = false;

	public IUComparator(int sortKey) {
		this.key = sortKey;
		showingId = sortKey == IU_ID;
	}

	/**
	 * Use the specified column config to determine whether the id should be used in
	 * lieu of an empty name when sorting.
	 */
	public void useColumnConfig(IUColumnConfig[] columnConfigs) {
		for (IUColumnConfig columnConfig : columnConfigs) {
			if (columnConfig.getColumnType() == IUColumnConfig.COLUMN_ID) {
				showingId = true;
				break;
			}
		}
	}

	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		IInstallableUnit iu1 = ProvUI.getAdapter(obj1, IInstallableUnit.class);
		IInstallableUnit iu2 = ProvUI.getAdapter(obj2, IInstallableUnit.class);
		if (iu1 == null || iu2 == null) {
			// If these are not iu's use the super class comparator.
			return super.compare(viewer, obj1, obj2);
		}

		if (key == IU_VERSION) {
			// Sorting on the version column directly, no string keys needed.
			int result = iu1.getVersion().compareTo(iu2.getVersion()) * direction;
			if (result == 0) {
				result = getNameOrId(iu1).compareToIgnoreCase(getNameOrId(iu2));
			}
			return result;
		}

		String key1, key2;
		if (key == IU_PROVIDER) {
			key1 = iu1.getProperty(IInstallableUnit.PROP_PROVIDER, ""); //$NON-NLS-1$
			key2 = iu2.getProperty(IInstallableUnit.PROP_PROVIDER, ""); //$NON-NLS-1$
		} else if (key == IU_ID) {
			key1 = iu1.getId();
			key2 = iu2.getId();
		} else {
			// IU_NAME: compare the iu names in the default locale.
			// If a name is not defined, we use blank if we know the id is shown in another
			// column. If the id is not shown elsewhere, then we are displaying it, so use
			// the id instead.
			key1 = getNameOrId(iu1);
			key2 = getNameOrId(iu2);
		}

		int result = key1.compareToIgnoreCase(key2) * direction;
		if (result == 0) {
			// We want to show later versions first so compare backwards.
			result = iu2.getVersion().compareTo(iu1.getVersion());
		}
		return result;
	}

	private String getNameOrId(IInstallableUnit iu) {
		String name = iu.getProperty(IInstallableUnit.PROP_NAME, null);
		if (name == null) {
			name = showingId ? "" : iu.getId(); //$NON-NLS-1$
		}
		return name;
	}

	public void sortAscending() {
		direction = ASCENDING;
	}

	public void sortDescending() {
		direction = DESCENDING;
	}

	public boolean isAscending() {
		return direction == ASCENDING;
	}

	public void setSortKey(int key) {
		this.key = key;
		showingId = showingId || key == IU_ID;
	}

	public int getSortKey() {
		return key;
	}
}
