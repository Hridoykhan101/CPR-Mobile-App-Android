package com.bluetooth.pa2123.resus.ui;

public class cprHistory {
    private int id;
    private String username = "";
    private float CR = 0;
    private int CC = 0;
    private int GC = 0;
    private String CF = "";
    private String currentTime = "";
    private int timer = 0;

    public cprHistory(int id, String username, float CR, int CC, int GC, String CF, String currentTime, int timer) {
        this.id = id;
        this.username = username;
        this.CR = CR;
        this.CC = CC;
        this.GC = GC;
        this.CF = CF;
        this.currentTime = currentTime;
        this.timer = timer;
    }

    //toString
    @Override
    public String toString() {
        return "\n" + currentTime +
                "\n\nRate = " + CR + " CPM" +
                "   \t\t\t\t\tFull Comp. = " + GC +
                "\nComp. Fraction = " + CF + "% " +
                "\tDuration: " + timer + " Sec\n";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCR() {
        return CR;
    }

    public void setCR(float CR) {
        this.CR = CR;
    }

    public int getTimer() { return timer; }

    public void setTimer(int timer) { this.timer = timer;}

    public int getCC() {
        return CC;
    }

    public void setCC(int CC) {
        this.CC = CC;
    }

    public int getGC() {
        return GC;
    }

    public void setGC(int GC) {
        this.GC = GC;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCF() {
        return CF;
    }

    public void setCF(String CF) {
        this.CF = CF;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }
}
