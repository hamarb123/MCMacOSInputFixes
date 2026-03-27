package com.hamarb123.macos_input_fixes.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public interface MouseHandlerInvokerMixin
{
	//allows the onScroll method to be invoked since with these mappings it is private
	@Invoker
	public void callOnScroll(long window, double horizontal, double vertical);
}
