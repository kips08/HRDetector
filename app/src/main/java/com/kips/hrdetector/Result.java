package com.kips.hrdetector;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.kips.hrdetector.helper.DatabaseHelper;
import com.kips.hrdetector.model.AvgHR;

import java.util.List;

public class Result extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        DatabaseHelper databaseHandler = new DatabaseHelper(this);

        Bundle bundle = getIntent().getExtras();
        int avgbpm = bundle.getInt("avgbpm");
        String stresslv = bundle.getString("stresslv");
        int[] bpmdata = bundle.getIntArray("bpmdata");
        String created_at = "";
//        Log.d("DBC", "AVGBPM : "+ avgbpm+" Stress Lv : "+stresslv);

//        Log.d("DBC", "inserting data");
        databaseHandler.saveAvg(new AvgHR(avgbpm, stresslv, created_at));
        Log.d("DBC", "VIEWING data");
        List<AvgHR> listavg = databaseHandler.findAll();
        for (AvgHR AH : listavg
        ) {
            Log.d("DBC", "A : " + AH.getId() + ", B : " + AH.getCreated_at());
        }
        for (int i = 0; i < 3; i++) {
            int x = i + 1;
            Log.d("DATABASECHECK", "Data bpm ke-" + x + " = " + bpmdata[i]);
        }

        setData();
    }

    void setData() {
        Bundle bundle = getIntent().getExtras();

        TextView tvAvgBpm = findViewById(R.id.tvAvgBpm);
        TextView tvStressLevel = findViewById(R.id.tvStressLevel);
        TextView tvData = findViewById(R.id.tvData);

        tvAvgBpm.setText(String.valueOf(bundle.getInt("avgbpm", 0)));
        tvStressLevel.setText(bundle.getString("stresslv"));

        int[] bpmdata = bundle.getIntArray("bpmdata");
        String dataList = "";
        for (int i = 0; i < bpmdata.length; i++) {
            dataList += bpmdata[i] + "\n";
        }
        tvData.setText(dataList);

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(Result.this, MainActivity.class));
        super.onBackPressed();
    }
}
