package net.fabricmc.fabric.mixin.entity.event.quilt;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
	@Accessor
	float getLastDamageTaken();
}
