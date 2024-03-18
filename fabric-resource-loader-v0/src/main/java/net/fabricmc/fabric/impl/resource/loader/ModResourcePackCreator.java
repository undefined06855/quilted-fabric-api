/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

package net.fabricmc.fabric.impl.resource.loader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.fabricmc.fabric.api.resource.ModResourcePack;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

/**
 * Represents a resource pack provider for mods and built-in mods resource packs.
 */
public class ModResourcePackCreator implements ResourcePackProvider {
	public static final String FABRIC = "fabric";
	private static final String PROGRAMMER_ART = "programmer_art";
	private static final String HIGH_CONTRAST = "high_contrast";
	public static final ResourcePackSource RESOURCE_PACK_SOURCE = new ResourcePackSource() {
		@Override
		public Text decorate(Text packName) {
			return Text.translatable("pack.nameAndSource", packName, Text.translatable("pack.source.fabricmod"));
		}

		@Override
		public boolean canBeEnabledLater() {
			return true;
		}
	};
	public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(ResourceType.CLIENT_RESOURCES);
	private final ResourceType type;

	public ModResourcePackCreator(ResourceType type) {
		this.type = type;
	}

	/**
	 * Registers the resource packs.
	 *
	 * @param consumer The resource pack profile consumer.
	 */
	public void register(Consumer<ResourcePackProfile> consumer) {
		// This should stay as it's been used in *some* mods, it's bad I know, but it's an easy way to inject resource
		// packs, it highlights the need for an API.

//		throw new RuntimeException("Not Currently implemented in QSL. If your mod uses this, please make a issue at https://github.com/QuiltMC/quilted-fabric-api");
		consumer.accept(ResourcePackProfile.create(
				FABRIC,
				Text.translatable("pack.name.fabricMods"),
				true,
				new PlaceholderResourcePack.Factory(this.type),
				this.type,
				ResourcePackProfile.InsertionPosition.TOP,
				RESOURCE_PACK_SOURCE
		));

		// Build a list of mod resource packs.
		registerModPack(consumer, null);

		if (this.type == ResourceType.CLIENT_RESOURCES) {
			// Programmer Art/High Contrast data packs can never be enabled.
			registerModPack(consumer, PROGRAMMER_ART);
			registerModPack(consumer, HIGH_CONTRAST);
		}

		// Register all built-in resource packs provided by mods.
		org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl.registerBuiltinPacks(this.type, consumer);

	}

	private void registerModPack(Consumer<ResourcePackProfile> consumer, @Nullable String subPath) {
		List<ModResourcePack> packs = new ArrayList<>();
		ModResourcePackUtil.appendModResourcePacks(packs, this.type, subPath);

		for (ModResourcePack pack : packs) {
			Text displayName = subPath == null
					? Text.translatable("pack.name.fabricMod", pack.getFabricModMetadata().getName())
					: Text.translatable("pack.name.fabricMod.subPack", pack.getFabricModMetadata().getName(), Text.translatable("resourcePack." + subPath + ".name"));
			ResourcePackProfile profile = ResourcePackProfile.create(
					pack.getName(),
					displayName,
					subPath == null,
					new ModResourcePackFactory(pack),
					this.type,
					ResourcePackProfile.InsertionPosition.TOP,
					RESOURCE_PACK_SOURCE
			);

			if (profile != null) {
				consumer.accept(profile);
			}
		}
	}
}
