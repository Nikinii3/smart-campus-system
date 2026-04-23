package org.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {
    private static final Logger LOGGER = Logger.getLogger(SensorUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(SensorUnavailableException ex) {
        LOGGER.warning("403 Forbidden: " + ex.getMessage());
        return Response.status(Response.Status.FORBIDDEN)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(403, "Forbidden", ex.getMessage()))
                .build();
    }
}