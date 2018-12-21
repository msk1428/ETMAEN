package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreatePerson implements Parcelable
{

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("userData")
    @Expose
    private String userData;
    public final static Parcelable.Creator<CreatePerson> CREATOR = new Creator<CreatePerson>() {


        @SuppressWarnings({
                "unchecked"
        })
        public CreatePerson createFromParcel(Parcel in) {
            return new CreatePerson(in);
        }

        public CreatePerson[] newArray(int size) {
            return (new CreatePerson[size]);
        }

    }
            ;

    protected CreatePerson(Parcel in) {
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.userData = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public CreatePerson() {
    }

    /**
     *
     * @param name
     * @param userData
     */
    public CreatePerson(String name, String userData) {
        super();
        this.name = name;
        this.userData = userData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserData() {
        return userData;
    }

    public void setUserData(String userData) {
        this.userData = userData;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(name);
        dest.writeValue(userData);
    }

    public int describeContents() {
        return 0;
    }

}
