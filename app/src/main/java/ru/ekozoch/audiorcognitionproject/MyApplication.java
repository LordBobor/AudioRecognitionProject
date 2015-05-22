package ru.ekozoch.audiorcognitionproject;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

import ru.ekozoch.audiorcognitionproject.parseClasses.DataPoint;
import ru.ekozoch.audiorcognitionproject.parseClasses.Hash;
import ru.ekozoch.audiorcognitionproject.parseClasses.Music;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);

        // Регистрация субклассов обязательно до вызова Parse.initialize()!
        ParseObject.registerSubclass(Music.class);
        ParseObject.registerSubclass(DataPoint.class);
        ParseObject.registerSubclass(Hash.class);

        Parse.initialize(this, "ii0l3JpEagg7uGTSukzz1LZCzT5vekeedLiLU1Bx", "Fb7MAekXbOo5Mn0Zl2k6KLVMIZDPzfpcIpwkscJT");

    }
}
