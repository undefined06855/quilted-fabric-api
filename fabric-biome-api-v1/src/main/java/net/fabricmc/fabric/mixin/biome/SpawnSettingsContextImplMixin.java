package net.fabricmc.fabric.mixin.biome;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

@Mixin(targets = "org/quiltmc/qsl/worldgen/biome/impl/modification/BiomeModificationContextImpl$SpawnSettingsContextImpl")
public abstract class SpawnSettingsContextImplMixin implements BiomeModificationContext.SpawnSettingsContext { }
