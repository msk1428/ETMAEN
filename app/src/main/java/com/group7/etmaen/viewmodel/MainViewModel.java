package com.group7.etmaen.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.group7.etmaen.database.AddEntry;
import com.group7.etmaen.database.AppDatabase;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    // Constant for logging
    private static final String TAG = MainViewModel.class.getSimpleName();

    private LiveData<List<AddEntry>> entries;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        Log.d(TAG, "Actively retrieving the tasks from the DataBase");
        entries = database.imageClassifierDao().loadAllClassifier();
    }

    public LiveData<List<AddEntry>> getTasks() {
        return entries;
    }
}
