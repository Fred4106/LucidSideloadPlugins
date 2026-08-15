package com.fred4106.improvedCharges.item.triggers;

public class OnGraphicChanged extends TriggerBase {
    public int[] graphicId;

    public OnGraphicChanged(int ...graphicId) {
        this.graphicId = graphicId;
    }
}
