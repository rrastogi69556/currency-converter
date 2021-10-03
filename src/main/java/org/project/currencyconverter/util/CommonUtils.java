package org.project.currencyconverter.util;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public final class CommonUtils
{
    private CommonUtils(){}

    /**
     * Fetches locale language passed by the user in the front-end.
     *
     * @param acceptLanguage passed as a request header
     * @return first Locale language and region
     */
    public static Optional<Locale> getLocale(String acceptLanguage) throws IllegalArgumentException
    {
        final List<Locale> acceptedLocales = new ArrayList<>();
        if (acceptLanguage != null)
        {
            final List<Locale.LanguageRange> ranges = Locale.LanguageRange.parse(acceptLanguage);

            if (!ranges.isEmpty())
            {
                ranges.forEach(languageRange -> {
                    final String localeString = languageRange.getRange();
                    final Locale locale = Locale.forLanguageTag(localeString);
                    Optional<Locale> localeOptional = Arrays.stream(Locale.getAvailableLocales())
                        .filter(individual -> individual.getLanguage().equalsIgnoreCase(locale.getLanguage()))
                        .findFirst();
                    localeOptional.ifPresent(acceptedLocales::add);
                });
            }
        }
        return acceptedLocales.isEmpty() ? Optional.empty() : Optional.of(acceptedLocales.get(0));
    }

    /**
     * This method maps the error codes and their description provided by: <a href="https://exchangeratesapi.io/documentation/">https://exchangeratesapi.io/documentation</a>
     */
    public static final ImmutableMap<Integer, String> errorCodeToDescription = ImmutableMap.<Integer,String>builder()
        .put(401, "You have not supplied a valid API Access Key.")
        .put(404, "The requested resource does not exist.")
        .put(429, "Your monthly usage limit has been reached. Please upgrade your Subscription Plan.")
        .put(101, "No API Key was specified or an invalid API Key was specified.")
        .put(103, "The requested API endpoint does not exist.")
        .put(104, "The maximum allowed API amount of monthly API requests has been reached.")
        .put(105, "The current subscription plan does not support this API endpoint.")
        .put(106, "The current request did not return any results.")
        .put(102, "The account this API request is coming from is inactive.")
        .put(201, "An invalid base currency has been entered.")
        .put(202, "One or more invalid symbols have been specified.")
        .put(301, "No date has been specified.")
        .put(302, "An invalid date has been specified.")
        .put(403, "No or an invalid amount has been specified.")
        .put(501, "No or an invalid timeframe has been specified.")
        .put(502, "No or an invalid \"start_date\" has been specified.")
        .put(503, "No or an invalid \"end_date\" has been specified.")
        .put(504, "An invalid timeframe has been specified.")
        .put(505, "The specified timeframe is too long, exceeding 365 days.")
        .build();
}
