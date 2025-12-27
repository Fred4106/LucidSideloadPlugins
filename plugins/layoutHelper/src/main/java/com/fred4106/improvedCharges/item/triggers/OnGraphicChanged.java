package com.fred4106.improvedCharges.item.triggers;

public class OnGraphicChanged extends TriggerBase {
    public final int[] graphicId;

    public OnGraphicChanged(final int ...graphicId) {
        this.graphicId = graphicId;
    }
}
