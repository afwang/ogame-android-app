package com.wikaba.ogapp.utils;

/**
 * This is a simple container class with references to account and universe credentials.
 * @author afwang
 *
 */
public class AccountCredentials {
	public String universe;
	public String username;
	public String passwd;
	
	public AccountCredentials() {
		universe = "";
		username = "";
		passwd = "";
	}
}