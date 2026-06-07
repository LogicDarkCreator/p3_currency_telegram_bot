package com.skillbox.cryptobot.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscribers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID uuid;

    @Column(name = "telegram_id", unique = true, nullable = false)
    private Long telegramId;

    @Column(name = "subscription_price")
    private Double subscriptionPrice;

    @Column(name = "last_notification_time")
    private Instant lastNotificationTime;

    public Subscriber(Long telegramId) {
        this.telegramId = telegramId;
        this.subscriptionPrice = null;
        this.lastNotificationTime = null;
    }
}