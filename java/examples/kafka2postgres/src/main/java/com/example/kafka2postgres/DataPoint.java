package com.example.kafka2postgres;

import java.time.LocalDateTime;

/**
 * Simple POJO class that represents
 * an enriched datapoint.
 */
public class DataPoint {
    private String id;
    private double lon;
    private double lat;
    private LocalDateTime timestamp;
    private double filteredLon;
    private double filteredLat;

    public DataPoint() {}

    public DataPoint(String id, double lon, double lat, LocalDateTime timestamp) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getLon() { return lon; }
    public void setLon(double lon) { this.lon = lon; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public double getFilteredLon() { return filteredLon; }
    public void setFilteredLon(double filteredLon) { this.filteredLon = filteredLon; }
    public double getFilteredLat() { return filteredLat; }
    public void setFilteredLat(double filteredLat) { this.filteredLat = filteredLat; }

    /**
     * For debugging.
     */
    @Override
    public String toString() {
        return String.format(
            "DataPoint(id=%s, lon=%.2f, lat=%.2f, timestamp=%s, filteredLon=%.2f, filteredLat=%.2f)",
            id, lon, lat, timestamp.toString(), filteredLon, filteredLat
        );
    }
}
