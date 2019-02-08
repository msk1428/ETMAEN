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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.group7.etmaen.adapter.AddClassifierAdapter;
import com.group7.etmaen.database.AddEntry;
import com.group7.etmaen.database.AppDatabase;
import com.group7.etmaen.model.AddFace;
import com.group7.etmaen.model.AddFaceResponse;
import com.group7.etmaen.model.DetectFaceResponse;
import com.group7.etmaen.model.FindSimilar;
import com.group7.etmaen.model.FindSimilarResponse;
import com.group7.etmaen.model.Message;
import com.group7.etmaen.model.UploadServerResponse;
import com.group7.etmaen.networking.api.Service;
import com.group7.etmaen.networking.generator.DataGenerator;
import com.group7.etmaen.viewmodel.AppExecutors;
import com.group7.etmaen.viewmodel.MainViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    @BindView(R.id.button_upload)
    Button button_upload;

    @BindView(R.id.recycler_view)
    RecyclerView recycler_view;

    @BindView(R.id.progress)
    ProgressBar progress;

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
    private String path,faceId,username,phonenumber;
    private static final String POST_PATH = "post_path";
    private String m_name, m_phonenumber, m_nationalid, m_imagename, m_uid;
    private AppDatabase mDb;
    private AddClassifierAdapter adapter;
    private String[] uploadImages;
    private int[] itemIds ;


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
        uploadImages = new String[] {getString(R.string.pick_gallery),getString(R.string.click_camera),getString(R.string.remove)} ;
        itemIds= new int[]{0, 1, 2};

        if (savedInstanceState != null) {
            if (path != null) {
                path = savedInstanceState.getString(POST_PATH);
                postPath = path;
                Glide.with(this).load(path).into(image_header);
            }
        }
        mDb = AppDatabase.getInstance(getApplicationContext());
        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AddClassifierAdapter(this, this);
        recycler_view.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                if (viewHolder instanceof AddClassifierAdapter.ClassifierViewHolder) {
                    int position = viewHolder.getAdapterPosition();
                    List<AddEntry> entryList = adapter.getClassifier();
                    String persistedId = entryList.get(position).getPersistedid();
                    AddEntry db_position = entryList.get(position);

                    // remove the item from recycler view
                    adapter.removeItem(viewHolder.getAdapterPosition());

                    deleteRecord(persistedId, db_position, position);
                }
            }
        }).attachToRecyclerView(recycler_view);

        setupViewModel();
    }

    private void deleteRecord(String persistedid, AddEntry position, int position1) {
        try{
            showProgress();
            Service service = DataGenerator.createService(Service.class, BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
            Call<Void> call = service.deleteFace(persistedid);

            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(AddFaceActivity.this, R.string.record_deleted_success, Toast.LENGTH_SHORT).show();
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            mDb.imageClassifierDao().deleteClassifier(position);
                        });
                        hideProgress();
                    } else {
                        hideProgress();
                        adapter.restoreItem(position, position1);
                        Toast.makeText(AddFaceActivity.this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                    hideProgress();
                    adapter.restoreItem(position, position1);
                    Toast.makeText(AddFaceActivity.this, R.string.error_deleting, Toast.LENGTH_SHORT).show();
                }
            });
        } catch(Exception e) {
            hideProgress();
            adapter.restoreItem(position, position1);
            Toast.makeText(AddFaceActivity.this, R.string.error_deleting, Toast.LENGTH_SHORT).show();

        }
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
                    Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(AddFaceActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void verifyData() {
        name.setError(null);
        contact_number.setError(null);

        if (input_name.length() == 0) {

            name.setError(getString(R.string.error_name));

        } else if (input_contact_number.length() == 0) {

            name.setError(getString(R.string.error_contact_number));

        } else {
            String name = input_name.getText().toString().trim();
            String contact_number = input_contact_number.getText().toString().trim();

            addFace();
        }
    }

    private void launchImagePicker(){
        new MaterialDialog.Builder(this)
                .title(R.string.uploadImages)
                .items(uploadImages)
                .itemsIds(itemIds)
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
            Toast.makeText(this, R.string.sorry_error, Toast.LENGTH_LONG).show();
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
    public Uri getOutputMediaFileUri(int type) { return Uri.fromFile(getOutputMediaFile(type));
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
    private void addFace() {
        showProgress();
        if (postPath == null || postPath.isEmpty()) {
            hideProgress();
            return;
        }
        try {
            InputStream in = new FileInputStream(new File(postPath));
            byte[] buf;
            try {
                buf = new byte[in.available()];
                while (in.read(buf) != -1);
                RequestBody requestBody = RequestBody
                        .create(MediaType.parse("application/octet-stream"), buf);

                Service userService = DataGenerator.createService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
                Call<List<DetectFaceResponse>> call = userService.detectFace(Boolean.TRUE, Boolean.FALSE,  requestBody);

                call.enqueue(new Callback<List<DetectFaceResponse>>() {
                    @Override
                    public void onResponse(Call<List<DetectFaceResponse>> call, Response<List<DetectFaceResponse>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<DetectFaceResponse> addFaceResponse = response.body();
                                if (!addFaceResponse.isEmpty()) {
                                    faceId = addFaceResponse.get(0).getFaceId();
                                    findFace();
                                } else {
                                    hideProgress();
                                    Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            hideProgress();
                            Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DetectFaceResponse>> call, Throwable t) {
                        hideProgress();
                        Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                hideProgress();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            hideProgress();
            e.printStackTrace();
        }
    }
    private FindSimilar findSimilar() {
        FindSimilar findSimilar = new FindSimilar();
        if (faceId != null) {
            findSimilar.setFaceId(faceId);
            findSimilar.setFaceListId("etmaenfacelist");
            findSimilar.setMaxNumOfCandidatesReturned(1);
            findSimilar.setMode("matchPerson");
        }

        return findSimilar;
    }

    private void findFace() {
        Service userService = DataGenerator.createService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
        Call<List<FindSimilarResponse>> call = userService.fetchSimilar(findSimilar());

        call.enqueue(new Callback<List<FindSimilarResponse>>() {
            @Override
            public void onResponse(Call<List<FindSimilarResponse>> call, Response<List<FindSimilarResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<FindSimilarResponse> findSimilarResponses = response.body();
                        if (findSimilarResponses.isEmpty() || findSimilarResponses == null) {
                            addFace(username,phonenumber);
                        } else {
                            hideProgress();
                            Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    hideProgress();
                    Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FindSimilarResponse>> call, Throwable t) {
                hideProgress();
                Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void addFace(String name, String phonenumber) {

        showProgress();
        if(postPath == null || postPath.isEmpty()){
            hideProgress();
            return;
        }
        String userDate = name + "," +phonenumber;

        try {
            InputStream in = new FileInputStream(new File(postPath));
            byte[] buf;
            try {
                buf = new byte[in.available()];
                while (in.read(buf) != -1);
                RequestBody requestBody = RequestBody .create(MediaType.parse("application/octet-stream"),buf);

                Service userService = DataGenerator.createService(Service.class,BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
                Call<AddFaceResponse> call = userService.addFace(userDate,requestBody);

                call.enqueue(new Callback<AddFaceResponse>() {
                    @Override
                    public void onResponse(Call<AddFaceResponse> call, Response<AddFaceResponse> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                AddFaceResponse addFaceResponse = response.body();
                                String persistedId = addFaceResponse.getPersistedFaceId();

                                //final AddEntry imageEntry = new AddEntry(name, phonenumber, persistedId, postPath, "");
                                // AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertClassifier(imageEntry));
                                //emptyInputEditText();
                                hideProgress();
                                Toast.makeText(AddFaceActivity.this, R.string.successfully_created, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            hideProgress();
                            Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<AddFaceResponse> call, Throwable t) {
                        hideProgress();
                        Toast.makeText(AddFaceActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();

                    }
                });

            } catch (IOException e) {
                hideProgress();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            hideProgress();
            e.printStackTrace();
        }
    }
    /**
     * This method is to empty all input edit text
     */
    private void emptyInputEditText() {
        input_name.setText("");
        input_contact_number.setText("");
        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals(null);
        path.equals(null);
        refreshActivity();
    }

    private void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        progress.setVisibility(View.GONE);
    }

    private void refreshActivity(){
        recreate();
    }


    @Override
    public void onItemClickListener(int itemId) {

    }
}
