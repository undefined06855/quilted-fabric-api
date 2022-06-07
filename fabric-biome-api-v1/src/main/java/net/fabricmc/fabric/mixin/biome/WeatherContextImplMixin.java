package net.fabricmc.fabric.mixin.biome;

import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

@Mixin(targets = "org/quiltmc/qsl/worldgen/biome/impl/modification/BiomeModificationContextImpl$WeatherContextImpl")
public abstract class WeatherContextImplMixin implements BiomeModificationContext.WeatherContext { }
