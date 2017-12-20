package com.jansen.sander.carrgbapp;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Sander on 20/12/2017.
 */

public class Color {
    @PrimaryKey(autoGenerate = true)
    private int cid;

    @ColumnInfo(name = "rgb_red")
    private int red;

    @ColumnInfo(name = "rgb_green")
    private int green;

    @ColumnInfo(name = "rgb_blue")
    private int blue;

    @ColumnInfo(name = "rgb_note")
    private String note;

    public int getCid() {
        return cid;
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public String getNote() {
        return note;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public Color(int red, int green, int blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
