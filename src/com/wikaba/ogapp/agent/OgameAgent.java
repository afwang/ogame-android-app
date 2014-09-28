package com.wikaba.ogapp.agent;


import android.annotation.SuppressLint;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 * @author afwang
 *
 */
public class OgameAgent {
	public static final String LOGIN_URL = "http://en.ogame.gameforge.com/main/login";
//	public static final String LOGIN_URL = "http://localhost:8080/";
	
	private Map<String, HttpCookie> cookieStore;
	
	public OgameAgent() {
//		CookieHandler handler = CookieHandler.getDefault();
		cookieStore = new HashMap<String, HttpCookie>();
	}
	
	/**
	 * Submits user credentials to Ogame. Parses and returns data from HTTP response
	 * @param universe - The name of the universe to log in to.
	 * @param username - Username of the account on universe to log in to.
	 * @return Object containing the data from the response
	 */
	public Object login(String universe, String username, String password) {
		final int timeoutMillis = 30 * 1000;
		HttpURLConnection connection = null;
		URI theUri;
		Collection<HttpCookie> cookies;
		StringBuilder cookieHeaderBuilder;
		boolean cookieAddedYet = false;
		boolean hasCookies = false;
		String cookieHeaderStr;
		int response;
		StringBuilder buffer = new StringBuilder();
		
		/*
		 * FIRST REQUEST
		 */
		System.out.println("START FIRST REQUEST (login)");
		String uri = LOGIN_URL;
		String parameters;
		try {
			parameters = "kid=&uni=" + URLEncoder.encode(universe, "UTF-8") + "&login=" + URLEncoder.encode(username, "UTF-8") + "&pass=" + URLEncoder.encode(password, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			System.out.println("Error: " + e1 + '\n' + e1.getMessage());
			e1.printStackTrace();
			return null;
		}
		String length = Integer.toString(parameters.length());
		try {
			connection = (HttpURLConnection)(new URL(uri)).openConnection();
			theUri = new URI(uri);
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			//No cookies to set on the first HTTP request
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", length);
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			System.out.println(parameters);
			writer.write(parameters);
			writer.flush();
			writer.close();
			
			connection.setInstanceFollowRedirects(false);
			response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				System.out.println("Response headers:");
				Set<Map.Entry<String, List<String>>> entrySet = responseHeaders.entrySet();
				for(Map.Entry<String, List<String>> mapping : entrySet) {
					List<String> values = mapping.getValue();
					for(String val : values) {
						System.out.println(mapping.getKey() + ": " + val);
					}
				}
				List<String> cookieHeaders = responseHeaders.get("Set-Cookie");
				for(String cookieHeader : cookieHeaders) {
					System.out.println(cookieHeader);
					List<HttpCookie> cookiesList = parseCookies(cookieHeader, theUri.getAuthority(), theUri.getPath());
					for(HttpCookie cookie : cookiesList) {
						cookieStore.put(cookie.getName(), cookie);
					}
				}
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
					System.out.println("Redirected to: " + uri);
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while((line = reader.readLine()) != null) {
					System.out.println(line);
				}
				reader.close();
			}
			else {
				System.err.println("Something went wrong!");
				BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while((line = errReader.readLine()) != null) {
					buffer.append(line);
					buffer.append('\n');
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END FIRST REQUEST (login)");
			
			/*
			 * SECOND REQUEST
			 */
			System.out.println("START SECOND REQUEST");
			connection = (HttpURLConnection)(new URL(uri)).openConnection();
			theUri = new URI(uri);
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setDoOutput(false);
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			
			cookies = cookieStore.values();
			cookieHeaderBuilder = new StringBuilder();
			cookieAddedYet = false;
			hasCookies = false;
			for(HttpCookie cookie : cookies) {
				String locationDomain = theUri.getAuthority();
				String cookieDomain = cookie.getDomain();
				String locationPath = theUri.getPath();
				String cookiePath = cookie.getPath();
				
				/* add the cookie to the list to be sent only if the URI authority (the domain)
				 * contains the cookie's domain.
				 */
				if(cookieDomain != null && !locationDomain.equals(cookieDomain) && cookieDomain.length() > 0 && cookieDomain.charAt(0) != '.') {
					cookieDomain = '.' + cookieDomain;
				}
				if(locationDomain.contains(cookieDomain) && locationPath.contains(cookiePath)) {
					if(cookieAddedYet) {
						cookieHeaderBuilder.append(',');
					}
					cookieHeaderBuilder.append(cookie.toString());
					cookieAddedYet = true;
					hasCookies = true;
				}
			}
			cookieHeaderStr = cookieHeaderBuilder.toString();
			if(hasCookies)
				connection.setRequestProperty("Cookie", cookieHeaderStr);
			
			connection.connect();
			
			response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				System.out.println("Response headers:");
				Set<Map.Entry<String, List<String>>> entrySet = responseHeaders.entrySet();
				for(Map.Entry<String, List<String>> mapping : entrySet) {
					List<String> values = mapping.getValue();
					for(String val : values) {
						System.out.println(mapping.getKey() + ": " + val);
					}
				}
				List<String> cookieHeaders = responseHeaders.get("Set-Cookie");
				for(String cookieHeader : cookieHeaders) {
					System.out.println(cookieHeader);
					List<HttpCookie> cookiesList = parseCookies(cookieHeader, theUri.getAuthority(), theUri.getPath());
					for(HttpCookie cookie : cookiesList) {
						cookieStore.put(cookie.getName(), cookie);
					}
				}
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
					System.out.println("Redirected to: " + uri);
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while((line = reader.readLine()) != null) {
					System.out.println(line);
				}
				reader.close();
			}
			else {
				System.err.println("Something went wrong!");
				BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while((line = errReader.readLine()) != null) {
					buffer.append(line);
					buffer.append('\n');
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END SECOND REQUEST");
			
			/*
			 * THIRD REQUEST (final request)
			 */
			System.out.println("START THIRD REQUEST");
			connection = (HttpURLConnection)(new URL(uri)).openConnection();
			theUri = new URI(uri);
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Host", theUri.getAuthority());
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			connection.setDoOutput(false);
			connection.setDoInput(true);
			connection.setRequestMethod("GET");
			connection.setInstanceFollowRedirects(false);
			
			cookies = cookieStore.values();
			cookieHeaderBuilder = new StringBuilder();
			cookieAddedYet = false;
			hasCookies = false;
			for(HttpCookie cookie : cookies) {
				String locationDomain = theUri.getAuthority();
				String cookieDomain = cookie.getDomain();
				String locationPath = theUri.getPath();
				String cookiePath = cookie.getPath();
				
				/* add the cookie to the list to be sent only if the URI authority (the domain)
				 * contains the cookie's domain.
				 */
				if(cookieDomain != null && !locationDomain.equals(cookieDomain) && cookieDomain.length() > 0 && cookieDomain.charAt(0) != '.') {
					cookieDomain = '.' + cookieDomain;
				}
				if(locationDomain.contains(cookieDomain) && locationPath.contains(cookiePath)) {
					if(cookieAddedYet) {
						cookieHeaderBuilder.append(';');
					}
					cookieHeaderBuilder.append(cookie.toString());
					
					System.out.println("Cookie added: " + cookie.toString());
					cookieAddedYet = true;
					hasCookies = true;
				}
			}
			cookieHeaderStr = cookieHeaderBuilder.toString();
			if(hasCookies)
				connection.setRequestProperty("Cookie", cookieHeaderStr);
			
			connection.connect();
			
			response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK || (response >= 300 && response < 400)) {
				System.out.println("Everything went okay! Response " + response);
				
				Map<String, List<String>> responseHeaders = connection.getHeaderFields();
				System.out.println("Response headers:");
				Set<Map.Entry<String, List<String>>> entrySet = responseHeaders.entrySet();
				for(Map.Entry<String, List<String>> mapping : entrySet) {
					List<String> values = mapping.getValue();
					for(String val : values) {
						System.out.println(mapping.getKey() + ": " + val);
					}
				}
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while((line = reader.readLine()) != null) {
					buffer.append(line);
					buffer.append('\n');
				}
				reader.close();
				
				List<String> locationHeader = responseHeaders.get("Location");
				if(locationHeader != null && locationHeader.size() > 0) {
					uri = locationHeader.get(0);
					System.out.println("Redirected to: " + uri);
				}
			}
			else {
				System.err.println("Something went wrong!");
				BufferedReader errReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				while((line = errReader.readLine()) != null) {
					buffer.append(line);
					buffer.append('\n');
				}
				errReader.close();
			}
			connection.disconnect();
			System.out.println("END THIRD REQUEST");
		}
		catch(MalformedURLException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		catch (URISyntaxException e) {
			System.err.println("URI error: " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		catch(Exception e) {
			System.err.println("What went wrong: " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
			
		return buffer;
	}
	
	/**
	 * We have to implement our own cookie parser because Java's HttpCookie.parse() method throws an Exception when
	 * it should not throw that Exception.
	 * @param cookieStr - The header value from one Set-Cookie header field. May contain multiple multiple cookies.
	 * @return A list of HttpCookie objects representing the cookies in cookieStr
	 */
	private List<HttpCookie> parseCookies(String cookieStr, String defaultDomain, String defaultPath) {
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
			//set expiration
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
		ArrayList<HttpCookie> cookieList = new ArrayList<HttpCookie>();
		cookieList.add(cookie);
		return cookieList;
	}
}
