package com.simplytapp.demo.api;

import com.simplytapp.demo.json.CardListRequest;
import com.simplytapp.demo.json.CardListResponse;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;

public interface CardService {

    String API_BASE_PATH = "/accounts/Api";

    @POST("/")
    void getCardList(@Body CardListRequest cardListRequest, Callback<CardListResponse> callback);
}
