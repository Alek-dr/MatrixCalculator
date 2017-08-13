package com.barmin.matrixcalculator;

import android.app.Application;

import com.barmin.matrixcalculator.Storage;

/**
 * Created by alexander on 13.08.17.
 */

public class App extends Application {

    @Override
    public void onCreate(){
        super.onCreate();
        Storage.getInstance();
    }
}
