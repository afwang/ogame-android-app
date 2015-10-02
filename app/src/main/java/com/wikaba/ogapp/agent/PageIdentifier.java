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
 * <p>This class contains the constants used to represent each possible page in Ogame.
 * This is used to track which page the user was last viewing.</p>
 *
 * Created by afwang on 9/3/15.
 */
public class PageIdentifier {
	public static final int OVERVIEW = 0;

	/**
	 * RESOURCE_BUILDINGS is associated with the buildings page that shows
	 * the resources (metal mines, crystal mines, power plants, etc.)
	 */
	public static final int RESOURCE_BUILDINGS = 1;

	/**
	 * RESOURCES is associated with the page to control mine output levels
	 */
	public static final int RESOURCES = 2;

	/**
	 * STRUCTURE_BUILDINGS is associated with other types of buildings, such
	 * as robotics factories, nanite factories, shipyards.
	 */
	public static final int STRUCTURE_BUILDINGS = 3;
}
