package com.hamarb123.macos_input_fixes;

@FunctionalInterface
public interface ScrollCallback
{
	void accept(double x, double y, double ungroupedX, double ungroupedY);
}
