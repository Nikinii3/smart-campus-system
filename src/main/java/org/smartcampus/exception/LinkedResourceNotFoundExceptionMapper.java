package org.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {
    private static final Logger LOGGER = Logger.getLogger(LinkedResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        LOGGER.warning("422 Unprocessable Entity: " + ex.getMessage());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(422, "Unprocessable Entity", ex.getMessage()))
                .build();
    }
}