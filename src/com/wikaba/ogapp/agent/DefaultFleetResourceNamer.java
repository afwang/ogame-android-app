/**
 * 
 */
package com.wikaba.ogapp.agent;

/**
 * This is a default implementation of FleetResourceNamer interface.
 * 
 * @author Alexander Wang
 *
 */
public class DefaultFleetResourceNamer implements FleetResourceNamer {
	private static final String METAL = "metal";
	private static final String CRYSTAL = "crystal";
	private static final String DEUT = "deuterium";
	
	private static final String LF = "Light Fighter";
	private static final String HF = "Heavy Fighter";
	private static final String CR = "Cruiser";
	private static final String BS = "Battleship";
	private static final String SC = "Small Cargo";
	private static final String LC = "Large Cargo";
	private static final String COLONY = "Colony Ship";
	private static final String BC = "Battlecruiser";
	private static final String BB = "Bomber";
	private static final String DS = "Destroyer";
	private static final String RIP = "Deathstar";
	private static final String RC = "Recycler";
	private static final String EP = "Espionage Probe";
	private static final String SS = "Solar Satellite";

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getMetalName()
	 */
	@Override
	public String getMetalName() {
		return METAL;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getCrystalName()
	 */
	@Override
	public String getCrystalName() {
		return CRYSTAL;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getDeutName()
	 */
	@Override
	public String getDeutName() {
		return DEUT;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getLightFighter()
	 */
	@Override
	public String getLightFighter() {
		return LF;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getHeavyFighter()
	 */
	@Override
	public String getHeavyFighter() {
		return HF;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getCruiser()
	 */
	@Override
	public String getCruiser() {
		return CR;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getBattleship()
	 */
	@Override
	public String getBattleship() {
		return BS;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getSmallCargo()
	 */
	@Override
	public String getSmallCargo() {
		return SC;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getLargeCargo()
	 */
	@Override
	public String getLargeCargo() {
		return LC;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getColonyShip()
	 */
	@Override
	public String getColonyShip() {
		return COLONY;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getBattlecruiser()
	 */
	@Override
	public String getBattlecruiser() {
		return BC;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getBomber()
	 */
	@Override
	public String getBomber() {
		return BB;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getDestroyer()
	 */
	@Override
	public String getDestroyer() {
		return DS;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getDeathstar()
	 */
	@Override
	public String getDeathstar() {
		return RIP;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getRecycler()
	 */
	@Override
	public String getRecycler() {
		return RC;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getProbe()
	 */
	@Override
	public String getProbe() {
		return EP;
	}

	/* (non-Javadoc)
	 * @see com.wikaba.ogapp.agent.FleetResourceNamer#getSatellite()
	 */
	@Override
	public String getSatellite() {
		return SS;
	}

}
