package com.greenmagics.ar.Utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;


import java.io.File;

import java.io.IOException;
import java.util.Random;

import rm.com.audiowave.AudioWaveView;


public class CustomRecorder implements CommunicateWithRecorderInterface {

    public static final int WRITE_REQUEST_CODE = 777 ;
    public static final  int RECORDING_REQUEST_CODE = 666 ;
    boolean isRecording ;
    private  Context ctx ;
    AudioWaveView waveView ;
    FireBaseConnectHelperClass fireBaseConnectHelperClass ;
    MediaRecorder myAudioRecorder ;

    File outputFile ;
    Handler handler ;
    Runnable updater ;
    LinearLayout visualizerContainer ;
    CountDownTimer timer ;
    int count = 0 ;
    ChangeRecordStatus changeRecordStatus ;

    public  CustomRecorder(Context ctx, AudioWaveView waveView , FireBaseConnectHelperClass fireBaseConnectHelperClass, LinearLayout visualizerContainer){
        this.ctx = ctx ;
        changeRecordStatus = (ChangeRecordStatus)ctx ;
        this.waveView = waveView ;
        this.fireBaseConnectHelperClass = fireBaseConnectHelperClass ;
        this.visualizerContainer = visualizerContainer ;

    }

    public void startRecording() {



        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MainArFolder");
        if(!dir.exists()){
            dir.mkdirs();
        }

         outputFile = new File(dir+"/myRecordedfile"+".3gp");

        if(!outputFile.exists()){
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(outputFile.getAbsolutePath());
        try {
            myAudioRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        myAudioRecorder.start();

        isRecording = true;

        changeRecordStatus.setRecordButtonStatus(true);

        handler = new Handler();
         updater = new Runnable() {
            public void run() {
                handler.postDelayed(this, 1);
                int maxAmplitude = myAudioRecorder.getMaxAmplitude();
                if (maxAmplitude != 0) {
                    byte[] b = new byte[maxAmplitude];
                    new Random().nextBytes(b) ;

                    waveView.setScaledData(b);
                }
            }
        };

         visualizerContainer.setVisibility(View.VISIBLE);
         handler.post(updater);



         timer  = new CountDownTimer(1000,1000) {
             @Override
             public void onTick(long millisUntilFinished) {


                 if(count >= 30){

                     stopRecording();
                     ((Activity)ctx).runOnUiThread(new Runnable() {
                         @Override
                         public void run() {
                             Toast.makeText(ctx,"Recording Finished as it can't exceed 30 Seconds",Toast.LENGTH_LONG).show();
                         }
                     });


                     timer.cancel();
                 }

                 count++ ;


             }

             @Override
             public void onFinish() {

                 count = 0 ;
             }
         };


         timer.start();



    }



    public void stopRecording() {
        // stops the recording activity
        if (myAudioRecorder != null && isRecording ) {

            handler.removeCallbacks(updater);
            isRecording = false;

            myAudioRecorder.pause();
            myAudioRecorder.reset();
            myAudioRecorder.release();
            myAudioRecorder = null;

            if(timer != null ){
                timer.cancel();
                timer = null ;
            }

            changeRecordStatus.setRecordButtonStatus(false);



        }
    }




    public void requestAudioPermission(){
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ((Activity)ctx).requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORDING_REQUEST_CODE);
        }
    }

    public void requestWriteExternalStoragePermission(){
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ((Activity)ctx).requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_REQUEST_CODE);
        }
    }

    @Override
    public void shareObject(String anchorId,String toUserCode ,String fromUserCode) {

        Log.d("Mohamed_tag2",anchorId);
        if(outputFile.exists()) {
            fireBaseConnectHelperClass.saveFileData(outputFile, anchorId);
            fireBaseConnectHelperClass.saveDatabaseAnchorId(anchorId);
            if(toUserCode != null && fromUserCode !=null) {
                fireBaseConnectHelperClass.sendMessage(toUserCode, fromUserCode, "File_" + anchorId + ".3gp");

            }

        }
    }


    public  interface  ChangeRecordStatus {
          void setRecordButtonStatus(boolean isRecording);
    }
}
