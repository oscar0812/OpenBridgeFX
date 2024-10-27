package com.oscarrtorres.openbridgefx.models;

public class TokenCostInfo {
    int tokenCount;
    double totalCost;

    public TokenCostInfo(int tokenCount, double totalCost) {
        this.tokenCount = tokenCount;
        this.totalCost = totalCost;
    }

    public void setTokenCount(int tokenCount) {
        this.tokenCount = tokenCount;
    }

    public int getTokenCount() {
        return tokenCount;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalCost() {
        return totalCost;
    }

    @Override
    public String toString() {
        return String.format("Tokens: %d, Cost: $%.8f", tokenCount, totalCost);
    }
}
