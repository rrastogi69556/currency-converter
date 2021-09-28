package org.project.currencyconverter.controller;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.project.currencyconverter.dto.ExchangeRatesDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
public class ExchangeControllerTest
{
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ExchangeRatesController exchangeRatesController;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeRatesController)
            .build();
    }

    @Test
    public void testGetSupportedSymbols() throws Exception
    {
        ExchangeRatesDTO exchangeRatesDTO = new ExchangeRatesDTO();
        exchangeRatesDTO.setBase("EUR");
        exchangeRatesDTO.setDate(new Date());
        exchangeRatesDTO.setTimestamp(Long.valueOf(2342343232L));
        exchangeRatesDTO.setSuccess(true);
        LinkedHashMap<String, Double> linkedHashMap = new LinkedHashMap();
        linkedHashMap.put("AUD",  1.607026);
        linkedHashMap.put("USD",  1.168637);
        exchangeRatesDTO.setRates(linkedHashMap);


        when(restTemplate.getForEntity(anyString(), eq(ExchangeRatesDTO.class))).thenReturn(ResponseEntity.of(Optional.of(exchangeRatesDTO)));

        mockMvc.perform(get("/v1/currency-converter/exchange?base={base}&symbols={symbols}", "EUR", "INR")
        .contentType(CONTENT_TYPE_JSON)
        .accept(CONTENT_TYPE_JSON))
            .andExpect(status().isOk());

        ResponseEntity<ExchangeRatesDTO> responseEntity = exchangeRatesController.getExchangeRates("EUR", "INR");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getBase()).isEqualTo("EUR");
        assertThat(responseEntity.getBody().getDate()).isNotNull();
        assertThat(responseEntity.getBody().getTimestamp()).isEqualTo(2342343232L);
    }

}
