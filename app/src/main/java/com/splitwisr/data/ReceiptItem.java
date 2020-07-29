package com.splitwisr.data;

import java.util.ArrayList;
import java.util.List;

public class ReceiptItem {
    public String name;
    public Double cost;
    public List<String> usersSplitting;

    public ReceiptItem(String s, Double d) {
        name = s;
        cost = d;
        usersSplitting = new ArrayList<>();
    }
}
