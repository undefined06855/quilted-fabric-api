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

import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.world.chunk.WorldChunk;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;

public final class LifecycleEventsImpl implements ModInitializer {
	@Override
	public void onInitialize() {
		// Part of impl for block entity events
		ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).fabric_markLoaded(chunk);
		});

		ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			((LoadedChunksCache) world).fabric_markUnloaded(chunk);
		});

		// Fire block entity unload events.
		// This handles the edge case where going through a portal will cause block entities to unload without warning.
		ServerChunkEvents.CHUNK_UNLOAD.register((world, chunk) -> {
			for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
				ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
			}
		});

		// We use the world unload event so worlds that are dynamically hot(un)loaded get (block) entity unload events fired when shut down.
		ServerWorldEvents.UNLOAD.register((server, world) -> {
			for (WorldChunk chunk : ((LoadedChunksCache) world).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, world);
				}
			}

			for (Entity entity : world.iterateEntities()) {
				ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, world);
			}
		});

		ServerLifecycleEvents.STARTING.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.invoker()::onServerStarting);
		ServerLifecycleEvents.READY.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.invoker()::onServerStarted);
		ServerLifecycleEvents.STOPPING.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.invoker()::onServerStopping);
		ServerLifecycleEvents.STOPPED.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.invoker()::onServerStopped);
		ServerTickEvents.START.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.START_SERVER_TICK.invoker()::onStartTick);
		ServerTickEvents.END.register(net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_SERVER_TICK.invoker()::onEndTick);
		ServerWorldTickEvents.START.register((server, world) -> net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.START_WORLD_TICK.invoker().onStartTick(world));
		ServerWorldTickEvents.END.register(((server, world) -> net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.END_WORLD_TICK.invoker().onEndTick(world)));
		ServerWorldLoadEvents.LOAD.register(ServerWorldEvents.LOAD.invoker()::onWorldLoad);
		ServerWorldLoadEvents.UNLOAD.register(ServerWorldEvents.UNLOAD.invoker()::onWorldUnload);
	}
}
