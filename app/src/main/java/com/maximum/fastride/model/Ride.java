package com.maximum.fastride.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Oleg Kleiman on 13-Apr-15.
 */
public class Ride implements Serializable  {

    @com.google.gson.annotations.SerializedName("id")
    public String Id;

    @com.google.gson.annotations.SerializedName("ridecode")
    private String rideCode;
    public String getRideCode() {
        return rideCode;
    }
    public void setRideCode(String value) { rideCode = value; }

    @com.google.gson.annotations.SerializedName("created")
    private Date created;
    public Date getCreated() { return created; }
    public void setCreated(Date value) { created = value; }

    @com.google.gson.annotations.SerializedName("driverid")
    private String driverid;
    public String getNameDriver() { return driverid; }
    public void setNameDriver(String value) { driverid = value; }

    @com.google.gson.annotations.SerializedName("carnumber")
    private String carNumber;
    public String getCarNumber() { return carNumber; }
    public void setCarNumber(String value) { carNumber = value; }

    @com.google.gson.annotations.SerializedName("approved")
    private Boolean approved;
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean value) { approved = value; }

    @com.google.gson.annotations.SerializedName("picture_url")
    private String picture_url;
    public String getPictureURL() { return picture_url; }
    public void setPictureURL(String value) { picture_url = value; }
}
