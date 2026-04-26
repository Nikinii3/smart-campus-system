package org.smartcampus.resource;

import org.smartcampus.exception.ResourceNotFoundException;
import org.smartcampus.exception.RoomNotEmptyException;
import org.smartcampus.model.DataStore;
import org.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(store.getRooms().values())).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().trim().isBlank()) {
            return Response.status(400).entity(error(400, "Bad Request", "Room 'id' is required.")).build();
        }
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return Response.status(400).entity(error(400, "Bad Request", "Room 'name' is required.")).build();
        }
        if (store.roomExists(room.getId())) {
            return Response.status(409).entity(error(409, "Conflict", "Room '" + room.getId() + "' already exists.")).build();
        }
        if (room.getSensorIds() == null) room.setSensorIds(new ArrayList<>());
        store.saveRoom(room);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room created successfully.");
        response.put("room", room);
        return Response.status(201).entity(response).build();
    }

    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        return Response.ok(room).build();
    }

    @PUT
    @Path("/{roomId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRoom(@PathParam("roomId") String roomId, Room updatedRoom) {
        Room existing = store.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));

        if (updatedRoom.getName() != null && !updatedRoom.getName().trim().isBlank()) {
            existing.setName(updatedRoom.getName());
        }
        if (updatedRoom.getCapacity() > 0) {
            existing.setCapacity(updatedRoom.getCapacity());
        }

        store.saveRoom(existing);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' updated successfully.");
        response.put("room", existing);
        return Response.ok(response).build();
    }

    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoomById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(roomId);
        }
        store.deleteRoom(roomId);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Room '" + roomId + "' deleted successfully.");
        response.put("deletedRoomId", roomId);
        return Response.ok(response).build();
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