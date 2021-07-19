package jp.ac.titech.itpro.sdl.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrainingActivity extends AppCompatActivity  implements SensorEventListener, Runnable {
    private final static String TAG = TrainingActivity.class.getSimpleName();
    private final static long COUNTING_JUDGEMENT_PERIOD_MS = 50;
    private final static long TIME_THRESHOLD = 200;
    private final static float ACCELERATION_THRESHOLD = 0.5f;

//    private TextView accelerationView;
//    private TextView infoView;
//    private TextView gravityView;
    private TextView trainingNameView;
    private TextView countView;

    private final String fileName = "count.txt";

    private SensorManager manager;
    private Sensor acceleration_sensor;
    private Sensor gravity_sensor;

    private SoundPlayer soundPlayer;

    private final Handler handler = new Handler();
    private final Timer timer = new Timer();

    private int training_id;

    private final static float alpha = 0.75f;
    private float ax, ay, az;
    private float gx, gy, gz;
    private long msStartUp;
    private long msStartDown;
    private int count = 0;
    private boolean isGoingUp = false; //上がっている状態
    private boolean isGoingDown = false; //下がっている状態
    private boolean isDown; //下がりきった状態

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training);
        Log.d(TAG, "onCreate");

        Intent acceptedIntent = getIntent();
        training_id = acceptedIntent.getIntExtra("training_id", MainActivity.PUSH_UPS);

//        accelerationView = findViewById(R.id.acceleration_view);
//        infoView = findViewById(R.id.info_view);
//        gravityView = findViewById(R.id.gravity_view);
        trainingNameView = findViewById(R.id.training_name_view);
        countView = findViewById(R.id.count_view);

        Button finish_button = findViewById(R.id.finish_button);
        finish_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - finish");
            Calendar cal = Calendar.getInstance();
            String date = (cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH));
            //String savedStr = date + "," + count;
            saveFile(fileName, date, count);
            Intent intent = new Intent(TrainingActivity.this, MainActivity.class);
            startActivity(intent);
        });

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
        gravity_sensor = manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        if (gravity_sensor == null) {
            Toast.makeText(this, R.string.toast_no_gravity_sensor, Toast.LENGTH_LONG).show();
        }

        soundPlayer = new SoundPlayer(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        manager.registerListener(this, acceleration_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        manager.registerListener(this, gravity_sensor, SensorManager.SENSOR_DELAY_NORMAL);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(TrainingActivity.this);
            }
        }, 0, COUNTING_JUDGEMENT_PERIOD_MS);
    }

    @Override
    public void run() {
        long msNow = System.currentTimeMillis();

        //加速度aの大きさ
        float acceleration_magnitude = (float)Math.sqrt(Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2));
        //加速度aベクトルと重力gベクトルのなす角の余弦
        float cos = (ax*gx + ay*gy + az*gz) /
                (float)Math.sqrt( (Math.pow(ax, 2) + Math.pow(ay, 2) + Math.pow(az, 2)) * (Math.pow(gx, 2) + Math.pow(gy, 2) + Math.pow(gz, 2)) );
        //加速度aの重力方向における大きさ
        float acceleration_in_gravity_direction = acceleration_magnitude * cos;

        if (acceleration_in_gravity_direction < -ACCELERATION_THRESHOLD) {
            if (!isGoingDown) {
                msStartDown = System.currentTimeMillis();
                isGoingDown = true;
                isGoingUp = false;
            }
            if (msNow - msStartDown > TIME_THRESHOLD) {
                isDown = true;
            }
        }

        if (acceleration_in_gravity_direction > ACCELERATION_THRESHOLD) {
            if (!isGoingUp) {
                msStartUp = System.currentTimeMillis();
                isGoingDown = false;
                isGoingUp = true;
            }
            if (isDown && msNow - msStartUp > TIME_THRESHOLD) {
                count++;
                if(count % 10 == 0)
                    soundPlayer.playCount10();
                else
                    soundPlayer.playCount();
                isDown = false;
            }
        }

        switch(training_id){
            case MainActivity.PUSH_UPS:
                trainingNameView.setText(R.string.push_ups);
                break;
            case MainActivity.ABS:
                trainingNameView.setText(R.string.abs);
                break;
            case MainActivity.SQUAT:
                trainingNameView.setText(R.string.squat);
                break;
        }

        countView.setText(String.valueOf(count));
//        accelerationView.setText(getString(R.string.acceleration_format, acceleration_in_gravity_direction));
//        infoView.setText(getString(R.string.info_format, isGoingDown, isGoingUp, isDown, count));
//        gravityView.setText(getString(R.string.gravity_format, gx, gy, gz));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        manager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor= event.sensor;
        if(sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            ax = alpha * ax + (1 - alpha) * event.values[0];
            ay = alpha * ay + (1 - alpha) * event.values[1];
            az = alpha * az + (1 - alpha) * event.values[2];
            Log.i(TAG, "ax=" + ax + ", ay=" + ay + ", az=" + az);
        }
        if(sensor.getType() == Sensor.TYPE_GRAVITY){
            gx = event.values[0];
            gy = event.values[1];
            gz = event.values[2];
            Log.i(TAG, "gx=" + ax + ", gy=" + ay + ", gz=" + az);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: accuracy=" + accuracy);
    }

    public void saveFile(String file, String date, int count) {
        List<String> lineList = new ArrayList<>();
        String line = null;
        boolean existsDate = false;

        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                line = lineBuffer ;
                String[] split = line.split(",", 0);
                if(split[MainActivity.DATE_INDEX].equals(date)) {
                    split[training_id] = String.valueOf(Integer.parseInt(split[training_id]) + count);
                    line = split[MainActivity.DATE_INDEX] + "," + split[MainActivity.PUSH_UPS] + "," + split[MainActivity.ABS] + "," + split[MainActivity.SQUAT];
                    existsDate = true;
                }
                lineList.add(line);
            }
            if(!existsDate) {
                String[] tmp = {date, "0", "0", "0"};
                tmp[training_id] = String.valueOf(count);
                lineList.add(tmp[MainActivity.DATE_INDEX] + "," + tmp[MainActivity.PUSH_UPS] + "," + tmp[MainActivity.ABS] + "," + tmp[MainActivity.SQUAT]);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        deleteFile(file);

        try (FileOutputStream fileOutputstream = openFileOutput(file, Context.MODE_PRIVATE|Context.MODE_APPEND)){
            for(int i = 0; i < lineList.size(); ++i) {
                fileOutputstream.write(lineList.get(i).getBytes());
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}