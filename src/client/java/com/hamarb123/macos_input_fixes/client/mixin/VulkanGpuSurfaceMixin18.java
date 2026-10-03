package com.hamarb123.macos_input_fixes.client.mixin;

import com.hamarb123.macos_input_fixes.client.ModOptions;
import com.mojang.renderpearl.backend.vulkan.VulkanGpuSurface;
import org.lwjgl.vulkan.EXTSwapchainColorspace;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR.Buffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VulkanGpuSurface.class)
public class VulkanGpuSurfaceMixin18
{
	@Inject(method = "pickSwapchainSurfaceFormat(Lorg/lwjgl/vulkan/VkSurfaceFormatKHR$Buffer;)Lorg/lwjgl/vulkan/VkSurfaceFormatKHR;", at = @At("HEAD"), cancellable = true)
	private void allowDisplayP3(Buffer formats, CallbackInfoReturnable<VkSurfaceFormatKHR> callbackInfo)
	{
		if (!ModOptions.useP3ColorSpace)
		{
			return;
		}

		for (VkSurfaceFormatKHR format : formats)
		{
			if ((format.colorSpace() == EXTSwapchainColorspace.VK_COLOR_SPACE_DISPLAY_P3_NONLINEAR_EXT) && (format.format() == VK10.VK_FORMAT_B8G8R8A8_UNORM || format.format() == VK10.VK_FORMAT_R8G8B8A8_UNORM))
			{
				callbackInfo.setReturnValue(format);
				return;
			}
		}
	}
}
