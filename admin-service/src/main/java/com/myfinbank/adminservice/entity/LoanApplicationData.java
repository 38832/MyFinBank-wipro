package com.myfinbank.adminservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
public class LoanApplicationData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;

    private double amount;

    private int termMonths;

    private double rate;

    private String status = "PENDING";

    private LocalDateTime appliedAt = LocalDateTime.now();

    public LoanApplicationData() {
    }

    public LoanApplicationData(Long customerId, double amount, int termMonths, double rate) {
        this.customerId = customerId;
        this.amount = amount;
        this.termMonths = termMonths;
        this.rate = rate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
