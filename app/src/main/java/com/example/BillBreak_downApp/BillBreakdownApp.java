package com.example.BillBreak_downApp;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillBreakdownApp {
    private Map<String, Double> users;
    private SQLiteAdapter SQLiteAdapter;

    public BillBreakdownApp(Context context) {
        users = new HashMap<>();
        SQLiteAdapter = new SQLiteAdapter(context);
    }

    public void addUser(String name, double amount) {
        users.put(name, amount);
    }

    public boolean isDuplicateName(String name) {
        return users.containsKey(name);
    }

    public void clearUserData() {
        users.clear();
    }

    public Map<String, Double> getEqualBreakdown() {
        Map<String, Double> equalBreakdown = new HashMap<>();
        double totalBill = getTotalBill();
        int num = users.size();

        if (num == 0) {
            return equalBreakdown;
        }

        double equalAmount = totalBill / num;

        for (String name : users.keySet()) {
            equalBreakdown.put(name, equalAmount);
        }

        return equalBreakdown;
    }

    public Map<String, Double> getUsersData() {
        return new HashMap<>(users);
    }

    public void setCustomPercentageBreakdown(Map<String, Double> percentages) {
        double totalBill = getTotalBill();

        for (Map.Entry<String, Double> entry : percentages.entrySet()) {
            String name = entry.getKey();
            double percentage = entry.getValue();
            double customAmount = totalBill * percentage / 100.0;
            users.put(name, customAmount);
        }
    }

    public void setCustomRatioBreakdown(Map<String, Integer> ratios) {
        double totalBill = getTotalBill();
        int totalRatio = 0;

        for (int ratio : ratios.values()) {
            totalRatio += ratio;
        }

        Map<String, Double> customBreakdown = new HashMap<>();
        for (Map.Entry<String, Integer> entry : ratios.entrySet()) {
            String name = entry.getKey();
            int ratio = entry.getValue();
            double customAmount = totalBill * ratio / (double) totalRatio;
            customBreakdown.put(name, customAmount);
        }

        users = customBreakdown;
    }

    public void setCustomAmountBreakdown(List<Double> amounts) {
        int i = 0;
        double totalAmount = getTotalBill();
        double totalCustomAmount = 0.0;
        for (double amount : amounts) {
            totalCustomAmount += amount;
        }

        if (Math.abs(totalCustomAmount - totalAmount) < 0.01) {
            for (String name : users.keySet()) {
                double customAmount = amounts.get(i);
                users.put(name, customAmount);
                i++;
            }
        } else {
            clearUserData();
        }
    }

    public Map<String, Double> getCustomBreakdown() {
        return users;
    }

    public double getTotalBill() {
        double total = 0.0;

        for (double amount : users.values()) {
            total += amount;
        }

        return total;
    }

    public void saveResult(String name, double amount) {
        SQLiteAdapter.openToWrite();
        SQLiteAdapter.saveResult(name, amount);
        SQLiteAdapter.close();
    }
}
