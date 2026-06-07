package com.skillbox.cryptobot.bot.command;

import com.skillbox.cryptobot.entity.Subscriber;
import com.skillbox.cryptobot.repository.SubscriberRepository;
import com.skillbox.cryptobot.utils.TextUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Service
@Slf4j
@AllArgsConstructor
public class GetSubscriptionCommand implements IBotCommand {

    private final SubscriberRepository subscriberRepository;

    @Override
    public String getCommandIdentifier() {
        return "get_subscription";
    }

    @Override
    public String getDescription() {
        return "Возвращает текущую подписку";
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Long chatId = message.getChatId();
        SendMessage answer = new SendMessage();
        answer.setChatId(chatId.toString());

        try {
            Subscriber subscriber = subscriberRepository.findByTelegramId(chatId).orElse(null);

            if (subscriber == null || subscriber.getSubscriptionPrice() == null) {
                answer.setText("❌ Активные подписки отсутствуют");
            } else {
                answer.setText(String.format("✅ Вы подписаны на стоимость биткоина %s USD",
                        TextUtil.toString(subscriber.getSubscriptionPrice())));
            }

            absSender.execute(answer);
        } catch (Exception e) {
            log.error("Error in get_subscription command for user {}", chatId, e);
            answer.setText("❌ Произошла ошибка при получении информации о подписке");
            try {
                absSender.execute(answer);
            } catch (Exception ex) {
                log.error("Error sending error message", ex);
            }
        }
    }
}