package org.project.currencyconverter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.exception.SupportedSymbolsException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SupportedSymbolsControllerTest
{
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper jsonMapper;

    @InjectMocks
    private SupportedSymbolsController supportedSymbolsController;

    @Test
    @DisplayName("given site is up, " +
        "when symbols API is invoked," +
        "then ok response with the supported currencies are returned")
    public void testGetSupportedSymbols() throws Exception
    {
        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        ConcurrentHashMap<String,String> symbols = new ConcurrentHashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        supportedSymbolsDTO.setSymbols(symbols);
        String json = new ObjectMapper().writeValueAsString(supportedSymbolsDTO);
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.ok(json));
        when(jsonMapper.readValue(anyString(), eq(SupportedSymbolsDTO.class))).thenReturn(supportedSymbolsDTO);

        ResponseEntity<SupportedSymbolsDTO> responseEntity = supportedSymbolsController.getSupportedSymbols();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getSymbols()).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test(expected = NullPointerException.class)
    @DisplayName("when response is null, then throw NullPointerException")
    public void testResponseEntityReturnsNull() throws JsonProcessingException
    {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(null);
        supportedSymbolsController.getSupportedSymbols();
    }

    @Test(expected = SupportedSymbolsException.class)
    @DisplayName("when external API throw error code, then throw SupportedSymbolsException")
    public void testResponseEntityReturnsErrorCode() throws JsonProcessingException
    {
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(ResponseEntity.status(401).body("Error occurred"));
        supportedSymbolsController.getSupportedSymbols();
    }
}
