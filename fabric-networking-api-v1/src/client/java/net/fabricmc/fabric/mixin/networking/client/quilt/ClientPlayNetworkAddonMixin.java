package net.fabricmc.fabric.mixin.networking.client.quilt;

import org.quiltmc.qsl.networking.impl.client.ClientPlayNetworkAddon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.MinecraftClient;

// TODO - Have a solution on Quilt's side that makes these mixins unnecessary
@Mixin(ClientPlayNetworkAddon.class)
public class ClientPlayNetworkAddonMixin {
	@Redirect(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;isOnThread()Z"))
	public boolean removeEquivalentCode(MinecraftClient instance) {
		return false;
	}
}
