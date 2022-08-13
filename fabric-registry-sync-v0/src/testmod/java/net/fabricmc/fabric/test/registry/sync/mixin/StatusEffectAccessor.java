package net.fabricmc.fabric.test.registry.sync.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

@Mixin(StatusEffect.class)
public interface StatusEffectAccessor {
	@Invoker("<init>")
	static StatusEffect createNewStatusEffect(StatusEffectCategory category, int color) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
