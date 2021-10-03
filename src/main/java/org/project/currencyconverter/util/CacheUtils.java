package org.project.currencyconverter.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the generic cache holder for the application of the supported currencies
 */
public final class CacheUtils
{
    private CacheUtils() {}

    // Thread-safe
    private static final Map<String, String> cache = new ConcurrentHashMap<>();


    public static Map<String, String> getCache()
    {
        return cache;
    }
}
