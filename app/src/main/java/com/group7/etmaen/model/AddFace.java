package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddFace implements Parcelable
{

    @SerializedName("url")
    @Expose
    private String url;
    public final static Parcelable.Creator<AddFace> CREATOR = new Creator<AddFace>() {


        @SuppressWarnings({
                "unchecked"
        })
        public AddFace createFromParcel(Parcel in) {
            return new AddFace(in);
        }

        public AddFace[] newArray(int size) {
            return (new AddFace[size]);
        }

    };

    protected AddFace(Parcel in) {
        this.url = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public AddFace() {
    }

    /**
     *
     * @param url
     */
    public AddFace(String url) {
        super();
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(url);
    }

    public int describeContents() {
        return 0;
    }

}
