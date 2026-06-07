package com.skillbox.cryptobot.repository;

import com.skillbox.cryptobot.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, String> {

    Optional<Subscriber> findByTelegramId(Long telegramId);

    @Modifying
    @Transactional
    @Query("UPDATE Subscriber s SET s.subscriptionPrice = :price WHERE s.telegramId = :telegramId")
    void updateSubscriptionPrice(@Param("telegramId") Long telegramId, @Param("price") Double price);

    @Modifying
    @Transactional
    @Query("UPDATE Subscriber s SET s.subscriptionPrice = null, s.lastNotificationTime = null WHERE s.telegramId = :telegramId")
    void deleteSubscription(@Param("telegramId") Long telegramId);

    List<Subscriber> findBySubscriptionPriceIsNotNull();

    @Modifying
    @Transactional
    @Query("UPDATE Subscriber s SET s.lastNotificationTime = :time WHERE s.telegramId = :telegramId")
    void updateLastNotificationTime(@Param("telegramId") Long telegramId, @Param("time") Instant time);
}