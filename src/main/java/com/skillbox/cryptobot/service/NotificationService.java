package com.skillbox.cryptobot.service;

import com.skillbox.cryptobot.bot.CryptoBot;
import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
public class NotificationService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private CryptoCurrencyService cryptoCurrencyService;

    @Autowired
    private CryptoBot cryptoBot;

    @Value("${notification.cooldown.minutes:10}")
    private long cooldownMinutes;

    @Scheduled(fixedDelayString = "${scheduler.price-check.fixed-delay:120000}")
    public void checkPricesAndNotify() {
        log.info("Starting scheduled price check");

        try {
            double currentPrice = cryptoCurrencyService.getBitcoinPrice();
            log.info("Current Bitcoin price: {}", currentPrice);

            List<Subscriber> subscribers = subscriberRepository.findBySubscriptionPriceIsNotNull();
            log.info("Found {} active subscribers", subscribers.size());

            for (Subscriber subscriber : subscribers) {
                checkAndNotifySubscriber(subscriber, currentPrice);
            }
        } catch (IOException e) {
            log.error("Error getting bitcoin price during scheduled check", e);
        } catch (Exception e) {
            log.error("Unexpected error during price check", e);
        }
    }

    private void checkAndNotifySubscriber(Subscriber subscriber, double currentPrice) {
        double subscriptionPrice = subscriber.getSubscriptionPrice();

        if (currentPrice <= subscriptionPrice) {
            if (shouldSendNotification(subscriber)) {
                sendNotification(subscriber, currentPrice);
                updateLastNotificationTime(subscriber);
            } else {
                log.debug("User {} is in cooldown period, skipping notification", subscriber.getTelegramId());
            }
        }
    }

    private boolean shouldSendNotification(Subscriber subscriber) {
        Instant lastNotification = subscriber.getLastNotificationTime();

        if (lastNotification == null) {
            return true;
        }

        Instant cooldownEnd = lastNotification.plus(cooldownMinutes, ChronoUnit.MINUTES);
        return Instant.now().isAfter(cooldownEnd);
    }

    private void sendNotification(Subscriber subscriber, double currentPrice) {
        Long chatId = subscriber.getTelegramId();
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(String.format("💰 Пора покупать, стоимость биткоина %s USD",
                TextUtil.toString(currentPrice)));

        try {
            cryptoBot.execute(message);
            log.info("Notification sent to user {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Error sending notification to user {}", chatId, e);
        }
    }

    private void updateLastNotificationTime(Subscriber subscriber) {
        subscriberRepository.updateLastNotificationTime(
                subscriber.getTelegramId(),
                Instant.now()
        );
    }
}