package com.wikaba.ogapp.agent.constants;

import com.wikaba.ogapp.R;
import com.wikaba.ogapp.agent.factories.ItemRepresentationFactory;
import com.wikaba.ogapp.agent.models.ItemRepresentation;

/**
 * Created by kevinleperf on 22/07/15.
 */

public class ResearchConstants extends ItemRepresentationConstant {
    public final ItemRepresentation RESEARCH_ENERGY = ItemRepresentationFactory.createResearch(113, R.string.energy);
    public final ItemRepresentation RESEARCH_LASER = ItemRepresentationFactory.createResearch(120, R.string.laser);
    public final ItemRepresentation RESEARCH_ION = ItemRepresentationFactory.createResearch(121, R.string.ion);
    public final ItemRepresentation RESEARCH_HYPERSPACE = ItemRepresentationFactory.createResearch(114, R.string.hyperspace);
    public final ItemRepresentation RESEARCH_PLASMA = ItemRepresentationFactory.createResearch(122, R.string.plasma);
    public final ItemRepresentation RESEARCH_COMBUSTION = ItemRepresentationFactory.createResearch(115, R.string.combustion);
    public final ItemRepresentation RESEARCH_IMPULSION = ItemRepresentationFactory.createResearch(117, R.string.impulsion);
    public final ItemRepresentation RESEARCH_HYPERSPACE_ENGINE = ItemRepresentationFactory.createResearch(118, R.string.hyperspace_engine);
    public final ItemRepresentation RESEARCH_SPY = ItemRepresentationFactory.createResearch(106, R.string.spy);
    public final ItemRepresentation RESEARCH_COMPUTER = ItemRepresentationFactory.createResearch(108, R.string.computer);
    public final ItemRepresentation RESEARCH_ASTROPHYSIC = ItemRepresentationFactory.createResearch(124, R.string.astrophysic);
    public final ItemRepresentation RESEARCH_INTERGALACTIC_NETWORK = ItemRepresentationFactory.createResearch(123, R.string.intergalactic_network);
    public final ItemRepresentation RESEARCH_GRAVITON = ItemRepresentationFactory.createResearch(199, R.string.graviton);
    public final ItemRepresentation RESEARCH_WEAPON = ItemRepresentationFactory.createResearch(109, R.string.weapon);
    public final ItemRepresentation RESEARCH_SHIELD = ItemRepresentationFactory.createResearch(110, R.string.shield);
    public final ItemRepresentation RESEARCH_PROTECTION = ItemRepresentationFactory.createResearch(111, R.string.protection);

    {
        _items.add(RESEARCH_ENERGY);
        _items.add(RESEARCH_LASER);
        _items.add(RESEARCH_ION);
        _items.add(RESEARCH_HYPERSPACE);
        _items.add(RESEARCH_PLASMA);
        _items.add(RESEARCH_COMBUSTION);
        _items.add(RESEARCH_IMPULSION);
        _items.add(RESEARCH_HYPERSPACE_ENGINE);
        _items.add(RESEARCH_SPY);
        _items.add(RESEARCH_COMPUTER);
        _items.add(RESEARCH_ASTROPHYSIC);
        _items.add(RESEARCH_INTERGALACTIC_NETWORK);
        _items.add(RESEARCH_GRAVITON);
        _items.add(RESEARCH_WEAPON);
        _items.add(RESEARCH_SHIELD);
        _items.add(RESEARCH_PROTECTION);
    }


    public ResearchConstants() {
        super();
    }

}
