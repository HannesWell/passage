/*******************************************************************************
 * Copyright (c) 2019 ArSysOp
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     ArSysOp - initial API and implementation
 *******************************************************************************/
package org.eclipse.passage.loc.report.internal.core;

import java.util.Set;

import org.eclipse.passage.lic.users.UserDescriptor;
import org.eclipse.passage.loc.yars.internal.api.Storage;

/**
 * FIXME doc
 * 
 * @since 0.1
 */
@SuppressWarnings("restriction")
public interface CustomerStorage extends Storage<UserDescriptor> {

	public Set<UserDescriptor> forProducts(Set<String> products);

}
