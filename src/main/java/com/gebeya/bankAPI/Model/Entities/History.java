package com.gebeya.bankAPI.Model.Entities;

import com.gebeya.bankAPI.Model.Enums.ResponseCode;
import com.gebeya.bankAPI.Model.Enums.SIDE;
import com.gebeya.bankAPI.Model.Enums.TransactionCode;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private TransactionCode transactionCode;
    @ManyToOne
    private Account account;
    private SIDE side;
    private double amount;
    private ResponseCode responseCode;
    private String phoneNo;
    private LocalDateTime transactionDate;

    public History() {
    }

    public History(TransactionCode transactionCode, Account account, SIDE side, double amount, ResponseCode responseCode, String phoneNo) {
        this.transactionCode = transactionCode;
        this.account = account;
        this.side = side;
        this.amount = amount;
        this.responseCode = responseCode;
        this.phoneNo = phoneNo;

    }

    public History(TransactionCode transactionCode, Account account, double amount, ResponseCode responseCode, String phoneNo) {
        this.transactionCode = transactionCode;
        this.account = account;
        this.amount = amount;
        this.responseCode = responseCode;
        this.phoneNo = phoneNo;

    }
    public History(String phoneNo,ResponseCode responseCode,TransactionCode transactionCode,Account account)
    {
        this.phoneNo = phoneNo;
        this.responseCode=responseCode;
        this.transactionCode=transactionCode;
        this.account=account;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TransactionCode getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(TransactionCode transactionCode) {
        this.transactionCode = transactionCode;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public SIDE getSide() {
        return side;
    }

    public void setSide(SIDE side) {
        this.side = side;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public ResponseCode getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(ResponseCode responseCode) {
        this.responseCode = responseCode;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    @PrePersist
    protected void onTransaction(){
        transactionDate = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "History{" +
                "id=" + id +
                ", transactionCode=" + transactionCode +
                ", account=" + account +
                ", side=" + side +
                ", amount=" + amount +
                ", responseCode=" + responseCode +
                ", phoneNo='" + phoneNo + '\'' +
                ", transactionDate=" + transactionDate +
                '}';
    }
}
