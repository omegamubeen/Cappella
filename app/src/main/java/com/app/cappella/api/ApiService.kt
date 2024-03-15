package com.app.cappella.api

import com.app.cappella.model.BabyProfile
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @GET("api/user/baby-profile-test/e71b3147-01f5-42bb/")
    suspend fun getBabyProfile(): Response<BabyProfile>

    @PUT("api/user/baby-profile-test/e71b3147-01f5-42bb/{uuid}/")
    @Multipart
    suspend fun updateBabyProfile(
        @Path("uuid") uuid: String,
        @Part("name") name: RequestBody,
        @Part("dob") dob: RequestBody,
        @Part("gender") gender: RequestBody,
        @Part profilePicture: MultipartBody.Part?
    ): Response<BabyProfile>
}
