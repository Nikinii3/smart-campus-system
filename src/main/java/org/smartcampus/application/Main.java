package org.smartcampus.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.smartcampus.exception.*;
import org.smartcampus.filter.ApiLoggingFilter;
import org.smartcampus.resource.DiscoveryResource;
import org.smartcampus.resource.RoomResource;
import org.smartcampus.resource.SensorResource;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static final String BASE_URI = "http://0.0.0.0:8080/api/v1";

    public static HttpServer startServer() {
        final ResourceConfig config = new ResourceConfig()
                .register(DiscoveryResource.class)
                .register(RoomResource.class)
                .register(SensorResource.class)
                .register(RoomNotEmptyExceptionMapper.class)
                .register(LinkedResourceNotFoundExceptionMapper.class)
                .register(SensorUnavailableExceptionMapper.class)
                .register(ResourceNotFoundExceptionMapper.class)
                .register(GlobalExceptionMapper.class)
                .register(ApiLoggingFilter.class)
                .register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();
        LOGGER.info("Smart Campus API started!");
        LOGGER.info("Discovery:  http://localhost:8080/api/v1");
        LOGGER.info("Rooms:      http://localhost:8080/api/v1/rooms");
        LOGGER.info("Sensors:    http://localhost:8080/api/v1/sensors");
    }
}