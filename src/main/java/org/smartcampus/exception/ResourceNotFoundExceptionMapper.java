package org.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {
    private static final Logger LOGGER = Logger.getLogger(ResourceNotFoundExceptionMapper.class.getName());

    @Override
    public Response toResponse(ResourceNotFoundException ex) {
        LOGGER.warning("404 Not Found: " + ex.getMessage());
        return Response.status(Response.Status.NOT_FOUND)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(404, "Not Found", ex.getMessage()))
                .build();
    }
}