package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class BuildingConstants extends ItemRepresentationConstant {
    public final ItemRepresentation ROBOT_FACTORY = ItemRepresentationFactory.createBuilding(14, R.string.robot_factory);
    public final ItemRepresentation SPACESHIP_FACTORY = ItemRepresentationFactory.createBuilding(21, R.string.spaceship_factory);
    public final ItemRepresentation LABORATORY = ItemRepresentationFactory.createBuilding(31, R.string.laboratory);
    public final ItemRepresentation SUPPLY_DEPOT = ItemRepresentationFactory.createBuilding(34, R.string.supply_depot);
    public final ItemRepresentation MISSILE_SILO = ItemRepresentationFactory.createBuilding(44, R.string.missile_silo);

    {
        _items.add(ROBOT_FACTORY);
        _items.add(SPACESHIP_FACTORY);
        _items.add(LABORATORY);
        _items.add(SUPPLY_DEPOT);
        _items.add(MISSILE_SILO);
    }


    public BuildingConstants() {
        super();
    }
}
