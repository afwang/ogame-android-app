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

package com.wikaba.ogapp.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is a simple container class with references to account and universe credentials.
 *
 * @author afwang
 */
public class AccountCredentials implements Parcelable {
	private long id;
	private String universe;
	private String username;
	private String passwd;
	private String lang;

	public AccountCredentials() {
		id = -1;
		universe = "";
		username = "";
		passwd = "";
		lang = "";
	}

	public AccountCredentials(AccountCredentials toCopy) {
		this.id = toCopy.id;
		this.universe = toCopy.universe;
		this.username = toCopy.username;
		this.passwd = toCopy.passwd;
		this.lang = toCopy.lang;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUniverse() {
		return universe;
	}

	public void setUniverse(String universe) {
		this.universe = universe;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	@Override
	public int describeContents() {
		return (int)id;
	}

	@Override
	public void writeToParcel(Parcel parcel, int flags) {
		parcel.writeLong(id);
		parcel.writeString(universe);
		parcel.writeString(username);
		parcel.writeString(passwd);
		parcel.writeString(lang);
	}

	public static final Parcelable.Creator<AccountCredentials> CREATOR
			= new Parcelable.Creator<AccountCredentials>() {
		@Override
		public AccountCredentials createFromParcel(Parcel source) {
			AccountCredentials creds = new AccountCredentials();
			creds.id = source.readLong();
			creds.universe = source.readString();
			creds.username = source.readString();
			creds.passwd = source.readString();
			creds.lang = source.readString();
			return creds;
		}

		@Override
		public AccountCredentials[] newArray(int size) {
			return new AccountCredentials[size];
		}
	};

	@Override
	public String toString() {
		return username + " " + universe;
	}
}