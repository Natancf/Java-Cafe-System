package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.InvalidPaymentException;

/**
 * Represents a payment transaction for an order.
 * Contains payment method and amount.
 */
public class Payment {
    
    private final PaymentMethod method;
    private final double amount;

    public Payment(PaymentMethod method, double amount) {
        this.method = method;
        this.amount = amount;
    }
    
    public PaymentMethod getMethod() { return method; }
    public double getAmount() { return amount; }
    
    /**
     * Validates the payment amount.
     * @throws InvalidPaymentException if amount is zero or negative
     */
    public void validatePayment() throws InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidPaymentException("Payment amount must be greater than zero.");
        }
    }
}