package com.hamarb123.macos_input_fixes.client.mixin.gui;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(targets = "net.minecraft.class_437" /*Screen*/, remap = false)
public interface ScreenAccessor7
{
	@Invoker(value = "method_25417(Lnet/minecraft/class_4587;Ljava/util/List;II)V", remap = false)
	public void renderOrderedTooltip(PoseStack matrices, List<?> lines, int x, int y);
}
