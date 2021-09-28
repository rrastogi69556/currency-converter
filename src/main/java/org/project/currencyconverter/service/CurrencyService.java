package org.project.currencyconverter.service;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.util.CacheUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.project.currencyconverter.controller.SupportedSymbolsController.SYMBOLS_API_URL;

@Service
@Slf4j
public class CurrencyService implements ICurrencyService
{

    private final String applicationUrl;
    private final RestTemplate restTemplate;


    @Autowired
    public CurrencyService(
        RestTemplate restTemplate,
        @Value("${application.url:http://localhost:8092}") String applicationUrl)
    {
        this.applicationUrl = applicationUrl;
        this.restTemplate = restTemplate;
    }


    /**
     * It retrieves the latest supported currencies by hitting external API
     *
     * @return the response with the payload and headers containing statusCode
     */
    @Override
    public ResponseEntity<SupportedSymbolsDTO> fetchLatestSupportedCurrencies()
    {
        log.info("Invoking symbols API");
        return restTemplate.getForEntity(applicationUrl + SYMBOLS_API_URL, SupportedSymbolsDTO.class);
    }


    /**
     * It updates the local cache which gets updated via cron scheduler time-to-time
     */
    @Override
    public void updateCacheLatestSupportedCurrencies()
    {
        updateCache(fetchLatestSupportedCurrencies());
    }


    private void updateCache(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsDTOResponseEntity)
    {
        if (nonNull(supportedSymbolsDTOResponseEntity.getBody()))
        {
            Map<String, String> supportedSymbols = supportedSymbolsDTOResponseEntity.getBody().getSymbols();
            updateCache(supportedSymbols);
            log.info("Supported Currencies cache updated");
        }
        log.warn("Supported Currencies cache not updated since response body was null");
    }


    private void updateCache(Map<String, String> supportedSymbols)
    {
        for (String key : supportedSymbols.keySet())
        {
            if (isNullKeyExists(supportedSymbols, key))
            {
                continue;
            }
            CacheUtils.getCache().putIfAbsent(key, supportedSymbols.get(key));
        }
    }


    public boolean isNullKeyExists(Map<String, String> cache, String key)
    {
        if (isNull(key) && log.isWarnEnabled())
        {
            log.warn("currency cannot be null. Key: [ {} ], value [ {} ]", null, cache.get(null));
            return true;
        }
        return false;
    }
}
