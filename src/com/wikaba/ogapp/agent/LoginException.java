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
 * This exception should be thrown when the OgameAgent fails to log in. This can occur
 * either from incorrect username, password, and server combinations.
 *
 */
public class LoginException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2302641662714340888L;

	/**
	 * 
	 */
	public LoginException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 */
	public LoginException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param throwable
	 */
	public LoginException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param detailMessage
	 * @param throwable
	 */
	public LoginException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

}
