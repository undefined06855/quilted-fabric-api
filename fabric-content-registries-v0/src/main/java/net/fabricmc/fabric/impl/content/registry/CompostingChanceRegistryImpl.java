/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 QuiltMC
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

package net.fabricmc.fabric.impl.content.registry;

import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;

import org.quiltmc.qsl.item.content.registry.api.ItemContentRegistries;
import org.quiltmc.qsl.registry.attachment.impl.RegistryEntryAttachmentHolder;

import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;

public class CompostingChanceRegistryImpl implements CompostingChanceRegistry {
	@Override
	public Float get(ItemConvertible item) {
		return ItemContentRegistries.COMPOST_CHANCE.get(item.asItem()).orElse(0.0F);
	}

	@Override
	public void add(ItemConvertible item, Float value) {
		ItemContentRegistries.COMPOST_CHANCE.put(item.asItem(), value);
	}

	/**
	 * @throws UnsupportedOperationException if ran on Fabric API, which currently doesn't implement this method
	 */
	@Override
	public void add(TagKey<Item> tag, Float value) throws UnsupportedOperationException {
		ItemContentRegistries.COMPOST_CHANCE.put(tag, value);
	}

	@Override
	public void remove(ItemConvertible item) {
		if (ItemContentRegistries.COMPOST_CHANCE.get(item.asItem()).isEmpty()) {
			RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM).valueTable.remove(ItemContentRegistries.COMPOST_CHANCE, item.asItem());
		}
	}

	/**
	 * @throws UnsupportedOperationException if ran on Fabric API, which currently doesn't implement this method
	 */
	@Override
	public void remove(TagKey<Item> tag) {
		if (ItemContentRegistries.COMPOST_CHANCE.tagKeySet().contains(tag)) {
			RegistryEntryAttachmentHolder.getBuiltin(Registry.ITEM).valueTagTable.remove(ItemContentRegistries.COMPOST_CHANCE, tag);
		}
	}

	@Override
	public void clear(ItemConvertible item) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}

	@Override
	public void clear(TagKey<Item> tag) {
		throw new UnsupportedOperationException("CompostingChanceRegistry operates directly on the vanilla map - clearing not supported!");
	}
}
