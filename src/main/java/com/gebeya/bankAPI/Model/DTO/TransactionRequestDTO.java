package com.gebeya.bankAPI.Model.DTO;



public class TransactionRequestDTO {

    private Long accountNo;
    private Double amount;



    public TransactionRequestDTO() {
    }

    public TransactionRequestDTO(Long accountNo, Double amount) {
        this.accountNo = accountNo;
        this.amount = amount;

    }

    public Long getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(Long accountNo) {
        this.accountNo = accountNo;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }



    @Override
    public String toString() {
        return "TransactionRequestDTO{" +
                "accountNo=" + accountNo +
                ", amount=" + amount +
                '}';
    }
}
