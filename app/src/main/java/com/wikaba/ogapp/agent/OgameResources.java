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

public class OgameResources {
	private long metal;
	private long crystal;
	private long deut;
	private long availEnergy;

	public long getMetal() {
		return metal;
	}

	public void setMetal(long metal) {
		this.metal = metal;
	}

	public long getCrystal() {
		return crystal;
	}

	public void setCrystal(long crystal) {
		this.crystal = crystal;
	}

	public long getDeut() {
		return deut;
	}

	public void setDeut(long deut) {
		this.deut = deut;
	}

	public long getAvailEnergy() {
		return availEnergy;
	}

	public void setAvailEnergy(long availEnergy) {
		this.availEnergy = availEnergy;
	}

	public long getMaxEnergy() {
		return maxEnergy;
	}

	public void setMaxEnergy(long maxEnergy) {
		this.maxEnergy = maxEnergy;
	}

	private long maxEnergy;
}
