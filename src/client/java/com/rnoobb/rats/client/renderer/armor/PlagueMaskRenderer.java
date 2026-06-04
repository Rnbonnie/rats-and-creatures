package com.rnoobb.rats.client.renderer.armor;

import com.rnoobb.rats.client.model.armor.PlagueMaskModel;
import com.rnoobb.rats.item.PlagueMaskItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class PlagueMaskRenderer extends GeoArmorRenderer<PlagueMaskItem> {
    public PlagueMaskRenderer() {
        super(new PlagueMaskModel());
    }
}
