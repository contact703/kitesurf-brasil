package com.kitesurf.brasil.api

import retrofit2.http.*
import retrofit2.Response

// ===== DATA CLASSES =====

data class User(
    val id: Int,
    val name: String,
    val username: String,
    val bio: String? = null,
    val avatar_url: String? = null,
    val cover_url: String? = null,
    val level: String = "iniciante",
    val location: String? = null,
    val instagram: String? = null,
    val followers_count: Int = 0,
    val following_count: Int = 0,
    val posts_count: Int = 0,
    val verified: Int = 0,
    val is_following: Boolean = false
)

data class Post(
    val id: Int = 0,
    val user_id: Int = 0,
    val content: String? = null,
    val media_url: Any? = null,
    val spot_id: Int? = null,
    val spot_name: String? = null,
    val likes_count: Int = 0,
    val comments_count: Int = 0,
    val created_at: String? = null,
    val name: String? = null,
    val username: String? = null,
    val avatar_url: String? = null,
    val verified: Int = 0,
    val liked_by_user: Boolean = false
)

data class Comment(
    val id: Int,
    val user_id: Int,
    val content: String,
    val created_at: String,
    val name: String,
    val username: String,
    val avatar_url: String? = null
)

data class Spot(
    val id: Int,
    val name: String,
    val description: String?,
    val location: String?,
    val state: String?,
    val wind_direction: String?,
    val best_months: String?,
    val difficulty: String?,
    val amenities: String?,
    val rating: Double = 0.0,
    val rating_count: Int = 0
)

data class Classified(
    val id: Int,
    val user_id: Int,
    val title: String,
    val description: String?,
    val category: String,
    val price: Double?,
    val condition: String?,
    val brand: String?,
    val size: String?,
    val location: String?,
    val views_count: Int = 0,
    val created_at: String? = null,
    val name: String? = null,
    val username: String? = null,
    val avatar_url: String? = null
)

data class Accommodation(
    val id: Int,
    val name: String,
    val description: String?,
    val location: String?,
    val state: String?,
    val price_range: String?,
    val price_min: Double?,
    val price_max: Double?,
    val contact_whatsapp: String?,
    val instagram: String?,
    val rating: Double = 0.0,
    val verified: Int = 0
)

data class Conversation(
    val id: Int,
    val other_user_id: Int,
    val other_user_name: String,
    val other_user_username: String,
    val other_user_avatar: String?,
    val last_message: String?,
    val unread_count: Int = 0,
    val last_message_at: String?
)

data class Message(
    val id: Int,
    val conversation_id: Int,
    val sender_id: Int,
    val content: String,
    val created_at: String,
    val sender_name: String,
    val sender_username: String,
    val sender_avatar: String?
)

data class ForumCategory(
    val id: Int,
    val name: String,
    val description: String?,
    val icon: String?,
    val color: String?,
    val topics_count: Int = 0
)

data class ForumTopic(
    val id: Int,
    val category_id: Int,
    val user_id: Int,
    val title: String,
    val content: String,
    val views_count: Int = 0,
    val replies_count: Int = 0,
    val likes_count: Int = 0,
    val created_at: String,
    val author_name: String? = null,
    val author_username: String? = null,
    val author_avatar: String? = null,
    val category_name: String? = null,
    val category_color: String? = null
)

data class ForumReply(
    val id: Int,
    val topic_id: Int,
    val user_id: Int,
    val content: String,
    val created_at: String,
    val author_name: String,
    val author_username: String,
    val author_avatar: String?
)

data class ChatRequest(val message: String, val session_id: String? = null)
data class ChatResponse(val response: String, val session_id: String?)
data class LikeResponse(val message: String, val liked: Boolean)
data class FollowResponse(val message: String, val following: Boolean)
data class SendMessageRequest(val sender_id: Int, val recipient_id: Int, val content: String)
data class PostRequest(val user_id: Int, val content: String, val spot_id: Int? = null)
data class CommentRequest(val user_id: Int, val content: String)
data class TopicRequest(val user_id: Int, val category_id: Int, val title: String, val content: String)
data class ReplyRequest(val user_id: Int, val content: String)
data class TopicDetail(
    val id: Int,
    val title: String,
    val content: String,
    val views_count: Int,
    val replies_count: Int,
    val likes_count: Int,
    val created_at: String,
    val author_name: String?,
    val author_username: String?,
    val author_avatar: String?,
    val category_name: String?,
    val replies: List<ForumReply>
)
data class CategoryTopics(val category: ForumCategory, val topics: List<ForumTopic>)
data class GenericResponse(val id: Int? = null, val message: String)

// ===== API INTERFACE =====

interface ApiService {
    // Chat
    @POST("chat")
    suspend fun sendChat(@Body request: ChatRequest): Response<ChatResponse>
    
    // Feed
    @GET("feed")
    suspend fun getFeed(@Query("userId") userId: Int? = null, @Query("limit") limit: Int = 20): Response<List<Post>>
    
    @GET("feed/user/{userId}")
    suspend fun getUserPosts(@Path("userId") userId: Int): Response<List<Post>>
    
    @POST("feed")
    suspend fun createPost(@Body request: PostRequest): Response<GenericResponse>
    
    @POST("feed/{id}/like")
    suspend fun likePost(@Path("id") postId: Int, @Body body: Map<String, Int>): Response<LikeResponse>
    
    @POST("feed/{id}/comment")
    suspend fun commentPost(@Path("id") postId: Int, @Body request: CommentRequest): Response<GenericResponse>
    
    // Users
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: Int, @Query("currentUserId") currentUserId: Int? = null): Response<User>
    
    @GET("users/username/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<User>
    
    @POST("users/{id}/follow")
    suspend fun followUser(@Path("id") userId: Int, @Body body: Map<String, Int>): Response<FollowResponse>
    
    @GET("users/{id}/followers")
    suspend fun getFollowers(@Path("id") userId: Int): Response<List<User>>
    
    @GET("users/{id}/following")
    suspend fun getFollowing(@Path("id") userId: Int): Response<List<User>>
    
    @GET("users/suggestions/{userId}")
    suspend fun getSuggestions(@Path("userId") userId: Int): Response<List<User>>
    
    @GET("users/search/{query}")
    suspend fun searchUsers(@Path("query") query: String): Response<List<User>>
    
    // Spots
    @GET("spots")
    suspend fun getSpots(): Response<List<Spot>>
    
    @GET("spots/{id}")
    suspend fun getSpot(@Path("id") spotId: Int): Response<Spot>
    
    @GET("spots/state/{state}")
    suspend fun getSpotsByState(@Path("state") state: String): Response<List<Spot>>
    
    // Classifieds
    @GET("classifieds")
    suspend fun getClassifieds(@Query("category") category: String? = null): Response<List<Classified>>
    
    @GET("classifieds/{id}")
    suspend fun getClassified(@Path("id") id: Int): Response<Classified>
    
    // Accommodations
    @GET("accommodations")
    suspend fun getAccommodations(@Query("state") state: String? = null): Response<List<Accommodation>>
    
    @GET("accommodations/{id}")
    suspend fun getAccommodation(@Path("id") id: Int): Response<Accommodation>
    
    // Messages
    @GET("messages/conversations/{userId}")
    suspend fun getConversations(@Path("userId") userId: Int): Response<List<Conversation>>
    
    @GET("messages/conversation/{conversationId}")
    suspend fun getMessages(@Path("conversationId") conversationId: Int, @Query("userId") userId: Int): Response<List<Message>>
    
    @POST("messages/send")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<GenericResponse>
    
    @GET("messages/search-users")
    suspend fun searchUsersForChat(@Query("q") query: String, @Query("userId") userId: Int): Response<List<User>>
    
    // Forum
    @GET("forum/categories")
    suspend fun getForumCategories(): Response<List<ForumCategory>>
    
    @GET("forum/category/{categoryId}")
    suspend fun getCategoryTopics(@Path("categoryId") categoryId: Int): Response<CategoryTopics>
    
    @GET("forum/topic/{topicId}")
    suspend fun getTopic(@Path("topicId") topicId: Int): Response<TopicDetail>
    
    @GET("forum/recent")
    suspend fun getRecentTopics(@Query("limit") limit: Int = 10): Response<List<ForumTopic>>
    
    @POST("forum/topic")
    suspend fun createTopic(@Body request: TopicRequest): Response<GenericResponse>
    
    @POST("forum/topic/{topicId}/reply")
    suspend fun replyTopic(@Path("topicId") topicId: Int, @Body request: ReplyRequest): Response<GenericResponse>
    
    @GET("forum/search")
    suspend fun searchForum(@Query("q") query: String): Response<List<ForumTopic>>
}
