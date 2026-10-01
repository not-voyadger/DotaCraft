package com.dotaCraft.Hero.impl.pudge;

import com.dotaCraft.Hero.Hero;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Pudge extends Hero {

    private ModeledEntity modeledEntity;
    private ActiveModel activeModel;

    public Pudge(Player player) {
        super(player, "Pudge", Attribute.STRENGTH, 30, 14, 16, 5.0, 0.8, 3.0, 1.4, 1.8);

        this.addAbility(0, new FleshHeap());
        this.addAbility(1, new MeatHook());

        //applyPudgeModel();
    }

    private void applyPudgeModel() {
        Player player = getPlayer();

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 1, false, false
        ));

        modeledEntity = ModelEngineAPI.createModeledEntity(player);

        activeModel = ModelEngineAPI.createActiveModel("pudge");

        if (activeModel != null && modeledEntity != null) {
            modeledEntity.addModel(activeModel, true);
        }
    }

    public void removePudgeModel() {
        Player player = getPlayer();

        if (modeledEntity != null && activeModel != null) {
            modeledEntity.removeModel("pudge");
        }

        if (player.isOnline()) {
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }
}