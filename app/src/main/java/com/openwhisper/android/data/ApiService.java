package com.openwhisper.android.data;

import com.openwhisper.android.model.ApiMessage;
import com.openwhisper.android.model.ChatSummary;
import com.openwhisper.android.model.LoginRequest;
import com.openwhisper.android.model.RegisterRequest;
import com.openwhisper.android.model.SendMessageBody;
import com.openwhisper.android.model.TokenResponse;
import com.openwhisper.android.model.UserProfile;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {

    @POST("token/")
    Call<TokenResponse> login(@Body LoginRequest body);

    @POST("register/")
    Call<ResponseBody> register(@Body RegisterRequest body);

    @GET("chats/")
    Call<List<ChatSummary>> listChats();

    @GET("chats/{id}/messages/")
    Call<List<ApiMessage>> listMessages(@Path("id") int chatId);

    @POST("chats/{id}/messages/")
    Call<ApiMessage> postMessage(@Path("id") int chatId, @Body SendMessageBody body);

    @GET("users/me/")
    Call<UserProfile> me();

    @POST("logout/")
    Call<ResponseBody> logout();
}
