package com.hamarb123.macos_input_fixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.menu.options.MouseOptionsScreen;

@Mixin(MouseOptionsScreen.class)
public class MouseOptionsScreenMixin
{
	//not mixed in properly
	@Inject(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonListWidget;addAll([Lnet/minecraft/client/options/Option;)V"))
	private void init_BeforeInvoke(CallbackInfo info)
	{
		//enable the mixin for addAll so that our options are added to the screen
		Common.setModifyAddAllParameter(true);
	}
}
