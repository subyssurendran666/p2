/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.metadata;

import static org.junit.Assert.assertThrows;

import org.eclipse.equinox.internal.p2.metadata.RequiredCapability;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.VersionRange;
import org.eclipse.equinox.p2.metadata.expression.ExpressionUtil;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.junit.Test;

/**
 * Tests that {@link IMatchExpression#toLDAPString(StringBuilder)} works for all
 * predefined {@link RequiredCapability} version-range templates. The expression-layer
 * overrides on {@code MatchExpression}, {@code Exists}, and {@code LambdaExpression}
 * are exercised by calling {@code toLDAPString()} directly; output syntax is validated
 * by passing the result through the LDAP filter parser.
 */
public class RequirementToLDAPFilterTest {

	private static final String NS = "org.eclipse.equinox.p2.iu"; //$NON-NLS-1$
	private static final String NAME = "my.bundle"; //$NON-NLS-1$

	/**
	 * Calls {@code toLDAPString()} on the match expression for the given range and
	 * validates that the output is syntactically valid RFC 4515 by parsing it.
	 */
	private static void assertValidLDAP(VersionRange range) throws Exception {
		IMatchExpression<IInstallableUnit> expr =
				RequiredCapability.createMatchExpressionFromRange(NS, NAME, range);
		StringBuilder buf = new StringBuilder();
		expr.toLDAPString(buf); // exercises MatchExpression -> Exists -> LambdaExpression
		ExpressionUtil.parseLDAP(buf.toString()); // throws InvalidSyntaxException if invalid
	}

	@Test
	public void testAll_nullRange() throws Exception {
		assertValidLDAP(null);
	}

	@Test
	public void testAll_emptyRange() throws Exception {
		assertValidLDAP(VersionRange.emptyRange);
	}

	@Test
	public void testStrict() throws Exception {
		assertValidLDAP(VersionRange.create("[1.0.0,1.0.0]")); //$NON-NLS-1$
	}

	@Test
	public void testOpenI() throws Exception {
		assertValidLDAP(VersionRange.create("1.0.0")); //$NON-NLS-1$
	}

	@Test
	public void testOpenN() throws Exception {
		assertValidLDAP(VersionRange.create("(1.0.0,)")); //$NON-NLS-1$
	}

	@Test
	public void testClosedII() throws Exception {
		assertValidLDAP(VersionRange.create("[1.0.0,2.0.0]")); //$NON-NLS-1$
	}

	@Test
	public void testClosedIN() throws Exception {
		assertValidLDAP(VersionRange.create("[1.0.0,2.0.0)")); //$NON-NLS-1$
	}

	@Test
	public void testClosedNI() throws Exception {
		assertValidLDAP(VersionRange.create("(1.0.0,2.0.0]")); //$NON-NLS-1$
	}

	@Test
	public void testClosedNN() throws Exception {
		assertValidLDAP(VersionRange.create("(1.0.0,2.0.0)")); //$NON-NLS-1$
	}

	// toLDAPString throws UnsupportedOperationException (pre-existing contract on
	// Expression.toLDAPString) when a node has no LDAP equivalent, e.g. ~= (Matches).
	@Test
	public void testNonSerializableThrows() {
		IMatchExpression<IInstallableUnit> custom = ExpressionUtil.getFactory()
				.matchExpression(ExpressionUtil.parse("id ~= $0"), //$NON-NLS-1$
						ExpressionUtil.getFactory().matchExpression(ExpressionUtil.parse("'some.id'"))); //$NON-NLS-1$
		StringBuilder buf = new StringBuilder();
		assertThrows(UnsupportedOperationException.class, () -> custom.toLDAPString(buf));
	}
}
