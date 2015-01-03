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

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * This is the custom CookieManager class that should be used together with the
 * Ogame agent. Unfortunately, there is a bug in Java 6 where
 * HTTP cookies are not being saved properly. Since Android runs on Java 6,
 * this means the bug also exists in Android systems. This class is a workaround
 * that bug.
 */
public class CustomCookieManager extends CookieManager {

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		List<String> cookieHeaders = responseHeaders.get("Set-Cookie");
		List<String> cookieHeaders2 = responseHeaders.get("Set-Cookie2");
		CookieStore cookieStore = getCookieStore();
		if(cookieHeaders != null) {
			for(String cookieHeader : cookieHeaders) {
				List<HttpCookie> cookiesList = parseCookies(cookieHeader, uri.getAuthority(), uri.getPath());
				for(HttpCookie cookie : cookiesList) {
//					System.out.println("Adding cookie to store: " + cookie.getName() + '=' + cookie.getValue()
//							+ "; expiration=" + (cookie.getMaxAge() + Calendar.getInstance().getTimeInMillis() / 1000)
//							+ "; domain=" + cookie.getDomain() + "; path =" + cookie.getPath());
					cookieStore.add(null, cookie);
				}
			}
		}
		if(cookieHeaders2 != null) {
			for(String cookieHeader : cookieHeaders2) {
				List<HttpCookie> cookiesList = parseCookies(cookieHeader, uri.getAuthority(), uri.getPath());
				for(HttpCookie cookie: cookiesList) {
//					System.out.println("Adding cookie to store: " + cookie.getName() + '=' + cookie.getValue()
//							+ "; expiration=" + (cookie.getMaxAge() + Calendar.getInstance().getTimeInMillis() / 1000)
//							+ "; domain=" + cookie.getDomain() + "; path =" + cookie.getPath());
					cookieStore.add(null, cookie);
				}
			}
		}
	}
	
	/**
	 * We have to implement our own cookie parser because Java's HttpCookie.parse() method throws an
	 * IllegalArgumentException, which goes against specification. See: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6790677
	 * 
	 * Pre-condition: defaultDomain and defaultPath must not be empty strings. defaultDomain must be a valid domain.
	 * defaultPath must be a valid path.
	 * 
	 * Post-condition: The list of cookies returned will properly contain the data encoded in the Set-Cookie header, cookieStr.
	 * The cookies will have a valid domain and path set, defaulting to defaultDomain and defaultPath if the domain and path are
	 * not set in the header.
	 * 
	 * @param cookieStr - The header value from one Set-Cookie header field without the "Set-Cookie" portion. May contain multiple multiple cookies.
	 * @param defaultDomain - default domain to set for cookies that do not have the "domain" attribute set in the header
	 * @param defaultPath - default path to set for cookies that do not have the "path" attribute set in the header
	 * @return A list of HttpCookie objects representing the cookies in cookieStr
	 */
	private List<HttpCookie> parseCookies(String cookieStr, String defaultDomain, String defaultPath) {
		//TODO: Enhance this method to parse cookies that come as a comma-delimited String.
		//TODO: Enhance this method to include the "Max-Age" attribute
		ArrayList<HttpCookie> cookieList = new ArrayList<HttpCookie>();
		HttpCookie cookie = null;
		String[] cookieTokens = cookieStr.split(";");
		
		String name = null;
		String value = null;
		String expiration = null;
		String path = null;
		String domain = null;
		
		for(String tok : cookieTokens) {
			String[] tokpieces = tok.split("=");
			if(tokpieces.length == 2) {
				tokpieces[0] = tokpieces[0].trim();
				tokpieces[1] = tokpieces[1].trim();
				
				if(tokpieces[0].equalsIgnoreCase("expires")) {
					expiration = tokpieces[1];
				}
				else if(tokpieces[0].equalsIgnoreCase("path")) {
					path = tokpieces[1];
				}
				else if(tokpieces[0].equalsIgnoreCase("domain")) {
					domain = tokpieces[1];
				}
				else { //name of the cookie
					name = tokpieces[0];
					value = tokpieces[1];
				}
			}
		}
		
		if(name == null)
			return null;
		
		cookie = new HttpCookie(name, value);
		cookie.setVersion(0);
		if(expiration != null) {
			//sample format: Tue, 15-Jan-2013 21:47:38 GMT
			//another sample from Ogame itself: Sat, 01-Nov-2014 21:22:40 GMT
			//All non-numerical strings are 3 letters long.
			String[] weekdaySplit = expiration.split(",");
			//weekday does not need to be parsed. We don't use that to calculate the time.
//			String weekday = weekdaySplit[0];
			expiration = weekdaySplit[1].trim();
			
			String[] dateSplit = expiration.split("-");
			String dom = dateSplit[0];
			String month = dateSplit[1];
			String[] yearSplit = dateSplit[2].split(" ");
			String year = yearSplit[0];
			
			String[] timeSplit = yearSplit[1].split(":");
			String hour = timeSplit[0];
			String minute = timeSplit[1];
			String seconds = timeSplit[2];
			
			//should just be GMT. Do not need to parse this.
//			String zone = yearSplit[2];
			
			TimeZone gmtZone = TimeZone.getTimeZone("GMT");
			Calendar cal = Calendar.getInstance(gmtZone);
			int iyear = Integer.valueOf(year);
			int imonth = 0;
			if(month.equals("Jan")) {
				imonth = Calendar.JANUARY;
			}
			else if(month.equals("Feb")) {
				imonth = Calendar.FEBRUARY;
			}
			else if(month.equals("Mar")) {
				imonth = Calendar.MARCH;
			}
			else if(month.equals("Apr")) {
				imonth = Calendar.APRIL;
			}
			else if(month.equals("May")) {
				imonth = Calendar.MAY;
			}
			else if(month.equals("Jun")) {
				imonth = Calendar.JUNE;
			}
			else if(month.equals("Jul")) {
				imonth = Calendar.JULY;
			}
			else if(month.equals("Aug")) {
				imonth = Calendar.AUGUST;
			}
			else if(month.equals("Sep")) {
				imonth = Calendar.SEPTEMBER;
			}
			else if(month.equals("Oct")) {
				imonth = Calendar.OCTOBER;
			}
			else if(month.equals("Nov")) {
				imonth = Calendar.NOVEMBER;
			}
			else if(month.equals("Dec")) {
				imonth = Calendar.DECEMBER;
			}
			int iday = Integer.valueOf(dom);
			int ihour = Integer.valueOf(hour);
			int iminute = Integer.valueOf(minute);
			int iseconds = Integer.valueOf(seconds);
			cal.set(iyear, imonth, iday, ihour, iminute, iseconds);
			long timeInMillis = cal.getTimeInMillis();
			long currentTimeInMillis = Calendar.getInstance().getTimeInMillis();
			cookie.setMaxAge((timeInMillis - currentTimeInMillis) / 1000);
		}
		if(path != null) {
			cookie.setPath(path);
		}
		else {
			cookie.setPath(defaultPath);
		}
		if(domain != null) {
			cookie.setDomain(domain);
		}
		else {
			cookie.setDomain(defaultDomain);
		}
		cookieList.add(cookie);
		return cookieList;
	}
}