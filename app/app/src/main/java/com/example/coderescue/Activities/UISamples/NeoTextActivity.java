package com.example.coderescue.Activities.UISamples;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NeoTextActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);
        tv.setText("Demo UI - NeoText");
        tv.setPadding(32, 32, 32, 32);
        setContentView(tv);
    }
}
