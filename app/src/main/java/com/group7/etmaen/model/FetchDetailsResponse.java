package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class FetchDetailsResponse implements Parcelable
{

    @SerializedName("error")
    @Expose
    private boolean error;
    @SerializedName("id")
    @Expose
    private int id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("phonenumber")
    @Expose
    private String phonenumber;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("persistedFaceId")
    @Expose
    private String persistedFaceId;
    public final static Parcelable.Creator<FetchDetailsResponse> CREATOR = new Creator<FetchDetailsResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public FetchDetailsResponse createFromParcel(Parcel in) {
            return new FetchDetailsResponse(in);
        }

        public FetchDetailsResponse[] newArray(int size) {
            return (new FetchDetailsResponse[size]);
        }

    }
            ;

    protected FetchDetailsResponse(Parcel in) {
        this.error = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.id = ((int) in.readValue((int.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.phonenumber = ((String) in.readValue((String.class.getClassLoader())));
        this.image = ((String) in.readValue((String.class.getClassLoader())));
        this.uid = ((String) in.readValue((String.class.getClassLoader())));
        this.persistedFaceId = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public FetchDetailsResponse() {
    }

    /**
     *
     * @param uid
     * @param id
     * @param phonenumber
     * @param error
     * @param persistedFaceId
     * @param name
     * @param image
     */
    public FetchDetailsResponse(boolean error, int id, String name, String phonenumber, String image, String uid, String persistedFaceId) {
        super();
        this.error = error;
        this.id = id;
        this.name = name;
        this.phonenumber = phonenumber;
        this.image = image;
        this.uid = uid;
        this.persistedFaceId = persistedFaceId;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPersistedFaceId() {
        return persistedFaceId;
    }

    public void setPersistedFaceId(String persistedFaceId) {
        this.persistedFaceId = persistedFaceId;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(error);
        dest.writeValue(id);
        dest.writeValue(name);
        dest.writeValue(phonenumber);
        dest.writeValue(image);
        dest.writeValue(uid);
        dest.writeValue(persistedFaceId);
    }

    public int describeContents() {
        return 0;
    }

}
