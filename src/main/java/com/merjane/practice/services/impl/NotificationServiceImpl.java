package com.merjane.practice.services.impl;

import com.merjane.practice.services.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

// Fake implementation: in production this calls an external e-mail/SMS provider.
@Service
public class NotificationServiceImpl implements NotificationService {
    @Override
    public void sendDelayNotification(int leadTime, String productName) {
        System.out.println("[NOTIF] Delay of " + leadTime + " days for " + productName);
    }

    @Override
    public void sendOutOfStockNotification(String productName) {
        System.out.println("[NOTIF] " + productName + " is out of stock");
    }

    @Override
    public void sendExpirationNotification(String productName, LocalDate expiryDate) {
        System.out.println("[NOTIF] " + productName + " expired on " + expiryDate);
    }
}
