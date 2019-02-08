package com.group7.etmaen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.florent37.rxgps.RxGps;
import com.group7.etmaen.adapter.VerifyAdapter;
import com.group7.etmaen.database.AddEntry;
import com.group7.etmaen.database.AppDatabase;
import com.group7.etmaen.database.VerifiedEntry;
import com.group7.etmaen.model.AddFace;
import com.group7.etmaen.model.AddFaceResponse;
import com.group7.etmaen.model.DetectFaceResponse;
import com.group7.etmaen.model.FetchDetailsResponse;
import com.group7.etmaen.model.FindSimilar;
import com.group7.etmaen.model.FindSimilarResponse;
import com.group7.etmaen.model.UploadServerResponse;
import com.group7.etmaen.model.VerifyUploadServerResponse;
import com.group7.etmaen.networking.api.Service;
import com.group7.etmaen.networking.generator.DataGenerator;
import com.group7.etmaen.viewmodel.AppExecutors;
import com.group7.etmaen.viewmodel.MainViewModel;
import com.group7.etmaen.viewmodel.VerifyViewModel;

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
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.group7.etmaen.VerifiedDetailActivity.EXTRA_VERIFIED_ID;
import static com.group7.etmaen.utils.Constants.AZURE_BASE_URL;
import static com.group7.etmaen.utils.Constants.FACE_LIST_ID;
import static com.group7.etmaen.utils.Constants.IMAGE;
import static com.group7.etmaen.utils.Constants.SERVER_BASE_URL;

public class VerifyActivity extends BaseActivity implements View.OnClickListener, VerifyAdapter.ItemClickListener {

    @BindView(R.id.tv_current_location)
    TextView locationText;

    @BindView(R.id.tv_current_address)
    TextView addressText;

    @BindView(R.id.selectImage)
    ImageView selectImage;

    @BindView(R.id.image_header)
    ImageView image_header;

    @BindView(R.id.button_verify)
    Button button_verify;

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
    private String m_imagename, faceId;
    private  RxGps rxGps;
    private AppDatabase mDb;
    private VerifyAdapter mAdapter;
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    private  AddEntry addEntry;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verify_face_layout);

        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        selectImage.setOnClickListener(this);
        button_verify.setOnClickListener(this);

        if (savedInstanceState != null) {
            if (path != null) {
                path = savedInstanceState.getString(POST_PATH);
                postPath = path;
                Glide.with(this).load(path).into(image_header);
            }
        }
        initpDialog();
        mDb = AppDatabase.getInstance(getApplicationContext());

        rxGps = new RxGps(this);
        getLocation();
        getStreet();

        recycler_view.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new VerifyAdapter(this, this);
        recycler_view.setAdapter(mAdapter);
        setupViewModel();
    }

    private void setupViewModel() {
        VerifyViewModel viewModel = ViewModelProviders.of(this).get(VerifyViewModel.class);
        viewModel.getTasks().observe(this, taskEntries -> mAdapter.setTasks(taskEntries));
    }

    @SuppressLint("CheckResult")
    private void getLocation () {
        rxGps.lastLocation()

                .doOnSubscribe(this::addDisposable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(location -> {
                    locationText.setText(location.getLatitude() + ", " + location.getLongitude());
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        displayError(throwable.getMessage());
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        displayError(throwable.getMessage());
                    }
                });

    }

    @SuppressLint("CheckResult")
    private void getStreet() {
        rxGps.locationLowPower()
                .flatMapMaybe(rxGps::geocoding)

                .doOnSubscribe(this::addDisposable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(address -> {
                    addressText.setText(getAddressText(address));
                }, throwable -> {
                    if (throwable instanceof RxGps.PermissionException) {
                        displayError(throwable.getMessage());
                    } else if (throwable instanceof RxGps.PlayServicesNotAvailableException) {
                        displayError(throwable.getMessage());
                    }
                });
    }

    public void displayError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getAddressText(Address address) {
        String addressText = "";
        final int maxAddressLineIndex = address.getMaxAddressLineIndex();

        for (int i = 0; i <= maxAddressLineIndex; i++) {
            addressText += address.getAddressLine(i);
            if (i != maxAddressLineIndex) {
                addressText += "\n";
            }
        }

        return addressText;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_verify:
                if (postPath == null) {
                    Toast.makeText(this, R.string.select_image, Toast.LENGTH_SHORT).show();
                } else {
                    addFace();
                }
                break;
            case R.id.selectImage:
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(VerifyActivity.this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.SEND_SMS, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
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
                Toast.makeText(VerifyActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
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

    private void addFace() {
        showpDialog();
        if (postPath == null || postPath.isEmpty()) {
            hidepDialog();
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

                Service userService = DataGenerator.createService(Service.class, BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
                Call<List<DetectFaceResponse>> call = userService.detectFace(Boolean.TRUE, Boolean.FALSE,  requestBody);

                call.enqueue(new Callback<List<DetectFaceResponse>>() {
                    @Override
                    public void onResponse(Call<List<DetectFaceResponse>> call, Response<List<DetectFaceResponse>> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                List<DetectFaceResponse> addFaceResponse = response.body();
                                faceId = addFaceResponse.get(0).getFaceId();
                                getStreet();
                                refreshActivity();
                                findFace();
                            }
                        } else {
                            hidepDialog();
                            Toast.makeText(VerifyActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<List<DetectFaceResponse>> call, Throwable t) {
                        hidepDialog();
                        Toast.makeText(VerifyActivity.this, R.string.error_creation, Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                hidepDialog();
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            hidepDialog();
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
        Service userService = DataGenerator.createService(Service.class, BuildConfig.COGNITIVE_SERVICE_API, AZURE_BASE_URL);
        Call<List<FindSimilarResponse>> call = userService.fetchSimilar(findSimilar());

        call.enqueue(new Callback<List<FindSimilarResponse>>() {
            @Override
            public void onResponse(Call<List<FindSimilarResponse>> call, Response<List<FindSimilarResponse>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<FindSimilarResponse> findSimilarResponses = response.body();
                        if (findSimilarResponses.isEmpty() || findSimilarResponses == null) {
                            hidepDialog();
                            Toast.makeText(VerifyActivity.this, R.string.no_face_matching, Toast.LENGTH_SHORT).show();
                        } else {
                            String persistedFaceId = findSimilarResponses.get(0).getPersistedFaceId();
                            fetchDetails(persistedFaceId);
                            hidepDialog();
                            Toast.makeText(VerifyActivity.this, R.string.person_found, Toast.LENGTH_SHORT).show();
                        }

                    }
                }else {
                    hidepDialog();
                    Toast.makeText(VerifyActivity.this, R.string.error_find_face, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<FindSimilarResponse>> call, Throwable t) {
                hidepDialog();
                Toast.makeText(VerifyActivity.this, R.string.error_find_face, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchDetails(String persistedFaceId) {

        Runnable r = () -> {
            addEntry = mDb.imageClassifierDao().loadEntryByPersistedFaceId(persistedFaceId);
        };
        Thread thread = new Thread(r);
        thread.start();

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (addEntry != null) {
                String name = addEntry.getName();
                String image = addEntry.getImage();
                String phonenumber = addEntry.getPhonenumber();
                String address = addressText.getText().toString();

                sendMySMS(phonenumber, name + " " + R.string.is_found + address);

                VerifiedEntry verifiedEntry = new VerifiedEntry(name, phonenumber, persistedFaceId, image, address);
                AppExecutors.getInstance().diskIO().execute(() -> mDb.imageClassifierDao().insertVerifiedImage(verifiedEntry));
                hidepDialog();
            }

        }, 2000);

    }

    public void sendMySMS(String phone, String message) {

        //Check if the phoneNumber is empty
        if (phone.isEmpty() || phone == null) {
            Toast.makeText(getApplicationContext(), R.string.valid_number, Toast.LENGTH_SHORT).show();
        } else {

            SmsManager sms = SmsManager.getDefault();
            // if message length is too long messages are divided
            List<String> messages = sms.divideMessage(message);
            for (String msg : messages) {
                PendingIntent sentIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_SENT"), 0);
                PendingIntent deliveredIntent = PendingIntent.getBroadcast(this, 0, new Intent("SMS_DELIVERED"), 0);
                sms.sendTextMessage(phone, null, msg, sentIntent, deliveredIntent);

            }
        }
    }

    public void onResume() {
        super.onResume();
        sentStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Unknown Error";
                switch (getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Sent Successfully !!";
                        break;
                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                        s = "Generic Failure Error";
                        break;
                    case SmsManager.RESULT_ERROR_NO_SERVICE:
                        s = "Error : No Service Available";
                        break;
                    case SmsManager.RESULT_ERROR_NULL_PDU:
                        s = "Error : Null PDU";
                        break;
                    case SmsManager.RESULT_ERROR_RADIO_OFF:
                        s = "Error : Radio is off";
                        break;
                    default:
                        break;
                }
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();

            }
        };
        deliveredStatusReceiver=new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent arg1) {
                String s = "Message Not Delivered";
                switch(getResultCode()) {
                    case Activity.RESULT_OK:
                        s = "Message Delivered Successfully";
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }
        };
        registerReceiver(sentStatusReceiver, new IntentFilter("SMS_SENT"));
        registerReceiver(deliveredStatusReceiver, new IntentFilter("SMS_DELIVERED"));
    }


    public void onPause() {
        super.onPause();
        unregisterReceiver(sentStatusReceiver);
        unregisterReceiver(deliveredStatusReceiver);
    }


    private void refreshActivity()
    {
        image_header.setImageResource(R.color.colorPrimary);
        postPath.equals(null);
        path.equals(null);
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
        Intent intent = new Intent(VerifyActivity.this, VerifiedDetailActivity.class);
        intent.putExtra(EXTRA_VERIFIED_ID, itemId);
        startActivity(intent);
    }
}
