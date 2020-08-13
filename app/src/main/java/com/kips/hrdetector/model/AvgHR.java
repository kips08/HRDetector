package com.kips.hrdetector.model;

public class AvgHR {
    int id;
    int avgbpm;
    String stresslv;
    String created_at;

    public AvgHR() {

    }

    public AvgHR(int avgbpm, String stresslv, String created_at){
        this.avgbpm = avgbpm;
        this.stresslv = stresslv;
        this.created_at = created_at;
    }

    public AvgHR(int id, int avgbpm, String stresslv, String created_at){
        this.avgbpm = avgbpm;
        this.stresslv = stresslv;
        this.id = id;
        this.created_at = created_at;
    }

    //SETTER AND GETTER
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAvgbpm() {
        return avgbpm;
    }

    public void setAvgbpm(int avgbpm) {
        this.avgbpm = avgbpm;
    }

    public String getStresslv() {
        return stresslv;
    }

    public void setStresslv(String stresslv) {
        this.stresslv = stresslv;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

}
