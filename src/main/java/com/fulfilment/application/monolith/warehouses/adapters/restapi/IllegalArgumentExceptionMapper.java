package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("type", exception.getClass().getName());
        body.put("status", Response.Status.BAD_REQUEST.getStatusCode());
        body.put("error", exception.getMessage() == null ? "Invalid request" : exception.getMessage());

        return Response.status(Response.Status.BAD_REQUEST)
            .type(MediaType.APPLICATION_JSON)
            .entity(body)
            .build();
    }
}
