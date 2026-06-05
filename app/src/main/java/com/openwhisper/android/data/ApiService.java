package com.openwhisper.android.data;

import com.openwhisper.android.model.ApiMessage;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.model.FriendRequestsResponse;
import com.openwhisper.android.model.LoginRequest;
import com.openwhisper.android.model.PublicUser;
import com.openwhisper.android.model.RegisterRequest;
import com.openwhisper.android.model.SendMessageBody;
import com.openwhisper.android.model.TokenResponse;
import com.openwhisper.android.model.UpdateChatBody;
import com.openwhisper.android.model.UserProfile;
import com.openwhisper.android.model.UsernameBody;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("token/")
    Call<TokenResponse> login(@Body LoginRequest body);

    @POST("register/")
    Call<ResponseBody> register(@Body RegisterRequest body);

    @GET("chats/")
    Call<List<ChatSummary>> listChats();

    @GET("chats/{id}/")
    Call<ChatSummary> getChat(@Path("id") int chatId);

    @PATCH("chats/{id}/")
    Call<ChatSummary> updateChat(@Path("id") int chatId, @Body UpdateChatBody body);

    @POST("chats/start/")
    Call<ChatSummary> startDm(@Body UsernameBody body);

    @POST("chats/{id}/invite/")
    Call<ChatSummary> inviteToChat(@Path("id") int chatId, @Body UsernameBody body);

    @DELETE("chats/{id}/members/{username}/")
    Call<ResponseBody> removeMember(@Path("id") int chatId, @Path("username") String username);

    @GET("chats/{id}/messages/")
    Call<List<ApiMessage>> listMessages(@Path("id") int chatId);

    @POST("chats/{id}/messages/")
    Call<ApiMessage> postMessage(@Path("id") int chatId, @Body SendMessageBody body);

    @Multipart
    @POST("chats/{id}/messages/")
    Call<ApiMessage> postMessageWithAttachment(
            @Path("id") int chatId,
            @Part("content") RequestBody content,
            @Part MultipartBody.Part attachment);

    @GET("users/search/")
    Call<List<PublicUser>> searchUsers(@Query("q") String query);

    @GET("users/me/friend-requests/")
    Call<FriendRequestsResponse> listFriendRequests();

    @POST("users/me/friend-requests/")
    Call<PublicUser> sendFriendRequest(@Body UsernameBody body);

    @POST("users/me/friend-requests/{username}/accept/")
    Call<PublicUser> acceptFriendRequest(@Path("username") String username);

    @DELETE("users/me/friend-requests/{username}/")
    Call<ResponseBody> cancelFriendRequest(@Path("username") String username);

    @GET("users/me/friends/")
    Call<List<PublicUser>> listFriends();

    @DELETE("users/me/friends/{username}/")
    Call<ResponseBody> removeFriend(@Path("username") String username);

    @GET("users/me/")
    Call<UserProfile> me();

    @POST("logout/")
    Call<ResponseBody> logout();
}
