package com.mikepn.template.v1.dtos.response.auth;

import com.mikepn.template.v1.enums.EResponseType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class Response<T> {
    private EResponseType type;
    private T payload;
}