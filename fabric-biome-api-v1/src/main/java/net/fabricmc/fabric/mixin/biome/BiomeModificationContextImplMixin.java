/*
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

<<<<<<<< HEAD:fabric-biome-api-v1/src/main/java/net/fabricmc/fabric/mixin/biome/BiomeModificationContextImplMixin.java
package net.fabricmc.fabric.mixin.biome;
========
package net.fabricmc.fabric.mixin.networking.client.accessor;
>>>>>>>> fabric/1.20.2:fabric-networking-api-v1/src/client/java/net/fabricmc/fabric/mixin/networking/client/accessor/ClientCommonNetworkHandlerAccessor.java

import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationContextImpl;
import org.spongepowered.asm.mixin.Mixin;

<<<<<<<< HEAD:fabric-biome-api-v1/src/main/java/net/fabricmc/fabric/mixin/biome/BiomeModificationContextImplMixin.java
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;

@Mixin(BiomeModificationContextImpl.class)
public abstract class BiomeModificationContextImplMixin implements BiomeModificationContext { }
========
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.ClientConnection;

@Mixin(ClientCommonNetworkHandler.class)
public interface ClientCommonNetworkHandlerAccessor {
	@Accessor
	ClientConnection getConnection();
}
>>>>>>>> fabric/1.20.2:fabric-networking-api-v1/src/client/java/net/fabricmc/fabric/mixin/networking/client/accessor/ClientCommonNetworkHandlerAccessor.java
