package main.java.br.usp.icmc.scc0204.javacafe.model;

import java.util.List;
import java.util.Map;

/**
 * Report containing sales analytics for a specific period.
 */
public class Report {
    
    private double totalRevenue;
    private int transactionCount;
    private List<Product> topThreeItems;
    private Map<String, Integer> productSalesCount; // Product ID -> Quantity sold

    public Report(double totalRevenue, int transactionCount, 
                  List<Product> topThreeItems, Map<String, Integer> productSalesCount) {
        this.totalRevenue = totalRevenue;
        this.transactionCount = transactionCount;
        this.topThreeItems = topThreeItems;
        this.productSalesCount = productSalesCount;
    }

    public double getTotalRevenue() { 
        return totalRevenue; 
    }
    
    public int getTransactionCount() { 
        return transactionCount; 
    }
    
    public List<Product> getTopThreeItems() { 
        return topThreeItems; 
    }
    
    /**
     * Gets the quantity sold for a specific product.
     * @param product The product
     * @return Quantity sold, or 0 if not found
     */
    public int getProductSalesCount(Product product) {
        if (product == null || productSalesCount == null) {
            return 0;
        }
        return productSalesCount.getOrDefault(product.getId(), 0);
    }
}