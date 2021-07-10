package jp.ac.titech.itpro.sdl.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class PushUpsActivity extends AppCompatActivity  implements SensorEventListener, Runnable {
    private final static String TAG = PushUpsActivity.class.getSimpleName();
    private final static long GRAPH_REFRESH_PERIOD_MS = 20;
    private final static long TIME_THRESHOLD = 200;
    private final static float ACCELERATION_THRESHOLD = 0.5f;

    private TextView infoView;

    private SensorManager manager;
    private Sensor acceleration_sensor;

    private final Handler handler = new Handler();
    private final Timer timer = new Timer();

    private final static float alpha = 0.75f;
    private float ax, ay, az;
    private long msStartUp;
    private long msStartDown;
    private int count = 0;
    private boolean isGoingUp = false; //上がっている状態
    private boolean isGoingDown = false; //下がっている状態
    private boolean isDown; //下がりきった状態

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_ups);
        Log.d(TAG, "onCreate");

        infoView = findViewById(R.id.info_view);

        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (manager == null) {
            Toast.makeText(this, R.string.toast_no_sensor_manager, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        acceleration_sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        if (acceleration_sensor == null) {
            Toast.makeText(this, R.string.toast_no_acceleration_sensor, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        manager.registerListener(this, acceleration_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(PushUpsActivity.this);
            }
        }, 0, GRAPH_REFRESH_PERIOD_MS);
    }

    @Override
    public void run() {
        infoView.setText(getString(R.string.info_format, az, isGoingDown, isGoingUp, isDown, count));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        ax = alpha * ax + (1 - alpha) * event.values[0];
        ay = alpha * ay + (1 - alpha) * event.values[1];
        az = alpha * az + (1 - alpha) * event.values[2];
        Log.i(TAG, "x=" + ax + ", y=" + ay + ", z=" + az);

//        isUp = false;
//        isDown = false;

        long msNow = System.currentTimeMillis();

        if(az < -ACCELERATION_THRESHOLD){
            if(!isGoingDown) {
                msStartDown = System.currentTimeMillis();
                isGoingDown = true;
                isGoingUp = false;
            }
            if(msNow - msStartDown > TIME_THRESHOLD) {
                isDown = true;
            }
        }

        if(az > ACCELERATION_THRESHOLD){
            if(!isGoingUp) {
                msStartUp = System.currentTimeMillis();
                isGoingDown = false;
                isGoingUp = true;
            }
            if(isDown && msNow - msStartUp > TIME_THRESHOLD) {
                count++;
                isDown = false;
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: accuracy=" + accuracy);
    }
}