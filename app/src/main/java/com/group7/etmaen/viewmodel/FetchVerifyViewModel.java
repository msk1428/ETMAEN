package com.group7.etmaen.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.group7.etmaen.database.AppDatabase;
import com.group7.etmaen.database.VerifiedEntry;

public class FetchVerifyViewModel extends ViewModel {
    private LiveData<VerifiedEntry> verify;

    public FetchVerifyViewModel(AppDatabase database, int id) {
        verify = database.imageClassifierDao().loadVerifiedImageById(id);
    }

    public LiveData<VerifiedEntry> getVerify() {
        return verify;
    }
}
