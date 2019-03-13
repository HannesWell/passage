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
package org.eclipse.passage.lic.jface.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.prefs.BackingStoreException;

public class LicensingColors {

	public static final String COLOR_LEVEL_OK = "COLOR_LEVEL_OK"; //$NON-NLS-1$
	public static final String COLOR_LEVEL_INFO = "COLOR_LEVEL_INFO"; //$NON-NLS-1$
	public static final String COLOR_LEVEL_WARN = "COLOR_LEVEL_WARN"; //$NON-NLS-1$
	public static final String COLOR_LEVEL_ERROR = "COLOR_LEVEL_ERROR"; //$NON-NLS-1$
	public static final String COLOR_LEVEL_FATAL = "COLOR_LEVEL_FATAL"; //$NON-NLS-1$

	public static final RGB RGB_LEVEL_OK = new RGB(0, 128, 0);
	public static final RGB RGB_LEVEL_INFO = new RGB(0, 128, 0);
	public static final RGB RGB_LEVEL_WARN = new RGB(255, 255, 0);
	public static final RGB RGB_LEVEL_ERROR = new RGB(255, 0, 0);
	public static final RGB RGB_LEVEL_FATAL = new RGB(128, 0, 0);

	public static final String BUNDLE_ID = "org.eclipse.passage.lic.jface"; //$NON-NLS-1$
	/**
	 * The Licensing color registry; <code>null</code> until lazily initialized.
	 */
	private static ColorRegistry colorRegistry = null;

	/**
	 * Returns the color in Licensing color registry with the given key, or
	 * <code>null</code> if none. Convenience method equivalent to
	 *
	 * <pre>
	 * LicensingColors.getColorRegistry().get(key)
	 * </pre>
	 *
	 * @param key the key
	 * @return the color, or <code>null</code> if none
	 */
	public static Color getColor(String key) {
		return getColorRegistry().get(key);
	}

	/**
	 * Returns the descriptor in Licensing color registry with the given key, or
	 * <code>null</code> if none. Convenience method equivalent to
	 *
	 * <pre>
	 * LicensingColors.getColorRegistry().getColorDescriptor(key)
	 * </pre>
	 *
	 * @param key the key
	 * @return the descriptor, or <code>null</code> if none
	 */
	public static ColorDescriptor getColorDescriptor(String key) {
		return getColorRegistry().getColorDescriptor(key);
	}

	/**
	 * Returns the color registry for Licensing.
	 * <p>
	 * Note that the static convenience methods <code>getColor</code> and
	 * <code>getColorDescriptor</code> are also provided on this class.
	 * </p>
	 * 
	 * @see LicensingColors#getColor(String)
	 * @see LicensingColors#getColorDescriptor(String)
	 * 
	 * @return the Licensing color registry
	 */
	public static ColorRegistry getColorRegistry() {
		if (colorRegistry == null) {
			colorRegistry = new ColorRegistry(Display.getCurrent());
			initializeDefaultColors();
			loadColors();
		}
		return colorRegistry;
	}

	/**
	 * Initialize default colors in Licensing color registry.
	 *
	 */
	private static void initializeDefaultColors() {
		Map<String, RGB> paths = new HashMap<>();
		paths.put(COLOR_LEVEL_OK, RGB_LEVEL_OK);
		paths.put(COLOR_LEVEL_INFO, RGB_LEVEL_INFO);
		paths.put(COLOR_LEVEL_WARN, RGB_LEVEL_WARN);
		paths.put(COLOR_LEVEL_ERROR, RGB_LEVEL_ERROR);
		paths.put(COLOR_LEVEL_FATAL, RGB_LEVEL_FATAL);

		declareColors(LicensingColors.class, paths);
	}

	/**
	 * Declares a Licensing colors for the key-> RGB {@link Map}. May only be called
	 * from a UI thread.
	 * 
	 * @param location the {@link Class} where the image is relative to
	 * @param rgbs     the the key-> RGB {@link Map}
	 * 
	 * @return the status of the operation
	 */
	public static IStatus declareColors(Class<?> location, Map<String, RGB> rgbs) {
		ColorRegistry registry = getColorRegistry();
		Set<Entry<String, RGB>> entries = rgbs.entrySet();
		for (Entry<String, RGB> entry : entries) {
			String key = entry.getKey();
			RGB rgb = entry.getValue();
			registry.put(key, rgb);
		}
		return Status.OK_STATUS;
	}

	public static IStatus storeColors() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(BUNDLE_ID);
		if (node == null) {
			return Status.CANCEL_STATUS;
		}
		ColorRegistry registry = getColorRegistry();
		for (String colorKey : registry.getKeySet()) {
			RGB rgb = registry.getRGB(colorKey);
			node.put(colorKey, rgb.toString());
		}
		try {
			node.flush();
		} catch (BackingStoreException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	public static IStatus loadColors() {
		IEclipsePreferences node = ConfigurationScope.INSTANCE.getNode(BUNDLE_ID);
		if (node == null) {
			return Status.CANCEL_STATUS;
		}
		ColorRegistry registry = getColorRegistry();
		for (String colorKey : registry.getKeySet()) {
			String storedValue = node.get(colorKey, "");
			if (storedValue != null && !storedValue.isEmpty()) {
				registry.put(colorKey, stringToRgb(storedValue));
			}
		}
		return Status.OK_STATUS;
	}

	private static RGB stringToRgb(String storedValue) {
		String pattern = "\\d{1,3}";
		Matcher matcher = Pattern.compile(pattern).matcher(storedValue);
		int red = 0, green = 0, blue = 0;
		if (matcher.find()) {
			red = Integer.valueOf(matcher.group(0));
		}
		if (matcher.find()) {
			green = Integer.valueOf(matcher.group(0));
		}
		if (matcher.find()) {
			blue = Integer.valueOf(matcher.group(0));
		}
		return new RGB(red, green, blue);
	}

	public static IStatus acceptColors(Map<String, RGB> colors) {
		ColorRegistry registry = getColorRegistry();
		for (Entry<String, RGB> color : colors.entrySet()) {
			registry.put(color.getKey(), color.getValue());
		}
		if (!colors.isEmpty()) {
			return storeColors();
		}

		return Status.OK_STATUS;
	}

}
