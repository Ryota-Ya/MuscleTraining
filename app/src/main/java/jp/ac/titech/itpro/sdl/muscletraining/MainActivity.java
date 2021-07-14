package jp.ac.titech.itpro.sdl.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();

    private TextView savedCountView;

    private final String fileName = "count.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        savedCountView = findViewById(R.id.saved_count_view);

        Button push_ups_button = findViewById(R.id.push_ups_button);
        push_ups_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Push Ups");
            Intent intent = new Intent(MainActivity.this, PushUpsActivity.class);
            startActivity(intent);
        });

        String str = readFile(fileName);
        if (str != null) {
            savedCountView.setText(str);
        } else {
            savedCountView.setText(R.string.read_error);
        }
    }

    public String readFile(String file) {
        String text = null;

        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                text = lineBuffer ;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}