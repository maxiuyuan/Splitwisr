package com.splitwisr.ui.balances;

public class BalanceViewObject {
    String otherUser;
    Double balance;
    boolean owesOtherUser;

    public BalanceViewObject(String otherUser, Double balance, boolean owesOtherUser) {
        this.otherUser = otherUser;
        this.balance = balance;
        this.owesOtherUser = owesOtherUser;
    }
}
