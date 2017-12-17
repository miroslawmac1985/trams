package com.yoho.trams;

public class Tram {

    public static final int HIGH_FLOOR = 1;
    public static final int LOW_FLOOR = 3;
    public static final int PARTED_FLOOR = 2;

    private String apiId;
    private String direction;
    private int heading;
    private int id;
    private double latitude;
    private String lineNumber;
    private boolean live;
    private double longitude;
    private int lowFloor;
    private String lowFloorDisplay;
    private String name;
    private String stockNumber;
    private String stockType;

    public Tram(String apiId, int id, String direction, int heading, String lowFloorDisplay, int lowFloor, double latitude, double longitude, String lineNumber, String name, boolean live, String stockNumber, String stockType) {
        this.apiId = apiId;
        this.id = id;
        this.direction = direction;
        this.heading = heading;
        this.lowFloorDisplay = lowFloorDisplay;
        this.lowFloor = lowFloor;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lineNumber = lineNumber;
        this.name = name;
        this.live = live;
        this.stockNumber = stockNumber;
        this.stockType = stockType;
    }

    public String getApiId() {
        return this.apiId;
    }

    public int getId() {
        return this.id;
    }

    public String getDirection() {
        return this.direction;
    }

    public int getHeading() {
        return this.heading;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public String getLineNumber() {
        return this.lineNumber;
    }

    public String getName() {
        return this.name;
    }

    public int getLowFloor() {
        return this.lowFloor;
    }

    public String getLowFloorDisplay() {
        return this.lowFloorDisplay;
    }

    public boolean isLive() {
        return this.live;
    }

    public String getStockNumber() {
        return this.stockNumber;
    }

    public String getStockType() {
        return this.stockType;
    }
}
