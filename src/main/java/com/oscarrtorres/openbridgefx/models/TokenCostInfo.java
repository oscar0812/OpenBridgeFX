package com.oscarrtorres.openbridgefx.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenCostInfo {
    int tokenCount;
    double totalCost;

    @Override
    public String toString() {
        return String.format("Tokens: %d, Cost: $%.8f", tokenCount, totalCost);
    }
}
