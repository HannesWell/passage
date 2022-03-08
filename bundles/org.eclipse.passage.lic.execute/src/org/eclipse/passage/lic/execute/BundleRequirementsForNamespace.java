package org.eclipse.passage.lic.execute;

import org.eclipse.passage.lic.api.registry.Registry;
import org.eclipse.passage.lic.api.registry.StringServiceId;
import org.eclipse.passage.lic.api.requirements.Requirement;
import org.eclipse.passage.lic.api.requirements.ResolvedRequirements;
import org.eclipse.passage.lic.api.requirements.ResolvedRequirementsRegistry;
import org.eclipse.passage.lic.base.registry.ReadOnlyRegistry;
import org.eclipse.passage.lic.equinox.requirements.BundleRequirements;
import org.eclipse.passage.lic.internal.base.requirements.FilteredRequirements;

@SuppressWarnings("restriction")
final class BundleRequirementsForNamespace implements ResolvedRequirementsRegistry {

	private final String namespace;
	private final Registry<StringServiceId, ResolvedRequirements> delegate;

	BundleRequirementsForNamespace(String namespace) {
		this.namespace = namespace;
		this.delegate = new ReadOnlyRegistry<>(//
				new FilteredRequirements(//
						new BundleRequirements(), //
						this::declaredInNamespace//
				));
	}

	@Override
	public Registry<StringServiceId, ResolvedRequirements> get() {
		return delegate;
	}

	private boolean declaredInNamespace(Requirement requirement) {
		return requirement.feature().identifier().startsWith(namespace);
	}

}
