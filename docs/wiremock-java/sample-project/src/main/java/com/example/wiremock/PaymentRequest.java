package com.example.wiremock;

public class PaymentRequest {
    private int amount;
    private String currency;
    private String description;

    public PaymentRequest() {}

    public PaymentRequest(int amount, String currency, String description) {
        this.amount = amount;
        this.currency = currency;
        this.description = description;
    }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
