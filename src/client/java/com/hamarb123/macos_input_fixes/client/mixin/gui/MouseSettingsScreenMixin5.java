package com.hamarb123.macos_input_fixes.client.mixin.gui;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hamarb123.macos_input_fixes.client.FabricReflectionHelper;

import net.minecraft.client.gui.screens.options.MouseSettingsScreen;
import net.minecraft.client.gui.components.OptionsList;
import com.mojang.blaze3d.vertex.PoseStack;

@Mixin(MouseSettingsScreen.class)
public abstract class MouseSettingsScreenMixin5
{
	@Shadow(aliases = "field_19246", remap = false)
	private OptionsList buttonList;

	@Inject(method = "method_25394(Lnet/minecraft/class_4587;IIF)V", at = @At("RETURN"), cancellable = true, remap = false)
	private void render(PoseStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		List<?> list = (List<?>)FabricReflectionHelper.GameOptionsScreen_getHoveredButtonTooltip(this.buttonList, mouseX, mouseY);
		if (list != null) ((ScreenAccessor7)this).renderOrderedTooltip(matrices, list, mouseX, mouseY);
	}
}
