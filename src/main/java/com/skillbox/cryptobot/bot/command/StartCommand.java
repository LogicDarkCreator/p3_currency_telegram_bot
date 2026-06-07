package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@AllArgsConstructor
@Slf4j
public class StartCommand implements IBotCommand {

    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Запускает бота";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long chatId = message.getChatId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            if (subscriberRepository.findByTelegramId(chatId).isEmpty()) {
                subscriberRepository.save(new Subscriber(chatId));
                log.info("New user registered: {}", chatId);
            }

            answer.setText("""
                    Привет! Данный бот помогает отслеживать стоимость биткоина.
                    Поддерживаемые команды:
                     /get_price - получить стоимость биткоина
                     /subscribe [цена] - подписаться на стоимость биткоина (например /subscribe 50000)
                     /get_subscription - получить информацию о текущей подписке
                     /unsubscribe - отменить подписку
                    """);
            absSender.execute(answer);
        } catch (TelegramApiException e) {
            log.error("Error occurred in /start command", e);
        }
    }
}