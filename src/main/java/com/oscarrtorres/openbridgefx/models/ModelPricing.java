package com.oscarrtorres.openbridgefx.models;

import com.knuddels.jtokkit.api.ModelType;

public class ModelPricing {
    private ModelType modelType;
    // inputCost and outputCost is per million tokens
    private double inputCost;
    private double outputCost;

    public ModelPricing() {

    }

    public ModelPricing(ModelType modelType, double inputCost, double outputCost) {
        this.modelType = modelType;
        this.inputCost = inputCost;
        this.outputCost = outputCost;
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public double getInputCost() {
        return inputCost;
    }

    public void setInputCost(double inputCost) {
        this.inputCost = inputCost;
    }

    public double getOutputCost() {
        return outputCost;
    }

    public void setOutputCost(double outputCost) {
        this.outputCost = outputCost;
    }
}

