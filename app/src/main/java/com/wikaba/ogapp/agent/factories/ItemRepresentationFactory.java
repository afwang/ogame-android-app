package com.wikaba.ogapp.agent.factories;

import com.wikaba.ogapp.agent.constants.BuildingConstants;
import com.wikaba.ogapp.agent.constants.DefenseConstants;
import com.wikaba.ogapp.agent.constants.ResearchConstants;
import com.wikaba.ogapp.agent.constants.ResourceConstants;
import com.wikaba.ogapp.agent.constants.ShipConstants;
import com.wikaba.ogapp.agent.models.AbstractItemInformation;
import com.wikaba.ogapp.agent.models.ItemRepresentation;
import com.wikaba.ogapp.agent.parsers.AbstractParser;
import com.wikaba.ogapp.agent.parsers.pages.BuildingParser;
import com.wikaba.ogapp.agent.parsers.pages.DefenseParser;
import com.wikaba.ogapp.agent.parsers.pages.ResearchParser;
import com.wikaba.ogapp.agent.parsers.pages.ShipyardParser;
import com.wikaba.ogapp.utils.Constants;

/**
 * Created by kevinleperf on 22/07/15.
 */
public class ItemRepresentationFactory {
    private static final BuildingConstants _static_building = new BuildingConstants();
    private static final DefenseConstants _static_defense = new DefenseConstants();
    private static final ResearchConstants _static_research = new ResearchConstants();
    private static final ResourceConstants _static_resource = new ResourceConstants();
    private static final ShipConstants _static_ship = new ShipConstants();

    public static ItemRepresentation createResource(int index, int resource, int drawable) {
        return create(Constants.RESOURCES, index, resource, drawable, new BuildingParser());
    }

    public static ItemRepresentation createBuilding(int index, int resource, int drawable) {
        return create(Constants.BUILDING, index, resource, drawable, new BuildingParser());
    }

    public static ItemRepresentation createShip(int index, int resource, int drawable) {
        return create(Constants.SHIPYARD, index, resource, drawable, new ShipyardParser());
    }

    public static ItemRepresentation createDefense(int index, int resource, int drawable) {
        return create(Constants.DEFENSE, index, resource, drawable, new DefenseParser());
    }

    public static ItemRepresentation createResearch(int index, int resource, int drawable) {
        return create(Constants.RESEARCH, index, resource, drawable, new ResearchParser());
    }


    private static ItemRepresentation create(String page, int index, int resource, int drawable,
                                             AbstractParser<? extends AbstractItemInformation> parser) {
        return new ItemRepresentation(page, index, resource, drawable, parser);
    }

    public static final BuildingConstants getBuildingConstants() {
        return _static_building;
    }

    public static final DefenseConstants getDefenseConstants() {
        return _static_defense;
    }

    public static final ResearchConstants getResearchConstants() {
        return _static_research;
    }

    public static final ResourceConstants getResourceConstants() {
        return _static_resource;
    }

    public static final ShipConstants getShipConstants() {
        return _static_ship;
    }
}
