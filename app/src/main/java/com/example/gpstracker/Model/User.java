package com.example.gpstracker.Model;

/**
 * Created by Uri Robinov on 20/7/2019.
 */
public class User  {

    private String name;
    private String email;
    private String password;
    private String code;
    private String isSharing;
    private Double lat;
    private Double lng;
    private String image;
    private String UserId;
    private String battery;

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getBattery() {
        return battery;
    }

    public void setBatteyrLevel(String batteyLevel) {
        this.battery = batteyLevel;
    }

    public User(){}


    public User(String name, String email, String password, String code, String isSharing,
                Double lat, Double lng, String image, String userId, String batteryLevel) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.code = code;
        this.isSharing = isSharing;
        this.lat = lat;
        this.lng = lng;
        this.image = image;
        this.UserId = userId;
        this.battery = batteryLevel;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getIsSharing() {
        return isSharing;
    }

    public void setIsSharing(String isSharing) {
        this.isSharing = isSharing;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
