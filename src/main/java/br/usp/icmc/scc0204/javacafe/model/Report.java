package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.List;
import java.util.Map;

/**
 * Report containing sales analytics for a specific period.
 */
public class Report {
    
    private final double totalRevenue;
    private final int transactionCount;
    private final List<Product> topThreeItems;
    private final Map<String, Integer> productSalesCount;

    public Report(double totalRevenue, int transactionCount, 
                  List<Product> topThreeItems, Map<String, Integer> productSalesCount) {
        this.totalRevenue = totalRevenue;
        this.transactionCount = transactionCount;
        this.topThreeItems = topThreeItems;
        this.productSalesCount = productSalesCount;
    }

    public double getTotalRevenue() { return totalRevenue; }
    public int getTransactionCount() { return transactionCount; }
    public List<Product> getTopThreeItems() { return topThreeItems; }
    
    /**
     * Gets the quantity sold for a specific product.
     */
    public int getProductSalesCount(Product product) {
        if (product == null || productSalesCount == null) {
            return 0;
        }
        return productSalesCount.getOrDefault(product.getId(), 0);
    }
}