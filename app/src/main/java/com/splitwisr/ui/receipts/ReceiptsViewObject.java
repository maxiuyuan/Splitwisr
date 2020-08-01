package com.splitwisr.ui.receipts;

import com.splitwisr.data.users.User;

import java.util.List;

public class ReceiptsViewObject {
    String itemName;
    String itemCost;
    List<User> splitWith;

    public ReceiptsViewObject(String itemName, String itemCost, List<User> splitWith) {
        this.itemName = itemName;
        this.itemCost = itemCost;
        this.splitWith = splitWith;
    }

    public ReceiptsViewObject(String itemName, String itemCost) {
        this.itemName = itemName;
        this.itemCost = itemCost;
    }
}
