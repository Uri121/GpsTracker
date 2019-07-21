package com.example.gpstracker.Model;
/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class CurrentFriend {
    private String id;
    private Double lat;
    private Double lng;

    public CurrentFriend(String id, Double lat, Double lng) {
        this.id = id;
        this.lat = lat;
        this.lng = lng;
    }
    public CurrentFriend(){}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }
}
