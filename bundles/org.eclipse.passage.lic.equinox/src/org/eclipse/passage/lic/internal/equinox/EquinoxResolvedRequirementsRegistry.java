package org.eclipse.passage.lic.internal.equinox;

import org.eclipse.passage.lic.internal.api.registry.Registry;
import org.eclipse.passage.lic.internal.api.registry.RuntimeRegistry;
import org.eclipse.passage.lic.internal.api.registry.StringServiceId;
import org.eclipse.passage.lic.internal.api.requirements.ResolvedRequirements;
import org.eclipse.passage.lic.internal.api.requirements.ResolvedRequirementsRegistry;
import org.eclipse.passage.lic.internal.base.registry.BaseRuntimeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@SuppressWarnings("restriction")

@Component
public final class EquinoxResolvedRequirementsRegistry implements ResolvedRequirementsRegistry {

	private final RuntimeRegistry<StringServiceId, ResolvedRequirements> registry = //
			new BaseRuntimeRegistry<StringServiceId, ResolvedRequirements>();

	@Override
	public Registry<StringServiceId, ResolvedRequirements> get() {
		return registry;
	}

	@Reference(cardinality = ReferenceCardinality.MULTIPLE)
	public void add(ResolvedRequirements service) {
		registry.register(service);
	}

	public void remove(ResolvedRequirements service) {
		registry.unregister(service);
	}

}
