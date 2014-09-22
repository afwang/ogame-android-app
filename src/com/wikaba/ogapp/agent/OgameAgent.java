package com.wikaba.ogapp.agent;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * This class represents 1 account on 1 universe. If the user is playing multiple accounts simultaneously,
 * there should be multiple OgameAgent objects; there should be as many OgameAgent objects as there are
 * accounts.
 * @author afwang
 *
 */
public class OgameAgent {
	public static final String LOGIN_URL = "http://en.ogame.gameforge.com/main/login";
	
	/**
	 * Submits user credentials to Ogame. Parses and returns data from HTTP response
	 * @param universe - The name of the universe to log in to.
	 * @param username - Username of the account on universe to log in to.
	 * @return Object containing the data from the response
	 */
	public Object login(String universe, String username, String password) {
		final int timeoutMillis = 30 * 1000;
		HttpURLConnection connection = null;
		
		StringBuffer buffer = new StringBuffer();
		
		try {
			connection = (HttpURLConnection)(new URL(LOGIN_URL)).openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(timeoutMillis);
			connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
			connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
			connection.setRequestProperty("DNT", "1");
			connection.setRequestProperty("Referer", "http://en.ogame.gameforge.com/");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:32.0) Gecko/20100101 Firefox/32.0");
			connection.setInstanceFollowRedirects(true);
			connection.setDoOutput(true);
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			
			String parameters = "kid=&universe=" + URLEncoder.encode(universe, "UTF-8") + "&login=" + URLEncoder.encode(username, "UTF-8") + "&pass=" + URLEncoder.encode(password, "UTF-8");
			System.out.println(parameters);
			writer.write(parameters);
			writer.flush();
			writer.close();
			connection.connect();
			
			int response = connection.getResponseCode();
			if(response == HttpURLConnection.HTTP_OK) {
				System.out.println("Everything went okay!");
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				while((line = reader.readLine()) != null) {
					buffer.append(line);
					buffer.append('\n');
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
		}
		catch(MalformedURLException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		catch(IOException e) {
			System.err.println("Something wrong happened! " + e + '\n' + e.getMessage());
			e.printStackTrace();
		}
		finally {
			if(connection != null) {
				connection.disconnect();
			}
		}
			
		return buffer;
	}
}
