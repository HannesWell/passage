package org.eclipse.passage.lic.api.tests.fakes;

import org.eclipse.passage.lic.internal.api.requirements.Feature;

@SuppressWarnings("restriction")
public final class FakeFeature implements Feature {

	private final String identity;

	public FakeFeature(String identity) {
		this.identity = identity;
	}

	public FakeFeature() {
		this(Long.toHexString(System.currentTimeMillis()));
	}

	@Override
	public String name() {
		return String.format("Fake feature %s ", identity); //$NON-NLS-1$
	}

	@Override
	public String version() {
		return "0.0.7"; //$NON-NLS-1$
	}

	@Override
	public String identifier() {
		return String.format("fake_%s ", identity); //$NON-NLS-1$
	}

	@Override
	public String provider() {
		return "API tests"; //$NON-NLS-1$
	}

}
