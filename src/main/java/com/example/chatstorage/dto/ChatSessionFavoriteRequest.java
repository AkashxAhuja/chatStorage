package com.example.chatstorage.dto;

import jakarta.validation.constraints.NotNull;

public class ChatSessionFavoriteRequest {

    @NotNull
    private Boolean favorite;

    public Boolean getFavorite() {
        return favorite;
    }

    public void setFavorite(Boolean favorite) {
        this.favorite = favorite;
    }
}
