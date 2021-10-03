package org.project.currencyconverter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.project.currencyconverter.dto.ExchangeRatesDTO;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.util.CacheUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class CurrencyConverterControllerTest
{
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ICurrencyService currencyService;

    @Mock
    private ObjectMapper jsonMapper;

    @InjectMocks
    private CurrencyConverterController currencyConverterController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(currencyConverterController)
            .build();
    }

    @Test
    @DisplayName("Given valid source,target currencies and monetary value is provided  " +
        "when convert API is invoked" +
        "then ok response with convertedAmount is returned")
    public void testGetConvertRates() throws Exception
    {
        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        Map<String,String> symbols = new LinkedHashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        supportedSymbolsDTO.setSymbols(symbols);

        ResponseEntity<SupportedSymbolsDTO> entity = ResponseEntity.of(Optional.of(supportedSymbolsDTO));
        CacheUtils.getCache().putAll(symbols);

        ExchangeRatesDTO exchangeRatesDTO = new ExchangeRatesDTO();
        exchangeRatesDTO.setBase("EUR");
        exchangeRatesDTO.setDate(new Date());
        exchangeRatesDTO.setTimestamp(2342343232L);
        exchangeRatesDTO.setSuccess(true);
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("AUD",  BigDecimal.valueOf(1.607026));
        linkedHashMap.put("USD",  BigDecimal.valueOf(1.168637));
        exchangeRatesDTO.setRates(linkedHashMap);
        String exchangeJson = new ObjectMapper().writeValueAsString(exchangeRatesDTO);

        doNothing().when(currencyService).updateETagAndDateCache(entity);
        when(currencyService.fetchLatestSupportedCurrencies(anyString(), anyLong())).thenReturn(entity);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok(exchangeJson));
        when(jsonMapper.readValue(anyString(), eq(ExchangeRatesDTO.class))).thenReturn(exchangeRatesDTO);


        mockMvc.perform(get("/v1/currency-converter/convert?sourceCurrency=EUR&targetCurrency=USD&monetaryValue=1")
            .contentType(CONTENT_TYPE_JSON)
            .accept(CONTENT_TYPE_JSON))
            .andExpect(status().isOk());

    }
}
