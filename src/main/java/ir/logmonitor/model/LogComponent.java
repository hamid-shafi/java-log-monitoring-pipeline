package ir.logmonitor.model;

public enum LogComponent {
    AUTH,           // login, signup, verification
    PAYMENT,        // Payment processing
    ORDER,          // Order creation and management
    PRODUCT,        // Product information
    NOTIFICATION,   // sending email, SMS, notification
    SYSTEM          // System startup, shutdown, configuration
}
