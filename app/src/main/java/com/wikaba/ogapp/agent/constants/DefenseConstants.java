package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class DefenseConstants extends ItemRepresentationConstant {
    public final ItemRepresentation ROCKET_LAUNCHER = ItemRepresentationFactory.createDefense(401, R.string.rocket_launcher);
    public final ItemRepresentation LIGHT_LASER = ItemRepresentationFactory.createDefense(402, R.string.light_laser);
    public final ItemRepresentation HEAVY_LASER = ItemRepresentationFactory.createDefense(403, R.string.heavy_laser);
    public final ItemRepresentation GAUSS_CANNON = ItemRepresentationFactory.createDefense(404, R.string.gauss_cannon);
    public final ItemRepresentation ION_CANNON = ItemRepresentationFactory.createDefense(405, R.string.ion_cannon);
    public final ItemRepresentation PLASMA_TURRET = ItemRepresentationFactory.createDefense(406, R.string.plasma_turret);
    public final ItemRepresentation SMALL_SHIELD_DOME = ItemRepresentationFactory.createDefense(407, R.string.small_shield_dome);
    public final ItemRepresentation LARGE_SHIELD_DOME = ItemRepresentationFactory.createDefense(408, R.string.large_shield_dome);
    public final ItemRepresentation ANTI_BALLISTIC_MISSILE = ItemRepresentationFactory.createDefense(502, R.string.anti_ballistic_missile);
    public final ItemRepresentation INTERPLANETARY_MISSILE = ItemRepresentationFactory.createDefense(503, R.string.interplanetary_missile);

    {
        _items.add(ROCKET_LAUNCHER);
        _items.add(LIGHT_LASER);
        _items.add(HEAVY_LASER);
        _items.add(GAUSS_CANNON);
        _items.add(ION_CANNON);
        _items.add(PLASMA_TURRET);
        _items.add(SMALL_SHIELD_DOME);
        _items.add(LARGE_SHIELD_DOME);
        _items.add(ANTI_BALLISTIC_MISSILE);
        _items.add(INTERPLANETARY_MISSILE);
    }


    public DefenseConstants() {
        super();
    }
}
