/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.tag;

import java.util.function.Supplier;

import org.quiltmc.qsl.tag.api.TagRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;

import net.fabricmc.fabric.impl.tag.extension.TagFactoryImpl;

/**
 * A factory for accessing datapack tags.
 */
@Deprecated
public interface TagFactory<T> {
	TagFactory<Item> ITEM = TagRegistry.ITEM::create;
	TagFactory<Block> BLOCK = TagRegistry.BLOCK::create;
	TagFactory<Fluid> FLUID = TagRegistry.FLUID::create;
	TagFactory<GameEvent> GAME_EVENT = TagRegistry.GAME_EVENT::create;
	TagFactory<EntityType<?>> ENTITY_TYPE = TagRegistry.ENTITY_TYPE::create;
	TagFactory<Biome> BIOME = TagRegistry.BIOME::create;

	/**
	 * Create a new tag factory for specified registry.
	 *
	 * @param registryKey the key of the registry.
	 * @param dataType    the data type of this tag group, vanilla uses "tags/[plural]" format for built-in groups.
	 */
	static <T> TagFactory<T> of(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		return TagFactoryImpl.of(registryKey, dataType);
	}

	static <T> TagFactory<T> of(Supplier<TagGroup<T>> tagGroupSupplier) {
		return TagFactoryImpl.of(tagGroupSupplier);
	}

	Tag.Identified<T> create(Identifier id);
}
