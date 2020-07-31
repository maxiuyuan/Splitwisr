package com.splitwisr.ui.balances;

public class BalanceViewObject {
    String otherEmail;
    String otherName;
    Double balance;
    boolean owesOtherUser;

    public BalanceViewObject(String otherEmail, String otherName,  Double balance, boolean owesOtherUser) {
        this.otherEmail = otherEmail;
        this.otherName = otherName;
        this.balance = balance;
        this.owesOtherUser = owesOtherUser;
    }
}
