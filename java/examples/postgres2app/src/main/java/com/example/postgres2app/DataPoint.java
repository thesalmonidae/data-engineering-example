package com.example.postgres2app;

/**
 * Simple POJO class that represents
 * an enriched datapoint.
 */
public class DataPoint {
    private String id;
    private String timestamp;
    private Double lon;
    private Double lat;
    private Double filteredLon;
    private Double filteredLat;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getFilteredLon() {
        return filteredLon;
    }

    public void setFilteredLon(Double filteredLon) {
        this.filteredLon = filteredLon;
    }

    public Double getFilteredLat() {
        return filteredLat;
    }

    public void setFilteredLat(Double filteredLat) {
        this.filteredLat = filteredLat;
    }
}
