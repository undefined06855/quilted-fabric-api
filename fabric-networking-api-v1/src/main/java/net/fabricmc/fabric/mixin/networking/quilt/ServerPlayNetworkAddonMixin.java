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
