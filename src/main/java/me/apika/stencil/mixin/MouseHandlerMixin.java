package me.apika.stencil.mixin;

import me.apika.stencil.spawnproof.SpawnProofManager;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void stencil$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci)
	{
		if (SpawnProofManager.getInstance().onScroll(yOffset))
		{
			ci.cancel();
		}
	}
}
