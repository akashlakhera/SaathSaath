package com.example.android.saathsaath;

import android.content.Intent;
import android.icu.lang.UProperty;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> issues;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        issues = new ArrayList<>();
        issues.add("Harrasmant");
        issues.add("Clealiness");
        startActivity(new Intent(MainActivity.this, UploadActivity.class));

    }
}
