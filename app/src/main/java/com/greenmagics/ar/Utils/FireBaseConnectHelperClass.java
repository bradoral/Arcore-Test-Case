
package com.greenmagics.ar.Utils;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.greenmagics.ar.models.Message;
import com.greenmagics.ar.models.user;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class  FireBaseConnectHelperClass {

    private static final String TAG = FireBaseConnectHelperClass.class.getName();
    private static final String KEY_ROOT_DIR = "Anchors_AR";
    private static final String KEY_AUDIO_DIR = "AUDIO_FILES";
    private final DatabaseReference rootRef;
    private  final  DatabaseReference audioRef ;
    StorageReference firebaseStorage ;
    StorageHelper storageHelper ;
    FirebaseDataRetrieveInterface firebaseDataRetrieveInterface ;
    MyMediaPlayer mediaPlayer ;

    public FireBaseConnectHelperClass(Context context) {
        FirebaseApp firebaseApp = FirebaseApp.initializeApp(context);
        rootRef = FirebaseDatabase.getInstance(firebaseApp).getReference().child(KEY_ROOT_DIR);
        audioRef = FirebaseDatabase.getInstance(firebaseApp).getReference(KEY_AUDIO_DIR);
        DatabaseReference.goOnline();
        firebaseStorage =  FirebaseStorage.getInstance().getReference();
        storageHelper = new StorageHelper(context);
        firebaseDataRetrieveInterface = (FirebaseDataRetrieveInterface)context ;
         mediaPlayer = new MyMediaPlayer();




    }

    public void  saveFileData(File file, String audioFileName){
        firebaseStorage.child("File_"+audioFileName + ".3gp").putFile(Uri.fromFile(file)) ;
    }

    public  void saveDatabaseAnchorId(String anchorId){

        user u = new user(String.valueOf(storageHelper.getShortcode()),anchorId);

        rootRef.child("users").child(String.valueOf(storageHelper.getShortcode())).setValue(u);

    }


    public void getUsers(){

        rootRef.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator =  dataSnapshot.getChildren().iterator();
                List<String> l = new ArrayList<>();
                while (iterator.hasNext()){
                    user u =  iterator.next().getValue(user.class);
                    if(u != null) {
                        l.add(u.getShortcode());
                    }
                }

                firebaseDataRetrieveInterface.setUsersCodes(l);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }



    public void sendMessage(String toUserShortCode , String fromUserShortCode , String audioFileName){


        Message message = new Message(audioFileName,toUserShortCode,fromUserShortCode,String.valueOf(System.currentTimeMillis()/1000)
        ,"new");
        rootRef.child("messages").child(toUserShortCode).child(fromUserShortCode).setValue(message);

        firebaseDataRetrieveInterface.isMessageSent(true);
    }


    public void getMessages() {

        rootRef.child("messages").child(String.valueOf(storageHelper.getShortcode())).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
               Iterator<DataSnapshot> iterator =  dataSnapshot.getChildren().iterator();
               List<Message> list = new ArrayList<>();
               boolean isAnyNewMsgs = false ;

               while(iterator.hasNext()){
                 Message message =   iterator.next().getValue(Message.class);
                 list.add(message);
                   Log.d("Mohamed_tag_messages",message.getSenderCode());

                   if(message.getStatus().equals("new")){

                      isAnyNewMsgs = true ;
                   }

               }


                   firebaseDataRetrieveInterface.updateInboxItemState(isAnyNewMsgs);


                firebaseDataRetrieveInterface.updateCurrentMessages(list);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    public void startNewSessionWithMessage(Message message) {
        //first we update message status and time
        message.setStatus("opened");
        message.setLastTimeUpdated(String.valueOf(System.currentTimeMillis()/1000));
        rootRef.child("messages").child(message.getReceiverCode()).child(message.getSenderCode()).setValue(message);
        //second we will start Session with this message data
        firebaseDataRetrieveInterface.startNewSessionWithThisMessage(message);
        Log.d("Mohamed_tag_newsession","started");

    }

    public void playAudioFile(String audioFileName) {
        String audioUrl = "https://firebasestorage.googleapis.com/v0/b/myarproj-22c33.appspot.com/o/"+audioFileName+"?alt=media&token=fae4c64b-0059-4612-bbfd-aa589e3394d5";
        mediaPlayer.playAudioFromUrl(audioUrl);
        Log.d("Mohamed_tag_PLAYAUDIO","PLAY AUDIO");
    }
}