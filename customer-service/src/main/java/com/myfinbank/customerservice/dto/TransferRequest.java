package com.myfinbank.customerservice.dto;

public class TransferRequest {
    private Long sourceCustomerId;
    private String targetEmail;
    private double amount;

    public Long getSourceCustomerId() {
        return sourceCustomerId;
    }

    public void setSourceCustomerId(Long sourceCustomerId) {
        this.sourceCustomerId = sourceCustomerId;
    }

    public String getTargetEmail() {
        return targetEmail;
    }

    public void setTargetEmail(String targetEmail) {
        this.targetEmail = targetEmail;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
