package com.group7.etmaen;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.group7.etmaen.utils.Constants.PHONE_NUMBER;

public class BaseActivity extends AppCompatActivity{
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }

    public void addDisposable(Disposable disposable){
        this.compositeDisposable.add(disposable);
    }

   public void showWay() {
       /*InfoActivity infoActivity = new InfoActivity();
       String phone_number = infoActivity.PHONE_NUMBER;*/

       String phone = PHONE_NUMBER;
   }
}
