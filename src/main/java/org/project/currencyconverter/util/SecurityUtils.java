package org.project.currencyconverter.util;

import java.util.Base64;

/**
 * This class provides the decoded key to be used internally by the application
 */
public final class SecurityUtils
{
    private SecurityUtils() {}

    public static String decodeKey(String encodedKey) {
        return new String(Base64.getDecoder().decode(encodedKey));
    }
}