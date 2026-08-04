package com.example.consumer;

public class RiderLocation {

    private String id;
    private String lat;
    private String lon;
    private String vector;

    public RiderLocation() {
    }

    public RiderLocation(String id, String lat, String lon, String vector) {
        this.id = id;
        this.lat = lat;
        this.lon = lon;
        this.vector = vector;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getVector() {
        return vector;
    }

    public void setVector(String vector) {
        this.vector = vector;
    }
}
