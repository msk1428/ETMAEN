package com.group7.etmaen.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CreatePersonResponse implements Parcelable
{

    @SerializedName("personId")
    @Expose
    private String personId;
    public final static Parcelable.Creator<CreatePersonResponse> CREATOR = new Creator<CreatePersonResponse>() {


        @SuppressWarnings({
                "unchecked"
        })
        public CreatePersonResponse createFromParcel(Parcel in) {
            return new CreatePersonResponse(in);
        }

        public CreatePersonResponse[] newArray(int size) {
            return (new CreatePersonResponse[size]);
        }

    }
            ;

    protected CreatePersonResponse(Parcel in) {
        this.personId = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     *
     */
    public CreatePersonResponse() {
    }

    /**
     *
     * @param personId
     */
    public CreatePersonResponse(String personId) {
        super();
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(personId);
    }

    public int describeContents() {
        return 0;
    }

}
