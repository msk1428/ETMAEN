package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UploadServerResponse implements Parcelable
{

    @SerializedName("error")
    @Expose
    private boolean error;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("phonenumber")
    @Expose
    private String phonenumber;
    @SerializedName("nationalid")
    @Expose
    private String nationalid;
    @SerializedName("imagename")
    @Expose
    private String imagename;
    @SerializedName("uid")
    @Expose
    private String uid;
    public final static Parcelable.Creator<UploadServerResponse> CREATOR = new Creator<UploadServerResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public UploadServerResponse createFromParcel(Parcel in) {
            return new UploadServerResponse(in);
        }

        public UploadServerResponse[] newArray(int size) {
            return (new UploadServerResponse[size]);
        }

    };

    protected UploadServerResponse(Parcel in) {
        this.error = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.name = ((String) in.readValue((String.class.getClassLoader())));
        this.phonenumber = ((String) in.readValue((String.class.getClassLoader())));
        this.nationalid = ((String) in.readValue((String.class.getClassLoader())));
        this.imagename = ((String) in.readValue((String.class.getClassLoader())));
        this.uid = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public UploadServerResponse() {
    }

    /**
     *
     * @param message
     * @param phonenumber
     * @param error
     * @param name
     * @param nationalid
     * @param imagename
     */
    public UploadServerResponse(boolean error, String message, String name, String phonenumber, String nationalid, String imagename, String uid) {
        super();
        this.error = error;
        this.message = message;
        this.name = name;
        this.phonenumber = phonenumber;
        this.nationalid = nationalid;
        this.imagename = imagename;
        this.uid = uid;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public String getNationalid() {
        return nationalid;
    }

    public void setNationalid(String nationalid) {
        this.nationalid = nationalid;
    }

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(error);
        dest.writeValue(message);
        dest.writeValue(name);
        dest.writeValue(phonenumber);
        dest.writeValue(nationalid);
        dest.writeValue(imagename);
        dest.writeValue(uid);
    }

    public int describeContents() {
        return 0;
    }

}
