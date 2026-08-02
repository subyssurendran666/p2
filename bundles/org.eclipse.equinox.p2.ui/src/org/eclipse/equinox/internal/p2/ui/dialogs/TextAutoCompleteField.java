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
package org.eclipse.equinox.internal.p2.ui.dialogs;

import java.util.ArrayList;
import org.eclipse.core.text.StringMatcher;
import org.eclipse.jface.fieldassist.*;
import org.eclipse.swt.widgets.Text;

/**
 * TextAutoCompleteField is an auto complete field appropriate for pattern
 * matching the text in a {@link Text} field against a fixed, externally
 * supplied list of proposal strings. Unlike {@link ComboAutoCompleteField}, it
 * has no notion of "items already in the widget", since a Text field has no
 * items of its own; proposals must always be supplied with
 * {@link #setProposalStrings(String[])}.
 *
 * @since 2.10
 */
public class TextAutoCompleteField {

	ContentProposalAdapter adapter;
	Text text;
	String[] proposalStrings = new String[0];

	public TextAutoCompleteField(Text t) {
		this.text = t;
		adapter = new ContentProposalAdapter(text, new TextContentAdapter(), getProposalProvider(), null, null);
		adapter.setPropagateKeys(true);
		adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
	}

	public void setProposalStrings(String[] proposals) {
		proposalStrings = proposals == null ? new String[0] : proposals;
	}

	public IContentProposal[] getProposals(String contents) {
		return getProposalProvider().getProposals(contents, contents.length());
	}

	IContentProposalProvider getProposalProvider() {
		return (contents, position) -> {
			if (contents.length() == 0 || proposalStrings.length == 0) {
				return new IContentProposal[0];
			}
			StringMatcher matcher = new StringMatcher("*" + contents + "*", true, false); //$NON-NLS-1$ //$NON-NLS-2$
			ArrayList<String> matches = new ArrayList<>();
			for (String item : proposalStrings) {
				if (matcher.match(item)) {
					matches.add(item);
				}
			}

			// Don't autoactivate if the only proposal exactly matches what's already typed.
			// Prevents the popup reopening right after a proposal has just been accepted.
			if (matches.size() == 1 && matches.get(0).equals(text.getText())) {
				return new IContentProposal[0];
			}

			if (matches.isEmpty()) {
				return new IContentProposal[0];
			}

			IContentProposal[] proposals = new IContentProposal[matches.size()];
			for (int i = 0; i < matches.size(); i++) {
				final String proposal = matches.get(i);
				proposals[i] = new IContentProposal() {

					@Override
					public String getContent() {
						return proposal;
					}

					@Override
					public int getCursorPosition() {
						return proposal.length();
					}

					@Override
					public String getDescription() {
						return null;
					}

					@Override
					public String getLabel() {
						return null;
					}
				};
			}
			return proposals;
		};
	}
}
