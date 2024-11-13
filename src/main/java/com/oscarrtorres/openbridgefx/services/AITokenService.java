package com.oscarrtorres.openbridgefx.services;

import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.Encoding;
import com.knuddels.jtokkit.api.EncodingRegistry;
import com.knuddels.jtokkit.api.ModelType;
import com.oscarrtorres.openbridgefx.models.ModelPricing;
import com.oscarrtorres.openbridgefx.models.TokenCostInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AITokenService {
    private Map<ModelType, ModelPricing> modelPricingMap = new HashMap<>();
    private ModelType modelType;
    EncodingRegistry registry = Encodings.newDefaultEncodingRegistry();
    Encoding encoding;

    public AITokenService() {
    }

    public LinkedHashMap<ModelType, ModelPricing> getDefaultModelPricingMap() {
        LinkedHashMap<ModelType, ModelPricing> map = new LinkedHashMap<>();
        map.put(ModelType.GPT_4O, new ModelPricing(ModelType.GPT_4O, 2.50, 1.25));
        map.put(ModelType.GPT_4O_MINI, new ModelPricing(ModelType.GPT_4O_MINI, 0.15, 0.60));
        return map;
    }

    public void setModelPricingList(List<ModelPricing> modelPricingList) {
        this.modelPricingMap = new HashMap<>();
        for(ModelPricing modelPricing: modelPricingList) {
            this.modelPricingMap.putIfAbsent(modelPricing.getModelType(), modelPricing);
        }
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
        encoding = registry.getEncodingForModel(modelType);
    }

    public TokenCostInfo getPromptInfo(String prompt) {
        ModelPricing model = modelPricingMap.get(modelType);
        int tokenCount = encoding.countTokens(prompt);
        double totalPrice = (tokenCount / 1_000_000.0) * model.getInputCost();

        return new TokenCostInfo(tokenCount, totalPrice);
    }

    public TokenCostInfo getResponseInfo(String response) {
        ModelPricing model = modelPricingMap.get(modelType);
        int tokenCount = encoding.countTokens(response);
        double totalPrice = (tokenCount / 1_000_000.0) * model.getOutputCost();

        return new TokenCostInfo(tokenCount, totalPrice);
    }

    public ModelType getModelType() {
        return modelType;
    }
}
