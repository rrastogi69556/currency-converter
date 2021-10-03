package org.project.currencyconverter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*_________________________________________________________________________________
 | This class is used for transmission of Currency conversion state via REST API   |
 |_________________________________________________________________________________|
*/
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyConversionRequestDTO
{
    @JsonProperty("source_currency")
    private String sourceCurrency;

    @JsonProperty("target_currency")
    private String targetCurrency;

    @JsonProperty("monetary_value")
    BigDecimal monetaryValue;
}
