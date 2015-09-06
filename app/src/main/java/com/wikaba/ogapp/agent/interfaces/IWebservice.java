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

package com.wikaba.ogapp.agent.interfaces;

import com.squareup.okhttp.ResponseBody;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by kevinleperf on 18/07/15.
 */
public interface IWebservice {

	@GET("/")
	Call<ResponseBody> getMain();

	@FormUrlEncoded
	//@Headers("Accept-Language: en-US")
	@POST("/main/login")
	Call<ResponseBody> loginStep1(
			@Field("kid") String kid,
			@Field("uni") String uni,
			@Field("login") String login,
			@Field("pass") String pass
	);

	@GET("/game/reg/login2.php")
	@Headers({"Accept-Language: en-US", "Accept: text/html"})
	ResponseBody loginStep2(@Query("data") String data);

	@Headers({"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
			"Accept-Encoding: gzip,deflate,sdch",
			"Accept-Language: fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4",
			"Cache-Control: max-age=0",
			"Connection: keep-alive",
			"Referer: http://fr.ogame.gameforge.com/",
	})
	@GET("/game/index.php")
	ResponseBody loginStep3(@Query("page") String page);

	@GET("/game/index.php")
	Call<ResponseBody> getSinglePage(@Query("page") String page_name);

	@GET("/game/index.php")
	Call<ResponseBody> getSinglePageWithAjaxParameter(
			@Query("page") String page_name,
			@Query("ajax") int ajax
	);

	@Headers({"X-Requested-With: XMLHttpRequest"})
	@GET("/game/index.php")
	Call<ResponseBody> getSinglePageFromCategory(
			@Query("page") String page_name,
			@Query("ajax") int ajax,
			@Query("type") int category
	);
}
