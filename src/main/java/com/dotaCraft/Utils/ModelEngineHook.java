package com.dotaCraft.Utils;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.LivingEntity;

public class ModelEngineHook {

    public static ActiveModel applyModel(LivingEntity entity, String modelId, float scale) {
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(entity);
        ActiveModel activeModel = ModelEngineAPI.createActiveModel(modelId);

        if (activeModel != null && modeledEntity != null) {
            activeModel.setScale(scale);

            modeledEntity.addModel(activeModel, true);
            return activeModel;
        }
        return null;
    }

    public static void removeModel(LivingEntity entity, String modelId) {
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(entity);
        if (modeledEntity != null) {
            modeledEntity.removeModel(modelId);
        }
    }
}