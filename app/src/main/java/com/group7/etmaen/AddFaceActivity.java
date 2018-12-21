package com.group7.etmaen;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.group7.etmaen.adapter.AddClassifierAdapter;
import com.group7.etmaen.database.AddEntry;
import com.group7.etmaen.database.AppDatabase;
import com.group7.etmaen.model.AddFace;
import com.group7.etmaen.model.AddFaceResponse;
import com.group7.etmaen.model.Message;
import com.group7.etmaen.model.UploadServerResponse;
import com.group7.etmaen.networking.api.Service;
import com.group7.etmaen.networking.generator.DataGenerator;
import com.group7.etmaen.viewmodel.AppExecutors;
import com.group7.etmaen.viewmodel.MainViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.group7.etmaen.utils.Constants.AZURE_BASE_URL;
import static com.group7.etmaen.utils.Constants.FACE_LIST_ID;
import static com.group7.etmaen.utils.Constants.IMAGE;
import static com.group7.etmaen.utils.Constants.SERVER_BASE_URL;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener, AddClassifierAdapter.ItemClickListener {

    @BindView(R.id.selectImage)
    ImageView selectImage;

    @BindView(R.id.image_header)
    ImageView image_header;

    @BindView(R.id.name)
    TextInputLayout name;

    @BindView(R.id.input_name)
    EditText input_name;

    @BindView(R.id.contact_number)
    TextInputLayout contact_number;

    @BindView(R.id.input_contact_number)
    EditText input_contact_number;

    @BindView(R.id.national_id)
    TextInputLayout national_id;

    @BindView(R.id.input_national_id)
    EditText input_national_id;

    @BindView(R.id.button_upload)
    Button button_upload;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;

    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int CAMERA_PIC_REQUEST = 1111;
    public static final int MEDIA_TYPE_IMAGE = 1;
    private Uri fileUri;
    private String mediaPath;
    private String mImageFileLocation = "";
    public static final String IMAGE_DIRECTORY_NAME = "Android File Upload";
    private String postPath;
    private static final String TAG = AddFaceActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private String path;
    private static final String POST_PATH = "post_path";
    private String m_name, m_phonenumber, m_nationalid, m_imagename, m_uid;
    private AppDatabase mDb;
    private AddClassifierAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_face_layout);

        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectImage.setOnClickListener(this);
        button_upload.setOnClickListener(this);

        if (savedInstanceState != null) {
            if (path != null) {
                path = savedInstanceState.getString(POST_PATH);
                postPath = path;
                Glide.with(this).load(path).into(image_header);
            }
        }
        initpDialog();
        mDb = AppDatabase.getInstance(getApplicationContext());
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddClassifierAdapter(this, this);
        recycler_view.setAdapter(adapter);

        setupViewModel();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getTasks().observe(this, taskEntries -> adapter.setTasks(taskEntries));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_upload:
                if (postPath == null) {
                    Toast.makeText(this, "please select an image", Toast.LENGTH_SHORT).show();
                } else {
                    verifyData();
                }
                break;
            case R.id.selectImage:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(AddFaceActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
                    } else {
                        launchImagePicker();
                    }
                } else {
                    launchImagePicker();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            }else{
                Toast.makeText(AddFaceActivity.this, "Permission denied, the permissions are very important for the apps usage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void verifyData() {
        name.setError(null);
        contact_number.setError(null);
        national_id.setError(null);

        if (input_name.length() == 0) {

            name.setError(getString(R.string.error_name));

        } else if (input_contact_number.length() == 0) {

            name.setError(getString(R.string.error_contact_number));

        } else {
            String national_id;
            String name = input_name.getText().toString().trim();
            String contact_number = input_contact_number.getText().toString().trim();
            national_id = input_national_id.getText().toString().trim();
            if (national_id.isEmpty()){
                national_id = "nil";
            }

            submitDetails(name, contact_number, national_id);
        }
    }


    private void launchImagePicker(){
        new MaterialDialog.Builder(this)
                .title(R.string.uploadImages)
                .items(R.array.uploadImages)
                .itemsIds(R.array.itemIds)
                .itemsCallback((dialog, view, which, text) -> {
                    switch (which) {
                        case 0:
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent, REQUEST_PICK_PHOTO);
                            break;
                        case 1:
                            captureImage();
                            break;
                        case 2:
                            image_header.setImageResource(R.color.colorPrimary);
                            postPath.equals(null);
                            path.equals(null);
                            refreshActivity();
                            break;
                    }
                })
                .show();
    }

    /**
     * Launching camera app to capture image
     */
    private void captureImage() {
        if (Build.VERSION.SDK_INT > 21) { //use this if Lollipop_Mr1 (API 22) or above
            Intent callCameraApplicationIntent = new Intent();
            callCameraApplicationIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

            // We give some instruction to the intent to save the image
            File photoFile = null;

            try {
                // If the createImageFile will be successful, the photo file will have the address of the file
                photoFile = createImageFile();
                // Here we call the function that will try to catch the exception made by the throw function
            } catch (IOException e) {
                Logger.getAnonymousLogger().info("Exception error in generating the file");
                e.printStackTrace();
            }
            // Here we add an extra file to the intent to put the address on to. For this purpose we use the FileProvider, declared in the AndroidManifest.
            Uri outputUri = FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    photoFile);
            callCameraApplicationIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);

            // The following is a new line with a trying attempt
            callCameraApplicationIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Logger.getAnonymousLogger().info("Calling the camera App by intent");

            // The following strings calls the camera app and wait for his file in return.
            startActivityForResult(callCameraApplicationIntent, CAMERA_PIC_REQUEST);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

            // start the image capture Intent
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }
    }

    File createImageFile() throws IOException {
        Logger.getAnonymousLogger().info("Generating the image - method started");

        // Here we create a "non-collision file name", alternatively said, "an unique filename" using the "timeStamp" functionality
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmSS").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp;
        // Here we specify the environment location and the exact path where we want to save the so-created file
        File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/photo_saving_app");
        Logger.getAnonymousLogger().info("Storage directory set");

        // Then we create the storage directory if does not exists
        if (!storageDirectory.exists()) storageDirectory.mkdir();

        // Here we create the file using a prefix, a suffix and a directory
        File image = new File(storageDirectory, imageFileName + ".jpg");
        // File image = File.createTempFile(imageFileName, ".jpg", storageDirectory);

        // Here the location is saved into the string mImageFileLocation
        Logger.getAnonymousLogger().info("File name and path set");

        mImageFileLocation = image.getAbsolutePath();
        fileUri = Uri.parse(mImageFileLocation);
        // The file is returned to the previous intent across the camera application
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO || requestCode == REQUEST_PICK_PHOTO) {
                if (data != null) {
                    // Get the Image from data
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};

                    Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    assert cursor != null;
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    mediaPath = cursor.getString(columnIndex);
                    // Set the Image in ImageView for Previewing the Media
                    image_header.setImageBitmap(BitmapFactory.decodeFile(mediaPath));
                    cursor.close();

                    postPath = mediaPath;
                    path = mediaPath;
                }

            }else if (requestCode == CAMERA_PIC_REQUEST){
                if (Build.VERSION.SDK_INT > 21) {
                    Glide.with(this).load(mImageFileLocation).into(image_header);
                    postPath = mImageFileLocation;
                    path = postPath;
                }else{
                    Glide.with(this).load(fileUri).into(image_header);
                    postPath = fileUri.getPath();
                    path = postPath;
                }
            }
        }
        else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Sorry, there was an error!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putString(POST_PATH, path);
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        path = savedInstanceState.getString(POST_PATH);
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + ".jpg");
        }  else {
            return null;
        }

        return mediaFile;
    }

    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    @NonNull
    private MultipartBody.Part prepareFilePart(String partName) {

        File file = new File(postPath);

        // create RequestBody instance from file
        RequestBody requestFile =
                RequestBody.create(
                        MediaType.parse("*/*"),
                        file
                );

        // MultipartBody.Part is used to send also the actual file name
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void submitDetails(String name, String contact, String nationalid){
        showpDialog();
        Service userService = DataGenerator.createService(Service.class, SERVER_BASE_URL);

        // create part for file (photo, video, ...)
        MultipartBody.Part body = prepareFilePart("sender");

        // create a map of data to pass along
        RequestBody authName = createPartFromString(name);
        RequestBody authContact = createPartFromString(contact);
        RequestBody authNationalId = createPartFromString(nationalid);

        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("name", authName);
        map.put("phonenumber", authContact);
        map.put("nationalid", authNationalId);

        Call<UploadServerResponse> call = userService.createRecord(map, body);
        call.enqueue(new Callback<UploadServerResponse>() {
            @Override
            public void onResponse(Call<UploadServerResponse> call, Response<UploadServerResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        UploadServerResponse uploadServerResponse = response.body();
                        m_name = uploadServerResponse.getName();
                        m_phonenumber = uploadServerResponse.getPhonenumber();
                        m_nationalid = uploadServerResponse.getNationalid();
                        m_imagename = uploadServerResponse.getImagename();
                        m_uid = uploadServerResponse.getUid();

                        final AddEntry imageEntry = new AddEntry(m_name, m_phonenumber, m_nationalid, null, postPath, m_uid);
                        AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertClassifier(imageEntry));
                        emptyInputEditText();
                        addFace();
                    }
                } else {
                    hidepDialog();
                    Toast.makeText(AddFaceActivity.this, "error uploading image", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadServerResponse> call, Throwable t) {
                hidepDialog();
                Toast.makeText(AddFaceActivity.this, "error uploading image " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private AddFace addFaceModel() {
        AddFace addFace = new AddFace();
        addFace.setUrl(IMAGE + m_imagename);
        return addFace;
    }

    private void addFace() {
        String userData = m_name + "," + m_phonenumber + "," + m_nationalid;
        Service userService = DataGenerator.createService(Service.class, BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
        Call<AddFaceResponse> call = userService.addFace(FACE_LIST_ID, userData,  addFaceModel());

        call.enqueue(new Callback<AddFaceResponse>() {
            @Override
            public void onResponse(Call<AddFaceResponse> call, Response<AddFaceResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        AddFaceResponse addFaceResponse = response.body();
                        String persistedId = addFaceResponse.getPersistedFaceId();
                        updateRecord(persistedId);
                        Toast.makeText(AddFaceActivity.this, "successfully created, please wait " + persistedId, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    hidepDialog();
                    Toast.makeText(AddFaceActivity.this, "error creation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AddFaceResponse> call, Throwable t) {
                hidepDialog();
                Toast.makeText(AddFaceActivity.this, "error creation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecord(String persistedFaceId) {
        showpDialog();
        Service userService = DataGenerator.createService(Service.class, SERVER_BASE_URL);
        Call<Message> call = userService.updateWithPersistedId(m_uid, persistedFaceId);

        call.enqueue(new Callback<Message>() {
            @Override
            public void onResponse(Call<Message> call, Response<Message> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        Message message = response.body();
                        String user_message = message.getMessage();
                        hidepDialog();
                        Toast.makeText(AddFaceActivity.this, user_message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    hidepDialog();
                    Toast.makeText(AddFaceActivity.this, "error updating record", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Message> call, Throwable t) {
                hidepDialog();
                Toast.makeText(AddFaceActivity.this, "error updating record", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        input_name.setText("");
        input_contact_number.setText("");
        input_national_id.setText("");
        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals(null);
        path.equals(null);
        refreshActivity();
    }

    private void refreshActivity(){
        recreate();
    }

    protected void initpDialog() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(true);
    }


    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }

    @Override
    public void onItemClickListener(int itemId) {

    }
}
