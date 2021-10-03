package org.project.currencyconverter.builder;


public class UrlBuilder
{
    protected static final String URL_PARAMETER_DELIMITER = "&";
    protected static final String URL_PARAMETER_EQUAL_OPERATOR = "=";
    protected static final String URL_PARAMETER_FIRST_DELIMITER = "?";
    protected static final String EMPTY = "";
    // Using initial size reduces the re-allocation of copying the whole string to new one. This is relatively more soothing to memory. 130 is roughly :
    // http://localhost:8092/v1/currency-converter/convert?sourceCurrency=EUR&targetCurrency=INR&monetaryValue=12345678901234567890
    @SuppressWarnings("PMD")
    private final StringBuilder url = new StringBuilder(130);

    public UrlBuilder withBaseUrl(String value) {
        url.append(value);
        return this;
    }

    public UrlBuilder withRequestParameter1(String key, String value) {
        url.append(EMPTY
            .concat(URL_PARAMETER_FIRST_DELIMITER)
            .concat(key)
            .concat(URL_PARAMETER_EQUAL_OPERATOR)
            .concat(value));
        return this;
    }

    public UrlBuilder withOtherRequestParameter(String key, Object value) {
        url.append(EMPTY
            .concat(URL_PARAMETER_DELIMITER)
            .concat(key)
            .concat(URL_PARAMETER_EQUAL_OPERATOR)
            .concat(String.valueOf(value)));
        return this;
    }

    public UrlBuilder withPathParameter(String value) {
        url.append(value);
        return this;
    }

    public String build() {
        return url.toString();
    }

    public static UrlBuilder builder() {
        return new UrlBuilder();
    }
}
