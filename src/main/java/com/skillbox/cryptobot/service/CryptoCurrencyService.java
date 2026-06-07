package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.client.BinanceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class CryptoCurrencyService {
    private final AtomicReference<Double> price = new AtomicReference<>();
    private final BinanceClient client;

    public CryptoCurrencyService(BinanceClient client) {
        this.client = client;
    }

    public double getBitcoinPrice() throws IOException {
        if (price.get() == null) {
            refreshPrice();
        }
        return price.get();
    }

    @Scheduled(fixedDelayString = "${scheduler.price-check.fixed-delay:120000}")
    public void refreshPrice() {
        try {
            double newPrice = client.getBitcoinPrice();
            price.set(newPrice);
            log.info("Price refreshed: {} USD", newPrice);
        } catch (IOException e) {
            log.error("Error refreshing bitcoin price", e);
        }
    }
}