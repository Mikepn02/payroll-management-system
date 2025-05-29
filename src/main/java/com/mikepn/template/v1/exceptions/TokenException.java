package com.mikepn.template.v1.exceptions;

import com.mikepn.template.v1.dtos.response.auth.ErrorResponse;
import com.mikepn.template.v1.dtos.response.auth.Response;
import com.mikepn.template.v1.enums.EResponseType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

public class TokenException extends RuntimeException {
    private final HttpStatus status = HttpStatus.UNAUTHORIZED;

    public TokenException(String message) {
        super(message);
    }

    public ResponseEntity<Response> getResponseEntity(){
        List<String> details = new ArrayList<>();
        details.add(super.getMessage());
        ErrorResponse errorResponse = new ErrorResponse().setMessage("You do not have authority to access this resource").setDetails(details);
        Response<ErrorResponse> response = new Response<>();
        response.setPayload(errorResponse);
        response.setType(EResponseType.UNAUTHORIZED);
        return new ResponseEntity<Response>(response, status);
    }
}