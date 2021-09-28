package org.project.currencyconverter.service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.util.CacheUtils;
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
    public void testFetchLatestSupportedCurrencies() {

        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        HashMap<String,String> symbols = new HashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");

        supportedSymbolsDTO.setSymbols(symbols);

        when(restTemplate.getForEntity(anyString(), eq(SupportedSymbolsDTO.class))).thenReturn(ResponseEntity.of(Optional.of(supportedSymbolsDTO)));

        ResponseEntity<SupportedSymbolsDTO> responseEntity = currencyService.fetchLatestSupportedCurrencies();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isEqualTo(true);

    }

    @Test
    public void testUpdateCacheLatestSupportedCurrencies() {
        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        LinkedHashMap<String,String> symbols = new LinkedHashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        symbols.put(null, "Australian Dollar");
        supportedSymbolsDTO.setSymbols(symbols);

        CacheUtils.getCache().clear();

        when(restTemplate.getForEntity(anyString(), eq(SupportedSymbolsDTO.class))).thenReturn(ResponseEntity.of(Optional.of(supportedSymbolsDTO)));

        currencyService.updateCacheLatestSupportedCurrencies();

        assertThat(CacheUtils.getCache()).isNotEmpty();
        assertThat(CacheUtils.getCache().size()).isEqualTo(2);

    }


    @Test
    public void testNullKeyInCache() {
        CacheUtils.getCache().put("USD", "United Stated Dollar");
        CacheUtils.getCache().put("EUR", "European Euro");
        CacheUtils.getCache().put(null, "Australian Dollar");

        boolean doesNullExist = currencyService.isNullKeyExists(CacheUtils.getCache(), null);

        assertThat(doesNullExist).isEqualTo(true);

    }
}
