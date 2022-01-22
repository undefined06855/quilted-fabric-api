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

package net.fabricmc.fabric.impl.command.client;

import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.DISPATCHER;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v1.ClientCommandManager.literal;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;
import org.quiltmc.qsl.command.mixin.HelpCommandAccessor;

import net.minecraft.text.LiteralText;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Environment(EnvType.CLIENT)
public final class ClientCommandInternals implements ClientCommandRegistrationCallback {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String API_COMMAND_NAME = "fabric-command-api-v1:client";
	private static final String SHORT_API_COMMAND_NAME = "fcc";

	private static int executeRootHelp(CommandContext<FabricClientCommandSource> context) {
		return executeHelp(DISPATCHER.getRoot(), context);
	}

	private static int executeArgumentHelp(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		ParseResults<FabricClientCommandSource> parseResults = DISPATCHER.parse(StringArgumentType.getString(context, "command"), context.getSource());
		List<ParsedCommandNode<FabricClientCommandSource>> nodes = parseResults.getContext().getNodes();

		if (nodes.isEmpty()) {
			throw HelpCommandAccessor.getFailedException().create();
		}

		return executeHelp(Iterables.getLast(nodes).getNode(), context);
	}

	private static int executeHelp(CommandNode<FabricClientCommandSource> startNode, CommandContext<FabricClientCommandSource> context) {
		Map<CommandNode<FabricClientCommandSource>, String> commands = DISPATCHER.getSmartUsage(startNode, context.getSource());

		for (String command : commands.values()) {
			context.getSource().sendFeedback(new LiteralText("/" + command));
		}

		return commands.size();
	}

	public static void addCommands(CommandDispatcher<FabricClientCommandSource> target) {
		var originalToCopy = new Object2ObjectOpenHashMap<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>>();
		originalToCopy.put(DISPATCHER.getRoot(), target.getRoot());
		copyChildren(DISPATCHER.getRoot(), target.getRoot(), originalToCopy);
	}

	/**
	 * Copies the child commands from origin to target, filtered by {@code child.canUse(source)}.
	 * Mimics vanilla's CommandManager.makeTreeForSource.
	 *
	 * @param origin         the source command node
	 * @param target         the target command node
	 * @param originalToCopy a mutable map from original command nodes to their copies, used for redirects;
	 *                       should contain a mapping from origin to target
	 */
	private static void copyChildren(
			CommandNode<FabricClientCommandSource> origin,
			CommandNode<FabricClientCommandSource> target,
			Map<CommandNode<FabricClientCommandSource>, CommandNode<FabricClientCommandSource>> originalToCopy
	) {
		for (CommandNode<FabricClientCommandSource> child : origin.getChildren()) {
			ArgumentBuilder<FabricClientCommandSource, ?> builder = child.createBuilder();

			// Set up redirects
			if (builder.getRedirect() != null) {
				builder.redirect(originalToCopy.get(builder.getRedirect()));
			}

			CommandNode<FabricClientCommandSource> result = builder.build();
			originalToCopy.put(child, result);
			target.addChild(result);

			if (!child.getChildren().isEmpty()) {
				copyChildren(child, result, originalToCopy);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher) {
		if (!DISPATCHER.getRoot().getChildren().isEmpty()) {
			// Register an API command if there are other commands;
			// these helpers are not needed if there are no client commands
			LiteralArgumentBuilder<FabricClientCommandSource> help = literal("help");
			help.executes(ClientCommandInternals::executeRootHelp);
			help.then(argument("command", StringArgumentType.greedyString()).executes(ClientCommandInternals::executeArgumentHelp));

			CommandNode<FabricClientCommandSource> mainNode = DISPATCHER.register(literal(API_COMMAND_NAME).then(help));
			DISPATCHER.register(literal(SHORT_API_COMMAND_NAME).redirect(mainNode));

			addCommands((CommandDispatcher) dispatcher);
		}

		// noinspection CodeBlock2Expr
		DISPATCHER.findAmbiguities((parent, child, sibling, inputs) -> {
			LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", DISPATCHER.getPath(child), DISPATCHER.getPath(sibling), inputs);
		});
	}
}
