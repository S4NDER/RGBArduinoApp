package com.jansen.sander.carrgbapp;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by Sander on 20/12/2017.
 */

@Dao
public interface Color_Db_API {

    @Insert
    void insertAllColors(CustomColor...customColors);

    @Delete
    void delete(CustomColor customColor);

    @Query("SELECT * FROM customcolor")
    List<CustomColor> getStoredColors();

    @Query("SELECT * FROM customcolor WHERE cid IN (:customColorIds)")
    List<CustomColor> loadAllByIds(int[] customColorIds);
}
