package com.jansen.sander.carrgbapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.ArrayList;

/**
 * Created by Sander on 20/12/2017.
 */

@Dao
public interface Color_Db_API {

    @Insert
    void insertAllColors(Color...colors);

    @Delete
    void delete(Color color);

    @Query("SELECT * FROM color")
    ArrayList<Color> getStoredColors();
}
