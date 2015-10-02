/*
	Copyright 2015 Kevin Le Perf

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

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieStore;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class ReceivedCookiesInterceptor implements Interceptor {

	private CustomCookieManager cm;
	private HashMap<String, String> _cookies = new HashMap<>();

	public ReceivedCookiesInterceptor() {
		cm = new CustomCookieManager();
	}

	public HashMap<String, String> getCookies() {
		return _cookies;
	}

	public CookieStore getCookieStore() {
		return cm.getCookieStore();
	}

	/**
	 * Client interceptor
	 *
	 * @param chain
	 * @return
	 * @throws IOException
	 */
	@Override
	public Response intercept(Chain chain) throws IOException {
		Request req = chain.request();
		Map<String, List<String>> requestHeaders = req.headers().toMultimap();
		URI uri = req.uri();
		Map<String, List<String>> cookieHeaders = cm.get(uri, requestHeaders);
		if(cookieHeaders != null && cookieHeaders.size() > 0) {
			Request.Builder newReq = req.newBuilder();
			String cookieKey = "Cookie";
			List<String> cookieValue = cookieHeaders.get(cookieKey);
			newReq.addHeader(cookieKey, cookieValue.get(0));
			req = newReq.build();
		}

		Response originalResponse = chain.proceed(req);
		Map<String, List<String>> responseHeaders = originalResponse.headers().toMultimap();

		cm.put(uri, responseHeaders);

		return originalResponse;
	}
}
