package org.project.currencyconverter.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.util.CacheUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CurrencyServiceTest
{
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    @DisplayName("given when ETag and Date is provided," +
        " when Symbols API is invoked," +
        "then API returns NOT_MODIFIED response")
    public void testFetchLatestSupportedCurrencies() {

        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        HashMap<String,String> symbols = new HashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        supportedSymbolsDTO.setSymbols(symbols);

        HttpHeaders headers = new HttpHeaders();
        headers.setIfNoneMatch("22342423423");
        headers.setIfModifiedSince(2332423432423L);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), eq(entity), eq(SupportedSymbolsDTO.class))).thenReturn(ResponseEntity.status(HttpStatus.NOT_MODIFIED).headers(headers).body(supportedSymbolsDTO));

        ResponseEntity<SupportedSymbolsDTO> responseEntity = currencyService.fetchLatestSupportedCurrencies("22342423423", 2332423432423L);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(304);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isEqualTo(true);

    }

    @Test
    @DisplayName("given ETag and Date is provided in the header with If-None-Match header and the content modified," +
        "when update cache is invoked," +
        "then cache is updated")
    public void testUpdateCacheLatestSupportedCurrencies() {
        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        LinkedHashMap<String,String> symbols = new LinkedHashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        supportedSymbolsDTO.setSymbols(symbols);

        CacheUtils.getCache().clear();

        HttpHeaders headers = new HttpHeaders();
        headers.setIfNoneMatch("\"22342423423\"");
        headers.setIfModifiedSince(2332423432423L);
        headers.setETag("\"22342423423\"");

        ResponseEntity<SupportedSymbolsDTO> supportedSymbolsEntity = ResponseEntity.status(HttpStatus.OK).headers(headers).body(supportedSymbolsDTO);

        currencyService.updateETagAndDateCache(supportedSymbolsEntity);

        assertThat(CacheUtils.getCache()).isNotEmpty();
        assertThat(CacheUtils.getCache().size()).isEqualTo(4);

    }
}
