package com.decideme.client.model;

/**
 * Created by vivek_shah on 26/8/16.
 */
public class LatLngClass {

    private String worker_id;
    private String worker_name;
    private String worker_image;
    private String worker_price;
    private String worker_lat;
    private String worker_long;

    public LatLngClass(String worker_id, String worker_name, String worker_image, String worker_price,
                       String worker_lat, String worker_long) {
        this.worker_id = worker_id;
        this.worker_name = worker_name;
        this.worker_image = worker_image;
        this.worker_price = worker_price;
        this.worker_lat = worker_lat;
        this.worker_long = worker_long;
    }

    public String getWorker_id() {
        return worker_id;
    }

    public String getWorker_name() {
        return worker_name;
    }

    public String getWorker_image() {
        return worker_image;
    }

    public String getWorker_price() {
        return worker_price;
    }

    public String getWorker_lat() {
        return worker_lat;
    }

    public String getWorker_long() {
        return worker_long;
    }


}
