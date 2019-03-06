/*******************************************************************************
 * Copyright (c) 2018-2019 ArSysOp
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
package org.eclipse.passage.lic.internal.equinox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.passage.lic.base.LicensingEvents.LicensingLifeCycle;
import org.eclipse.passage.lic.equinox.ApplicationConfigurations;
import org.eclipse.passage.lic.runtime.AccessManager;
import org.eclipse.passage.lic.runtime.LicensingRequirement;
import org.eclipse.passage.lic.runtime.FeaturePermission;
import org.eclipse.passage.lic.runtime.LicensingCondition;
import org.eclipse.passage.lic.runtime.LicensingConfiguration;
import org.eclipse.passage.lic.runtime.RestrictionVerdict;
import org.eclipse.passage.lic.runtime.inspector.FeatureCase;
import org.eclipse.passage.lic.runtime.inspector.FeatureInspector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(property = EventConstants.EVENT_TOPIC + "=" + LicensingLifeCycle.TOPIC_ALL)
public class EquinoxFeatureInspector implements FeatureInspector, EventHandler {

	private final Map<String, List<EquinoxFeatureCase>> cases = new HashMap<>();
	private final Map<String, List<LicensingRequirement>> requirements = new HashMap<>();
	private final Map<String, List<RestrictionVerdict>> restrictions = new HashMap<>();

	private AccessManager accessManager;

	@Reference
	public void bindAccessManager(AccessManager accessManager) {
		this.accessManager = accessManager;
	}

	public void unbindAccessManager(AccessManager accessManager) {
		this.accessManager = null;
	}

	@Override
	public void handleEvent(Event event) {
		System.out.println("EquinoxFeatureInspector.handleEvent() " + event);
		event.getPropertyNames();
		// TODO Auto-generated method stub

	}

	@Override
	public LicensingConfiguration getLicensingConfiguration() {
		return ApplicationConfigurations.getLicensingConfiguration();
	}

	@Override
	public FeatureCase inspectFeatures(String... identifiers) {
		return inspectFeatures(Arrays.asList(identifiers));
	}

	@Override
	public FeatureCase inspectFeatures(Iterable<String> identifiers) {
		List<String> ids = new ArrayList<String>();
		identifiers.forEach(ids::add);
		if (ids.isEmpty()) {
			LicensingConfiguration configuration = getLicensingConfiguration();
			Iterable<LicensingRequirement> resolved = accessManager.resolveRequirements(configuration);
			resolved.forEach(r -> ids.add(r.getFeatureIdentifier()));
		}
		EquinoxFeatureCase efc = new EquinoxFeatureCase(this, ids);
		for (String featureId : ids) {
			cases.computeIfAbsent(featureId, list -> new ArrayList<>()).add(efc);
		}
		return efc;
	}

	void close(EquinoxFeatureCase equinoxFeatureCase) {
		Iterable<String> featureIdentifiers = equinoxFeatureCase.getFeatureIdentifiers();
		for (String featureId : featureIdentifiers) {
			List<EquinoxFeatureCase> list = cases.get(featureId);
			list.remove(equinoxFeatureCase);
			if (list.isEmpty()) {
				cases.remove(featureId);
			}
		}
	}

	public Iterable<LicensingRequirement> getRequirements(Iterable<String> featureIdentifiers) {
		LicensingConfiguration configuration = getLicensingConfiguration();
		List<LicensingRequirement> result = new ArrayList<>();
		for (String featureId : featureIdentifiers) {
			List<LicensingRequirement> found = requirements.computeIfAbsent(featureId, list -> new ArrayList<>());
			if (found.isEmpty()) {
				Iterable<LicensingRequirement> examined = resolveFeatureRequirements(featureId, configuration);
				examined.forEach(found::add);
			}
			found.forEach(result::add);
		}
		return result;
	}

	public Iterable<RestrictionVerdict> getRestrictions(Iterable<String> featureIdentifiers) {
		LicensingConfiguration configuration = getLicensingConfiguration();
		List<RestrictionVerdict> result = new ArrayList<>();
		for (String featureId : featureIdentifiers) {
			List<RestrictionVerdict> found = restrictions.computeIfAbsent(featureId, list -> new ArrayList<>());
			if (found.isEmpty()) {
				Iterable<RestrictionVerdict> examined = examineFeaturePermissons(featureId, configuration);
				examined.forEach(found::add);
			}
			found.forEach(result::add);
		}
		return result;
	}

	public Iterable<RestrictionVerdict> examineFeaturePermissons(String featureId,
			LicensingConfiguration configuration) {
		List<LicensingRequirement> requirements = new ArrayList<LicensingRequirement>();
		Iterable<LicensingRequirement> resolved = accessManager.resolveRequirements(configuration);
		for (LicensingRequirement requirement : resolved) {
			if (featureId.equals(requirement.getFeatureIdentifier())) {
				requirements.add(requirement);
			}
		}
		List<LicensingCondition> conditions = new ArrayList<LicensingCondition>();
		Iterable<LicensingCondition> extractConditions = accessManager.extractConditions(configuration);
		for (LicensingCondition condition : extractConditions) {
			if (featureId.equals(condition.getFeatureIdentifier())) {
				conditions.add(condition);
			}
		}
		Iterable<FeaturePermission> permissions = accessManager.evaluateConditions(conditions, configuration);

		List<RestrictionVerdict> verdicts = new ArrayList<RestrictionVerdict>();
		Iterable<RestrictionVerdict> examined = accessManager.examinePermissons(requirements, permissions,
				configuration);
		examined.forEach(verdicts::add);
		return verdicts;
	}

	public Iterable<LicensingRequirement> resolveFeatureRequirements(String featureId,
			LicensingConfiguration configuration) {
		if (featureId == null) {
			return Collections.emptyList();
		}
		List<LicensingRequirement> result = new ArrayList<>();
		Iterable<LicensingRequirement> resolveRequirements = accessManager.resolveRequirements(configuration);
		for (LicensingRequirement requirement : resolveRequirements) {
			if (featureId.equals(requirement.getFeatureIdentifier())) {
				result.add(requirement);
			}
		}
		return Collections.unmodifiableList(result);
	}

}
