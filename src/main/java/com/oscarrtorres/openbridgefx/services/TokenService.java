package com.oscarrtorres.openbridgefx.services;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.models.ModelPricing;
import com.oscarrtorres.openbridgefx.models.TokenCostInfo;

import java.util.HashMap;
import java.util.Map;

public class TokenService {
    private final Map<ModelType, ModelPricing> modelPricingMap = new HashMap<>();
    private ModelType modelType;
    EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    Encoding encoding;

    public TokenService(ModelType modelType) {
        // Initialize model pricing data
        modelPricingMap.put(ModelType.GPT_4O_MINI, new ModelPricing(ModelType.GPT_4O_MINI, 0.15, 0.60));


        this.modelType = modelType;
        encoding = registry.getEncodingForModel(modelType);
    }

    public TokenCostInfo getPromptInfo(String prompt) {
        ModelPricing model = modelPricingMap.get(modelType);
        int tokenCount = encoding.countTokens(prompt);
        double totalPrice = (tokenCount / 1_000_000.0) * model.getInputCostPerMillionTokens();

        return new TokenCostInfo(tokenCount, totalPrice);
    }

    public TokenCostInfo getResponseInfo(String response) {
        ModelPricing model = modelPricingMap.get(modelType);
        int tokenCount = encoding.countTokens(response);
        double totalPrice = (tokenCount / 1_000_000.0) * model.getOutputCostPerMillionTokens();

        return new TokenCostInfo(tokenCount, totalPrice);
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
        encoding = registry.getEncodingForModel(modelType);
    }
    public ModelType getModelType() {
        return modelType;
    }
}
