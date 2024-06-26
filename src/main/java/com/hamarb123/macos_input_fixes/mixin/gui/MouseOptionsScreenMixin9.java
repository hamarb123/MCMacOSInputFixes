package com.hamarb123.macos_input_fixes.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.hamarb123.macos_input_fixes.Common;
import net.minecraft.client.gui.screen.option.MouseOptionsScreen;

@Mixin(MouseOptionsScreen.class)
public class MouseOptionsScreenMixin9
{
	//sets the modifyAddAllParameter() flag, which is checked and reset in ButtonListWidgetMixin1
	@Inject(method = "init()V", at = @At("HEAD"))
	private void init_BeforeInvoke(CallbackInfo info)
	{
		//enable the mixin for addAll so that our options are added to the screen
		Common.setModifyAddAllParameter(true);
	}
}
