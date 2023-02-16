package com.hamarb123.macos_input_fixes.mixin.gui;

import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.FabricReflectionHelper;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

@Mixin(MouseOptionsScreen.class)
public abstract class MouseOptionsScreenMixin5
{
    @Shadow
	private ButtonListWidget buttonList;

	@SuppressWarnings("unchecked")
	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;IIF)V", at = @At("RETURN"), cancellable = true)
	private void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo info)
	{
		List<OrderedText> list = (List<OrderedText>)FabricReflectionHelper.GameOptionsScreen_getHoveredButtonTooltip(this.buttonList, mouseX, mouseY);
		if (list != null) ((MouseOptionsScreen)(Object)this).renderOrderedTooltip(matrices, list, mouseX, mouseY);
	}
}
