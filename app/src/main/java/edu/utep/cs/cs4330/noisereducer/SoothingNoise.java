package edu.utep.cs.cs4330.noisereducer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by Eric on 4/30/2017.
 */

public class SoothingNoise extends Activity {
    Spinner spinner;
    Button play;

    MediaPlayer player;
    ArrayList<String> sounds;
    ArrayList<Integer> RID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soothing);

        spinner = (Spinner) findViewById(R.id.spinner);
        play = (Button) findViewById(R.id.play);

        Field[] fields = R.raw.class.getFields();
        sounds = new ArrayList<>();
        RID = new ArrayList<>();
        for(int i = 0; i < fields.length; i++){
            String toAdd = fields[i].getName();

            if(toAdd.contains("soothing")){
                toAdd = toAdd.substring(9);
                toAdd = toAdd.replace('_', ' ');
                sounds.add(toAdd);
                try {RID.add(fields[i].getInt(fields[i]));} catch (IllegalAccessException e) {}
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(SoothingNoise.this, R.layout.spinner_item, sounds);
        spinner.setAdapter(adapter);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(play.getText().toString().equalsIgnoreCase("play")) {
                    play.setText("Stop");
                    play.setTextColor(Color.argb(200, 201, 240, 255));

                    int ID = RID.get(spinner.getSelectedItemPosition());

                    player = MediaPlayer.create(SoothingNoise.this, ID);
                    player.setLooping(true);
                    player.start();
                }
                else{
                    play.setText("Play");
                    play.setTextColor(Color.BLACK);

                    player.stop();
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(player.isPlaying()){
            player.stop();
            player.release();
        }
    }

    public void changeSpinnerPopupHeight(){
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);

            android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinner);

            popupWindow.setHeight(getPixelDensity(SoothingNoise.this, 400));
        }
        catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
        }
    }

    public int getPixelDensity(Context ctx, float dps) {
        float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }
}
