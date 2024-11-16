package com.oscarrtorres.openmodelfx.models;

import com.knuddels.jtokkit.api.ModelType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModelPricing {
    private ModelType modelType;
    // inputCost and outputCost is per million tokens
    private double inputCost;
    private double outputCost;
}

