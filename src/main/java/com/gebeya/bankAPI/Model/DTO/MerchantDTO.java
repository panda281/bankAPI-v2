package com.gebeya.bankAPI.Model.DTO;

public class MerchantDTO {
    private String mobileNo;
    private long accountNo;
    private int otp;
    private long DefaultCustomerAccountNo;
    private int transactionCode;

    public MerchantDTO() {
    }

    public MerchantDTO(String mobileNo, long accountNo, int otp, long DefaultCustomerAccountNo, int transactionCode) {
        this.mobileNo = mobileNo;
        this.accountNo = accountNo;
        this.otp = otp;
        this.DefaultCustomerAccountNo = DefaultCustomerAccountNo;
        this.transactionCode = transactionCode;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public long getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(long accountNo) {
        this.accountNo = accountNo;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    public long getDefaultCustomerAccountNo() {
        return DefaultCustomerAccountNo;
    }

    public void setDefaultCustomerAccountNo(long defaultCustomerAccountNo) {
        this.DefaultCustomerAccountNo = defaultCustomerAccountNo;
    }

    public int getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(int transactionCode) {
        this.transactionCode = transactionCode;
    }
}
