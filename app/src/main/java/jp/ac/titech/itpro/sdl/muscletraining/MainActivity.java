package jp.ac.titech.itpro.sdl.muscletraining;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = MainActivity.class.getSimpleName();
    private final static int DATE_INDEX = 0;
    private final static int PUSH_UPS = 1;
    private final static int ABS = 2;
    private final static int Squat = 3;

    private TextView savedCountView;
    private CalendarView calendarView;

    private final String fileName = "count.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        deleteFile(fileName);

        setMainView();
    }

    protected void setMainView(){
        setContentView(R.layout.activity_main);

        Button push_ups_button = findViewById(R.id.push_ups_button);
        push_ups_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Push Ups");
            Intent intent = new Intent(MainActivity.this, TrainingActivity.class);
            startActivity(intent);
        });

        Button record_button = findViewById(R.id.record_button);
        record_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - Record");
            setRecordView();
        });
    }

    protected void setRecordView(){
        setContentView(R.layout.activity_main_record);

        savedCountView = findViewById(R.id.saved_count_view);

        calendarView = findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = year + "-" + month + "-" + dayOfMonth;
            String str = readLineFromFile(fileName, date);
            if (str != null) {
                savedCountView.setText(str);
            } else {
                savedCountView.setText(getString(R.string.no_data, date));
            }
        });

        Button close_button = findViewById(R.id.close_button);
        close_button.setOnClickListener(v -> {
            Log.d(TAG, "onClick - close");
            setMainView();
        });
    }

    public String readLineFromFile(String file, String date) {
        String line = null;
        String targetLine = null;

        try (FileInputStream fileInputStream = openFileInput(file);
             BufferedReader reader= new BufferedReader(new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))) {
            String lineBuffer;
            while( (lineBuffer = reader.readLine()) != null ) {
                line = lineBuffer ;
                if(line.split(",", 0)[DATE_INDEX].equals(date))
                    targetLine = line;
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return targetLine;
    }
}

