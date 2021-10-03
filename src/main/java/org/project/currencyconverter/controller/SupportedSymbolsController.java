package org.project.currencyconverter.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.builder.UrlBuilder;
import org.project.currencyconverter.dto.SupportedSymbolsDTO;
import org.project.currencyconverter.exception.SupportedSymbolsException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.isNull;
import static org.project.currencyconverter.controller.SupportedSymbolsController.SUPPORTED_SYMBOLS_API;
import static org.project.currencyconverter.controller.SupportedSymbolsController.SYMBOLS_API_URL;
import static org.project.currencyconverter.util.CommonUtils.errorCodeToDescription;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.project.currencyconverter.util.GlobalConstantUtils.ENCODED_ACCESS_KEY;
import static org.project.currencyconverter.util.GlobalConstantUtils.HTTP_GET;
import static org.project.currencyconverter.util.SecurityUtils.decodeKey;

/**
 * This class is the controller for symbols supported by: <a href="https://exchangeratesapi.io/">https://exchangeratesapi.io/</a>
 * Moreover, it helps generate the {@code ETag} and {@code Date} in the headers as shallowEtagHeaderFilter bean is used.
 */
@RestController
@Slf4j
@RequestMapping(SYMBOLS_API_URL)
@Api(tags = {SUPPORTED_SYMBOLS_API})
public class SupportedSymbolsController implements InitializingBean
{
    /*_________________________
   |  SWAGGER CONSTANTS  |
   |________________________|
   */
    public static final String SUPPORTED_SYMBOLS_API="Supported Currency API";
    private static final String SUPPORTED_SYMBOLS_DESCRIPTION = "This API fetches external API and returns the real-time supported currencies to convert.";
    private static final String API_FETCH_TAG = "SYMBOLS_FETCH_API";
    /*_________________________
      | API URL MAPPINGS   |
      |________________________|
    */
    public static final String SYMBOLS_API_URL = "/v1/currency-converter/symbols";

   /*_______________________
    | Controller Constants   |
    |________________________|
    */
    private static final String DECODED_KEY = decodeKey(ENCODED_ACCESS_KEY);

    private final String externalSupportedSymbolsEndpoint;
    private final RestTemplate restTemplate;
    private String symbolApiUrl = "";
    private final ObjectMapper jsonMapper;

    public SupportedSymbolsController(@Value("${external.endpoint.supported.symbols:http://api.exchangeratesapi.io/v1/symbols}") String externalSupportedSymbolsEndpoint,
                                      ObjectMapper jsonMapper,
                                      RestTemplate restTemplate) {
        this.externalSupportedSymbolsEndpoint = externalSupportedSymbolsEndpoint;
        this.restTemplate = restTemplate;
        this.jsonMapper = jsonMapper;
    }

    @GetMapping
    @ApiOperation(value = SUPPORTED_SYMBOLS_DESCRIPTION, httpMethod = HTTP_GET, tags = {API_FETCH_TAG}, produces = CONTENT_TYPE_JSON, response = SupportedSymbolsDTO.class)
    public ResponseEntity<SupportedSymbolsDTO> getSupportedSymbols() throws JsonProcessingException
    {

        ResponseEntity<String> response = restTemplate.getForEntity(symbolApiUrl, String.class);

        throwErrorIfErrorResponse(response);

        printInfoLogIfEnabled();

        printDebugLogIfEnabled(response);

        return ResponseEntity
            .status(response.getStatusCode())
            .body(jsonMapper.readValue(response.getBody(), SupportedSymbolsDTO.class));
    }


    private void printDebugLogIfEnabled(ResponseEntity<String> response)
    {
        if(log.isDebugEnabled()) {
            log.debug("Response received [ {} ]", response.getBody());
        }
    }


    private void printInfoLogIfEnabled()
    {
        if(log.isInfoEnabled())
        {
            log.info("Response received");
        }
    }


    private void throwErrorIfErrorResponse(ResponseEntity<String> responseEntity) throws SupportedSymbolsException {
        if(returnsError(responseEntity, errorCodeToDescription)) {
            throw new SupportedSymbolsException(responseEntity.getStatusCodeValue(), errorCodeToDescription.getOrDefault(responseEntity.getStatusCodeValue(), "Did not get " +
                "success response from https://exchangeratesapi.io/documentation/ API"));
        }
    }

    private boolean returnsError(ResponseEntity<String> responseEntity, ImmutableMap<Integer, String> errorCodeToDescription) {
        return isNull(responseEntity)
            || errorCodeToDescription.containsKey(responseEntity.getStatusCodeValue());
    }

    @Override
    public void afterPropertiesSet()
    {
        symbolApiUrl = UrlBuilder.builder()
            .withBaseUrl(externalSupportedSymbolsEndpoint)
            .withRequestParameter1("access_key", DECODED_KEY)
            .build();
    }
}
