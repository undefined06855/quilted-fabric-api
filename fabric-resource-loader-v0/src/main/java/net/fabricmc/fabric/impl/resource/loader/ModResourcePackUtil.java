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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.io.IOUtils;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Internal utilities for managing resource packs.
 */
public final class ModResourcePackUtil {
	public static final Gson GSON = new Gson();

	private ModResourcePackUtil() {
	}

	public static boolean containsDefault(String filename, boolean modBundled) {
		return "pack.mcmeta".equals(filename) || (modBundled && "pack.png".equals(filename));
	}

	public static InputStream getDefaultIcon() throws IOException {
		Optional<Path> loaderIconPath = FabricLoader.getInstance().getModContainer("fabric-resource-loader-v0")
				.flatMap(resourceLoaderContainer -> resourceLoaderContainer.getMetadata().getIconPath(512).flatMap(resourceLoaderContainer::findPath));

		if (loaderIconPath.isPresent()) {
			return Files.newInputStream(loaderIconPath.get());
		}

		// Should never happen in practice
		return null;
	}

	public static InputStream openDefault(ModContainer container, ResourceType type, String filename) throws IOException {
		switch (filename) {
		case "pack.mcmeta":
			String description = Objects.requireNonNullElse(container.getMetadata().getName(), "");
			String metadata = serializeMetadata(SharedConstants.getGameVersion().getResourceVersion(type), description);
			return IOUtils.toInputStream(metadata, Charsets.UTF_8);
		case "pack.png":
			Optional<Path> path = container.getMetadata().getIconPath(512).flatMap(container::findPath);

			if (path.isPresent()) {
				return Files.newInputStream(path.get());
			} else {
				return getDefaultIcon();
			}
		default:
			return null;
		}
	}

	public static PackResourceMetadata getMetadataPack(int packVersion, Text description) {
		return new PackResourceMetadata(description, packVersion, Optional.empty());
	}

	public static JsonObject getMetadataPackJson(int packVersion, Text description) {
		return PackResourceMetadata.SERIALIZER.toJson(getMetadataPack(packVersion, description));
	}

	public static String serializeMetadata(int packVersion, String description) {
		// This seems to be still manually deserialized
		JsonObject pack = getMetadataPackJson(packVersion, Text.literal(description));
		JsonObject metadata = new JsonObject();
		metadata.add("pack", pack);
		return GSON.toJson(metadata);
	}

	public static Text getName(ModMetadata info) {
		if (info.getName() != null) {
			return Text.literal(info.getName());
		} else {
			return Text.translatable("pack.name.fabricMod", info.getId());
		}
	}
}
