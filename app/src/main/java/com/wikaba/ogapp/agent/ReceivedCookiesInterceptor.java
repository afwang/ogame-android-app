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
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.HashMap;

import retrofit.RequestInterceptor;

/**
 * Created by kevinleperf on 19/07/15.
 */
public class ReceivedCookiesInterceptor implements RequestInterceptor, Interceptor {
    HashMap<String, String> _cookies = new HashMap<>();

    public void addCookie(String name, String content) {
        _cookies.put(name, content);
    }

    public void cleanCookie() {
        _cookies.clear();
    }

    public HashMap<String, String> getCookies() {
        return _cookies;
    }

    public void deleteCookie(String name) {
        _cookies.remove(name);
    }

    /**
     * Intercept emission
     *
     * @param request
     */
    @Override
    public void intercept(RequestInterceptor.RequestFacade request) {
        String cookies = "";
        for (String header : _cookies.values()) {
            if (cookies.length() > 0) cookies += "; ";
            cookies += header.split(";")[0];
        }
        if (cookies.length() > 0) request.addHeader("Cookie", cookies);

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
        com.squareup.okhttp.Response originalResponse = chain.proceed(chain.request());

        if (!originalResponse.headers("Set-Cookie").isEmpty()) {

            for (String header : originalResponse.headers("Set-Cookie")) {
                String name = header.split("=")[0].trim();
                _cookies.put(name, header);
            }
        }

        return originalResponse;
    }
}
