package qa.ebuilder.com.testapp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import io.ebuilder.mobile.services.DeviceDataCollectorClient;
import io.ebuilder.mobile.services.DeviceDataCollectorFactory;
import io.ebuilder.mobile.services.Version;
import io.ebuilder.mobile.services.license.DefaultLicenseClient;
import io.ebuilder.mobile.services.license.LicenseAwareClient;
import io.ebuilder.mobile.services.license.LicenseData;
import io.ebuilder.mobile.services.license.LicenseSettings;
import io.ebuilder.mobile.services.logging.Logger;
import io.ebuilder.mobile.services.provider.SettingsContent;
import io.ebuilder.mobile.services.scheduler.Scheduler;
import io.ebuilder.mobile.services.settings.SettingsBuilder;
import io.ebuilder.mobile.services.settings.SettingsProvider;

/**
 * Created by viar1 on 2017-06-21.
 */

public class DeviceDataCollectorFact {
    private static final String LOG_TAG = "ddc";

    private static DefaultLicenseClient licenseClient;

    private static final String TAG = DeviceDataCollectorFactory.class.getSimpleName();

    private static ExecutorService executorService = Executors.newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull final Runnable r) {
            final Thread thread = new Thread(r);
            thread.setName(DeviceDataCollectorFactory.class.getSimpleName()
                    + "-" + UUID.randomUUID().toString());
            return thread;
        }
    });

    static {
        Log.i(TAG, "DDC Version: " + Version.current().toString());
    }

    public static void init(final Context context) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try{
                final SettingsProvider settingsProvider = new SettingsContent();
                if (settingsProvider.isPresent(context)) {
                    Logger.setup(context, LOG_TAG, settingsProvider);
                    System.out.println("DDC SDK Init");
                    getLicenseClient().awareOfLicense(context, getLicenseSettings(),
                            new LicenseAwareClient.OnLicenseValidWithResult<Void>() {
                                @Override
                                public Void handleValidLicense(final LicenseData licenseData) {
                                    System.out.println("DDC SDK handleValidLicense");
                                    Scheduler.reschedule(context, settingsProvider, licenseData);
                                    return null;
                                }
                            }, new LicenseAwareClient.OnLicenseInvalidWithResult<Void>() {
                                @Override
                                public Void handleInvalidLicense() {
                                    System.out.println("DDC SDK handleValidLicense Cancel");
                                    Scheduler.cancel(context);
                                    return null;
                                }
                            });
                }}catch(Exception ex){
                    System.out.println("DDC SDK error" + ex.toString());
                }
            }
        });
    }

    public static SettingsBuilder setup(final Context context,
                                        final SettingsBuilder.Environment environment,
                                        final String imei, final String systemId) {
        return new SettingsContent().of(environment, imei, systemId,
                new SettingsContent.SettingsReadyListener() {
                    @Override
                    public void onSettingsAreReady(final SettingsProvider settingsProvider) {
                        init(context);
                    }
                });
    }

    private static DefaultLicenseClient getLicenseClient() {
        synchronized (DeviceDataCollectorFactory.class) {
            if (licenseClient == null) {
                final SettingsProvider settingsProvider = new SettingsContent();
                licenseClient = new DefaultLicenseClient(settingsProvider) {
                    @Override
                    public DeviceDataCollectorClient newClientInstance() {
                        return new DefaultDeviceDataCollectorCli(settingsProvider);
                    }
                };
            }
        }
        return licenseClient;
    }

    /* package protected */
    static DeviceDataCollectorClient retrieve(final Context context) {
        return getLicenseClient().retrieve(context);
    }

    /* package protected */
    static LicenseAwareClient<String> getLicenseAwareClient() {
        return getLicenseClient();
    }

    /* package protected */
    static LicenseSettings<String> getLicenseSettings() {
        return getLicenseClient();
    }
}
