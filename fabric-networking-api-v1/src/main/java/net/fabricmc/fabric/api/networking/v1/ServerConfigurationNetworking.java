/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2024 The Quilt Project
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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.impl.networking.QuiltUtils;

/**
 * Offers access to configuration stage server-side networking functionalities.
 *
 * <p>Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side network handlers.
 *
 * <p>This class should be only used for the logical server.
 *
 * <p>See {@link ServerPlayNetworking} for information on how to use the packet
 * object-based API.
 *
 * <p>See the documentation on each class for more information.
 *
 * @see ServerLoginNetworking
 * @see ServerConfigurationNetworking
 * @deprecated Use Quilt Networking's {@link org.quiltmc.qsl.networking.api.ServerConfigurationNetworking} instead.
 */
@Deprecated
public final class ServerConfigurationNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The handler runs on the network thread. After reading the buffer there, the server
	 * must be modified in the server thread by calling {@link ThreadExecutor#execute(Runnable)}.
	 *
	 * <p>If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * <p>For new code, {@link #registerGlobalReceiver(PacketType, ConfigurationPacketHandler)}
	 * is preferred, as it is designed in a way that prevents thread safety issues.
	 *
	 * @param channelName the id of the channel
	 * @param channelHandler the handler
	 * @return false if a handler is already registered to the channel
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationNetworkHandler, Identifier, ConfigurationChannelHandler)
	 */
	public static boolean registerGlobalReceiver(Identifier channelName, ConfigurationChannelHandler channelHandler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Registers a handler for a packet type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, PacketType)} to unregister the existing handler.
	 *
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel
	 * @see ServerConfigurationNetworking#unregisterGlobalReceiver(PacketType)
	 * @see ServerConfigurationNetworking#registerReceiver(ServerConfigurationNetworkHandler, PacketType, ConfigurationPacketHandler)
	 */
	public static <T extends FabricPacket> boolean registerGlobalReceiver(PacketType<T> type, ConfigurationPacketHandler<T> handler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.registerGlobalReceiver(type.getId(), wrapTyped(type, handler));
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(Identifier, ConfigurationChannelHandler)
	 * @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)
	 */
	@Nullable
	public static ServerConfigurationNetworking.ConfigurationChannelHandler unregisterGlobalReceiver(Identifier channelName) {
		return (ConfigurationChannelHandler) org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.unregisterGlobalReceiver(channelName);
	}

	/**
	 * Removes the handler for a packet type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code type} is guaranteed not to have an associated handler after this call.
	 *
	 * @param type the packet type
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerGlobalReceiver(PacketType, ConfigurationPacketHandler)}
	 * @see ServerConfigurationNetworking#registerGlobalReceiver(PacketType, ConfigurationPacketHandler)
	 * @see ServerConfigurationNetworking#unregisterReceiver(ServerConfigurationNetworkHandler, PacketType)
	 */
	@Nullable
	public static <T extends FabricPacket> ServerConfigurationNetworking.ConfigurationPacketHandler<T> unregisterGlobalReceiver(PacketType<T> type) {
		ConfigurationChannelHandler handler = (ConfigurationChannelHandler) org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.unregisterGlobalReceiver(type.getId());
		return handler instanceof ConfigurationPacketWrapper<?> proxy ? (ConfigurationPacketHandler<T>) proxy.actualHandler() : null;
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.getGlobalReceivers();
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(Identifier, ConfigurationChannelHandler)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationNetworkHandler}.
	 *
	 * <p>The handler runs on the network thread. After reading the buffer there, the world
	 * must be modified in the server thread by calling {@link ThreadExecutor#execute(Runnable)}.
	 *
	 * <p>For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * <p>If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * <p>For new code, {@link #registerReceiver(ServerConfigurationNetworkHandler, PacketType, ConfigurationPacketHandler)}
	 * is preferred, as it is designed in a way that prevents thread safety issues.
	 *
	 * @param networkHandler the handler
	 * @param channelName the id of the channel
	 * @param channelHandler the handler
	 * @return false if a handler is already registered to the channel name
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static boolean registerReceiver(ServerConfigurationNetworkHandler networkHandler, Identifier channelName, ConfigurationChannelHandler channelHandler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.registerReceiver(networkHandler, channelName, channelHandler);
	}

	/**
	 * Registers a handler for a packet type.
	 * This method differs from {@link ServerConfigurationNetworking#registerGlobalReceiver(PacketType, ConfigurationPacketHandler)} since
	 * the channel handler will only be applied to the client represented by the {@link ServerConfigurationNetworkHandler}.
	 *
	 * <p>For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerConfigurationNetworkHandler, PacketType)} to unregister the existing handler.
	 *
	 * @param networkHandler the network handler
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel name
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static <T extends FabricPacket> boolean registerReceiver(ServerConfigurationNetworkHandler networkHandler, PacketType<T> type, ConfigurationPacketHandler<T> handler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.registerReceiver(networkHandler, type.getId(), wrapTyped(type, handler));
	}

	/**
	 * Removes the handler of a channel.
	 *
	 * <p>The {@code channelName} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel name
	 */
	@Nullable
	public static ServerConfigurationNetworking.ConfigurationChannelHandler unregisterReceiver(ServerConfigurationNetworkHandler networkHandler, Identifier channelName) {
		var old = org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.unregisterReceiver(networkHandler, channelName);

		if (old instanceof ConfigurationChannelHandler fabric) {
			return fabric;
		} else if (old != null) {
			return (server, handler, buf, responseSender) -> {
				if (old instanceof org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.ChannelReceiver r) {
					r.receive(server, handler, buf, QuiltUtils.toQuiltSender(responseSender));
				} else {
					throw new UnsupportedOperationException("Receiver does not accept byte bufs, cannot bridge to Quilt");
				}
			};
		} else {
			return null;
		}
	}

	/**
	 * Removes the handler for a packet type.
	 *
	 * <p>The {@code type} is guaranteed not to have an associated handler after this call.
	 *
	 * @param type the type of the packet
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerReceiver(ServerConfigurationNetworkHandler, PacketType, ConfigurationPacketHandler)}
	 */
	@Nullable
	public static <T extends FabricPacket> ServerConfigurationNetworking.ConfigurationPacketHandler<T> unregisterReceiver(ServerConfigurationNetworkHandler networkHandler, PacketType<T> type) {
		final var receiver = org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.unregisterReceiver(networkHandler, type.getId());

		if (receiver != null) {
			return unwrapTyped(receiver);
		}

		throw new IllegalStateException("Cannot unregister receiver while not in game!");
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerConfigurationNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.getReceived(handler);
	}

	/**
	 * Gets all channel names that a connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static Set<Identifier> getSendable(ServerConfigurationNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.getSendable(handler);
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerConfigurationNetworkHandler handler, Identifier channelName) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.canSend(handler, channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param handler the network handler
	 * @param type the packet type
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerConfigurationNetworkHandler handler, PacketType<?> type) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.canSend(handler, type.getId());
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param channelName the channel name
	 * @param buf the packet byte buf which represents the payload of the packet
	 * @return a new packet
	 */
	public static Packet<ClientCommonPacketListener> createS2CPacket(Identifier channelName, PacketByteBuf buf) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.createS2CPacket(channelName, buf);
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param packet the fabric packet
	 * @return a new packet
	 */
	public static <T extends FabricPacket> Packet<ClientCommonPacketListener> createS2CPacket(T packet) {
		return QuiltUtils.createS2CPacket(packet, ServerConfigurationNetworking::createS2CPacket);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the player/client
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerConfigurationNetworkHandler handler) {
		return QuiltUtils.toFabricSender(org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.getSender(handler));
	}

	/**
	 * Sends a packet to a configuring player.
	 *
	 * @param handler the handler to send the packet to
	 * @param channelName the channel of the packet
	 * @param buf the payload of the packet.
	 */
	public static void send(ServerConfigurationNetworkHandler handler, Identifier channelName, PacketByteBuf buf) {
		Objects.requireNonNull(handler, "Server configuration entity cannot be null");
		Objects.requireNonNull(channelName, "Channel name cannot be null");
		Objects.requireNonNull(buf, "Packet byte buf cannot be null");

		handler.sendPacket(createS2CPacket(channelName, buf));
	}

	/**
	 * Sends a packet to a configuring player.
	 *
	 * @param handler the network handler to send the packet to
	 * @param packet the packet
	 */
	public static <T extends FabricPacket> void send(ServerConfigurationNetworkHandler handler, T packet) {
		Objects.requireNonNull(handler, "Server configuration handler cannot be null");
		Objects.requireNonNull(packet, "Packet cannot be null");
		Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

		handler.sendPacket(createS2CPacket(packet));
	}

	// Helper methods

	/**
	 * Returns the <i>Minecraft</i> Server of a server configuration network handler.
	 *
	 * @param handler the server configuration network handler
	 */
	public static MinecraftServer getServer(ServerConfigurationNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.getServer(handler);
	}

	private ServerConfigurationNetworking() {
	}

	private record ConfigurationPacketWrapper<T extends FabricPacket>(PacketType<T> type, ConfigurationPacketHandler<T> actualHandler) implements ConfigurationChannelHandler {
		@Override
		public void receive(MinecraftServer server, ServerConfigurationNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			T packet = type.read(buf);

			if (server.isOnThread()) {
				actualHandler.receive(packet, handler, responseSender);
			} else {
				server.execute(() -> {
					if (((org.quiltmc.qsl.networking.mixin.accessor.AbstractServerPacketHandlerAccessor) handler).getConnection().isOpen()) {
						actualHandler.receive(packet, handler, responseSender);
					}
				});
			}
		}
	}

	private static <T extends FabricPacket> ConfigurationChannelHandler wrapTyped(PacketType<T> type, ConfigurationPacketHandler<T> actualHandler) {
		return new ConfigurationPacketWrapper<>(type, actualHandler);
	}

	@Nullable
	@SuppressWarnings({"unchecked"})
	private static <T extends FabricPacket> ConfigurationPacketHandler<T> unwrapTyped(@Nullable org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.CustomChannelReceiver<?> receiver) {
		if (receiver == null) return null;
		if (receiver instanceof ConfigurationPacketWrapper<?> wrapper) return (ConfigurationPacketHandler<T>) wrapper.actualHandler();
		return null;
	}

	@FunctionalInterface
	public interface ConfigurationChannelHandler extends org.quiltmc.qsl.networking.api.ServerConfigurationNetworking.ChannelReceiver {
		default void receive(MinecraftServer server, ServerConfigurationNetworkHandler handler, PacketByteBuf buf, org.quiltmc.qsl.networking.api.PacketSender<CustomPayload> responseSender) {
			receive(server, handler, buf, QuiltUtils.toFabricSender(responseSender));
		}

		/**
		 * Handles an incoming packet.
		 *
		 * <p>This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain ThreadExecutor#submit(Runnable) scheduled} using the server instance from {@link ServerConfigurationNetworking#getServer(ServerConfigurationNetworkHandler)}.
		 *
		 * <p>An example usage of this is:
		 * <pre>{@code
		 * ServerConfigurationNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, handler, buf, responseSender) -> {
		 * 	boolean fire = buf.readBoolean();
		 *
		 * 	// All operations on the server must be executed on the server thread
		 * 	server.execute(() -> {
		 *
		 * 	});
		 * });
		 * }</pre>
		 * @param server the server
		 * @param handler the network handler that received this packet, representing the client who sent the packet
		 * @param buf the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerConfigurationNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender);
	}

	/**
	 * A thread-safe packet handler utilizing {@link FabricPacket}.
	 * @param <T> the type of the packet
	 */
	@FunctionalInterface
	public interface ConfigurationPacketHandler<T extends FabricPacket> {
		/**
		 * Handles an incoming packet.
		 *
		 * <p>Unlike {@link ServerPlayNetworking.PlayPacketHandler} this method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain ThreadExecutor#submit(Runnable) scheduled} using the Minecraft server instance from {@link ServerConfigurationNetworking#getServer(ServerConfigurationNetworkHandler)}.
		 *
		 * <p>An example usage of this:
		 * <pre>{@code
		 * // See FabricPacket for creating the packet
		 * ServerConfigurationNetworking.registerReceiver(BOOM_PACKET_TYPE, (packet, responseSender) -> {
		 *
		 * });
		 * }</pre>
		 *
		 *
		 * @param packet the packet
		 * @param networkHandler the network handler
		 * @param responseSender the packet sender
		 * @see FabricPacket
		 */
		void receive(T packet, ServerConfigurationNetworkHandler networkHandler, PacketSender responseSender);
	}
}
