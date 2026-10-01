package com.example.unimarket.response;

/**
 * Generic acknowledgement for operations that return no data.
 */
public record MessageResponse(String message) {

    public static MessageResponse of(String message) {
        return new MessageResponse(message);
    }
}
