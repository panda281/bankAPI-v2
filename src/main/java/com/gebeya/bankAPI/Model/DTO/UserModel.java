package com.gebeya.bankAPI.Model.DTO;

public class UserModel {
    String userName;
    String phoneNumber;

    public UserModel() {
        super();

    }

    public UserModel(String userName, String phoneNumber) {
        super();
        this.userName = userName;
        this.phoneNumber = phoneNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

}
