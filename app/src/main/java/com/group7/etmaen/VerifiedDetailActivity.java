package com.group7.etmaen;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.group7.etmaen.database.AppDatabase;
import com.group7.etmaen.database.VerifiedEntry;
import com.group7.etmaen.viewmodel.FetchVerifyViewModel;
import com.group7.etmaen.viewmodel.VerifyViewModelFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.group7.etmaen.utils.Constants.IMAGE;

public class VerifiedDetailActivity extends AppCompatActivity {

    public static final String EXTRA_VERIFIED_ID = "extraImageId";
    private static final int DEFAULT_VERIFIED_ID = -1;
    private int mVerifiedId = DEFAULT_VERIFIED_ID;
    public static final String INSTANCE_VERIFIED_ID = "instanceTaskId";
    private AppDatabase mDb;
    private BroadcastReceiver sentStatusReceiver, deliveredStatusReceiver;
    private String phonenumber, name, address;

    @BindView(R.id.classifier_image)
    ImageView classifier_image;

    @BindView(R.id.name_value)
    TextView name_value;

    @BindView(R.id.address_value)
    TextView address_value;

    @BindView(R.id.phone_value)
    TextView phone_value;

    @BindView(R.id.send_message)
    AppCompatButton send_message;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.verify_detail);

        ButterKnife.bind(this);
        mDb = AppDatabase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_VERIFIED_ID)) {
            // populate the UI
            mVerifiedId = intent.getIntExtra(EXTRA_VERIFIED_ID, DEFAULT_VERIFIED_ID);

            VerifyViewModelFactory factory = new VerifyViewModelFactory(mDb, mVerifiedId);
            final FetchVerifyViewModel viewModel
                    = ViewModelProviders.of(this, factory).get(FetchVerifyViewModel.class);

            viewModel.getVerify().observe(this, new Observer<VerifiedEntry>() {
                @Override
                public void onChanged(@Nullable VerifiedEntry verifiedEntry) {
                    viewModel.getVerify().removeObserver(this);
                    populateUI(verifiedEntry);
                }
            });
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        send_message.setOnClickListener(view -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED)) {
                    ActivityCompat.requestPermissions(VerifiedDetailActivity.this, new String[] { Manifest.permission.SEND_SMS }, 0);
                } else {
                    sendMessage();
                }
            } else {
                sendMessage();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                sendMessage();
            }else{
                Toast.makeText(VerifiedDetailActivity.this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void populateUI(VerifiedEntry verifiedEntry) {
        if (verifiedEntry == null) {
            return;
        }
        address = verifiedEntry.getAddress();
        String image = verifiedEntry.getImage();
        name = verifiedEntry.getName();
        phonenumber = verifiedEntry.getPhonenumber();
        setTitle(name);

        address_value.setText(address);
        name_value.setText(name);
        phone_value.setText(phonenumber);

        Glide.with(getApplicationContext())
                .load(image)
                .into(classifier_image);

    }

    private void sendMessage() {

        sendMySMS(phonenumber, name + " " + R.string.is_found + address);

    }

    public void sendMySMS(String phone, String message) {

        //Check if the phoneNumber is empty
        if (phone.isEmpty()) {
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
}
