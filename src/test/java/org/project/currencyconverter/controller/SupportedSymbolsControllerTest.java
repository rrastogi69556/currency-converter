package org.project.currencyconverter.controller;

import java.util.LinkedHashMap;
import java.util.Optional;
import org.junit.Test;
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

    @InjectMocks
    private SupportedSymbolsController supportedSymbolsController;

    @Test
    public void testGetSupportedSymbols() throws Exception
    {
        SupportedSymbolsDTO supportedSymbolsDTO = new SupportedSymbolsDTO();
        supportedSymbolsDTO.setSuccess(true);

        LinkedHashMap<String,String> symbols = new LinkedHashMap<>();
        symbols.put("USD", "United Stated Dollar");
        symbols.put("EUR", "European Euro");
        symbols.put(null, "Australian Dollar");
        supportedSymbolsDTO.setSymbols(symbols);

        when(restTemplate.getForEntity(anyString(), eq(SupportedSymbolsDTO.class))).thenReturn(ResponseEntity.of(Optional.of(supportedSymbolsDTO)));

        ResponseEntity<SupportedSymbolsDTO> responseEntity = supportedSymbolsController.getSupportedSymbols();

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().isSuccess()).isEqualTo(true);
        assertThat(responseEntity.getBody().getSymbols()).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
    }

    @Test(expected = SupportedSymbolsException.class)
    public void testResponseEntityReturnsNull() {
        when(restTemplate.getForEntity(anyString(), eq(SupportedSymbolsDTO.class))).thenReturn(null);

        supportedSymbolsController.getSupportedSymbols();
    }
}
