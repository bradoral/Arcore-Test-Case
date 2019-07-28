package com.greenmagics.ar.Utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class MyMediaPlayer {

    MediaPlayer mp ;

    public MyMediaPlayer(){
        mp = new MediaPlayer();
    }
    public void playAudioFromUrl(String fileUrl){

        try {


            mp = new MediaPlayer();
            mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mp.setDataSource(fileUrl);
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.d("Mohamed_MediaPlayer",fileUrl);
                    mp.start();
                }
            });
            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    stopAudio();
                }
            });
            mp.prepare();
            Log.d("Mohamed_Tag_Mediaplayer","prepared");
        } catch (IOException e) {
            Log.d("Mohamed_Tag_Mediaplayer",e.getCause().toString());
            e.printStackTrace();
        }
        // handle exception


    }

    public void stopAudio(){
       if(mp != null && mp.isPlaying()){
           mp.stop();
           mp.reset();
           mp.release();
       }
    }
}
