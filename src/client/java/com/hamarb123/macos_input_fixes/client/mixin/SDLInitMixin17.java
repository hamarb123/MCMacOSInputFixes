package com.hamarb123.macos_input_fixes.client.mixin;

import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SDLInit.class)
public class SDLInitMixin17
{
	@Inject(method = "SDL_Init", at = @At("HEAD"))
	private static void SDL_Init(int flags, CallbackInfoReturnable<Boolean> callbackInfo)
	{
		// We use this to enable momentum scrolling inside interfaces, like previously.
		SDLHints.SDL_SetHint(SDLHints.SDL_HINT_MAC_SCROLL_MOMENTUM, "1");
	}
}
