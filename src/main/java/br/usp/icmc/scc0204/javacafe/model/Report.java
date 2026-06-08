package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.List;

public class Report {
    
    private double totalRevenue;
    private int transactionCount;
    private List<Product> topThreeItems;

    public Report(double totalRevenue, int transactionCount, List<Product> topThreeItems) {
        this.totalRevenue = totalRevenue;
        this.transactionCount = transactionCount;
        this.topThreeItems = topThreeItems;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public int getTransactionCount() { return transactionCount; }
    public List<Product> getTopThreeItems() { return topThreeItems; }
}