package com.group7.etmaen.networking;

import static com.group7.etmaen.utils.Constants.SERVER_BASE_URL;

public interface Routes {

    String UPLOAD_IMAGE_SERVER = "etmaen/createuser";
    String VERIFY_IMAGE_SERVER = "etmaen/verify";
    String ADD_FACE = "facelists/";
    String UPDATE_RECORD = "etmaen/persisted";
    String FETCH_DETAILS = "etmaen/fetchdetails/";
    String DETECT_FACE = "detect";
    String FIND_SIMILAR = "findsimilars";

}
