/*******************************************************************************
 * Copyright (c) 2020 ArSysOp
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
package org.eclipse.passage.lic.internal.api;

import org.eclipse.passage.lic.internal.api.requirements.Requirement;
import org.eclipse.passage.lic.internal.api.restrictions.ExaminationCertificate;
import org.eclipse.passage.lic.internal.api.restrictions.Restriction;
import org.eclipse.passage.lic.internal.api.restrictions.RestrictionLevel;

/**
 * <p>
 * Main entry point for a code base under license protection.
 * </p>
 * FIXME: explain exhaustively how to use, with code examples
 * 
 * @see org.eclipse.passage.lic.api
 */
public interface Passage {

	/**
	 * <p>
	 * Full feathered access cycle invocation returns complete set of data and
	 * diagnostics to take an informed decision
	 * <p>
	 * 
	 * <p>
	 * Call this method from your {@code feature} code to ensure it is not used
	 * without proper license.
	 * </p>
	 * 
	 * @param feature string identifier of the feature under licensing.
	 * @see org.eclipse.passage.lic.api
	 */
	ServiceInvocationResult<ExaminationCertificate> checkLicense(String feature);

	/**
	 * <p>
	 * Ask Passage if a product user has enough license coverage to exploit the
	 * given {@code feature}. Access cycle invocation is simplified to be called in
	 * a headless environment.
	 * </p>
	 * <p>
	 * The full-feathered response is reduced to simple boolean value according to
	 * the following rules:
	 * </p>
	 * <ul>
	 * <li>If there is no {@linkplain Requirement} a product defined for the
	 * feature, then the answer is {@code true} - the feature can be used.</li>
	 * <li>IF not all the defined {@linkplain Requirement}s found for the feature
	 * are satisfied by user's license coverage, then we look at how severe are
	 * those which stay unsatisfied. If these are only of {@code info} or @{cod
	 * warning} restriction levels, then the feature can be used and the answer is
	 * again {@code true}</li>
	 * <li>Is it's not the case, or if the Passage Framework suspects sabotage or is
	 * poorly configured and cannot properly operate, then the answer is
	 * {@code false} and the feature cannot be used.</li>
	 * </ul>
	 * <p>
	 * Use it in the case you cannot afford full feather License Dialog appearance.
	 * Like to implement {@code action::isEnabled} of sorts or to guide simple
	 * control flow.
	 * </p>
	 * 
	 * @param feature string identifier of the feature under licensing.
	 * @return {@code true} if the given {@code feature} can be used and
	 *         {@code false} otherwise
	 * 
	 * @see Requirement
	 * @see Restriction
	 * @see RestrictionLevel
	 * @see org.eclipse.passage.lic.api
	 */
	boolean canUse(String feature);

	/**
	 * Get the product identification found and used by Passage across all the
	 * access cycle phases
	 */
	ServiceInvocationResult<LicensedProduct> product();

}
