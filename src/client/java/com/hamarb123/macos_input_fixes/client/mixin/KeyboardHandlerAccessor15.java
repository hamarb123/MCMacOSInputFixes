package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor15
{
	@Invoker
	void callKeyPress(long handle, int action, KeyEvent event);
}
