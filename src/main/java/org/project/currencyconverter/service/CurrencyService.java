package org.project.currencyconverter.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.project.currencyconverter.builder.UrlBuilder;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.nonNull;
import static org.project.currencyconverter.util.CacheUtils.getCache;
import static org.project.currencyconverter.util.GlobalConstantUtils.ENCODED_ACCESS_KEY;
import static org.project.currencyconverter.util.SecurityUtils.decodeKey;
import static org.springframework.http.HttpHeaders.DATE;
import static org.springframework.http.HttpHeaders.ETAG;

@Service
@Slf4j
public class CurrencyService implements ICurrencyService, InitializingBean
{
    /*_______________________
    | Service Constants      |
    |________________________|
    */
    private static final String DECODED_KEY = decodeKey(ENCODED_ACCESS_KEY);

    /*_________________________
     | API URL MAPPINGS   |
     |________________________|
   */
    public static final String SYMBOLS_API_URL = "/v1/currency-converter/symbols";


    private final RestTemplate restTemplate;
    private final String applicationUrl;
    private String symbolApiUrl = "";

    @Autowired
    public CurrencyService(@Value("${application.url:http://localhost:8092}") String applicationUrl,
        RestTemplate restTemplate
    )
    {
        this.applicationUrl = applicationUrl;
        this.restTemplate = restTemplate;
    }


    /**
     * It retrieves the latest supported currencies by hitting external API with some added headers to save network bandwidth
     * @param eTag returned by the server upon first request. From then onwards, only then the content be fetched when there is an eTag change
     * @param ifModifiedSince returned by the server upon first request. Used {@code If-Modified-Since} and {@code If-None-Match} to validate the last content header
     * @return the response with the payload and headers containing statusCode
     */
    @Override
    public ResponseEntity<SupportedSymbolsDTO> fetchLatestSupportedCurrencies(String eTag, long ifModifiedSince )
    {
        log.info("Invoking symbols API");
        if(nonNull(eTag) && ifModifiedSince > 0)
        {
            HttpHeaders headers = new HttpHeaders();
            headers.setIfNoneMatch(eTag);
            headers.setIfModifiedSince(ifModifiedSince);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return restTemplate.exchange(symbolApiUrl, HttpMethod.GET, entity, SupportedSymbolsDTO.class);
        }
        // if no eTag, then, can fetch directly
        return restTemplate.getForEntity(symbolApiUrl, SupportedSymbolsDTO.class);
    }


    /**
     * It updates the header details(Etag and Date) in the local cache for the subsequent requests to save network bandwidth.
     * @param supportedSymbolsEntity response entity received from the server to update cache if something added/modified.
     */
    @Override
    public void updateETagAndDateCache(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity)
    {
        updateCacheIfModified(supportedSymbolsEntity);
    }


    private void updateCacheIfModified(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity)
    {
        if(isRequestModified(supportedSymbolsEntity)) {
            log.info("contents got modified in the server");
            updateHeaders(supportedSymbolsEntity);
            updateContent(supportedSymbolsEntity);
        }

    }

    private void updateHeaders(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity)
    {
        getCache().put(ETAG, supportedSymbolsEntity.getHeaders().getETag());
        getCache().put(DATE, String.valueOf(supportedSymbolsEntity.getHeaders().getDate()));
        log.info("Cache headers updated");
    }

    private boolean isRequestModified(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity)
    {
        return supportedSymbolsEntity.getHeaders().containsKey(ETAG)
            && (!getCache().containsKey(ETAG) || !supportedSymbolsEntity.getHeaders().get(ETAG).get(0).equals(getCache().get(ETAG)));

    }

    private void updateContent(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsDTOResponseEntity)
    {
        if (nonNull(supportedSymbolsDTOResponseEntity.getBody()))
        {
            Map<String, String> supportedSymbols = supportedSymbolsDTOResponseEntity.getBody().getSymbols();
            updateCache(supportedSymbols);
            log.info("Supported Currencies cache updated");
        } else
        {
            log.warn("Supported Currencies cache not updated since response body was null");
        }
    }


    private void updateCache(Map<String, String> supportedSymbols)
    {
        for (String key : supportedSymbols.keySet())
        {
            getCache().putIfAbsent(key, supportedSymbols.get(key));
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception
    {
        symbolApiUrl = UrlBuilder.builder()
            .withBaseUrl(applicationUrl)
            .withPathParameter(SYMBOLS_API_URL)
            .withRequestParameter1("access_key", DECODED_KEY)
            .build();
    }
}
