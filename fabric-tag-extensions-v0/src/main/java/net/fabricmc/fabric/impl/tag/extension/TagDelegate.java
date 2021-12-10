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

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.tag.api.TagType;

import net.minecraft.tag.TagGroup;
import net.minecraft.util.Identifier;

/*
 * Patchouli uses TagDelegate, for some reason.
 */
@ApiStatus.Internal
public class TagDelegate<T> extends org.quiltmc.qsl.tag.impl.TagDelegate<T> {
	public TagDelegate(Identifier id, Supplier<TagGroup<T>> tagGroupSupplier) {
		this(id, TagType.OPTIONAL, tagGroupSupplier);
	}

	public TagDelegate(Identifier id, TagType type, Supplier<TagGroup<T>> tagGroupSupplier) {
		super(id, type, tagGroupSupplier);
	}
}
