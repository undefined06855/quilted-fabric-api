/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.thread.ThreadExecutor;

import net.fabricmc.fabric.impl.networking.QuiltUtils;

/**
 * Offers access to play stage server-side networking functionalities.
 *
 * <p>Server-side networking functionalities include receiving serverbound packets, sending clientbound packets, and events related to server-side network handlers.
 *
 * <p>This class should be only used for the logical server.
 *
 * <h2>Packet object-based API</h2>
 *
 * <p>This class provides a classic registration method, {@link #registerGlobalReceiver(Identifier, PlayChannelHandler)},
 * and a newer method utilizing packet objects, {@link #registerGlobalReceiver(PacketType, PlayPacketHandler)}.
 * For most mods, using the newer method will improve the readability of the code by separating packet
 * reading/writing code to a separate class. Additionally, the newer method executes the callback in the
 * server thread, ensuring thread safety. For this reason using the newer method is highly recommended.
 * The two methods are network-compatible with each other, so long as the buffer contents are read and written
 * in the same order.
 *
 * <p>The newer, packet object-based API involves three classes:
 *
 * <ul>
 *     <li>A class implementing {@link FabricPacket} that is "sent" over the network</li>
 *     <li>{@link PacketType} instance, which represents the packet's type (and its channel)</li>
 *     <li>{@link PlayPacketHandler}, which handles the packet (usually implemented as a functional interface)</li>
 * </ul>
 *
 * <p>See the documentation on each class for more information.
 *
 * @see ServerLoginNetworking
 * @see ServerConfigurationNetworking
 * @deprecated Use Quilt Networking's {@link org.quiltmc.qsl.networking.api.ServerPlayNetworking} instead.
 */
@Deprecated
public final class ServerPlayNetworking {
	/**
	 * Registers a handler to a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The handler runs on the network thread. After reading the buffer there, the world
	 * must be modified in the server thread by calling {@link ThreadExecutor#execute(Runnable)}.
	 *
	 * <p>If a handler is already registered to the {@code channel}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * <p>For new code, {@link #registerGlobalReceiver(PacketType, PlayPacketHandler)}
	 * is preferred, as it is designed in a way that prevents thread safety issues.
	 *
	 * @param channelName the id of the channel
	 * @param channelHandler the handler
	 * @return false if a handler is already registered to the channel
	 * @see ServerPlayNetworking#unregisterGlobalReceiver(Identifier)
	 * @see ServerPlayNetworking#registerReceiver(ServerPlayNetworkHandler, Identifier, PlayChannelHandler)
	 */
	public static boolean registerGlobalReceiver(Identifier channelName, PlayChannelHandler channelHandler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.registerGlobalReceiver(channelName, channelHandler);
	}

	/**
	 * Registers a handler for a packet type.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, PacketType)} to unregister the existing handler.
	 *
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel
	 * @see ServerPlayNetworking#unregisterGlobalReceiver(PacketType)
	 * @see ServerPlayNetworking#registerReceiver(ServerPlayNetworkHandler, PacketType, PlayPacketHandler)
	 */
	public static <T extends FabricPacket> boolean registerGlobalReceiver(PacketType<T> type, PlayPacketHandler<T> handler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.registerGlobalReceiver(type.getId(), wrapTyped(type, handler));
	}

	/**
	 * Removes the handler of a channel.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code channel} is guaranteed not to have a handler after this call.
	 *
	 * @param channelName the id of the channel
	 * @return the previous handler, or {@code null} if no handler was bound to the channel
	 * @see ServerPlayNetworking#registerGlobalReceiver(Identifier, PlayChannelHandler)
	 * @see ServerPlayNetworking#unregisterReceiver(ServerPlayNetworkHandler, Identifier)
	 */
	@Nullable
	public static PlayChannelHandler unregisterGlobalReceiver(Identifier channelName) {
		org.quiltmc.qsl.networking.api.ServerPlayNetworking.CustomChannelReceiver old = org.quiltmc.qsl.networking.api.ServerPlayNetworking.unregisterGlobalReceiver(channelName);

		if (old instanceof PlayChannelHandler fabric) {
			return fabric;
		} else if (old != null) {
			return (server, player, buf, handler, sender) -> {
				if (old instanceof org.quiltmc.qsl.networking.api.ServerPlayNetworking.ChannelReceiver r) {
					r.receive(server, player, buf, handler, QuiltUtils.toQuiltSender(sender));
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
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * <p>The {@code type} is guaranteed not to have an associated handler after this call.
	 *
	 * @param type the packet type
	 * @return the previous handler, or {@code null} if no handler was bound to the channel,
	 * or it was not registered using {@link #registerGlobalReceiver(PacketType, PlayPacketHandler)}
	 * @see ServerPlayNetworking#registerGlobalReceiver(PacketType, PlayPacketHandler)
	 * @see ServerPlayNetworking#unregisterReceiver(ServerPlayNetworkHandler, PacketType)
	 */
	@Nullable
	public static <T extends FabricPacket> PlayPacketHandler<T> unregisterGlobalReceiver(PacketType<T> type) {
		PlayChannelHandler handler = (PlayChannelHandler) ServerNetworkingImpl.PLAY.unregisterGlobalReceiver(type.getId());
		return handler instanceof PlayPacketWrapper<?> proxy ? (PlayPacketHandler<T>) proxy.actualHandler() : null;
	}

	/**
	 * Gets all channel names which global receivers are registered for.
	 * A global receiver is registered to all connections, in the present and future.
	 *
	 * @return all channel names which global receivers are registered for.
	 */
	public static Set<Identifier> getGlobalReceivers() {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getGlobalReceivers();
	}

	/**
	 * Registers a handler to a channel.
	 * This method differs from {@link ServerPlayNetworking#registerGlobalReceiver(Identifier, PlayChannelHandler)} since
	 * the channel handler will only be applied to the player represented by the {@link ServerPlayNetworkHandler}.
	 *
	 * <p>The handler runs on the network thread. After reading the buffer there, the world
	 * must be modified in the server thread by calling {@link ThreadExecutor#execute(Runnable)}.
	 *
	 * <p>For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * <p>If a handler is already registered to the {@code channelName}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, Identifier)} to unregister the existing handler.
	 *
	 * <p>For new code, {@link #registerReceiver(ServerPlayNetworkHandler, PacketType, PlayPacketHandler)}
	 * is preferred, as it is designed in a way that prevents thread safety issues.
	 *
	 * @param networkHandler the handler
	 * @param channelName the id of the channel
	 * @param channelHandler the handler
	 * @return false if a handler is already registered to the channel name
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static boolean registerReceiver(ServerPlayNetworkHandler networkHandler, Identifier channelName, PlayChannelHandler channelHandler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.registerReceiver(networkHandler, channelName, channelHandler);
	}

	/**
	 * Registers a handler for a packet type.
	 * This method differs from {@link ServerPlayNetworking#registerGlobalReceiver(PacketType, PlayPacketHandler)} since
	 * the channel handler will only be applied to the player represented by the {@link ServerPlayNetworkHandler}.
	 *
	 * <p>For example, if you only register a receiver using this method when a {@linkplain ServerLoginNetworking#registerGlobalReceiver(Identifier, ServerLoginNetworking.LoginQueryResponseHandler)}
	 * login response has been received, you should use {@link ServerPlayConnectionEvents#INIT} to register the channel handler.
	 *
	 * <p>If a handler is already registered for the {@code type}, this method will return {@code false}, and no change will be made.
	 * Use {@link #unregisterReceiver(ServerPlayNetworkHandler, PacketType)} to unregister the existing handler.
	 *
	 * @param networkHandler the network handler
	 * @param type the packet type
	 * @param handler the handler
	 * @return {@code false} if a handler is already registered to the channel name
	 * @see ServerPlayConnectionEvents#INIT
	 */
	public static <T extends FabricPacket> boolean registerReceiver(ServerPlayNetworkHandler networkHandler, PacketType<T> type, PlayPacketHandler<T> handler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.registerReceiver(networkHandler, type.getId(), wrapTyped(type, handler));
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
	public static PlayChannelHandler unregisterReceiver(ServerPlayNetworkHandler networkHandler, Identifier channelName) {
		var old = org.quiltmc.qsl.networking.api.ServerPlayNetworking.unregisterReceiver(networkHandler, channelName);

		if (old instanceof PlayChannelHandler fabric) {
			return fabric;
		} else if (old != null) {
			return (server, player, handler, buf, sender) -> {
				if (old instanceof org.quiltmc.qsl.networking.api.ServerPlayNetworking.ChannelReceiver r) {
					r.receive(server, player, handler, buf, QuiltUtils.toQuiltSender(sender));
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
	 * or it was not registered using {@link #registerReceiver(ServerPlayNetworkHandler, PacketType, PlayPacketHandler)}
	 */
	@Nullable
	public static <T extends FabricPacket> PlayPacketHandler<T> unregisterReceiver(ServerPlayNetworkHandler networkHandler, PacketType<T> type) {
		final var receiver = org.quiltmc.qsl.networking.api.ServerPlayNetworking.unregisterReceiver(networkHandler, type.getId());

		if (receiver != null) {
			return unwrapTyped(receiver);
		}

		throw new IllegalStateException("Cannot unregister receiver while not in game!");
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param player the player
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerPlayerEntity player) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getReceived(player);
	}

	/**
	 * Gets all the channel names that the server can receive packets on.
	 *
	 * @param handler the network handler
	 * @return All the channel names that the server can receive packets on
	 */
	public static Set<Identifier> getReceived(ServerPlayNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getReceived(handler);
	}

	/**
	 * Gets all channel names that the connected client declared the ability to receive a packets on.
	 *
	 * @param player the player
	 * @return All the channel names the connected client declared the ability to receive a packets on
	 */
	public static Set<Identifier> getSendable(ServerPlayerEntity player) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getSendable(player);
	}

	/**
	 * Gets all channel names that a connected client declared the ability to receive a packets on.
	 *
	 * @param handler the network handler
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static Set<Identifier> getSendable(ServerPlayNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getSendable(handler);
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param player the player
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerPlayerEntity player, Identifier channelName) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.canSend(player, channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param player the player
	 * @param type the packet type
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerPlayerEntity player, PacketType<?> type) {
		Objects.requireNonNull(player, "Server player entity cannot be null");

		return canSend(player.networkHandler, type.getId());
	}

	/**
	 * Checks if the connected client declared the ability to receive a packet on a specified channel name.
	 *
	 * @param handler the network handler
	 * @param channelName the channel name
	 * @return {@code true} if the connected client has declared the ability to receive a packet on the specified channel
	 */
	public static boolean canSend(ServerPlayNetworkHandler handler, Identifier channelName) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.canSend(handler, channelName);
	}

	/**
	 * Checks if the connected client declared the ability to receive a specific type of packet.
	 *
	 * @param handler the network handler
	 * @param type the packet type
	 * @return {@code true} if the connected client has declared the ability to receive a specific type of packet
	 */
	public static boolean canSend(ServerPlayNetworkHandler handler, PacketType<?> type) {
		Objects.requireNonNull(handler, "Server play network handler cannot be null");
		Objects.requireNonNull(type, "Packet type cannot be null");

		return ServerNetworkingImpl.getAddon(handler).getSendableChannels().contains(type.getId());
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param channelName the channel name
	 * @param buf the packet byte buf which represents the payload of the packet
	 * @return a new packet
	 */
	public static Packet<ClientCommonPacketListener> createS2CPacket(Identifier channelName, PacketByteBuf buf) {
		Objects.requireNonNull(channelName, "Channel cannot be null");
		Objects.requireNonNull(buf, "Buf cannot be null");

		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.createS2CPacket(channelName, buf);
	}

	/**
	 * Creates a packet which may be sent to a connected client.
	 *
	 * @param packet the fabric packet
	 * @return a new packet
	 */
	public static <T extends FabricPacket> Packet<ClientCommonPacketListener> createS2CPacket(T packet) {
		return QuiltUtils.createS2CPacket(packet, ServerPlayNetworking::createS2CPacket);
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param player the player
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerPlayerEntity player) {
		return QuiltUtils.toFabricSender(org.quiltmc.qsl.networking.api.ServerPlayNetworking.getSender(player));
	}

	/**
	 * Gets the packet sender which sends packets to the connected client.
	 *
	 * @param handler the network handler, representing the connection to the player/client
	 * @return the packet sender
	 */
	public static PacketSender getSender(ServerPlayNetworkHandler handler) {
		return QuiltUtils.toFabricSender(org.quiltmc.qsl.networking.api.ServerPlayNetworking.getSender(handler));
	}

	/**
	 * Sends a packet to a player.
	 *
	 * @param player the player to send the packet to
	 * @param channelName the channel of the packet
	 * @param buf the payload of the packet.
	 */
	public static void send(ServerPlayerEntity player, Identifier channelName, PacketByteBuf buf) {
		org.quiltmc.qsl.networking.api.ServerPlayNetworking.send(player, channelName, buf);
	}

	/**
	 * Sends a packet to a player.
	 *
	 * @param player the player to send the packet to
	 * @param packet the packet
	 */
	public static <T extends FabricPacket> void send(ServerPlayerEntity player, T packet) {
		Objects.requireNonNull(player, "Server player entity cannot be null");
		Objects.requireNonNull(packet, "Packet cannot be null");
		Objects.requireNonNull(packet.getType(), "Packet#getType cannot return null");

		player.networkHandler.sendPacket(createS2CPacket(packet));
	}

	// Helper methods

	/**
	 * Returns the <i>Minecraft</i> Server of a server play network handler.
	 *
	 * @param handler the server play network handler
	 */
	public static MinecraftServer getServer(ServerPlayNetworkHandler handler) {
		return org.quiltmc.qsl.networking.api.ServerPlayNetworking.getServer(handler);
	}

	private ServerPlayNetworking() {
	}

	private record PlayPacketWrapper<T extends FabricPacket>(PacketType<T> type, PlayPacketHandler<T> actualHandler) implements PlayChannelHandler {
		@Override
		public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			T packet = type.read(buf);

			if (server.isOnThread()) {
				// Do not submit to the render thread if we're already running there.
				// Normally, packets are handled on the network IO thread - though it is
				// not guaranteed (for example, with 1.19.4 S2C packet bundling)
				// Since we're handling it right now, connection check is redundant.
				actualHandler.receive(packet, player, responseSender);
			} else {
				server.execute(() -> {
					if (handler.isConnectionOpen()) actualHandler.receive(packet, player, responseSender);
				});
			}
		}
	}

	private static <T extends FabricPacket> PlayChannelHandler wrapTyped(PacketType<T> type, PlayPacketHandler<T> actualHandler) {
		return new PlayPacketWrapper<>(type, actualHandler);
	}

	@Nullable
	@SuppressWarnings({"unchecked"})
	private static <T extends FabricPacket> PlayPacketHandler<T> unwrapTyped(@Nullable org.quiltmc.qsl.networking.api.ServerPlayNetworking.CustomChannelReceiver<?> receiver) {
		if (receiver == null) return null;
		if (receiver instanceof PlayPacketWrapper<?> wrapper) return (PlayPacketHandler<T>) wrapper.actualHandler();
		return null;
	}

	@Deprecated
	@FunctionalInterface
	public interface PlayChannelHandler extends org.quiltmc.qsl.networking.api.ServerPlayNetworking.ChannelReceiver {
		@Override
		default void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, org.quiltmc.qsl.networking.api.PacketSender responseSender) {
			this.receive(server, player, handler, buf, QuiltUtils.toFabricSender(responseSender));
		}

		/**
		 * Handles an incoming packet.
		 *
		 * <p>This method is executed on {@linkplain io.netty.channel.EventLoop netty's event loops}.
		 * Modification to the game should be {@linkplain ThreadExecutor#submit(Runnable) scheduled} using the provided Minecraft server instance.
		 *
		 * <p>An example usage of this is to create an explosion where the player is looking:
		 * <pre>{@code
		 * ServerPlayNetworking.registerReceiver(new Identifier("mymod", "boom"), (server, player, handler, buf, responseSender) -> {
		 * 	boolean fire = buf.readBoolean();
		 *
		 * 	// All operations on the server or world must be executed on the server thread
		 * 	server.execute(() -> {
		 * 		ModPacketHandler.createExplosion(player, fire);
		 * 	});
		 * });
		 * }</pre>
		 * @param server the server
		 * @param player the player
		 * @param handler the network handler that received this packet, representing the player/client who sent the packet
		 * @param buf the payload of the packet
		 * @param responseSender the packet sender
		 */
		void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender);
	}

	/**
	 * A thread-safe packet handler utilizing {@link FabricPacket}.
	 * @param <T> the type of the packet
	 */
	@FunctionalInterface
	public interface PlayPacketHandler<T extends FabricPacket> {
		/**
		 * Handles the incoming packet. This is called on the server thread, and can safely
		 * manipulate the world.
		 *
		 * <p>An example usage of this is to create an explosion where the player is looking:
		 * <pre>{@code
		 * // See FabricPacket for creating the packet
		 * ServerPlayNetworking.registerReceiver(BOOM_PACKET_TYPE, (player, packet, responseSender) -> {
		 * 	ModPacketHandler.createExplosion(player, packet.fire());
		 * });
		 * }</pre>
		 *
		 * <p>The server and the network handler can be accessed via {@link ServerPlayerEntity#server}
		 * and {@link ServerPlayerEntity#networkHandler}, respectively.
		 *
		 * @param packet the packet
		 * @param player the player that received the packet
		 * @param responseSender the packet sender
		 * @see FabricPacket
		 */
		void receive(T packet, ServerPlayerEntity player, PacketSender responseSender);
	}
}
