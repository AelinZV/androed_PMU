package com.example.myapplication

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface CbrApi {
    @GET("scripts/xml_metall.asp")
    fun getMetalRates(
        @Query("date_req1") from: String,
        @Query("date_req2") to: String
    ): Call<ResponseBody>
}
