package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VerifyUploadServerResponse implements Parcelable
{

    @SerializedName("error")
    @Expose
    private boolean error;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("imagename")
    @Expose
    private String imagename;

    public final static Parcelable.Creator<VerifyUploadServerResponse> CREATOR = new Creator<VerifyUploadServerResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public VerifyUploadServerResponse createFromParcel(Parcel in) {
            return new VerifyUploadServerResponse(in);
        }

        public VerifyUploadServerResponse[] newArray(int size) {
            return (new VerifyUploadServerResponse[size]);
        }

    };

    protected VerifyUploadServerResponse(Parcel in) {
        this.error = ((boolean) in.readValue((boolean.class.getClassLoader())));
        this.message = ((String) in.readValue((String.class.getClassLoader())));
        this.imagename = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public VerifyUploadServerResponse() {
    }

    /**
     *
     * @param message
     * @param error
     * @param imagename
     */
    public VerifyUploadServerResponse(boolean error, String message, String imagename) {
        super();
        this.error = error;
        this.message = message;
        this.imagename = imagename;
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

    public String getImagename() {
        return imagename;
    }

    public void setImagename(String imagename) {
        this.imagename = imagename;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(error);
        dest.writeValue(message);
        dest.writeValue(imagename);
    }

    public int describeContents() {
        return 0;
    }

}

