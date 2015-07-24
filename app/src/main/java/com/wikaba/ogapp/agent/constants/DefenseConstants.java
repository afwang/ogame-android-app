package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class DefenseConstants extends ItemRepresentationConstant {
    public final ItemRepresentation ROCKET_LAUNCHER = ItemRepresentationFactory.createDefense(401, R.string.rocket_launcher, R.drawable.defense_401);
    public final ItemRepresentation LIGHT_LASER = ItemRepresentationFactory.createDefense(402, R.string.light_laser, R.drawable.defense_402);
    public final ItemRepresentation HEAVY_LASER = ItemRepresentationFactory.createDefense(403, R.string.heavy_laser, R.drawable.defense_403);
    public final ItemRepresentation GAUSS_CANNON = ItemRepresentationFactory.createDefense(404, R.string.gauss_cannon, R.drawable.defense_404);
    public final ItemRepresentation ION_CANNON = ItemRepresentationFactory.createDefense(405, R.string.ion_cannon, R.drawable.defense_405);
    public final ItemRepresentation PLASMA_TURRET = ItemRepresentationFactory.createDefense(406, R.string.plasma_turret, R.drawable.defense_406);
    public final ItemRepresentation SMALL_SHIELD_DOME = ItemRepresentationFactory.createDefense(407, R.string.small_shield_dome, R.drawable.defense_407);
    public final ItemRepresentation LARGE_SHIELD_DOME = ItemRepresentationFactory.createDefense(408, R.string.large_shield_dome, R.drawable.defense_408);
    public final ItemRepresentation ANTI_BALLISTIC_MISSILE = ItemRepresentationFactory.createDefense(502, R.string.anti_ballistic_missile, R.drawable.defense_502);
    public final ItemRepresentation INTERPLANETARY_MISSILE = ItemRepresentationFactory.createDefense(503, R.string.interplanetary_missile, R.drawable.defense_503);

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
