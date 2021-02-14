/*******************************************************************************
 * Copyright (c) 2020, 2021 ArSysOp
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ArSysOp - initial API and implementation
 *******************************************************************************/
package org.eclipse.passage.lbc.base.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.eclipse.passage.lbc.internal.base.BaseFlotingRequestHandled;
import org.eclipse.passage.lbc.internal.base.Failure;
import org.eclipse.passage.lic.floating.model.net.ServerAuthenticationExpression;
import org.eclipse.passage.lic.floating.model.net.ServerAuthenticationType;
import org.eclipse.passage.lic.internal.api.EvaluationType;
import org.eclipse.passage.lic.internal.api.PassageAction;
import org.eclipse.passage.lic.internal.base.ProductIdentifier;
import org.eclipse.passage.lic.internal.base.ProductVersion;
import org.eclipse.passage.lic.internal.base.StringNamedData;
import org.eclipse.passage.lic.internal.net.handle.NetResponse;
import org.junit.Test;

public final class MineTest {

	@Test
	public void demandsProductIdentifier() {
		testDemandProductInformation(version());
	}

	@Test
	public void demandsProductVersion() {
		testDemandProductInformation(id());
	}

	@Test
	public void demandsUser() {
		NetResponse response = new BaseFlotingRequestHandled(//
				new RequestConstructed()//
						.withAction(new PassageAction.Mine())//
						.withParameters(Arrays.asList(//
								id(), //
								version(), //
								authType(), //
								authExpression()//
						)).get()//
		).get();
		assertFailedWithCode(response, new Failure.BadRequestNoUser());
	}

	private void testDemandProductInformation(StringNamedData half) {
		NetResponse response = new BaseFlotingRequestHandled(//
				new RequestConstructed()//
						.withAction(new PassageAction.Mine())//
						.withParameters(Arrays.asList(////
								half, //
								authType(), //
								authExpression()//
						)).get()//
		).get();
		assertFailedWithCode(response, new Failure.BadRequestInvalidProduct());
	}

	private void assertFailedWithCode(NetResponse response, Failure expected) {
		assertTrue(response.failed());
		assertEquals(expected.error().code(), response.error().code());
	}

	private ProductVersion version() {
		return new ProductVersion("1.1.0"); //$NON-NLS-1$
	}

	private ProductIdentifier id() {
		return new ProductIdentifier("sample-product");//$NON-NLS-1$
	}

	private ServerAuthenticationType authType() {
		return new ServerAuthenticationType(new EvaluationType.Hardware().identifier());
	}

	private ServerAuthenticationExpression authExpression() {
		return new ServerAuthenticationExpression("os.family=*"); //$NON-NLS-1$
	}

}
