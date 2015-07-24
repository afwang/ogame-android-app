package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class ShipConstants extends ItemRepresentationConstant {
    public final ItemRepresentation LIGHT_FIGHTER = ItemRepresentationFactory.createShip(204, R.string.lightfighter, R.drawable.ship_204);
    public final ItemRepresentation HEAVY_FIGHTER = ItemRepresentationFactory.createShip(205, R.string.heavyfighter, R.drawable.ship_205);
    public final ItemRepresentation CRUISER = ItemRepresentationFactory.createShip(206, R.string.cruiser, R.drawable.ship_206);
    public final ItemRepresentation BATTLESHIP = ItemRepresentationFactory.createShip(207, R.string.battleship, R.drawable.ship_207);
    public final ItemRepresentation SMALL_CARGO = ItemRepresentationFactory.createShip(202, R.string.smallcargo, R.drawable.ship_202);
    public final ItemRepresentation LARGE_CARGO = ItemRepresentationFactory.createShip(203, R.string.largecargo, R.drawable.ship_203);
    public final ItemRepresentation COLONY_SHIP = ItemRepresentationFactory.createShip(208, R.string.colony, R.drawable.ship_208);
    public final ItemRepresentation BATTLE_CRUISER = ItemRepresentationFactory.createShip(215, R.string.battlecruiser, R.drawable.ship_215);
    public final ItemRepresentation BOMBER = ItemRepresentationFactory.createShip(211, R.string.bomber, R.drawable.ship_211);
    public final ItemRepresentation DESTROYER = ItemRepresentationFactory.createShip(213, R.string.destroyer, R.drawable.ship_213);
    public final ItemRepresentation DEATHSTAR = ItemRepresentationFactory.createShip(214, R.string.deathstar, R.drawable.ship_214);
    public final ItemRepresentation RECYCLER = ItemRepresentationFactory.createShip(209, R.string.recycler, R.drawable.ship_209);
    public final ItemRepresentation ESPIONNAGE_PROBE = ItemRepresentationFactory.createShip(210, R.string.mission_espionage, R.drawable.ship_210);
    public final ItemRepresentation SOLAR_SATELLITE = ItemRepresentationFactory.createShip(212, R.string.solar_power_plant, R.drawable.ship_212);
    //todo tracker

    {
        _items.add(LIGHT_FIGHTER);
        _items.add(HEAVY_FIGHTER);
        _items.add(CRUISER);
        _items.add(BATTLESHIP);
        _items.add(SMALL_CARGO);
        _items.add(LARGE_CARGO);
        _items.add(COLONY_SHIP);
        _items.add(BATTLE_CRUISER);
        _items.add(BOMBER);
        _items.add(DESTROYER);
        _items.add(DEATHSTAR);
        _items.add(RECYCLER);
        _items.add(ESPIONNAGE_PROBE);
        _items.add(SOLAR_SATELLITE);
    }


    public ShipConstants() {
        super();
    }
}
