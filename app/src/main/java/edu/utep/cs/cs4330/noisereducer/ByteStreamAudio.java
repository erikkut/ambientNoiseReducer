package edu.utep.cs.cs4330.noisereducer;


import android.app.Activity;
import android.graphics.Color;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Eric on 5/1/2017.
 */

public class ByteStreamAudio extends Activity {
    Button record;
    Button play;
    TextView status;

    MediaRecorder myRecorder;
    LoopMediaPlayer myPlayer;

    String directory;
    String encoded;
    File file ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loop);

        record = (Button) findViewById(R.id.record);
        play = (Button) findViewById(R.id.play);
        status = (TextView) findViewById(R.id.status);

        directory =  Environment.getExternalStorageDirectory().getAbsolutePath();

        myRecorder = new MediaRecorder();
        myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myRecorder.setOutputFile(directory +"/"+ "noiseReducer.3gp");

        try {
            myPlayer = LoopMediaPlayer.create(directory +"/"+ "phasedShifted.3gp");
        } catch (IOException e) {
            e.printStackTrace();
        }

        play.setEnabled(false);
        status.setAlpha(0.6f);

        record.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(record.getText().toString().equalsIgnoreCase("record")){
                    record.setTextColor(Color.argb(200, 201, 240, 255));
                    record.setText("Stop recording");
                    play.setAlpha(0.5f);
                    play.setEnabled(false);

                    //start recording
                    start();

                }
                else{
                    record.setTextColor(Color.BLACK);
                    record.setText("Record");
                    play.setAlpha(1.0f);
                    play.setEnabled(true);
                    status.setTextColor(Color.BLACK);
                    status.setText("Sound loop saved.");

                    //stop recording
                    try {
                        stop();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        play.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(play.getText().toString().equalsIgnoreCase("play")){
                    play.setText("Stop");
                    record.setAlpha(0.5f);
                    record.setEnabled(false);

                    //start playing
                    play();
                }
                else{
                    play.setText("Play");
                    record.setAlpha(1.0f);
                    record.setEnabled(true);

                    //stop playing
                    stopPlay();
                }
            }
        });
    }

    public void start(){
        try {
            myRecorder.prepare();
            myRecorder.start();
        } catch (IllegalStateException e) {
            // start:it is called before prepare()
            // prepare: it is called after start() or before setOutputFormat()
            e.printStackTrace();
        } catch (IOException e) {
            // prepare() fails
            e.printStackTrace();
        }
    }

    public void stop() throws IOException{

        try {
            myRecorder.stop();
//            myRecorder.release();
//            myRecorder  = null;
            // file = new File(Environment.getExternalStorageDirectory() + "/noiseReducer.3gp");


            FileInputStream in = new FileInputStream(file = new File(directory +"/"+ "noiseReducer.3gp"));
            byte fileContent[] = new byte[(int)file.length()];

            in.read(fileContent,0,fileContent.length);

            encoded = Base64.encodeToString(fileContent,0);
            //  Utilities.log("~~~~~~~~ Encoded: ", encoded);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void play() {
        try{
            FileOutputStream out = new FileOutputStream(file = new File(directory +"/"+ "phaseShifted.3gp"));
            byte[] decoded = Base64.decode(encoded, 0);

            out.write(decoded);
            out.close();

            myPlayer = LoopMediaPlayer.create(directory +"/" + "phaseShifted.3gp");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopPlay(){
        myPlayer.stop();
    }
}
