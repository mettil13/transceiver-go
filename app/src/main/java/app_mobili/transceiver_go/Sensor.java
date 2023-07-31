package app_mobili.transceiver_go;

import android.content.Context;

public class Sensor {
    private static Context context;
    protected float SensorValue;

    public Sensor () {
        SensorValue = 0;
        this.setContext(getContext());
    }

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        Sensor.context = context;
    }
}
