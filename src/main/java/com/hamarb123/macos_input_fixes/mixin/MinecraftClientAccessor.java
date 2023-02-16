package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.Window;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor
{
	//allows the window field to be accessed since with these mappings it is private
	//we could use the getWindow() function, but it is only available in 1.15+
	@Accessor
	Window getWindow();

	@Accessor
	TextRenderer getTextRenderer();
}
