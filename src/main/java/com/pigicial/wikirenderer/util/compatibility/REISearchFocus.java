package com.pigicial.wikirenderer.util.compatibility;

import me.shedaniel.rei.api.client.REIRuntime;
import me.shedaniel.rei.api.client.gui.widgets.TextField;

public class REISearchFocus {

    public static boolean isSearchFieldFocused() {
        try {
            REIRuntime runtime = REIRuntime.getInstance();
            if (runtime == null) return false;

            TextField searchField = runtime.getSearchTextField();
            return searchField != null && searchField.isFocused();

        } catch (Exception e) {
            return false;
        }
    }
}