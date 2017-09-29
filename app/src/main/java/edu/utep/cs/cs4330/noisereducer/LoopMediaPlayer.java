package edu.utep.cs.cs4330.noisereducer;

import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Eric on 4/29/2017.
 */

public class LoopMediaPlayer {
    private Thread DJ;

    private MediaPlayer playerOne = null;
    private MediaPlayer playerTwo = null;


    public static LoopMediaPlayer create(String path) throws IOException {
        return new LoopMediaPlayer(path);
    }

    private LoopMediaPlayer(String path) throws IOException {
        playerOne = new MediaPlayer();
        playerOne.setDataSource(path);
        playerOne.prepare();

        playerTwo = new MediaPlayer();
        playerTwo.setDataSource(path);
        playerTwo.prepare();

        playerOne.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                playerOne.start();
            }
        });

        startDJ();
    }

    public void startDJ(){
        DJ = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    if(playerOne.isPlaying() && playerOne.getDuration()-playerOne.getCurrentPosition() < 550){
                        playerTwo.start();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        playerOne.stop();
                    }
                    else if(playerTwo.isPlaying() && playerTwo.getDuration()-playerTwo.getCurrentPosition() < 550){
                        playerOne.start();
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        playerTwo.stop();
                    }
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        DJ.start();
    }



    public void stop() throws IllegalStateException {
        playerOne.stop();
        playerTwo.stop();
        DJ.interrupt();
    }
}