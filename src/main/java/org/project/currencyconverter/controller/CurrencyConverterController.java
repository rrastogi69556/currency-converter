package org.project.currencyconverter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.project.currencyconverter.builder.UrlBuilder;
import org.project.currencyconverter.dto.CurrencyConversionRequestDTO;
import org.project.currencyconverter.dto.CurrencyConversionResponseDTO;
import org.project.currencyconverter.dto.ExchangeRatesDTO;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.exception.CurrencyConversionException;
import org.project.currencyconverter.exception.ExchangeRatesException;
import org.project.currencyconverter.exception.SupportedSymbolsException;
import org.project.currencyconverter.model.ExchangeRates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.nonNull;
import static org.project.currencyconverter.util.CacheUtils.getCache;
import static org.project.currencyconverter.util.CommonUtils.errorCodeToDescription;
import static org.project.currencyconverter.util.CommonUtils.getLocale;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.project.currencyconverter.util.GlobalConstantUtils.ENCODED_ACCESS_KEY;
import static org.project.currencyconverter.util.GlobalConstantUtils.HTTP_GET;
import static org.project.currencyconverter.util.SecurityUtils.decodeKey;
import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.HttpHeaders.ETAG;

/**
 * This class is the controller for converting the source to target currency.
 * It also validates the basic input parameters like source, target currencies among other validations
 * All exceptions are formatted and returned in json format in {@link org.project.currencyconverter.exception.ApiErrorHandler}
 */
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
    private static final String API_FETCH_TAG = "CONVERT_FETCH_API";
    /*_______________________
    | Controller Constants   |
    |________________________|
    */
    protected static final int DECIMAL_DIGITS = 6;
    private static final String DECODED_KEY = decodeKey(ENCODED_ACCESS_KEY);

    private final RestTemplate restTemplate;
    private final String exchangeUrl;
    private final ICurrencyService currencyService;
    private final ObjectMapper jsonMapper;


    @Autowired
    public CurrencyConverterController(
        RestTemplate restTemplate,
        ObjectMapper jsonMapper,
        @Value("${external.endpoint.currency.exchange}") String exchangeUrl,
        ICurrencyService currencyService)
    {
        this.exchangeUrl = exchangeUrl;
        this.restTemplate = restTemplate;
        this.currencyService = currencyService;
        this.jsonMapper = jsonMapper;
    }


    /**
     * Created the cache of supported currencies for validating the correct target/source currency. It uses {@code Etag} and {@code Date} in response header to know if the
     * content changed in the symbols API which saves N/W bandwidth. It also make use of {@code If-None-Match} and {@code If-Modified-Since} header to determine the content
     * changed. The external API:  @see <a href="https://exchangeratesapi.io/documentation/">https://exchangeratesapi.io/documentation</a>
     *
     * @param currencyConversionRequestDTO contains sourceCurrency, targetCurrency and monetaryValue to convert
     * @param language regional subset to be passed to view currency in local regional format.
     * @return a JSON response which all above three values
     * @throws SupportedSymbolsException when some problem arises while fetching symbols API
     * @throws CurrencyConversionException when some problem arises while fetching convert API
     * @throws RestClientException when some problem arises in calling APIs
     */
    @GetMapping
    @ApiOperation(value = CURRENCY_CONVERSION_DESCRIPTION, httpMethod = HTTP_GET, tags = {
        API_FETCH_TAG}, produces = CONTENT_TYPE_JSON, response = CurrencyConversionResponseDTO.class, notes = "Limited currently to fetch first language to determine " +
        "currency. Accept-Language header: https://datatracker.ietf" +
        ".org/doc/html/rfc2616#page-104")
    @CrossOrigin(origins="http://localhost:8092", allowedHeaders = "*")
    public CurrencyConversionResponseDTO getConvertRates(
          CurrencyConversionRequestDTO currencyConversionRequestDTO,
        @RequestHeader(value = "accept-language", defaultValue = "en") String language
    ) throws SupportedSymbolsException,
             CurrencyConversionException, ExchangeRatesException, RestClientException, JsonProcessingException
    {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        fetchAndValidateLatestSupportedCurrencies(currencyConversionRequestDTO.getSourceCurrency(), currencyConversionRequestDTO.getTargetCurrency());

        String url = UrlBuilder.builder()
            .withBaseUrl(exchangeUrl)
            .withRequestParameter1("access_key", DECODED_KEY)
            .withOtherRequestParameter("base", currencyConversionRequestDTO.getSourceCurrency())
            .withOtherRequestParameter("symbols", currencyConversionRequestDTO.getTargetCurrency())
            .build();

        ResponseEntity<ExchangeRatesDTO> fetchRates = getExchangeRatesDTOResponseEntity(url);

        Map.Entry<String, BigDecimal> targetCurrencyExchangeRates = getExchangeRate(currencyConversionRequestDTO, fetchRates);

        Locale locale = validateAndGetLocaleIfPresent(language);

        BigDecimal convertAmount = targetCurrencyExchangeRates.getValue().multiply(currencyConversionRequestDTO.getMonetaryValue()).setScale(DECIMAL_DIGITS, RoundingMode.HALF_UP);

        String convertedAmount = getConvertedAmountWithI8ln(locale, convertAmount);

        stopWatch.stop();
        log.info("Total time taken by convert API: {} ms", stopWatch.getTotalTimeMillis());
        return CurrencyConversionResponseDTO.builder()
            .sourceCurrency(currencyConversionRequestDTO.getSourceCurrency())
            .targetCurrency(new ExchangeRates(currencyConversionRequestDTO.getTargetCurrency(), targetCurrencyExchangeRates.getValue()))
            .monetaryValue(currencyConversionRequestDTO.getMonetaryValue())
            .convertedAmount(convertedAmount)
            .build();

    }


    private Map.Entry<String, BigDecimal> getExchangeRate(CurrencyConversionRequestDTO currencyConversionRequestDTO, ResponseEntity<ExchangeRatesDTO> fetchRates)
    {
        Map<String, BigDecimal> currencyRates = Objects.requireNonNull(fetchRates.getBody()).getRates();
        Optional<Map.Entry<String, BigDecimal>> targetCurrencyIfPresent = currencyRates.entrySet().stream().filter(currency -> currency.getKey().equalsIgnoreCase(currencyConversionRequestDTO.getTargetCurrency())).findFirst();
        return targetCurrencyIfPresent.orElseThrow(() -> new ExchangeRatesException(202, errorCodeToDescription.get(202)));
    }


    private ResponseEntity<ExchangeRatesDTO> getExchangeRatesDTOResponseEntity(String url) throws ExchangeRatesException, JsonProcessingException
    {
        ResponseEntity<String> fetchRates = restTemplate.getForEntity(url, String.class);

        if(errorCodeToDescription.containsKey(fetchRates.getStatusCodeValue())) {
            throw new ExchangeRatesException(fetchRates.getStatusCodeValue(), errorCodeToDescription.get(fetchRates.getStatusCodeValue()));
        }
        return ResponseEntity
            .status(fetchRates.getStatusCode())
            .body(jsonMapper.readValue(fetchRates.getBody(), ExchangeRatesDTO.class));
    }


    private Locale validateAndGetLocaleIfPresent(String language) throws CurrencyConversionException
    {
        Optional<Locale> locale = getLocale(language);
        if (!locale.isPresent())
        {
            throw new CurrencyConversionException(406, String.format("No such Locale %s supported yet", language));
        }
        return locale.get();
    }


    private String getConvertedAmountWithI8ln(Locale locale, BigDecimal convertAmount)
    {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        numberFormat.setMaximumFractionDigits(DECIMAL_DIGITS);
        numberFormat.setMinimumFractionDigits(DECIMAL_DIGITS);
        return numberFormat.format(convertAmount);
    }


    private void fetchAndValidateLatestSupportedCurrencies(String sourceCurrency, String targetCurrency)
    {
        // for the first call, we don't have this information so default value.
        String eTag = null;
        long date = 0;

        if(getCache().containsKey(ETAG)) {
            eTag = getCache().get(ETAG);
        }
        if(getCache().containsKey(DATE)) {
            date = Long.parseLong(getCache().get(DATE));
        }
        ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity = currencyService.fetchLatestSupportedCurrencies(eTag, date);

        currencyService.updateETagAndDateCache(supportedSymbolsEntity);

        Map<String, String> cacheMap = nonNull(supportedSymbolsEntity) && nonNull(supportedSymbolsEntity.getBody()) ? supportedSymbolsEntity.getBody().getSymbols() : getCache();

        throwErrorIfCurrencyNotValid(sourceCurrency, targetCurrency, cacheMap);
    }


    private void throwErrorIfCurrencyNotValid(String sourceCurrency, String targetCurrency, Map<String, String> symbolList) throws CurrencyConversionException
    {
        boolean doesExistSource = symbolList.keySet().stream().anyMatch(symbol -> symbol.equalsIgnoreCase(sourceCurrency));
        boolean doesExistTarget = symbolList.keySet().stream().anyMatch(symbol -> symbol.equalsIgnoreCase(targetCurrency));
        if (!doesExistSource || !doesExistTarget)
        {
            throw new CurrencyConversionException(400, "Either source currency or target currency is not supported");
        }
    }
}
