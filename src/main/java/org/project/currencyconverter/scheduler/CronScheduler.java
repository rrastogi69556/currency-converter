package org.project.currencyconverter.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.project.currencyconverter.abstraction.ICurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CronScheduler
{

    private final ICurrencyService currencyService;

    @Autowired
    public CronScheduler(ICurrencyService currencyService)
    {
        this.currencyService = currencyService;
    }

    @Scheduled(cron = "${fetch.currencies.cron.scheduler.expression:0 0/1 * * * *}")
    public void updateSupportedCurrenciesCache() throws Exception {
        currencyService.updateCacheLatestSupportedCurrencies();
    }


}
