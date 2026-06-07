package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.service.CryptoCurrencyService;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import java.io.IOException;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SubscribeCommand implements IBotCommand {

    private static final Pattern PRICE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?$");

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private CryptoCurrencyService cryptoCurrencyService;

    @Override
    public String getCommandIdentifier() {
        return "subscribe";
    }

    @Override
    public String getDescription() {
        return "Подписывает пользователя на стоимость биткоина";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long chatId = message.getChatId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        if (arguments == null || arguments.length == 0) {
            answer.setText("❌ Пожалуйста, укажите цену для подписки.\nПример: /subscribe 50000");
            executeMessage(absSender, answer);
            return;
        }

        String priceStr = arguments[0];

        if (!PRICE_PATTERN.matcher(priceStr).matches()) {
            answer.setText("❌ Неверный формат цены. Пожалуйста, введите число.\nПример: /subscribe 50000");
            executeMessage(absSender, answer);
            return;
        }

        try {
            double subscriptionPrice = Double.parseDouble(priceStr);

            if (subscriberRepository.findByTelegramId(chatId).isEmpty()) {
                subscriberRepository.save(new Subscriber(chatId));
            }

            subscriberRepository.updateSubscriptionPrice(chatId, subscriptionPrice);

            double currentPrice = cryptoCurrencyService.getBitcoinPrice();

            answer.setText(String.format("✅ Новая подписка создана на стоимость %s USD\n\n" +
                            "💰 Текущая цена биткоина: %s USD",
                    TextUtil.toString(subscriptionPrice),
                    TextUtil.toString(currentPrice)));

            executeMessage(absSender, answer);
            log.info("User {} subscribed to price {}", chatId, subscriptionPrice);

        } catch (NumberFormatException e) {
            answer.setText("❌ Неверный формат числа. Пожалуйста, введите корректную цену.");
            executeMessage(absSender, answer);
        } catch (IOException e) {
            log.error("Error getting bitcoin price for user {}", chatId, e);
            answer.setText("❌ Не удалось получить текущую цену биткоина. Попробуйте позже.");
            executeMessage(absSender, answer);
        }
    }

    private void executeMessage(AbsSender absSender, SendMessage message) {
        try {
            absSender.execute(message);
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
}