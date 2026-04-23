package org.smartcampus.resource;

import org.smartcampus.exception.SensorUnavailableException;
import org.smartcampus.model.DataStore;
import org.smartcampus.model.Sensor;
import org.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sensorId", sensorId);
        response.put("totalReadings", readings.size());
        response.put("readings", readings);
        return Response.ok(response).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensorById(sensorId).get();
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId, sensor.getStatus());
        }
        if (reading == null) {
            return Response.status(400).entity("Request body with value field is required.").build();
        }
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        store.addReadingForSensor(sensorId, reading);
        sensor.setCurrentValue(reading.getValue());
        store.saveSensor(sensor);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Reading recorded successfully.");
        response.put("sensorId", sensorId);
        response.put("updatedSensorCurrentValue", sensor.getCurrentValue());
        response.put("reading", reading);
        return Response.status(201).entity(response).build();
    }

    @GET
    @Path("/{readingId}")
    public Response getReadingById(@PathParam("readingId") String readingId) {
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        SensorReading found = readings.stream()
                .filter(r -> r.getId().equals(readingId))
                .findFirst().orElse(null);
        if (found == null) {
            return Response.status(404).entity("Reading '" + readingId + "' not found.").build();
        }
        return Response.ok(found).build();
    }
}