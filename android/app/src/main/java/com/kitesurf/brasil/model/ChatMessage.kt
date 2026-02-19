package com.kitesurf.brasil.model

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatResponse(
    val response: String,
    val sessionId: String?
)

data class Spot(
    val id: Int,
    val name: String,
    val description: String?,
    val location: String,
    val latitude: Double?,
    val longitude: Double?,
    val windDirection: String?,
    val bestMonths: String?,
    val difficulty: String,
    val rating: Float,
    val ratingCount: Int,
    val photos: List<String>?
)

data class Classified(
    val id: Int,
    val userId: Int,
    val title: String,
    val description: String?,
    val category: String,
    val price: Double?,
    val condition: String?,
    val photos: List<String>?,
    val location: String?,
    val contact: String?,
    val sellerName: String?
)

data class User(
    val id: Int,
    val name: String,
    val email: String?,
    val bio: String?,
    val avatarUrl: String?,
    val level: String,
    val location: String?
)
