package com.myfinbank.customerservice.dto;

public class BalanceResponse {

    private Long customerId;
    private Long accountId;
    private String accountType;
    private double balance;

    public BalanceResponse(Long customerId, Long accountId, String accountType, double balance) {
        this.customerId = customerId;
        this.accountId = accountId;
        this.accountType = accountType;
        this.balance = balance;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
