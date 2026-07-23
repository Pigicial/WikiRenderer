package com.pigicial.wikirenderer.screen.owo.inject;

import com.pigicial.wikirenderer.screen.owo.core.UIComponent;

/**
 * A marker interface for components which consume
 * text input when focused - this is used to prevent handled
 * screens from closing when said component is focused and the
 * inventory key is pressed
 */
public interface GreedyInputUIComponent extends UIComponent {}