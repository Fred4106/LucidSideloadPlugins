package com.fred4106.improvedCharges.store.utils;

public class WidgetMenuAction {
    public String selectedOption;
    public int childIndex;
    public String childString;

    public WidgetMenuAction(
        String selectedOption,
        int childIndex,
        String childString
    ) {
        this.selectedOption = selectedOption;
        this.childIndex = childIndex;
        this.childString = childString;
    }
}
