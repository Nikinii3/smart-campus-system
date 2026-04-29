package org.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
@Produces (MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();

        //API metadata
        response.put("name", "Smart Campus Sensor & Room Management API System");
        response.put("version", "1.0.0");
        response.put("description",
                "RESTful API for track and manage campus rooms and Iot sensors.");

        //Administrative contact
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("name", "Campus Facilities Management");
        contact.put("email", "facilities@university.ac.uk");
        contact.put("department", "School of Computer Science and Engineering");
        response.put("contact", contact);

        //Collection of links for primary resource - HATEOAS
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", "/smart-campus-api/api/v1");
        links.put("rooms", "/smart-campus-api/api/v1/rooms");
        links.put("sensors", "/smart-campus-api/api/v1/sensors");
        response.put("links", links);

        //API capabilities summary
        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("rooms", "Create, list, retrieve, and decommission campus rooms");
        capabilities.put("sensors", "Register, list, filter, and monitor IoT sensors");
        capabilities.put("readings", "Record and retrieve historical sensor data via /api/v1/sensors/{sensorId}/readings");
        response.put("capabilities", capabilities);

        response.put("timestamp", System.currentTimeMillis());
        response.put("status", "operational");

        return Response.ok(response).build();
    }
}
