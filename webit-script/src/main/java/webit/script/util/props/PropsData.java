// Copyright (c) 2003-2013, Jodd Team (jodd.org). All Rights Reserved.
package webit.script.util.props;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import webit.script.util.StringUtil;

/**
 * Props data storage for base and profile properties. Properties can be
 * lookuped and modified only through this class.
 */
class PropsData implements Cloneable {

    private static final int MAX_INNER_MACROS = 100;
    private static final String APPEND_SEPARATOR = ",";
    private final HashMap<String, PropsValue> baseProperties;
    private final HashMap<String, Map<String, PropsValue>> profileProperties;
    /**
     * If set, duplicate props will be appended to the end, separated by comma.
     */
    private final static boolean appendDuplicateProps = false;
//	/**
//	 * When set, missing macros will be replaces with an empty string.
//	 */
//	private boolean ignoreMissingMacros;
    private final static boolean skipEmptyProps = true;

    PropsData() {
        this.baseProperties = new HashMap<String, PropsValue>();
        this.profileProperties = new HashMap<String, Map<String, PropsValue>>();
    }

//	protected PropsData(final HashMap<String, PropsValue> properties, final HashMap<String, Map<String, PropsValue>> profiles) {
//		this.baseProperties = properties;
//		this.profileProperties = profiles;
//	}
//
//	@Override
//	public PropsData clone() {
//		final HashMap<String, PropsValue> newBase = new HashMap<String, PropsValue>();
//		final HashMap<String, Map<String, PropsValue>> newProfiles = new HashMap<String, Map<String, PropsValue>>();
//
//		newBase.putAll(baseProperties);
//		for (final Map.Entry<String, Map<String, PropsValue>> entry : profileProperties.entrySet()) {
//			final Map<String, PropsValue> map = new HashMap<String, PropsValue>(entry.getValue().size());
//			map.putAll(entry.getValue());
//			newProfiles.put(entry.getKey(), map);
//		}
//
//		final PropsData pd = new PropsData(newBase, newProfiles);
//		pd.appendDuplicateProps = appendDuplicateProps;
//		pd.ignoreMissingMacros = ignoreMissingMacros;
//		pd.skipEmptyProps = skipEmptyProps;
//		return pd;
//	}
    // ---------------------------------------------------------------- misc
    /**
     * Puts key-value pair into the map, with respect of appending duplicate
     * properties
     */
    private void put(final Map<String, PropsValue> map, final String key, final String value, final boolean append) {
        String realValue = value;
        if (append || appendDuplicateProps) {
            PropsValue pv = map.get(key);
            if (pv != null) {
                realValue = StringUtil.concat(pv.value, APPEND_SEPARATOR, realValue);
            }
        }
        map.put(key, new PropsValue(realValue));
    }

    // ---------------------------------------------------------------- properties
//    /**
//     * Counts base properties.
//     */
//    public int countBaseProperties() {
//        return baseProperties.size();
//    }
    /**
     * Adds base property.
     */
    void putBaseProperty(final String key, final String value, final boolean append) {
        put(baseProperties, key, value, append);
    }

    /**
     * Returns base property or
     * <code>null</code> if it doesn't exist.
     */
    PropsValue getBaseProperty(final String key) {
        return baseProperties.get(key);
    }

    // ---------------------------------------------------------------- profiles
//	/**
//	 * Counts profile properties. Note: this method is not
//	 * that easy on execution.
//	 */
//	public int countProfileProperties() {
//		final HashSet<String> profileKeys = new HashSet<String>();
//
//		for (final Map<String, PropsValue> map : profileProperties.values()) {
//			for (final String key : map.keySet()) {
//				if (!baseProperties.containsKey(key)) {
//					profileKeys.add(key);
//				}
//			}
//		}
//		return profileKeys.size();
//	}
    /**
     * Adds profile property.
     */
    void putProfileProperty(final String key, final String value, final String profile, final boolean append) {
        Map<String, PropsValue> map = profileProperties.get(profile);
        if (map == null) {
            map = new HashMap<String, PropsValue>();
            profileProperties.put(profile, map);
        }
        put(map, key, value, append);
    }

//	/**
//	 * Returns profile property.
//	 */
//	public PropsValue getProfileProperty(final String profile, final String key) {
//		final Map<String, PropsValue> profileMap = profileProperties.get(profile);
//		if (profileMap == null) {
//			return null;
//		}
//		return profileMap.get(key);
//	}
    // ---------------------------------------------------------------- lookup
    /**
     * Lookup props value through profiles and base properties.
     */
    private String lookupValue(final String key, final String... profiles) {
        if (profiles != null) {
            nextprofile:
            for (String profile : profiles) {
                while (true) {
                    final Map<String, PropsValue> profileMap = this.profileProperties.get(profile);
                    if (profileMap == null) {
                        continue nextprofile;
                    }
                    final PropsValue value = profileMap.get(key);
                    if (value != null) {
                        return value.getValue();
                    }
                    final int ndx = profile.lastIndexOf('.');
                    if (ndx == -1) {
                        break;
                    }
                    profile = profile.substring(0, ndx);
                }
            }
        }
        final PropsValue value = getBaseProperty(key);
        return value == null ? null : value.getValue();
    }

    // ---------------------------------------------------------------- resolve
    /**
     * Resolves all macros in this props set. Called once on initialization.
     */
    void resolveMacros() {
        // create string template pareser that will be used internally
        //StringTemplateParser stringTemplateParser = new StringTemplateParser();
        //stringTemplateParser.setResolveEscapes(false);

//		if (!ignoreMissingMacros) {
//			stringTemplateParser.setReplaceMissingKey(false);
//		} else {
//			stringTemplateParser.setReplaceMissingKey(true);
//			stringTemplateParser.setMissingKeyReplacement("");
//		}

        // start parsing
        int loopCount = 0;
        while (loopCount++ < MAX_INNER_MACROS) {
            boolean replaced = resolveMacros(this.baseProperties, null);

            for (final Map.Entry<String, Map<String, PropsValue>> entry : profileProperties.entrySet()) {
                String profile = entry.getKey();

                replaced = resolveMacros(entry.getValue(), profile) || replaced;
            }

            if (!replaced) {
                break;
            }
        }
    }

    private boolean resolveMacros(final Map<String, PropsValue> map, final String profile) {
        boolean replaced = false;

        final StringTemplateParser.MacroResolver macroResolver = new StringTemplateParser.MacroResolver() {
            public String resolve(String macroName) {
                return lookupValue(macroName, profile);
            }
        };

        Iterator<Map.Entry<String, PropsValue>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, PropsValue> entry = iterator.next();
            final PropsValue pv = entry.getValue();
            final String newValue = StringTemplateParser.parse(pv.value, macroResolver);

            if (!newValue.equals(pv.value)) {
                if (skipEmptyProps) {
                    if (newValue.length() == 0) {
                        iterator.remove();
                        replaced = true;
                        continue;
                    }
                }

                pv.resolved = newValue;
                replaced = true;
            } else {
                pv.resolved = null;
            }
        }
        return replaced;
    }

    // ---------------------------------------------------------------- extract
    /**
     * Extract props to target map.
     */
    void extract(final Map target, final String[] profiles) {
        if (profiles != null) {
            for (String profile : profiles) {
                while (true) {
                    final Map<String, PropsValue> map = this.profileProperties.get(profile);
                    if (map != null) {
                        extractMap(target, map);
                    }

                    final int ndx = profile.indexOf('.');
                    if (ndx == -1) {
                        break;
                    }
                    profile = profile.substring(0, ndx);
                }
            }
        }
        extractMap(target, this.baseProperties);
    }

    @SuppressWarnings("unchecked")
    private void extractMap(final Map target, final Map<String, PropsValue> map) {
        for (Map.Entry<String, PropsValue> entry : map.entrySet()) {
            final String key = entry.getKey();

            if (!target.containsKey(key)) {
                target.put(key, entry.getValue().getValue());
            }
        }
    }
}
