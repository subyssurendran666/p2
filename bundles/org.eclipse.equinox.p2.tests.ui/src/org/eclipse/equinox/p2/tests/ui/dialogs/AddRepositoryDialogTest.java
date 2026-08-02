/*******************************************************************************
 *  Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.equinox.p2.tests.ui.dialogs;

import java.net.URI;
import java.util.Arrays;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.equinox.internal.p2.ui.dialogs.RepositoryNameAndLocationDialog;
import org.eclipse.equinox.internal.p2.ui.dialogs.TextAutoCompleteField;
import org.eclipse.equinox.p2.tests.ui.AbstractProvisioningUITest;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.widgets.Shell;

/**
 * Tests that {@link RepositoryNameAndLocationDialog} wires autocomplete
 * proposals from the repository manager into its Location field.
 */
public class AddRepositoryDialogTest extends AbstractProvisioningUITest {

	/**
	 * Dialog subclass that exposes the protected autocomplete field for testing.
	 */
	static class TestDialog extends RepositoryNameAndLocationDialog {
		TestDialog(Shell shell, org.eclipse.equinox.p2.ui.ProvisioningUI ui) {
			super(shell, ui);
			setBlockOnOpen(false);
		}

		@Override
		public TextAutoCompleteField getUrlAutoComplete() {
			return super.getUrlAutoComplete();
		}
	}

	private URI enabledRepo;
	private URI disabledRepo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		enabledRepo = new URI("https://enabled.example.com/p2");
		disabledRepo = new URI("https://disabled.example.com/p2");
		ui.getRepositoryTracker().addRepository(enabledRepo, null, ui.getSession());
		ui.getRepositoryTracker().addRepository(disabledRepo, null, ui.getSession());
		metaManager.setEnabled(disabledRepo, false);
	}

	@Override
	protected void tearDown() throws Exception {
		ui.getRepositoryTracker().removeRepositories(new URI[] { enabledRepo, disabledRepo }, ui.getSession());
		super.tearDown();
	}

	/**
	 * Enabled repositories must NOT be proposed
	 */
	public void testEnabledRepoIsNotProposed() {
		TestDialog dialog = new TestDialog(null, ui);
		dialog.open();
		try {
			String[] proposals = getProposalContents(dialog, "enabled.example");
			assertFalse("Enabled repo URL should not be proposed",
					Arrays.asList(proposals).contains(URIUtil.toUnencodedString(enabledRepo)));
		} finally {
			dialog.close();
		}
	}

	/**
	 * Disabled repositories must be proposed
	 */
	public void testDisabledRepoIsProposed() {
		TestDialog dialog = new TestDialog(null, ui);
		dialog.open();
		try {
			String[] proposals = getProposalContents(dialog, "disabled.example");
			assertTrue("Disabled repo URL should be proposed",
					Arrays.asList(proposals).contains(URIUtil.toUnencodedString(disabledRepo)));
		} finally {
			dialog.close();
		}
	}

	private String[] getProposalContents(TestDialog dialog, String filter) {
		IContentProposal[] raw = dialog.getUrlAutoComplete().getProposals(filter);
		String[] result = new String[raw.length];
		for (int i = 0; i < raw.length; i++) {
			result[i] = raw[i].getContent();
		}
		return result;
	}
}
