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
}
