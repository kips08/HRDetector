package com.kips.hrdetector.model;

public class DetailHR {
    int id;
    int bpm;
    int id_AvgHR;

    public DetailHR() {

    }

    public DetailHR(int bpm, int id_AvgHR){
        this.bpm = bpm;
        this.id_AvgHR = id_AvgHR;
    }

    public DetailHR(int id, int bpm, int id_AvgHR){
        this.id = id;
        this.bpm = bpm;
        this.id_AvgHR = id_AvgHR;
    }


    //SETTER
    public void setId(int id) {
        this.id = id;
    }

    public void setBpm(int bpm) {
        this.bpm = bpm;
    }

    public void setId_AvgHR(int id_AvgHR) {
        this.id_AvgHR = id_AvgHR;
    }

    // GETTER
    public int getId() {
        return id;
    }

    public int getBpm() {
        return bpm;
    }

    public int getId_AvgHR() {
        return id_AvgHR;
    }
}
