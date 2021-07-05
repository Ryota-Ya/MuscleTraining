package jp.ac.titech.itpro.sdl.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button push_ups_button = findViewById(R.id.push_ups_button);
        push_ups_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Push Ups");
            Intent intent = new Intent(MainActivity.this, PushUpsActivity.class);
            startActivity(intent);
        });
    }
}