package org.smartcampus.resource;

import org.smartcampus.exception.LinkedResourceNotFoundException;
import org.smartcampus.exception.ResourceNotFoundException;
import org.smartcampus.model.DataStore;
import org.smartcampus.model.Room;
import org.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> all = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.trim().isEmpty()) {
            all = all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }
        return Response.ok(all).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(400).entity(error(400, "Bad Request", "Sensor 'id' is required.")).build();
        }
        if (sensor.getType() == null || sensor.getType().trim().isEmpty()) {
            return Response.status(400).entity(error(400, "Bad Request", "Sensor 'type' is required.")).build();
        }
        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(400).entity(error(400, "Bad Request", "Sensor 'roomId' is required.")).build();
        }
        if (store.sensorExists(sensor.getId())) {
            return Response.status(409).entity(error(409, "Conflict", "Sensor '" + sensor.getId() + "' already exists.")).build();
        }
        if (!store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException("Room", sensor.getRoomId());
        }

        // Block MAINTENANCE or OFFLINE sensors from being assigned to a room
        if (sensor.getStatus() != null) {
            String upperStatus = sensor.getStatus().toUpperCase();
            if (upperStatus.equals("MAINTENANCE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Cannot assign sensor '" + sensor.getId() + "' to a room because it is in MAINTENANCE status. " +
                                "Only ACTIVE sensors can be assigned to rooms.")).build();
            }
            if (upperStatus.equals("OFFLINE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Cannot assign sensor '" + sensor.getId() + "' to a room because it is OFFLINE. " +
                                "Only ACTIVE sensors can be assigned to rooms.")).build();
            }
            if (!upperStatus.equals("ACTIVE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Sensor 'status' must be one of: ACTIVE, MAINTENANCE, OFFLINE.")).build();
            }
            sensor.setStatus(upperStatus);
        } else {
            sensor.setStatus("ACTIVE");
        }

        store.saveSensor(sensor);
        Room room = store.getRoomById(sensor.getRoomId()).get();
        room.addSensorId(sensor.getId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor registered successfully.");
        response.put("sensor", sensor);
        return Response.status(201).entity(response).build();
    }

    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensorById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));
        return Response.ok(sensor).build();
    }

    @PUT
    @Path("/{sensorId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {

        Sensor existing = store.getSensorById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));


        if (updatedSensor.getType() != null && !updatedSensor.getType().trim().isEmpty()) {
            existing.setType(updatedSensor.getType());
        }


        if (updatedSensor.getStatus() != null && !updatedSensor.getStatus().trim().isEmpty()) {
            String newStatus = updatedSensor.getStatus().toUpperCase();
            if (!newStatus.equals("ACTIVE") &&
                    !newStatus.equals("MAINTENANCE") &&
                    !newStatus.equals("OFFLINE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Status must be one of: ACTIVE, MAINTENANCE, OFFLINE.")).build();
            }
            existing.setStatus(newStatus);
        }

        if (updatedSensor.getCurrentValue() != 0) {
            existing.setCurrentValue(updatedSensor.getCurrentValue());
        }

        if (updatedSensor.getRoomId() != null && !updatedSensor.getRoomId().trim().isEmpty()) {

            // Block MAINTENANCE sensor from being added to a new room
            if (existing.getStatus().equals("MAINTENANCE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Cannot move sensor '" + sensorId + "' to a new room because it is in MAINTENANCE status. " +
                                "Update the status to ACTIVE first.")).build();
            }

            // Block OFFLINE sensor from being moved to a new room
            if (existing.getStatus().equals("OFFLINE")) {
                return Response.status(400).entity(error(400, "Bad Request",
                        "Cannot move sensor '" + sensorId + "' to a new room because it is OFFLINE. " +
                                "Update the status to ACTIVE first.")).build();
            }

            // Validate the new room exists
            if (!store.roomExists(updatedSensor.getRoomId())) {
                throw new LinkedResourceNotFoundException("Room", updatedSensor.getRoomId());
            }

            // Remove sensor from old room
            store.getRoomById(existing.getRoomId())
                    .ifPresent(oldRoom -> oldRoom.removeSensorId(sensorId));

            // Update sensor to new room
            existing.setRoomId(updatedSensor.getRoomId());
            store.getRoomById(updatedSensor.getRoomId())
                    .ifPresent(newRoom -> newRoom.addSensorId(sensorId));
        }

        store.saveSensor(existing);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor '" + sensorId + "' updated successfully.");
        response.put("sensor", existing);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensorById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));
        if (sensor.getRoomId() != null) {
            store.getRoomById(sensor.getRoomId())
                    .ifPresent(room -> room.removeSensorId(sensorId));
        }
        store.deleteSensor(sensorId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Sensor '" + sensorId + "' deleted successfully.");
        response.put("deletedSensorId", sensorId);
        return Response.ok(response).build();
    }

    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        store.getSensorById(sensorId)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", sensorId));
        return new SensorReadingResource(sensorId);
    }

    private Map<String, Object> error(int status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("timestamp", System.currentTimeMillis());
        return body;
    }
}