package com.hamarb123.macos_input_fixes.client;

@FunctionalInterface
public interface KeyCallback
{
	void accept(int key, int scancode, int action, int modifiers);
}
