package com.gebeya.bankAPI.Model.DTO;

public class TransactionRequestDTOtemp {
    private long defaultUserAccountNo;
    private double amount;
    private String mobileNo;
    private int otp;
    private long merchantUserAccountNo;

    public TransactionRequestDTOtemp() {
    }

    public TransactionRequestDTOtemp(long defaultUserAccountNo, double amount, String mobileNo, int otp, long merchantUserAccountNo) {
        this.defaultUserAccountNo = defaultUserAccountNo;
        this.amount = amount;
        this.mobileNo = mobileNo;
        this.otp = otp;
        this.merchantUserAccountNo = merchantUserAccountNo;
    }

    public TransactionRequestDTOtemp(long defaultUserAccountNo, double amount){
        this.defaultUserAccountNo = defaultUserAccountNo;
        this.amount = amount;
    }

    public TransactionRequestDTOtemp(long defaultUserAccountNo, double amount, int otp, long merchantUserAccountNo)
    {
        this.defaultUserAccountNo = defaultUserAccountNo;
        this.amount = amount;
        this.otp =otp;
        this.merchantUserAccountNo = merchantUserAccountNo;
    }
    public TransactionRequestDTOtemp(long merchantUserAccountNo, int otp, String MobileNo)
    {
        this.merchantUserAccountNo = merchantUserAccountNo;
        this.otp =otp;
        this.mobileNo =MobileNo;
    }

    public long getDefaultUserAccountNo() {
        return defaultUserAccountNo;
    }

    public void setDefaultUserAccountNo(long defaultUserAccountNo) {
        this.defaultUserAccountNo = defaultUserAccountNo;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }

    public long getMerchantUserAccountNo() {
        return merchantUserAccountNo;
    }

    public void setMerchantUserAccountNo(long merchantUserAccountNo) {
        this.merchantUserAccountNo = merchantUserAccountNo;
    }

    @Override
    public String toString() {
        return "TransactionRequestDTOtemp{" +
                "defaultUserAccountNo=" + defaultUserAccountNo +
                ", amount=" + amount +
                ", mobileNo='" + mobileNo + '\'' +
                ", otp=" + otp +
                ", merchantUserAccountNo=" + merchantUserAccountNo +
                '}';
    }
}
