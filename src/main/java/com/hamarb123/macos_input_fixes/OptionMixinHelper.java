package com.hamarb123.macos_input_fixes;

public interface OptionMixinHelper
{
	//works on both `SimpleOption` (1.19+) and `CyclingOption` (1.14-1.18)
	//sets the instance to create buttons that omit the 'key text'
	void setOmitBuilderKeyText();
}
