/*
    Copyright 2014 Alexander Wang
    
    This file is part of Ogame on Android.

    Ogame on Android is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Ogame on Android is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Ogame on Android.  If not, see <http://www.gnu.org/licenses/>.
*/

package com.wikaba.ogapp.agent;
/**
 * An implementation of this interface's methods should
 * return names for the various ships and resources.
 * 
 * A default implementation is offered at {@code DefaultFleetResourceNamer}.
 * @author Alexander Wang
 *
 */
public interface FleetResourceNamer {
	String getMetalName();
	String getCrystalName();
	String getDeutName();
	
	String getLightFighter();
	String getHeavyFighter();
	String getCruiser();
	String getBattleship();
	String getSmallCargo();
	String getLargeCargo();
	String getColonyShip();
	String getBattlecruiser();
	String getBomber();
	String getDestroyer();
	String getDeathstar();
	String getRecycler();
	String getProbe();
	String getSatellite();
}