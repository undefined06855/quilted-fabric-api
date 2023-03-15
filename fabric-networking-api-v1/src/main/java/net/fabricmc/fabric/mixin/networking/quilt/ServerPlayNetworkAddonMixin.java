/*
 * Copyright 2023 QuiltMC
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

package net.fabricmc.fabric.mixin.networking.quilt;

import org.quiltmc.qsl.networking.impl.server.ServerPlayNetworkAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.server.MinecraftServer;

// TODO - Have a solution on Quilt's side that makes these mixins unnecessary
@Mixin(ServerPlayNetworkAddon.class)
public class ServerPlayNetworkAddonMixin {
	@Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;isOnThread()Z"))
	public boolean removeEquivalentCode(MinecraftServer instance) {
		return false;
	}
}
