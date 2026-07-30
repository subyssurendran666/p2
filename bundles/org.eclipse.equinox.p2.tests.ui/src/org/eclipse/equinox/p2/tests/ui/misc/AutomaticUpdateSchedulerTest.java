/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - regression test for bug #862 (int overflow in computeFuzzyPoll)
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.ui.misc;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateMessages;
import org.eclipse.equinox.internal.p2.ui.sdk.scheduler.AutomaticUpdateScheduler;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.junit.Test;

public class AutomaticUpdateSchedulerTest {

	private static final String P_FUZZY_RECURRENCE = AutomaticUpdateScheduler.P_FUZZY_RECURRENCE;

	// 30 * 86,400,000 = 2,592,000,000 > Integer.MAX_VALUE (2,147,483,647).
	@Test
	public void testComputeFuzzyPollOnceAMonthDoesNotOverflow() throws Exception {
		long result = invokeComputeFuzzyPoll(AutomaticUpdateMessages.SchedulerStartup_OnceAMonth);
		assertEquals("Once-a-month poll period must equal 30 days in ms, was negative before fix (bug #862)",
				30L * 24 * 60 * 60 * 1000, result);
	}

	private static long invokeComputeFuzzyPoll(String recurrence) throws Exception {
		Method m = AutomaticUpdateScheduler.class.getDeclaredMethod("computeFuzzyPoll", IPreferenceStore.class);
		m.setAccessible(true);
		PreferenceStore store = new PreferenceStore();
		store.setValue(P_FUZZY_RECURRENCE, recurrence);
		return (long) m.invoke(null, store);
	}
}
