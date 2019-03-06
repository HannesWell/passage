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
package org.eclipse.passage.lic.base;

import static org.eclipse.passage.lic.base.LicensingProperties.LICENSING_CONDITION_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.passage.lic.base.LicensingEvents.LicensingLifeCycle;
import org.eclipse.passage.lic.base.restrictions.RestrictionVerdicts;
import org.eclipse.passage.lic.runtime.AccessManager;
import org.eclipse.passage.lic.runtime.ConditionEvaluator;
import org.eclipse.passage.lic.runtime.ConditionMiner;
import org.eclipse.passage.lic.runtime.LicensingRequirement;
import org.eclipse.passage.lic.runtime.FeaturePermission;
import org.eclipse.passage.lic.runtime.LicensingCondition;
import org.eclipse.passage.lic.runtime.LicensingConfiguration;
import org.eclipse.passage.lic.runtime.PermissionExaminer;
import org.eclipse.passage.lic.runtime.RequirementResolver;
import org.eclipse.passage.lic.runtime.RestrictionExecutor;
import org.eclipse.passage.lic.runtime.RestrictionVerdict;

public abstract class BaseAccessManager implements AccessManager {

	private final List<RequirementResolver> requirementResolvers = new ArrayList<>();
	private final List<ConditionMiner> conditionMiners = new ArrayList<>();
	private final Map<String, ConditionEvaluator> conditionEvaluators = new HashMap<>();
	private final List<RestrictionExecutor> restrictionExecutors = new ArrayList<>();

	private PermissionExaminer permissionExaminer;

	protected void bindRequirementResolver(RequirementResolver requirementResolver) {
		requirementResolvers.add(requirementResolver);
	}

	protected void unbindRequirementResolver(RequirementResolver configurationResolver) {
		requirementResolvers.remove(configurationResolver);
	}

	protected void bindConditionMiner(ConditionMiner conditionMiner) {
		registerConditionMiner(conditionMiner);
	}

	protected void unbindConditionMiner(ConditionMiner conditionMiner) {
		unregisterConditionMiner(conditionMiner);
	}

	protected void bindConditionEvaluator(ConditionEvaluator conditionEvaluator, Map<String, Object> properties) {
		Object conditionType = properties.get(LICENSING_CONDITION_TYPE);
		String type = String.valueOf(conditionType);
		// FIXME: check permissions
		conditionEvaluators.put(type, conditionEvaluator);
	}

	protected void unbindConditionEvaluator(ConditionEvaluator conditionEvaluator, Map<String, Object> properties) {
		Object conditionType = properties.get(LICENSING_CONDITION_TYPE);
		String type = String.valueOf(conditionType);
		conditionEvaluators.remove(type);
	}

	protected void bindPermissionExaminer(PermissionExaminer permissionExaminer) {
		this.permissionExaminer = permissionExaminer;
	}

	protected void unbindPermissionExaminer(PermissionExaminer permissionExaminer) {
		this.permissionExaminer = null;
	}

	protected void bindRestrictionExecutor(RestrictionExecutor restrictionExecutor) {
		restrictionExecutors.add(restrictionExecutor);
	}

	protected void unbindRestrictionExecutor(RestrictionExecutor restrictionExecutor) {
		restrictionExecutors.remove(restrictionExecutor);
	}
	
	protected void registerConditionMiner(ConditionMiner conditionMiner) {
		conditionMiners.add(conditionMiner);
	}
	
	protected void unregisterConditionMiner(ConditionMiner conditionMiner) {
		conditionMiners.remove(conditionMiner);
	}

	@Override
	public void executeAccessRestrictions(LicensingConfiguration configuration) {
		Iterable<LicensingRequirement> requirements = resolveRequirements(configuration);
		Iterable<LicensingCondition> conditions = extractConditions(configuration);
		Iterable<FeaturePermission> permissions = evaluateConditions(conditions, configuration);
		Iterable<RestrictionVerdict> verdicts = examinePermissons(requirements, permissions, configuration);
		executeRestrictions(verdicts);
	}

	@Override
	public Iterable<LicensingRequirement> resolveRequirements(LicensingConfiguration configuration) {
		List<LicensingRequirement> result = new ArrayList<>();
		for (RequirementResolver configurationResolver : requirementResolvers) {
			Iterable<LicensingRequirement> requirements = configurationResolver
					.resolveConfigurationRequirements(configuration);
			for (LicensingRequirement requirement : requirements) {
				result.add(requirement);
			}
		}
		List<LicensingRequirement> unmodifiable = Collections.unmodifiableList(result);
		postEvent(LicensingLifeCycle.REQUIREMENTS_RESOLVED, unmodifiable);
		return unmodifiable;
	}

	@Override
	public Iterable<LicensingCondition> extractConditions(LicensingConfiguration configuration) {
		List<LicensingCondition> result = new ArrayList<>();
		for (ConditionMiner conditionMiner : conditionMiners) {
			Iterable<LicensingCondition> conditions = conditionMiner.extractLicensingConditions(configuration);
			for (LicensingCondition condition : conditions) {
				result.add(condition);
			}
		}
		List<LicensingCondition> unmodifiable = Collections.unmodifiableList(result);
		postEvent(LicensingLifeCycle.CONDITIONS_EXTRACTED, unmodifiable);
		return unmodifiable;
	}

	@Override
	public Iterable<FeaturePermission> evaluateConditions(Iterable<LicensingCondition> conditions,
			LicensingConfiguration configuration) {
		List<FeaturePermission> result = new ArrayList<>();
		if (conditions == null) {
			String message = "Evaluation rejected for invalid conditions";
			logError(message, new NullPointerException());
			List<FeaturePermission> empty = Collections.emptyList();
			postEvent(LicensingLifeCycle.CONDITIONS_EVALUATED, empty);
			return empty;
		}
		List<FeaturePermission> unmodifiable = Collections.unmodifiableList(result);
		Map<String, List<LicensingCondition>> map = new HashMap<>();
		List<LicensingCondition> invalid = new ArrayList<>();
		for (LicensingCondition condition : conditions) {
			if (condition == null) {
				String message = "Evaluation rejected for invalid condition";
				logError(message, new NullPointerException());
				continue;
			}
			String validate = validate(condition);
			if (validate == null) {
				String type = condition.getConditionType();
				List<LicensingCondition> list = map.computeIfAbsent(type, key -> new ArrayList<>());
				list.add(condition);
			} else {
				logError(validate, null);
				invalid.add(condition);
			}
		}
		Set<String> types = map.keySet();
		for (String type : types) {
			ConditionEvaluator evaluator = conditionEvaluators.get(type);
			if (evaluator == null) {
				String message = String.format("No evaluator available for type %s", type);
				logError(message, new NullPointerException());
				continue;
			}
			Iterable<FeaturePermission> permissions = evaluator.evaluateConditions(map.get(type), configuration);
			for (FeaturePermission permission : permissions) {
				result.add(permission);
			}
		}
		postEvent(LicensingLifeCycle.CONDITIONS_EVALUATED, unmodifiable);
		return unmodifiable;
	}

	@Override
	public Iterable<RestrictionVerdict> examinePermissons(Iterable<LicensingRequirement> requirements,
			Iterable<FeaturePermission> permissions, LicensingConfiguration configuration) {
		if (configuration == null) {
			logError("Invalid configuration", new NullPointerException());
			List<RestrictionVerdict> examined = Collections.emptyList();
			postEvent(LicensingLifeCycle.PERMISSIONS_EXAMINED, examined);
			return examined;
		}
		if (requirements == null) {
			logError("Invalid configuration requirements", new NullPointerException());
			List<RestrictionVerdict> examined = Collections.emptyList();
			postEvent(LicensingLifeCycle.PERMISSIONS_EXAMINED, examined);
			return examined;
		}
		if (permissionExaminer == null) {
			String message = String.format("No permission examiner defined, rejecting all %s", requirements);
			logError(message, new NullPointerException());
			List<RestrictionVerdict> verdicts = new ArrayList<>();
			for (LicensingRequirement requirement : requirements) {
				if (requirement == null) {
					logError("Invalid configuration requirement ignored", new NullPointerException());
					continue;
				}
				RestrictionVerdict verdict = RestrictionVerdicts.createError(requirement, RestrictionVerdicts.CODE_CONFIGURATION_ERROR);
				verdicts.add(verdict);
			}
			postEvent(LicensingLifeCycle.PERMISSIONS_EXAMINED, Collections.unmodifiableList(verdicts));
			return verdicts;
		}
		Iterable<RestrictionVerdict> examined = permissionExaminer.examine(requirements, permissions);
		postEvent(LicensingLifeCycle.PERMISSIONS_EXAMINED, examined);
		return examined;
	}

	@Override
	public void executeRestrictions(Iterable<RestrictionVerdict> restrictions) {
		for (RestrictionExecutor executor : restrictionExecutors) {
			try {
				executor.execute(restrictions);
			} catch (Exception e) {
				String message = String.format("%s failed to execute %s", executor, restrictions);
				logError(message, e);
			}
		}
		postEvent(LicensingLifeCycle.RESTRICTIONS_EXECUTED, restrictions);
	}

	protected String validate(LicensingCondition condition) {
		Date validFrom = condition.getValidFrom();
		if (validFrom == null) {
			String format = "Valid from not specified for condition %s";
			return String.format(format, condition);
		}
		Date now = new Date();
		if (validFrom.after(now)) {
			String format = "Valid from starts in the future for condition %s";
			return String.format(format, condition);
		}
		Date validUntil = condition.getValidUntil();
		if (validUntil == null) {
			String format = "Valid until not specified for condition %s";
			return String.format(format, condition);
		}
		if (validUntil.before(now)) {
			String format = "Valid until ends in the past for condition %s";
			return String.format(format, condition);
		}
		return null;
	}

	protected abstract void postEvent(String topic, Object data);

	protected abstract void logError(String message, Throwable e);

}
