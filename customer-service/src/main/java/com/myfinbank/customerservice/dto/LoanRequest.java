package com.myfinbank.customerservice.dto;

public class LoanRequest {
    private Long customerId;
    private double amount;
    private int termMonths;
    private double rate;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(int termMonths) {
        this.termMonths = termMonths;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
