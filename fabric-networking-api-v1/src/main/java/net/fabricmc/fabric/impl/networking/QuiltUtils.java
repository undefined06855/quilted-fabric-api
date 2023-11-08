package net.fabricmc.fabric.impl.networking;

import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketSender;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class QuiltUtils {
	public static <T extends PacketListener> Packet<T> createS2CPacket(FabricPacket packet, BiFunction<Identifier, PacketByteBuf, Packet<T>> createPacketFunction) {
		Objects.requireNonNull(packet, "Packet cannot be null");
		Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

		PacketByteBuf buf = PacketByteBufs.create();
		packet.write(buf);
		return createPacketFunction.apply(packet.getType().getId(), buf);
	}

	public static PacketSender toQuiltSender(net.fabricmc.fabric.api.networking.v1.PacketSender sender) {
		if (sender instanceof Quilt2FabricPacketSender s) {
			return s.sender;
		} else {
			return new Fabric2QuiltPacketSender(sender);
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
