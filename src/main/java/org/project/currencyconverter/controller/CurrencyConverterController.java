package org.project.currencyconverter.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.project.currencyconverter.dto.CurrencyConversionResponseDTO;
import org.project.currencyconverter.dto.ExchangeRatesDTO;
import org.project.currencyconverter.exception.CurrencyConversionException;
import org.project.currencyconverter.exception.SupportedSymbolsException;
import org.project.currencyconverter.model.ExchangeRates;
import org.project.currencyconverter.util.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static org.project.currencyconverter.controller.ExchangeRatesController.EXCHANGE_API_URL;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.project.currencyconverter.util.GlobalConstantUtils.HTTP_GET;

@RestController
@RequestMapping("/v1/currency-converter/convert")
@Api(tags = {"Real-time Currency Conversion API"})
@Slf4j
public class CurrencyConverterController
{
    /*_________________________
    |  SWAGGER CONSTANTS  |
    |________________________|
    */
    public static final String CURRENCY_CONVERSION_DESCRIPTION = "This API provides the real-time conversion rates of the currency";
    protected static final int DECIMAL_DIGITS = 6;

    private static final String API_FETCH_TAG = "CONVERT_FETCH_API";
    private final RestTemplate restTemplate;
    private final String applicationUrl;
    private final ICurrencyService currencyService;


    @Autowired
    public CurrencyConverterController(
        RestTemplate restTemplate,
        @Value("${application.url:http://localhost:8092}") String applicationUrl,
        ICurrencyService currencyService)
    {
        this.applicationUrl = applicationUrl;
        this.restTemplate = restTemplate;
        this.currencyService = currencyService;
    }


    /**
     * Created the cache of supported currencies for validating the correct target/source currency.
     * https://exchangeratesapi.io/ -> reported that it is updated within 1 hour, cron scheduler time would be then same time or little after.
     *
     * @param sourceCurrency: base currency
     * @param targetCurrency: currency you wanted to convert to
     * @param monetaryValue:  the amount you wanted to convert
     * @return a JSON response which all above three values
     * @throws SupportedSymbolsException when some problem arises while fetching symbols API
     * @throws CurrencyConversionException when some problem arises while fetching convert API
     */
    @GetMapping
    @ApiOperation(value = CURRENCY_CONVERSION_DESCRIPTION, httpMethod = HTTP_GET, tags = {
        API_FETCH_TAG}, produces = CONTENT_TYPE_JSON, response = CurrencyConversionResponseDTO.class, notes = "Limited currently to fetch first language to determine " +
        "currency. Accept-Language header: https://datatracker.ietf" +
        ".org/doc/html/rfc2616#page-104")

    public CurrencyConversionResponseDTO getConvertRates(
        @RequestParam("sourceCurrency") String sourceCurrency,
        @RequestParam("targetCurrency") String targetCurrency,
        @RequestParam("monetaryValue") double monetaryValue,
        @RequestHeader(value = "accept-language", defaultValue = "en") String language
    ) throws SupportedSymbolsException,
             CurrencyConversionException
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        fetchAndValidateLatestSupportedCurrencies(sourceCurrency, targetCurrency);

        String url = applicationUrl
            + (EXCHANGE_API_URL)
            + ("?base=" + sourceCurrency)
            + ("&symbols=" + targetCurrency);
        ResponseEntity<ExchangeRatesDTO> fetchRates = restTemplate.getForEntity(url, ExchangeRatesDTO.class);

        Map<String, Double> currencyRates = fetchRates.getBody().getRates();
        Map.Entry<String, Double> targetCurrencyExchangeRates =
            currencyRates.entrySet().stream().filter(currency -> currency.getKey().equalsIgnoreCase(targetCurrency)).findFirst().get();

        Optional<Locale> locale = getLocale(language);
        if (!locale.isPresent())
        {
            throw new CurrencyConversionException(406, "No such locale supported yet");
        }

        double convertAmount = targetCurrencyExchangeRates.getValue() * monetaryValue;

        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale.get());
        numberFormat.setMaximumFractionDigits(DECIMAL_DIGITS);
        numberFormat.setMinimumFractionDigits(DECIMAL_DIGITS);

        stopWatch.stop();
        log.info("Total time taken by convert API: {} ms", stopWatch.getTotalTimeMillis());
        return CurrencyConversionResponseDTO.builder()
            .sourceCurrency(sourceCurrency)
            .targetCurrency(new ExchangeRates(targetCurrency, targetCurrencyExchangeRates.getValue()))
            .monetaryValue(monetaryValue)
            .convertedAmount(numberFormat.format(convertAmount))
            .build();

    }


    /**
     * limited to fetch first locale language only for now.
     *
     * @param acceptLanguage passed as a request header
     * @return first Locale language and region
     */
    private Optional<Locale> getLocale(String acceptLanguage) throws IllegalArgumentException
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


    private void fetchAndValidateLatestSupportedCurrencies(String sourceCurrency, String targetCurrency)
    {
        fetchAndUpdateCache();

        throwErrorIfCurrencyNotValid(sourceCurrency, targetCurrency);
    }


    private void throwErrorIfCurrencyNotValid(String sourceCurrency, String targetCurrency) throws CurrencyConversionException
    {
        Map<String, String> symbolList = CacheUtils.getCache();

        boolean doesExistSource = symbolList.keySet().stream().anyMatch(symbol -> symbol.equalsIgnoreCase(sourceCurrency));
        boolean doesExistTarget = symbolList.keySet().stream().anyMatch(symbol -> symbol.equalsIgnoreCase(targetCurrency));
        if (!doesExistSource || !doesExistTarget)
        {
            throw new CurrencyConversionException(400, "Either source currency or target currency is not supported by external API: https://exchangeratesapi.io/");
        }
    }


    private void fetchAndUpdateCache()
    {
        //fetch from cache first
        Map<String, String> symbolList = CacheUtils.getCache();

        //If not present in cache, then hit request and store in cache
        if (symbolList.isEmpty())
        {
            if (log.isInfoEnabled())
            {
                log.info("Invoking symbols API to update cache");
            }
            currencyService.updateCacheLatestSupportedCurrencies();
        }

        if (log.isInfoEnabled())
        {
            log.info("Supported currencies cache {}", symbolList);
        }
    }
}
