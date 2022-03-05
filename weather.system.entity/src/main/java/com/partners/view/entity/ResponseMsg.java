package com.partners.view.entity;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Tolerate;

@Data
@Builder
public class ResponseMsg implements Serializable {

    private static final long serialVersionUID = 1L;
    private int statusCode;
    private String message;
    private Object messageObject;

    @Tolerate
    public ResponseMsg() {
    }
}
