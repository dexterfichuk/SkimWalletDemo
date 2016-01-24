package com.simplytapp.demo.json;

import com.google.gson.annotations.SerializedName;

public class CardListRequest {

    public static final String COMMAND_GET_CARD_LIST = "GetCardList";

    @SerializedName("DATA")
    private final RequestData data;

    public CardListRequest(RequestData data) {
        this.data = data;
    }
}