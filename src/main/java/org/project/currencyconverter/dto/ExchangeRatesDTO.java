package org.project.currencyconverter.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Objects.isNull;
import static org.project.currencyconverter.util.GlobalConstantUtils.EMPTY;

@JsonInclude(NON_NULL)
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRatesDTO
{
    private boolean success;
    private long timestamp;
    private String base;
    private String date;
    private Map<String, BigDecimal> rates;


    public void setDate(Date date)
    {
        this.date = formatDate(date);
    }


    public String formatDate(Date date) {
        if (isNull(date)) {
            return EMPTY;
        }

        LocalDate localDate = date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTimeFormatter.format(localDate);
    }
}
