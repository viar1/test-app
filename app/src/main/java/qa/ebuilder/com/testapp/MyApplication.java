package qa.ebuilder.com.testapp;

import android.app.Application;


import com.facebook.stetho.Stetho;

import io.ebuilder.mobile.services.DeviceDataCollectorFactory;

/**
 * Created by viar1 on 2017-06-20.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        DeviceDataCollectorFactory.init(this);

    }
}
