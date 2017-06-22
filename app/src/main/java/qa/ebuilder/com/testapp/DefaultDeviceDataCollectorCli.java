package qa.ebuilder.com.testapp;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import io.ebuilder.mobile.services.DeviceDataCollectorClient;
import io.ebuilder.mobile.services.logging.Logger;
import io.ebuilder.mobile.services.modules.DecoratedDataProviderModule;
import io.ebuilder.mobile.services.modules.ModulesFactory;
import io.ebuilder.mobile.services.provider.DataContent;
import io.ebuilder.mobile.services.settings.SettingsProvider;

/**
 * Created by viar1 on 2017-06-21.
 */

public class DefaultDeviceDataCollectorCli implements DeviceDataCollectorClient {
    private SettingsProvider settingsProvider;

    DefaultDeviceDataCollectorCli(final SettingsProvider settingsProvider) {
        this.settingsProvider = settingsProvider;
    }

    @Override
    public void collectAndStore(final Context context) {
        final Map<String, Map<String, ? super Object>> result = new HashMap<>();
        final Long startTime = System.currentTimeMillis();
        Logger.w("DefaultDeviceDataCollectorClient::collectAndStore -> start collecting data");
        for (final String moduleName : ModulesFactory.getModuleNames()) {
            final DecoratedDataProviderModule module = ModulesFactory
                    .requestForInstance(moduleName, settingsProvider);
            Map<String, ? super Object> values = result.get(moduleName);
            if (values == null) {
                values = new HashMap<>();
                result.put(moduleName, values);
            }
            values.putAll(module.collectData(context));
        }
        DataContent.insert(context, result);
        final Long finishedTime = System.currentTimeMillis();
        Logger.w("DefaultDeviceDataCollectorClient::collectAndStore -> finished collecting data, took: "
                + (finishedTime - startTime) + "ms");
    }
}
