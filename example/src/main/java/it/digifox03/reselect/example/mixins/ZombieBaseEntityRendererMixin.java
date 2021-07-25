package it.digifox03.reselect.example.mixins;

import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static it.digifox03.reselect.example.Reselectors.reselectZombie;

@Mixin(ZombieBaseEntityRenderer.class)
abstract class ZombieBaseEntityRendererMixin {
    @Inject(method = "getTexture", at = @At("RETURN"), cancellable = true)
    void getTexture(ZombieEntity zombieEntity, CallbackInfoReturnable<Identifier> cir) {
        cir.setReturnValue(reselectZombie(zombieEntity));
    }
}
