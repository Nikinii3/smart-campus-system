package org.smartcampus.application;

import org.smartcampus.exception.*;
import org.smartcampus.filter.ApiLoggingFilter;
import org.smartcampus.resource.DiscoveryResource;
import org.smartcampus.resource.RoomResource;
import org.smartcampus.resource.SensorResource;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(ResourceNotFoundExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);
        classes.add(ApiLoggingFilter.class);
        classes.add(JacksonFeature.class);
        return classes;
    }
}