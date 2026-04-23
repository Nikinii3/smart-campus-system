package org.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {
    private static final Logger LOGGER = Logger.getLogger(RoomNotEmptyExceptionMapper.class.getName());

    @Override
    public Response toResponse(RoomNotEmptyException ex) {
        LOGGER.warning("409 Conflict: " + ex.getMessage());
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(409, "Conflict", ex.getMessage()))
                .build();
    }
}