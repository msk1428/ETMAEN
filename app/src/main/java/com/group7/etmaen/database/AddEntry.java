package com.group7.etmaen.database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.sql.Blob;
import java.util.Date;

/**
 * Created by delaroy on 9/4/18.
 */

@Entity
public class AddEntry {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    private int id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "phonenumber")
    private String phonenumber;

    @ColumnInfo(name = "persistedid")
    private String persistedid;

    @ColumnInfo(name = "image")
    private String image;

    @ColumnInfo(name = "uid")
    private String uid;


    @Ignore
    public AddEntry(String name, String phonenumber, String persistedid, String image, String uid) {
        this.name = name;
        this.phonenumber = phonenumber;
        this.persistedid = persistedid;
        this.image = image;
        this.uid = uid;
    }

    public AddEntry(int id, String name, String phonenumber, String persistedid, String image, String uid) {
        this.id = id;
        this.name = name;
        this.phonenumber = phonenumber;
        this.persistedid = persistedid;
        this.image = image;
        this.uid = uid;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setPersistedid(String persistedid){ this.persistedid = persistedid; }

    public String getPersistedid() { return persistedid; }

    public void setImage(String image) { this.image = image; }

    public String getImage() { return image; }

    public void setUid(String uid) { this.uid = uid; }

    public String getUid() { return uid; }

}
