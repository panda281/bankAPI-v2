package com.gebeya.bankAPI.Model.DTO;

public class TransactionRequestDTOtemp {
    private long dAccountNo;
    private double amount;
    private String mobileNo;
    private int otp;
    private long mAccountNo;

    public TransactionRequestDTOtemp() {
    }

    public TransactionRequestDTOtemp(long dAccountNo, double amount, String mobileNo, int otp, long mAccountNo) {
        this.dAccountNo = dAccountNo;
        this.amount = amount;
        this.mobileNo = mobileNo;
        this.otp = otp;
        this.mAccountNo = mAccountNo;
    }

    public TransactionRequestDTOtemp(long dAccountNo, double amount){
        this.dAccountNo = dAccountNo;
        this.amount = amount;
    }

    public TransactionRequestDTOtemp(long dAccountNo, double amount, int otp, long mAccountNo)
    {
        this.dAccountNo = dAccountNo;
        this.amount = amount;
        this.otp =otp;
        this.mAccountNo = mAccountNo;
    }
    public TransactionRequestDTOtemp(long mAccountNo, int otp, String MobileNo)
    {
        this.mAccountNo = mAccountNo;
        this.otp =otp;
        this.mobileNo =MobileNo;
    }

    public long getdAccountNo() {
        return dAccountNo;
    }

    public void setdAccountNo(long dAccountNo) {
        this.dAccountNo = dAccountNo;
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

    public long getmAccountNo() {
        return mAccountNo;
    }

    public void setmAccountNo(long mAccountNo) {
        this.mAccountNo = mAccountNo;
    }

    @Override
    public String toString() {
        return "TransactionRequestDTOtemp{" +
                "DAccountNo=" + dAccountNo +
                ", amount=" + amount +
                ", MobileNo='" + mobileNo + '\'' +
                ", otp=" + otp +
                ", MAccountNo=" + mAccountNo +
                '}';
    }
}
