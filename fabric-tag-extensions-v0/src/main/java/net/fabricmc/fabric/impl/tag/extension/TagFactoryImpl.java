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

package net.fabricmc.fabric.impl.tag.extension;

import java.util.function.Supplier;

import org.quiltmc.qsl.tag.api.TagRegistry;

import net.minecraft.tag.Tag;
import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import net.fabricmc.fabric.api.tag.TagFactory;

@SuppressWarnings("ClassCanBeRecord")
public final class TagFactoryImpl<T> implements TagFactory<T> {
	public static <T> TagFactory<T> of(Supplier<TagGroup<T>> tagGroupSupplier) {
		return new TagFactoryImpl<>(TagRegistry.of(tagGroupSupplier));
	}

	public static <T> TagFactory<T> of(RegistryKey<? extends Registry<T>> registryKey, String dataType) {
		return new TagFactoryImpl<>(TagRegistry.of(registryKey, dataType));
	}

	private final TagRegistry<T> tagRegistry;

	private TagFactoryImpl(TagRegistry<T> tagRegistry) {
		this.tagRegistry = tagRegistry;
	}

	@Override
	public Tag.Identified<T> create(Identifier id) {
		return this.tagRegistry.create(id);
	}
}
