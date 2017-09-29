package edu.utep.cs.cs4330.noisereducer;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by Eric on 4/29/2017.
 */

public class LoopNoise extends Activity {
    public static final int RequestPermissionCode = 1;
    String audioSavePath = null;

    Button record, play;
    TextView status;
    MediaRecorder mediaRecorder;
    MediaPlayer mediaPlayer;
    Timer HACK_loopTimer;
    LoopMediaPlayer player;
    SoundPool soundpool;
    int soundID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loop);

        record = (Button) findViewById(R.id.record);
        play = (Button) findViewById(R.id.play);
        status = (TextView) findViewById(R.id.status);

        play.setEnabled(false);
        status.setAlpha(0.6f);
        audioSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() +"/"+ "noiseReducer.3gp";

        /** Record Sound */
        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkPermission()){
                    requestPermission();
                }
                else{
                    if(record.getText().toString().equalsIgnoreCase("record")){
                        record.setTextColor(Color.argb(200, 201, 240, 255));
                        record.setText("Stop recording");
                        play.setAlpha(0.5f);
                        play.setEnabled(false);

                        readyMediaRecorder();

                        //Start recording
                        try{
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                        } catch(IllegalStateException e){
                            e.printStackTrace();
                        } catch(IOException e){
                            e.printStackTrace();
                        }


                        //INVERSE SOUND HERE
                        try {
                            phaseShiftAudioFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        mediaRecorder.stop();
                        record.setTextColor(Color.BLACK);
                        record.setText("Record");
                        play.setAlpha(1.0f);
                        play.setEnabled(true);
                        status.setTextColor(Color.BLACK);
                        status.setText("Sound loop saved.");
                    }
                }
            }
        });

        status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        /** Play the recorded file */
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(play.getText().toString().equalsIgnoreCase("play")){
                    play.setText("Stop");
                    record.setAlpha(0.5f);
                    record.setEnabled(false);

                    /*mediaPlayer = new MediaPlayer();
                    mediaPlayer.setLooping(true);
                    try {
                        mediaPlayer.start();
                        mediaPlayer.setDataSource(audioSavePath);
                        mediaPlayer.prepare();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mediaPlayer.start();


                    HACK_loopTimer = new Timer();
                    TimerTask HACK_loopTask = new TimerTask() {
                        @Override public void run() {
                            mediaPlayer.seekTo(0);
                        }
                    };
                    long waitingTime = mediaPlayer.getDuration()-50;
                    HACK_loopTimer.schedule(HACK_loopTask, waitingTime, waitingTime);*/


                    try {
                        player = LoopMediaPlayer.create(audioSavePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

/*
                    soundpool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);

                    soundpool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                            play();
                        }
                    });

                    soundID = soundpool.load(audioSavePath, 1);*/
                }
                else{
                    play.setText("Play");
                    record.setAlpha(1.0f);
                    record.setEnabled(true);

                    player.stop();
                    /*
                    HACK_loopTimer.cancel();
                    if(mediaPlayer != null){
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        readyMediaRecorder();
                    }*/

                    //soundpool.stop(soundID);
                }
            }
        });
    }

    @Override
    protected void onPause(){
        super.onPause();

        if(player != null){
            player.stop();
        }
    }

    public void play(){
        soundpool.play(soundID, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    public void readyMediaRecorder(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioSavePath);
    }

    public void phaseShiftAudioFile() throws IOException {
        FileInputStream fileStream = new FileInputStream(audioSavePath);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] b = new byte[1024];

        for (int readNum; (readNum = fileStream.read(b)) != -1;) {
            byteStream.write(b, 0, readNum);
        }
        byte[] bytes = byteStream.toByteArray();

      /** DEPRECATED */
/*      String mMime = "audio/3gpp";
        MediaCodec  mediaCodec = MediaCodec.createDecoderByType(mMime);

        MediaFormat mMediaFormat = new MediaFormat();
        mMediaFormat = MediaFormat.createAudioFormat(mMime,
                mMediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE),
                mMediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
        mediaCodec.configure(mMediaFormat, null, null, 0);
        mediaCodec.start();

        MediaCodec.BufferInfo buf_info = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(buf_info, 0);
        byte[] pcm = new byte[buf_info.size];
        mOutputBuffers[outputBufferIndex].get(pcm, 0, buf_info.size);*/


        //IMPORTANT! The skip here is to skip the audio file header, and reach the raw actual audio data to modify.
        //Phase Shift
        for(int i = 22; i < bytes.length; i++){
            bytes[i] *= -1;
        }
        Log.d("bytes", Arrays.toString(bytes));

        FileOutputStream fileoutputstream = new FileOutputStream(audioSavePath);
        fileoutputstream.write(bytes);
        fileoutputstream.close();
    }

    public long readUint32(ByteArrayInputStream bis) {
        long result = 0;
        result += ((long) readUInt16(bis)) << 16;
        result += readUInt16(bis);
        return result;
    }
    public int readUInt16(ByteArrayInputStream bis) {
        int result = 0;
        result += bis.read() << 8;
        result += bis.read();
        return result;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(LoopNoise.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        toast("Permission Granted");
                    } else {
                        toast("Pemission Denied");
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),
                WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),
                RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }

    public void toast(String any){
        Toast.makeText(this, any, Toast.LENGTH_SHORT).show();
    }
}
