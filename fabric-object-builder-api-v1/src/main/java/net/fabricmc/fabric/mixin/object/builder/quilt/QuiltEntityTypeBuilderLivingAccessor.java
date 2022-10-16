package net.fabricmc.fabric.mixin.object.builder.quilt;

import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;

@Mixin(QuiltEntityTypeBuilder.Living.class)
public interface QuiltEntityTypeBuilderLivingAccessor {
	@Invoker("<init>")
	static QuiltEntityTypeBuilder.Living callInit(SpawnGroup spawnGroup, EntityType.EntityFactory function) {
		throw new IllegalStateException("Mixin injection failed.");
	}
}
