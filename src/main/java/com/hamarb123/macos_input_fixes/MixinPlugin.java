package com.hamarb123.macos_input_fixes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class MixinPlugin implements IMixinConfigPlugin
{
	@Override
	public void onLoad(String mixinPackage)
	{
	}

	@Override
	public String getRefMapperConfig()
	{
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
	{
		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets)
	{
	}

	@Override
	public List<String> getMixins()
	{
		//load certain mixins at runtime depending on whether certain classes are available
		List<String> li = new ArrayList<String>();
		MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
		boolean hasOptionClass = isClassPresent(resolver.mapClassName("intermediary", "net.minecraft.class_316"));
		boolean hasCyclingButtonWidgetClass = isClassPresent(resolver.mapClassName("intermediary", "net.minecraft.class_5676"));
		if (hasOptionClass)
		{
			li.add("gui.MouseOptionsScreenMixin2");
			if (hasCyclingButtonWidgetClass)
			{
				li.add("gui.CyclingOptionMixin4");
			}
		}
		else
		{
			li.add("gui.ButtonListWidgetMixin1");
			li.add("gui.MouseOptionsScreenMixin1");
			li.add("gui.SimpleOptionMixin1");
		}
		if (hasCyclingButtonWidgetClass)
		{
			li.add("gui.CyclingButtonWidgetBuilderMixin3");
		}
		return li;
	}

	private boolean isClassPresent(String className)
	{
        try
		{
            MixinService.getService().getBytecodeProvider().getClassNode(className);
            return true;
        }
		catch (ClassNotFoundException ignored)
		{
            // Class isn't present, skip this mixin.
            return false;
        }
		catch (Exception e)
		{
            // Something else went wrong which might be more serious.
            e.printStackTrace();
            return false;
        }
    }

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo)
	{
	}
}
