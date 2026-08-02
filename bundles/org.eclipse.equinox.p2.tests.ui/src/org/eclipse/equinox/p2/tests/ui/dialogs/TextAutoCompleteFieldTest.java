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

import org.eclipse.equinox.internal.p2.ui.dialogs.TextAutoCompleteField;
import org.eclipse.equinox.p2.tests.ui.AbstractProvisioningUITest;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

/**
 * Tests for {@link TextAutoCompleteField} proposal filtering logic.
 */
public class TextAutoCompleteFieldTest extends AbstractProvisioningUITest {

	private Shell shell;
	private Text text;
	private TextAutoCompleteField field;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Display display = Display.getDefault();
		shell = new Shell(display);
		text = new Text(shell, SWT.BORDER);
		field = new TextAutoCompleteField(text);
	}

	@Override
	protected void tearDown() throws Exception {
		shell.dispose();
		super.tearDown();
	}

	/** Substring match — proposal containing the typed text is returned. */
	public void testSubstringMatchReturnsProposal() {
		field.setProposalStrings(new String[] { "https://download.eclipse.org/releases/latest" });
		IContentProposal[] proposals = field.getProposals("eclipse");
		assertEquals(1, proposals.length);
		assertEquals("https://download.eclipse.org/releases/latest", proposals[0].getContent());
	}

	/** No match — typed text has no match in proposals. */
	public void testNoMatchReturnsEmpty() {
		field.setProposalStrings(new String[] { "https://download.eclipse.org/releases/latest" });
		IContentProposal[] proposals = field.getProposals("xyz999notasite");
		assertEquals(0, proposals.length);
	}

	/** Empty input — never show proposals when nothing is typed. */
	public void testEmptyInputReturnsEmpty() {
		field.setProposalStrings(new String[] { "https://download.eclipse.org/releases/latest" });
		IContentProposal[] proposals = field.getProposals("");
		assertEquals(0, proposals.length);
	}

	/** No proposal strings set — returns empty. */
	public void testNullProposalsReturnsEmpty() {
		field.setProposalStrings(null);
		IContentProposal[] proposals = field.getProposals("eclipse");
		assertEquals(0, proposals.length);
	}

	/** Exact single match suppression — popup should not reopen after acceptance. */
	public void testExactSingleMatchIsSuppressed() {
		String url = "https://download.eclipse.org/releases/latest";
		field.setProposalStrings(new String[] { url });
		text.setText(url);
		IContentProposal[] proposals = field.getProposals(url);
		assertEquals("Exact single match should be suppressed to avoid popup reopening", 0, proposals.length);
	}

	/** Multiple proposals — all matching entries are returned. */
	public void testMultipleMatchesReturned() {
		field.setProposalStrings(new String[] {
				"https://download.eclipse.org/releases/latest",
				"https://download.eclipse.org/releases/2024-12",
				"https://unrelated.example.com/p2"
		});
		IContentProposal[] proposals = field.getProposals("eclipse");
		assertEquals(2, proposals.length);
	}

	/** Match is case-insensitive. */
	public void testMatchIsCaseInsensitive() {
		field.setProposalStrings(new String[] { "https://download.Eclipse.org/releases/latest" });
		IContentProposal[] proposals = field.getProposals("eclipse");
		assertEquals(1, proposals.length);
	}

}
