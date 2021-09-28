package org.project.currencyconverter.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.dto.ExchangeRatesDTO;
import org.project.currencyconverter.exception.SupportedSymbolsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.project.currencyconverter.controller.ExchangeRatesController.EXCHANGE_API_URL;
import static org.project.currencyconverter.controller.ExchangeRatesController.EXCHANGE_RATES_API;
import static org.project.currencyconverter.util.ErrorMappingUtils.errorCodeToDescription;
import static org.project.currencyconverter.util.GlobalConstantUtils.CONTENT_TYPE_JSON;
import static org.project.currencyconverter.util.GlobalConstantUtils.ENCODED_ACCESS_KEY;
import static org.project.currencyconverter.util.GlobalConstantUtils.HTTP_GET;
import static org.project.currencyconverter.util.SecurityUtils.decodeKey;

@RestController
@Slf4j
@RequestMapping(EXCHANGE_API_URL)
@Api(tags = {EXCHANGE_RATES_API})
public class ExchangeRatesController
{
    /*_________________________
   |  SWAGGER CONSTANTS  |
   |________________________|
   */
    public static final String EXCHANGE_RATES_API="Real-time Exchange Provider API";
    public static final String EXCHANGE_RATES_DESCRIPTION="This API provides all the near to real-time exchange rates available from base to passed symbol currencies";
    private static final String API_FETCH_TAG = "EXCHANGE_FETCH_API";
    /*_________________________
      | API URL MAPPINGS       |
      |________________________|
    */
    public static final String EXCHANGE_API_URL = "/v1/currency-converter/exchange";

    /*_______________________
    | Controller Constants   |
    |________________________|
    */
    private static final String DECODED_KEY = decodeKey(ENCODED_ACCESS_KEY);
    private String externalExchangeEndpoint;
    private final RestTemplate restTemplate;

    public ExchangeRatesController(@Value("${external.endpoint.currency.exchange:http://api.exchangeratesapi.io/v1/latest}") String externalExchangeEndpoint,
                                      RestTemplate restTemplate) {
        this.externalExchangeEndpoint = externalExchangeEndpoint;
        this.restTemplate = restTemplate;
    }

    @GetMapping
    @ApiOperation(value = EXCHANGE_RATES_DESCRIPTION, httpMethod = HTTP_GET, tags = {API_FETCH_TAG}, produces = CONTENT_TYPE_JSON, response = ExchangeRatesDTO.class)
    public ResponseEntity<ExchangeRatesDTO> getExchangeRates(@RequestParam("base") String base, @RequestParam("symbols") String commaSeparatedSymbols) {

        String url = externalExchangeEndpoint
            + ("?access_key="+DECODED_KEY)
            + ("&base="+base)
            + ("&symbols="+commaSeparatedSymbols);

        ResponseEntity<ExchangeRatesDTO> response = restTemplate.getForEntity(url, ExchangeRatesDTO.class);

        throwErrorIfErrorResponse(response);

        printInfoLogIfEnabled();

        printDebugLogIfEnabled(response);

        return response;
    }


    private void printDebugLogIfEnabled(ResponseEntity<ExchangeRatesDTO> response)
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


    private void throwErrorIfErrorResponse(ResponseEntity<ExchangeRatesDTO> responseEntity) throws SupportedSymbolsException {
        if(returnsError(responseEntity)) {
            throw new SupportedSymbolsException(responseEntity.getStatusCodeValue(), errorCodeToDescription.getOrDefault(responseEntity.getStatusCodeValue(), "Did not get " +
                "success response from https://exchangeratesapi.io/documentation/ API"));
        }
    }

    private boolean returnsError(ResponseEntity<ExchangeRatesDTO> responseEntity) {
        return isNull(responseEntity.getBody())
            || responseEntity.getStatusCode().is4xxClientError()
            || responseEntity.getStatusCode().is1xxInformational()
            || nonNull(responseEntity.getBody()) && !responseEntity.getBody().isSuccess();
    }

}
