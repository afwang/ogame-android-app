package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class ResourceConstants extends ItemRepresentationConstant {
    public final ItemRepresentation METAL = ItemRepresentationFactory.createResource(1, R.string.metal_mine, R.drawable.resources_1);
    public final ItemRepresentation CRYSTAL = ItemRepresentationFactory.createResource(2, R.string.crystal_mine, R.drawable.resources_2);
    public final ItemRepresentation DEUTERIUM = ItemRepresentationFactory.createResource(3, R.string.deuterium_mine, R.drawable.resources_3);
    public final ItemRepresentation SOLAR_POWER_PLANT = ItemRepresentationFactory.createResource(4, R.string.solar_power_plant, R.drawable.resources_4);
    public final ItemRepresentation FUSION_POWER_PLANT = ItemRepresentationFactory.createResource(12, R.string.fusion_power_plant, R.drawable.resources_12);

    public final ItemRepresentation METAL_STOCK = ItemRepresentationFactory.createResource(22, R.string.metal_stock, R.drawable.resources_22);
    public final ItemRepresentation CRYSTAL_STOCK = ItemRepresentationFactory.createResource(23, R.string.crystal_stock, R.drawable.resources_23);
    public final ItemRepresentation DEUTERIUM_STOCK = ItemRepresentationFactory.createResource(24, R.string.deuterium_stock, R.drawable.resources_24);

    public final ItemRepresentation METAL_HIDEOUT = ItemRepresentationFactory.createBuilding(25, R.string.metal_stash, R.drawable.resources_25);
    public final ItemRepresentation CRYSTAL_HIDEOUT = ItemRepresentationFactory.createBuilding(26, R.string.crystal_stash, R.drawable.resources_26);
    public final ItemRepresentation DEUTERIUM_HIDEOUT = ItemRepresentationFactory.createBuilding(27, R.string.deuterium_stash, R.drawable.resources_27);

    {
        _items.add(METAL);
        _items.add(CRYSTAL);
        _items.add(DEUTERIUM);
        _items.add(SOLAR_POWER_PLANT);
        _items.add(FUSION_POWER_PLANT);
        _items.add(METAL_STOCK);
        _items.add(CRYSTAL_STOCK);
        _items.add(DEUTERIUM_STOCK);
        _items.add(METAL_HIDEOUT);
        _items.add(CRYSTAL_HIDEOUT);
        _items.add(DEUTERIUM_HIDEOUT);
    }


    public ResourceConstants() {
        super();
    }

}
