package com.app.cappella.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.app.cappella.api.RetrofitInstance
import com.app.cappella.model.BabyProfileState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.io.IOException
import java.net.URI

class BabyProfileViewModel : ViewModel() {

    private val _babyProfile = MutableStateFlow<BabyProfileState>(BabyProfileState.Loading)
    val babyProfile: StateFlow<BabyProfileState> = _babyProfile.asStateFlow()

    init {
        fetchBabyProfile()
    }

    private fun fetchBabyProfile() {
        viewModelScope.launch {
            _babyProfile.value = BabyProfileState.Loading
            try {
                val response = RetrofitInstance.api.getBabyProfile()
                if (response.isSuccessful) {
                    response.body()?.let {
                        _babyProfile.value = BabyProfileState.Success(it)
                    } ?: throw Exception("Failed to load profile data")
                } else {
                    throw Exception("Failed to retrieve profile")
                }
            } catch (e: Exception) {
                _babyProfile.value = BabyProfileState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun updateBabyProfile(context: Context, uuid: String, name: String, dob: String, gender: String, profilePictureUri: Uri?) {
        viewModelScope.launch {
            _babyProfile.value = BabyProfileState.Loading
            try {
                val nameRequestBody = RequestBody.create("text/plain".toMediaType(), name)
                val dobRequestBody = RequestBody.create("text/plain".toMediaType(), dob)
                val genderRequestBody = RequestBody.create("text/plain".toMediaType(), gender)
                val profilePictureRequestBody = profilePictureUri?.let { uriToRequestBody(it, context) }
                val profilePicturePart = profilePictureRequestBody?.let {
                    MultipartBody.Part.createFormData("profile_picture", "filename.jpg", it)
                }
                val response = RetrofitInstance.api.updateBabyProfile(uuid, nameRequestBody, dobRequestBody, genderRequestBody, profilePicturePart)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _babyProfile.value = BabyProfileState.Success(it)
                    } ?: throw Exception("Failed to update profile data")
                } else {
                    throw Exception("Failed to update profile")
                }
            } catch (e: Exception) {
                _babyProfile.value = BabyProfileState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun uriToRequestBody(uri: Uri, context: Context): RequestBody? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val byteArray = inputStream?.readBytes()
            inputStream?.close()
            byteArray?.let {
                RequestBody.create("image/jpeg".toMediaType(), it)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

}