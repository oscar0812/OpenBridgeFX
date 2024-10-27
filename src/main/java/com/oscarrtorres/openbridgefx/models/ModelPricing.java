package com.oscarrtorres.openbridgefx.models;

import com.knuddels.jtokkit.api.ModelType;

public class ModelPricing {
    private final ModelType modelType;
    private final double inputCostPerMillionTokens;
    private final double outputCostPerMillionTokens;

    public ModelPricing(ModelType modelType, double inputCost, double outputCost) {
        this.modelType = modelType;
        this.inputCostPerMillionTokens = inputCost;
        this.outputCostPerMillionTokens = outputCost;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public double getInputCostPerMillionTokens() {
        return inputCostPerMillionTokens;
    }

    public double getOutputCostPerMillionTokens() {
        return outputCostPerMillionTokens;
    }
}

