package com.hamarb123.macos_input_fixes.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

// Utility class for Ixeris mod compatibility (see issue #48)
public class IxerisCompat {
    private static final boolean INSTALLED = FabricLoader.getInstance().isModLoaded("ixeris");
    private static final Object IXERIS_API;
    private static final MethodHandle RUN_ON_RENDER_THREAD;

    static {
        if (INSTALLED) {
            // initialize Ixeris compatibility fields
            try {
                Class<?> clazz = Class.forName("me.decce.ixeris.api.IxerisApi");
                IXERIS_API = clazz.getDeclaredMethod("getInstance").invoke(null);
                MethodType methodType = MethodType.methodType(void.class, Runnable.class);
                MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                RUN_ON_RENDER_THREAD = lookup.findVirtual(clazz, "runOnRenderThread", methodType);
            } catch (Throwable t) {
                throw new RuntimeException("Exception initializing Ixeris compat!", t);
            }
        }
        else {
            IXERIS_API = null;
            RUN_ON_RENDER_THREAD = null;
        }
    }

    public static boolean isModPresent() {
        return INSTALLED;
    }

    public static void runOnRenderThread(Runnable runnable) {
        try {
            RUN_ON_RENDER_THREAD.invoke(IXERIS_API, runnable);
        } catch (Throwable e) {
            throw new RuntimeException("Exception invoking Ixeris method!", e);
        }
    }
}
