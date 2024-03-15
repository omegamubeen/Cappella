package com.app.cappella.model

data class BabyProfile(
    val id: String,
    val name: String,
    val dob: String,
    val gender: String,
    val profile_picture: String
)

sealed class BabyProfileState {
    data object Loading : BabyProfileState()
    data class Success(val babyProfile: BabyProfile) : BabyProfileState()
    data class Error(val message: String) : BabyProfileState()
}