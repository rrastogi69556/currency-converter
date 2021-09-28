package org.project.currencyconverter.abstraction;

import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.springframework.http.ResponseEntity;

public interface ICurrencyService
{
    /**
     * It retrieves the latest supported currencies by hitting external API
     * @return the response with the payload and headers containing statusCode
     */
    ResponseEntity<SupportedSymbolsDTO>  fetchLatestSupportedCurrencies();

    /**
     * It updates the local cache which gets updated via cron scheduler time-to-time
     */
    void updateCacheLatestSupportedCurrencies();
}
