package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.Mouse;

@Mixin(Mouse.class)
public interface MouseInvokerMixin
{
	//allows the onMouseScroll method to be invoked since with these mappings it is private
	@Invoker
	public void callOnMouseScroll(long window, double horizontal, double vertical);
}
