package com.partners.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SystemOption implements java.io.Serializable {

    private static final long serialVersionUID = -2701235081208080022L;
    private String optionId;
    private String optionValue;
    private char isSystem;
    private int version;
}
