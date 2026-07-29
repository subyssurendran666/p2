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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.p2.tests.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.net.URI;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.internal.p2.repository.DownloadStatus;
import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryStatus;
import org.eclipse.equinox.internal.p2.transport.ecf.RepositoryTransport;
import org.junit.Test;

/**
 * Tests that {@link DownloadStatus} correctly carries URI information, and that
 * the factory methods in {@link RepositoryTransport} and {@link RepositoryStatus}
 * attach the URI to every status they produce.
 */
public class DownloadStatusTest {

	private static final URI TEST_URI = URI.create("https://example.org/repository/artifact.jar"); //$NON-NLS-1$

	@Test
	public void testUriIsNullByDefault() {
		DownloadStatus status = new DownloadStatus(IStatus.OK, "test.plugin", "ok"); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull("URI should be null when not set", status.getUri()); //$NON-NLS-1$
	}

	@Test
	public void testSetAndGetUri() {
		DownloadStatus status = new DownloadStatus(IStatus.OK, "test.plugin", "ok"); //$NON-NLS-1$ //$NON-NLS-2$
		status.setUri(TEST_URI);
		assertEquals("URI should round-trip through getter", TEST_URI, status.getUri()); //$NON-NLS-1$
	}

	@Test
	public void testSetUriToNull() {
		DownloadStatus status = new DownloadStatus(IStatus.OK, "test.plugin", "ok"); //$NON-NLS-1$ //$NON-NLS-2$
		status.setUri(TEST_URI);
		status.setUri(null);
		assertNull("URI should be null after being set to null", status.getUri()); //$NON-NLS-1$
	}

	// toString tests
	@Test
	public void testToStringContainsUriWhenSet() {
		DownloadStatus status = new DownloadStatus(IStatus.OK, "test.plugin", "ok"); //$NON-NLS-1$ //$NON-NLS-2$
		status.setUri(TEST_URI);
		assertTrue("toString should contain the URI value", //$NON-NLS-1$
				status.toString().contains(TEST_URI.toString()));
	}

	@Test
	public void testToStringShowsUnknownWhenUriIsNull() {
		DownloadStatus status = new DownloadStatus(IStatus.OK, "test.plugin", "ok"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("toString should show 'Unknown' when URI is null", //$NON-NLS-1$
				status.toString().contains("URI=Unknown uri")); //$NON-NLS-1$
	}

	// RepositoryTransport factory method tests
	@Test
	public void testRepositoryTransportForExceptionSetsUri() {
		DownloadStatus status = RepositoryTransport.forException(new FileNotFoundException(), TEST_URI);
		assertEquals("forException should attach the URI to the returned status", //$NON-NLS-1$
				TEST_URI, status.getUri());
	}

	@Test
	public void testRepositoryTransportForExceptionConnectExceptionSetsUri() {
		DownloadStatus status = RepositoryTransport.forException(new ConnectException(), TEST_URI);
		assertEquals("forException (ConnectException) should attach the URI to the returned status", //$NON-NLS-1$
				TEST_URI, status.getUri());
	}

	@Test
	public void testRepositoryTransportForStatusSetsUri() {
		IStatus original = new Status(IStatus.ERROR, "test.plugin", 0, "fail", new ConnectException()); //$NON-NLS-1$ //$NON-NLS-2$
		DownloadStatus status = RepositoryTransport.forStatus(original, TEST_URI);
		assertEquals("forStatus should attach the URI to the returned status", //$NON-NLS-1$
				TEST_URI, status.getUri());
	}

	// RepositoryStatus factory method tests
	@Test
	public void testRepositoryStatusForExceptionSetsUri() {
		DownloadStatus status = RepositoryStatus.forException(new FileNotFoundException(), TEST_URI);
		assertEquals("RepositoryStatus.forException should attach the URI to the returned status", //$NON-NLS-1$
				TEST_URI, status.getUri());
	}

	@Test
	public void testRepositoryStatusForStatusSetsUri() {
		IStatus original = new Status(IStatus.ERROR, "test.plugin", 0, "fail", new ConnectException()); //$NON-NLS-1$ //$NON-NLS-2$
		DownloadStatus status = RepositoryStatus.forStatus(original, TEST_URI);
		assertEquals("RepositoryStatus.forStatus should attach the URI to the returned status", //$NON-NLS-1$
				TEST_URI, status.getUri());
	}
}
