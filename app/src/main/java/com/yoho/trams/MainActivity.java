package com.yoho.trams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends Activity {

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mapsAct = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapsAct);
            }
        }, 2000);

    }

}
