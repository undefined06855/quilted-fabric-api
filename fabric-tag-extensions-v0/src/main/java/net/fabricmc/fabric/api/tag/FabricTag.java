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

import org.quiltmc.qsl.tag.api.QuiltTag;

/**
 * Interface implemented by {@link net.minecraft.tag.Tag} instances when
 * Fabric API is present.
 *
 * @param <T>
 */
@Deprecated
public interface FabricTag<T> extends QuiltTag<T> {
	/**
	 * @return True if the given tag has been "replaced" by a datapack at least once.
	 */
	@Override
	boolean hasBeenReplaced();
}
