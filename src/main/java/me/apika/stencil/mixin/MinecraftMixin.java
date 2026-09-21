package me.apika.stencil.mixin;

import me.apika.stencil.spawnproof.SpawnProofManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin
{
	/** Runs on the use press and on every held repeat, so a held right click keeps placing. */
	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void stencil$startUseItem(CallbackInfo ci)
	{
		if (SpawnProofManager.getInstance().onUse())
		{
			ci.cancel();
		}
	}
}
