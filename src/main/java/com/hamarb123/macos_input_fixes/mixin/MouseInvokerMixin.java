package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public interface MouseInvokerMixin
{
	@Invoker
	public void callOnMouseScroll(long window, double horizontal, double vertical);
}
