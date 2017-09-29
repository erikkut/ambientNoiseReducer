package edu.utep.cs.cs4330.noisereducer;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
    Button loop;
    Button directed;
    Button soothing;

//    this.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        final ActivityOptions opt = ActivityOptions.makeCustomAnimation(MainActivity.this, android.R.anim.fade_in, android.R.anim.fade_out);

        loop = (Button) findViewById(R.id.loop);
        directed = (Button) findViewById(R.id.directed);
        soothing = (Button) findViewById(R.id.soothing);

        loop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, LoopNoise.class);
//                Intent i = new Intent(MainActivity.this, ByteStreamAudio.class);
                startActivity(i, opt.toBundle());
            }
        });

        directed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, DirectedNoise.class);
                startActivity(i, opt.toBundle());
            }
        });

        soothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SoothingNoise.class);
                startActivity(i, opt.toBundle());
            }
        });
    }
}
