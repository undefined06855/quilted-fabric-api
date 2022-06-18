package net.fabricmc.fabric.impl.registry.sync;

import org.quiltmc.qsl.command.api.ServerArgumentType;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class QuiltRegistryPatch {
	public static boolean checkForServerArgumentType(Identifier id) {
		return Registry.COMMAND_ARGUMENT_TYPE.get(id) instanceof ServerArgumentType;
	}
}
