package org.project.currencyconverter.abstraction;

import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.springframework.http.ResponseEntity;

public interface ICurrencyService
{
    /**
     * It retrieves the latest supported currencies by hitting external API with some added headers to save network bandwidth
     * @param eTag returned by the server upon first request. From then onwards, only then the content be fetched when there is an eTag change
     * @param ifModifiedSince returned by the server upon first request. Used {@code If-Modified-Since} and {@code If-None-Match} to validate the last content header
     * @return the response with the payload and headers containing statusCode
     */
    ResponseEntity<SupportedSymbolsDTO> fetchLatestSupportedCurrencies(String eTag, long ifModifiedSince );

    /**
     * It updates the header details(Etag and Date) in the local cache for the subsequent requests to save network bandwidth.
     * @param supportedSymbolsEntity response entity received from the server to update cache if something added/modified.
     */
    void updateETagAndDateCache(ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity);
}
