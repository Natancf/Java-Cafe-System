package main.java.br.usp.icmc.scc0204.javacafe.model;

import main.java.br.usp.icmc.scc0204.javacafe.exceptions.InvalidPaymentException;

public class Payment {
    
    private PaymentMethod method; 
    private double amount;

    public Payment(PaymentMethod method, double amount) {
        this.method = method;
        this.amount = amount;
    }
    
    public PaymentMethod getMethod() { return method; }
    public double getAmount() { return amount; }
    
    public void validatePayment() throws InvalidPaymentException {
        if (amount <= 0) {
            throw new InvalidPaymentException("O valor do pagamento deve ser maior que zero.");
        }
    }
}