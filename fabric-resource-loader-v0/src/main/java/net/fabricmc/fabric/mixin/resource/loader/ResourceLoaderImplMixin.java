package net.fabricmc.fabric.mixin.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;
import org.quiltmc.qsl.resource.loader.mixin.NamespaceResourceManagerAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.fabricmc.fabric.impl.resource.loader.FabricResource;
import net.fabricmc.fabric.impl.resource.loader.FabricResourceImpl;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.fabric.impl.resource.loader.ResourcePackSourceTracker;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.util.Identifier;

@Mixin(ResourceLoaderImpl.class)
abstract class ResourceLoaderImplMixin {
	@Inject(
		method = "appendResourcesFromGroup",
		at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", remap = false, shift = At.Shift.AFTER),
		locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void setSourcesForGroupResources(NamespaceResourceManagerAccessor manager, Identifier id, GroupResourcePack groupResourcePack, List<Resource> resources, CallbackInfo ci, List<ResourcePack> packs, Identifier metadataId, Iterator<ResourcePack> iterator, ResourcePack pack, InputStream metadataInputStream) throws IOException {
		if (resources.get(resources.size() - 1) instanceof FabricResourceImpl resource) {
			resource.setFabricPackSource(ResourcePackSourceTracker.getSource(pack));
		}
	}
}
