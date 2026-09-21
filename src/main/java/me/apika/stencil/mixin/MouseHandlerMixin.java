package me.apika.stencil.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import me.apika.stencil.input.Keybind;
import me.apika.stencil.spawnproof.SpawnProofManager;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin
{
	@Inject(method = "onButton", at = @At("HEAD"))
	private void stencil$onButton(long window, MouseButtonInfo info, int action, CallbackInfo ci)
	{
		Keybind.onMouseButton(info.button(), action == InputConstants.PRESS);
	}

	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void stencil$onScroll(long window, double xOffset, double yOffset, CallbackInfo ci)
	{
		if (SpawnProofManager.getInstance().onScroll(yOffset))
		{
			ci.cancel();
		}
	}
}
