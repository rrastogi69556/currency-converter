package org.project.currencyconverter.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is the generic cache holder for the application of the supported currencies
 */
public final class CacheUtils
{
    private CacheUtils() {}

    private static final Map<String, String> cache = new LinkedHashMap<>();

    public static Map<String, String> getCache()
    {
        return cache;
    }
}
