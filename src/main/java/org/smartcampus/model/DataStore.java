package org.smartcampus.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private static volatile DataStore instance;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new ConcurrentHashMap<>();

    private DataStore() {
    }

    public static DataStore getInstance() {
        if (instance == null) {
            synchronized (DataStore.class) {
                if (instance == null) {
                    instance = new DataStore();
                }
            }
        }
        return instance;
    }

    public Map<String, Room> getRooms() { return rooms; }
    public Optional<Room> getRoomById(String id) { return Optional.ofNullable(rooms.get(id)); }
    public Room saveRoom(Room room) { rooms.put(room.getId(), room); return room; }
    public boolean deleteRoom(String id) { return rooms.remove(id) != null; }
    public boolean roomExists(String id) { return rooms.containsKey(id); }

    public Map<String, Sensor> getSensors() { return sensors; }
    public Optional<Sensor> getSensorById(String id) { return Optional.ofNullable(sensors.get(id)); }
    public Sensor saveSensor(Sensor sensor) { sensors.put(sensor.getId(), sensor); return sensor; }
    public boolean deleteSensor(String id) { return sensors.remove(id) != null; }
    public boolean sensorExists(String id) { return sensors.containsKey(id); }

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public SensorReading addReadingForSensor(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>())).add(reading);
        return reading;
    }
}