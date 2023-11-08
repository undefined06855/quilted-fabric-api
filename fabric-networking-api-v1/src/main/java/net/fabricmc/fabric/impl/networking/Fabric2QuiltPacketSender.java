package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketSender;

class Fabric2QuiltPacketSender<C> implements PacketSender<C> {

	final net.fabricmc.fabric.api.networking.v1.PacketSender sender;

	Fabric2QuiltPacketSender(net.fabricmc.fabric.api.networking.v1.PacketSender fabric) {
		this.sender = fabric;
	}
	@Override
	public Packet<?> createPacket(Identifier channelName, PacketByteBuf buf) {
		return null;
	}

	@Override
	public Packet<?> createPacket(C payload) {
		return null;
	}

	@Override
	public void sendPacket(Packet<?> packet) {

	}

	@Override
	public void sendPacket(Packet<?> packet, @Nullable PacketCallbacks listener) {

	}
}
