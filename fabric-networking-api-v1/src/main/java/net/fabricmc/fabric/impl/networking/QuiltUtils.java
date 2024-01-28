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

package net.fabricmc.fabric.impl.networking;

import java.util.Objects;
import java.util.function.BiFunction;

import org.quiltmc.qsl.networking.api.PacketSender;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

public class QuiltUtils {
	public static <T extends PacketListener> Packet<T> createS2CPacket(FabricPacket packet, BiFunction<Identifier, PacketByteBuf, Packet<T>> createPacketFunction) {
		Objects.requireNonNull(packet, "Packet cannot be null");
		Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

		PacketByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		return createPacketFunction.apply(packet.getType().getId(), buf);
	}

	public static <C> PacketSender<C> toQuiltSender(net.fabricmc.fabric.api.networking.v1.PacketSender sender) {
		if (sender instanceof Quilt2FabricPacketSender s) {
			return (PacketSender<C>) s.sender;
		} else {
			return new Fabric2QuiltPacketSender<C>(sender);
		}
	}

	public static net.fabricmc.fabric.api.networking.v1.PacketSender toFabricSender(PacketSender<?> sender) {
		if (sender instanceof Fabric2QuiltPacketSender<?> s) {
			return s.sender;
		} else {
			return new Quilt2FabricPacketSender(sender);
		}
	}
}
