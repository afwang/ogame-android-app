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

import retrofit.client.Response;
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
    Response getMain();

    @FormUrlEncoded
    //@Headers("Accept-Language: en-US")
    @POST("/main/login")
    Response loginStep1(@Field("kid") String kid,
                               @Field("uni") String uni,
                               @Field("login") String login,
                               @Field("pass") String pass);

    @GET("/game/reg/login2.php")
    @Headers({"Accept-Language: en-US", "Accept: text/html"})
    Response loginStep2(@Query("data") String data);

    @GET("/game/index.php")
    Response loginStep3(@Query("page") String page);

    @GET("/game/index.php")
    Response getSinglePage(@Query("page") String page_name);

    @GET("/game/index.php")
    Response getSinglePageWithAjaxParameter(@Query("page") String page_name,
                                                   @Query("ajax") int ajax);

    @GET("/game/index.php")
    Response getSinglePageFromCategory(@Query("page") String page_name,
                                            @Query("ajax") int ajax,
                                       @Query("type") int category);
}
