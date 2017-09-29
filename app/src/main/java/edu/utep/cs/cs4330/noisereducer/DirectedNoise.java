package edu.utep.cs.cs4330.noisereducer;

import android.app.Activity;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Modified by Eric on 4/30/2017, from source: http://stackoverflow.com/questions/9413998/live-audio-recording-and-playing-in-android-and-thread-callback-handling
 */

public class DirectedNoise extends Activity {
    private String LOG_TAG = null;

    private Button start;
    private TextView status;

    private AudioRecord recorder = null;
    private AudioTrack player = null;
    private AudioManager audioManager = null;
    private int recorderBufSize, recordingSampleRate;
    private int trackBufSize;
    private short[] audioData;
    private boolean isRecording = false;
    private Thread startRecThread;

    private AudioRecord.OnRecordPositionUpdateListener posUpdateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directed);
        LOG_TAG = "onCreate";

        LOG_TAG = "Constructor";
        recorderBufSize = recordingSampleRate = trackBufSize = 0;

        // init function will initialize all the necessary variables ...
        init();

        if (recorder != null && player != null) {
            Log.e(LOG_TAG, "recorder and player initialized");
            audioData = new short[recorderBufSize / 2]; // since we are reading shorts

        } else {
            Log.e(LOG_TAG, "Problem inside init function ");
        }
        posUpdateListener = new AudioRecord.OnRecordPositionUpdateListener() {
            int numShortsRead = 0;

            @Override
            public void onPeriodicNotification(AudioRecord rec) {
//              String LOG_TAG = Thread.currentThread().getName();
//               Log.e(LOG_TAG, "inside position listener");
                audioData = new short[recorderBufSize / 2]; // divide by 2 since now we are reading shorts
                numShortsRead = rec.read(audioData, 0, audioData.length);
                player.write(audioData, 0, numShortsRead);

            }

            @Override
            public void onMarkerReached(AudioRecord recorder) {
                Log.e(LOG_TAG, "Marker Reached");
            }
        };
        // listener will be called every time 160 frames are reached
        recorder.setPositionNotificationPeriod(160);
        recorder.setRecordPositionUpdateListener(posUpdateListener);

        Log.e(LOG_TAG, "inside constructor");

        // getting the audio service
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        // creating Buttons one by one . . . .
        // button to start the recording process
        start = (Button) findViewById(R.id.start);
        status = (TextView) findViewById(R.id.status);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (start.getText().toString().equalsIgnoreCase("start")) {
                    start.setText("Stop");
                    start.setTextColor(Color.argb(200, 201, 240, 255));
                    status.setText("Running . . .");

                    isRecording = true;

                    startRecording();
                } else {
                    start.setText("Start");
                    start.setTextColor(Color.BLACK);
                    status.setText("Not running");

                    stop();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        // Clean up code . ..
        super.onDestroy();
        if (recorder != null)
            recorder.release();
        if (startRecThread != null && startRecThread.isAlive())
            startRecThread.destroy();
        if (recorder != null)
            recorder.release();
        if (player != null)
            player.release();
        startRecThread = null;
        recorder = null;
        player = null;
        start = null;
        status = null;
        audioData = null;
        System.gc();
    }

    private void init() {
        LOG_TAG = "initFunc";
        // int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
        short audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // for (int rate : mSampleRates) {
        this.recordingSampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
        try {
            // Log.d(LOG_TAG, "Attempting rate " + rate + "Hz, bits: " +
            // audioFormat);
            int bufrSize = AudioRecord.getMinBufferSize(this.recordingSampleRate,
                    AudioFormat.CHANNEL_IN_MONO, audioFormat);

            // lets find out the minimum required size for AudioTrack
            int audioTrackBufSize = AudioTrack.getMinBufferSize(this.recordingSampleRate,
                    AudioFormat.CHANNEL_OUT_MONO, audioFormat);

            if (bufrSize != AudioRecord.ERROR_BAD_VALUE
                    && bufrSize != AudioRecord.ERROR) {
                // check if we can instantiate and have a success
                if (audioTrackBufSize >= bufrSize) {
                    this.recorderBufSize = audioTrackBufSize;
                } else {
                    this.recorderBufSize = bufrSize;
                }

                AudioRecord rec = new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT, this.recordingSampleRate,
                        AudioFormat.CHANNEL_IN_MONO, audioFormat, this.recorderBufSize);

                if (rec != null && rec.getState() == AudioRecord.STATE_INITIALIZED) {

                    // storing variables for future use . . .
//                  this.recordingSampleRate = rate;
//                  this.recorderBufSize = bufrSize;

                    Log.e(LOG_TAG,
                            "Returning..(rate:channelConfig:audioFormat:recorderBufSize)"
                                    + this.recordingSampleRate + ":" + AudioFormat.CHANNEL_IN_MONO
                                    + ":" + audioFormat + ":" + this.recorderBufSize);

                    // Now create an instance of the AudioTrack
//                  int audioTrackBufSize = AudioTrack.getMinBufferSize(rate,
//                          AudioFormat.CHANNEL_OUT_MONO, audioFormat);

                    Log.e(LOG_TAG, "Audio Record / Track / Final buf size :" + bufrSize + "/ " + audioTrackBufSize + "/ " + this.recorderBufSize);


                    this.player = new AudioTrack(AudioManager.STREAM_MUSIC,
                            this.recordingSampleRate, AudioFormat.CHANNEL_OUT_MONO, audioFormat,
                            this.recorderBufSize, AudioTrack.MODE_STREAM);

                    this.recorder = rec;
                    this.player.stop();
                    this.player.flush();
                    this.player.setPlaybackRate(this.recordingSampleRate);
                    return;
                }
            }
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, this.recordingSampleRate + "Exception, keep trying.", e);
        } catch (Exception e) {
            Log.e(LOG_TAG, this.recordingSampleRate + "Some Exception!!", e);
        }
        // for loop for channel config ended here. . . .
        // for loop for audioFormat ended here. . .
        // }// for loop for sampleRate
        return;
    }

    private void startRecording() {
        LOG_TAG = "startRecording";

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager.setSpeakerphoneOn(false);
        player.flush();
        player.play();

        /* start a separate recording thread from here . . . */
        startRecThread = new Thread(new Runnable() {
            @Override
            public void run() {
//                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
//              String LOG_TAG = Thread.currentThread().getName();
                if (recorder.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.startRecording();
                }
//              Log.e(LOG_TAG, "running" +recorder.getRecordingState());
                while (recorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
                    recorder.read(audioData, 0, audioData.length);
                    try {

                        Thread.sleep(3000); // sleep for 1s
                    } catch (InterruptedException e) {
                        Log.e("run Method", "recorder thread is interrupted");
                    }
                }
            }
        });

        startRecThread.start();
        if (!startRecThread.isAlive())
            notifyAll();

        Log.e(LOG_TAG, "start Recording");
    }

    private void stopRecording() {
        LOG_TAG = "stopRecording";
        recorder.stop();

        if (startRecThread != null && startRecThread.isAlive()) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            startRecThread = null;
        }

        player.stop();
        player.flush();
        Log.e(LOG_TAG, "stop Recording");
    }

    private void stop() {
        if (isRecording) {
            isRecording = false;
            stopRecording();
        }
    }
}
