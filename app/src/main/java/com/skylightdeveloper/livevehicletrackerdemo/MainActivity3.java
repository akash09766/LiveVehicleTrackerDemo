package com.skylightdeveloper.livevehicletrackerdemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by akash.wangalwar on 27/10/17.
 */

public class MainActivity3 extends AppCompatActivity implements View.OnClickListener{

    private Button mStartBut;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_03);

        mStartBut = (Button) findViewById(R.id.start_but_id);
        mStartBut.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

//        startNavigation();
        startNavigation01();
    }

    private void startNavigation01() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=19.228290,72.977003"));
        startActivity(intent);
    }

    private void startNavigation() {
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?saddr=19.161494,73.001781&daddr=19.228290,72.977003"));
        startActivity(intent);
    }
}
