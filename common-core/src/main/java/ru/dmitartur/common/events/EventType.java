package ru.dmitartur.common.events;

/**
 * Unified event type registry for Kafka domain events.
 * Keep exhaustive and documented so producers/consumers can switch reliably.
 */
public enum EventType {
    // Telegram entry points
    TelegramIncoming,      // Raw incoming from Telegram (bot gateway → kafka)
    TelegramOutgoing,      // Message body to be sent to Telegram (backend → bot gateway)

    // User lifecycle
    StartCommand,          // User ran /start
    UserRegistrationSubmit,// Finalized registration payload with user data

    // Subscription lifecycle
    SubscriptionActivated, // Premium (or other) subscription activated
    SubscriptionExpired,   // Subscription expired

    // Payment lifecycle
    PaymentCreated,        // Payment entity created/initiated
    PaymentSucceeded,      // Payment succeeded, enable entitlements

    // Order events
    ORDER_CREATED,         // New order created
    ORDER_CANCELLED,       // Order cancelled

    // Reminders
    ReminderCreate         // Custom reminder creation request
}


