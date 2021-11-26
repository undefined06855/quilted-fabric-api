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

package net.fabricmc.fabric.impl.event.lifecycle;

import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;

import net.minecraft.block.entity.BlockEntity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;

@Environment(EnvType.CLIENT)
public final class ClientLifecycleEventsImpl implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Part of impl for block entity events
		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).fabric_markLoaded(chunk);
		});

		ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).fabric_markUnloaded(chunk);
		});

		ClientChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
			}
		});

		ClientLifecycleEvents.READY.register(net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STARTED.invoker()::onClientStarted);
		ClientLifecycleEvents.STOPPING.register(net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.invoker()::onClientStopping);
		ClientTickEvents.START.register(net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_CLIENT_TICK.invoker()::onStartTick);
		ClientTickEvents.END.register(net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.invoker()::onEndTick);
		ClientWorldTickEvents.START.register((client, world) -> net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.START_WORLD_TICK.invoker().onStartTick(world));
		ClientWorldTickEvents.END.register((client, world) -> net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_WORLD_TICK.invoker().onEndTick(world));
	}
}
