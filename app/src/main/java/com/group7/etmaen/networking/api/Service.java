package com.group7.etmaen.networking.api;

import com.group7.etmaen.model.AddFace;
import com.group7.etmaen.model.AddFaceResponse;
import com.group7.etmaen.model.DetectFaceResponse;
import com.group7.etmaen.model.FetchDetailsResponse;
import com.group7.etmaen.model.FindSimilar;
import com.group7.etmaen.model.FindSimilarResponse;
import com.group7.etmaen.model.Message;
import com.group7.etmaen.model.UploadServerResponse;
import com.group7.etmaen.model.VerifyUploadServerResponse;
import com.group7.etmaen.networking.Routes;

import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.group7.etmaen.utils.Constants.FACE_LIST_ID;

/**
 * Created by delaroy on 9/13/18.
 */

public interface Service {

    @Multipart
    @POST(Routes.UPLOAD_IMAGE_SERVER)
    Call<UploadServerResponse> createRecord(@PartMap() Map<String, RequestBody> partMap, @Part MultipartBody.Part file);

    @Multipart
    @POST(Routes.VERIFY_IMAGE_SERVER)
    Call<VerifyUploadServerResponse> verifyImageUpload(@Part MultipartBody.Part file);

    @POST(Routes.ADD_FACE + FACE_LIST_ID +  "/persistedFaces")
    Call<AddFaceResponse> addFace(@Query("userData") String userData, @Body RequestBody photo);

    @POST(Routes.DETECT_FACE)
    Call<List<DetectFaceResponse>> detectFace(@Query("returnFaceId") Boolean returnFaceId, @Query("returnFaceLandmarks") Boolean returnFaceLandmarks, @Body RequestBody photo);

    @FormUrlEncoded
    @PUT(Routes.UPDATE_RECORD + "/{uid}")
    Call<Message> updateWithPersistedId(@Path("uid") String uid, @Field("persistedFaceId") String persistedFaceId);

    @POST(Routes.FETCH_DETAILS + "{persistedFaceId}")
    Call<FetchDetailsResponse> fetchDetails(@Path("persistedFaceId") String persistedFaceId);

    @POST(Routes.FIND_SIMILAR)
    Call<List<FindSimilarResponse>> fetchSimilar(@Body FindSimilar findSimilar) ;

    @DELETE(Routes.DELETE_FACE + "persistedFaces/" + "{persistedFaceId}")
    Call<Void> deleteFace(@Path("persistedFaceId") String persistedFaceId);
}
