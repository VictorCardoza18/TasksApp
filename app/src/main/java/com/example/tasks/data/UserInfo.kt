package com.example.tasks.data

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val id: String? = null,
    val username: String,
    val email: String,
    val isAdmin: Boolean? = null,
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null,
    @SerializedName("__v") val v: Int? = null
)