package com.greenmagics.ar;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.button.MaterialButton;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.sceneform.AnchorNode;

import com.google.ar.sceneform.ux.TransformableNode;
import com.greenmagics.ar.Adapters.InboxAdapter;
import com.greenmagics.ar.Utils.CommunicateWithRecorderInterface;
import com.greenmagics.ar.Utils.CustomFloatingActionButton;
import com.greenmagics.ar.Utils.CustomRecorder;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;

import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.greenmagics.ar.Utils.FireBaseConnectHelperClass;
import com.greenmagics.ar.Utils.FirebaseDataRetrieveInterface;
import com.greenmagics.ar.Utils.StorageHelper;
import com.greenmagics.ar.models.Message;

import java.util.ArrayList;
import java.util.List;



import rm.com.audiowave.AudioWaveView;


public class MainActivity extends AppCompatActivity implements CustomRecorder.ChangeRecordStatus, FirebaseDataRetrieveInterface {

    private static final int CAMERA_REQUEST_CODE = 555;
    private boolean mUserRequestedInstall = true;
    Session mSession;
    private ModelRenderable myRenderable;
    ArFragment arFragment;
    private CustomFloatingActionButton record;
    AudioWaveView waveView;
    CustomRecorder myRecorder;
    Snackbar messageSnackbar;
    AnchorNode anchorNode;
    Anchor anchor;
    LinearLayout visualizerContainer;
    private float downXValue;
    private float downYValue;
    AlertDialog dialog;
    AlertDialog msgDialog;
    Config config;
    boolean isHostingAnchorCompleted;
    CommunicateWithRecorderInterface communicateWithRecorderInterface;
    StorageHelper storageHelper;
    String toUserCode;
    String fromUserCode;
    RecyclerView drawerList;
    ImageButton inboxBt;

    TransformableNode andy;
    List<String> usersCodes = new ArrayList<>();
    InboxAdapter adapter;

    @Override
    public void setRecordButtonStatus(boolean isRecording) {

        if (record != null) {
            if (isRecording) {
                record.setElevation(2);
                record.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            } else {
                record.setElevation(8);
                record.setBackgroundTintList(ColorStateList.valueOf(Color.RED));
            }
        }
    }

    @Override
    public void setUsersCodes(List<String> usersCodes) {

        this.usersCodes = usersCodes;
    }

    @Override
    public void isMessageSent(boolean isSent) {
        if (isSent) {
            showMessageSnackBar("Message Sent Successfully to Selected User");
        }

    }

    @Override
    public void updateCurrentMessages(List<Message> list) {

        this.messagesList.clear();
        this.messagesList.addAll(list);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void updateInboxItemState(boolean isAnyNewMsgs) {

        inboxBt.setSelected(isAnyNewMsgs);

    }

    @Override
    public void startNewSessionWithThisMessage(Message message) {

        String msgAnchorId = message.getAudioFileName().replace("File_", "").replace(".3gp", "");


        anchor = arFragment.getArSceneView().getSession().resolveCloudAnchor(msgAnchorId);
        appAnchorState = AppAnchorState.RESOLVING; // Add this line.
        showMessageSnackBar("Now Resolving anchor..."); // Add this line.


        if (anchorNode != null) {
            arFragment.getArSceneView().getScene().removeChild(anchorNode);
            anchorNode = null;
        }

        anchorNode = new AnchorNode(anchor);
        // Create the transformable andy and add it to the anchor.


        andy = new TransformableNode(arFragment.getTransformationSystem());
        andy.setParent(anchorNode);
        andy.setRenderable(myRenderable);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        andy.select();


        // Add some UI to show you that the anchor is being hosted.


         isHostingAnchorCompleted = false ;
         checkUpdatedAnchor(false, false);


        fireBaseConnectHelperClass.playAudioFile(message.getAudioFileName());

        drawer.setVisibility(View.GONE);




    }



    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }


    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    private FireBaseConnectHelperClass fireBaseConnectHelperClass;
    LinearLayout drawer;
    List<Message> messagesList = new ArrayList<>();


    // @SuppressLint("ClickableViewAccessibility")
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageHelper = new StorageHelper(this);
        fireBaseConnectHelperClass = new FireBaseConnectHelperClass(this);

        //check permission
        checkCameraPermission();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //init ArFragment and session
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneForm);
        visualizerContainer = findViewById(R.id.visualizer_container);
        record = findViewById(R.id.record);
        waveView = findViewById(R.id.mywave);
        drawer = findViewById(R.id.drawer);
        drawerList = findViewById(R.id.inbox_list);
        drawerList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InboxAdapter(this, messagesList);
        drawerList.setAdapter(adapter);
        TextView userIdView = findViewById(R.id.user_id_view);
        String userId = getString(R.string.id)+"\n"+storageHelper.getShortcode() ;
        userIdView.setText(userId);


        inboxBt = findViewById(R.id.inbox_bt);
        inboxBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.getVisibility() == View.VISIBLE) {
                    drawer.setVisibility(View.GONE);
                } else if (drawer.getVisibility() == View.GONE) {
                    drawer.setVisibility(View.VISIBLE);
                }
            }
        });


        fireBaseConnectHelperClass.saveDatabaseAnchorId(null);
        fireBaseConnectHelperClass.getUsers();
        fireBaseConnectHelperClass.getMessages();


        waveView.setOnTouchListener((v, event) -> {


            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN: {
                    // store the X value when the user's finger was pressed down
                    downXValue = event.getX();
                    downYValue = event.getY();
                    Log.v("", "= " + downYValue);
                    Log.d("drag", "down");

                    break;
                }

                case MotionEvent.ACTION_UP: {
                    // Get the X value when the user released his/her finger
                    float currentX = event.getX();
                    float currentY = event.getY();
                    // check if horizontal or vertical movement was bigger

                    if (Math.abs(downXValue - currentX) > Math.abs(downYValue
                            - currentY)) {
                        Log.v("dragx", "x");
                        // going backwards: pushing stuff to the right
                        if (downXValue < currentX) {
                            Log.v("drag", "right");

                        }

                        // going forwards: pushing stuff to the left
                        if (downXValue > currentX) {
                            Log.v("drag", "left");
                            showAlertDialog("Are you sure you want to delete this Recording ?",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            //here we will delete recording file and reset
                                            record.setEnabled(true);
                                            record.hide();
                                            visualizerContainer.setVisibility(View.GONE);

                                            if (anchorNode != null) {
                                                arFragment.getArSceneView().getScene().removeChild(anchorNode);
                                                anchorNode = null;
                                            }

                                        }
                                    });

                        }

                    } else {
                        Log.v("dragy", "y ");

                        if (downYValue < currentY) {
                            Log.v("drag", "down");

                        }
                        if (downYValue > currentY) {
                            Log.v("drag", "up");
                            showMessageAlertDialog();

                        }
                    }
                    break;
                }

            }


            return true;


        });


        //recorder init

        myRecorder = new CustomRecorder(this, waveView, fireBaseConnectHelperClass, visualizerContainer);

        communicateWithRecorderInterface = myRecorder;


        record.setOnTouchListener((v, event) -> {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //start recorder
                    myRecorder.requestAudioPermission();
                    myRecorder.requestWriteExternalStoragePermission();

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }

                    checkUpdatedAnchor(true, false);


                    break;
                case MotionEvent.ACTION_UP:
                    v.performClick();
                    //end recorder
                    myRecorder.stopRecording();


                    break;
                default:
                    break;
            }
            return true;
        });


        //get reference to visualizer

        //init Renderable Object
        ModelRenderable.builder().setSource(
                arFragment.getContext(),
                Uri.parse("file:///android_asset/Andy.sfb"))
                .setRegistryId(1)
                .build()
                .thenAccept(renderable -> myRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            return null;
                        });


    }

    @Override
    protected void onResume() {
        super.onResume();


        //check if Arcore is installed or return
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall)) {
                    case INSTALLED:

                        mSession = new Session(this);
                        arFragment.getArSceneView().setupSession(mSession);
                        config = new Config(mSession);
                        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
                        config.setCloudAnchorMode(Config.CloudAnchorMode.ENABLED);
                        mSession.configure(config);
                        //arFragment.getArSceneView().getSession().configure(config);

                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(this, "TODO: handle exception " + e, Toast.LENGTH_LONG)
                    .show();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }


        // set listener to show object on touch
        arFragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {

                    if (messageSnackbar != null && messageSnackbar.isShown()) {
                        messageSnackbar.dismiss();
                    }



                    anchor = arFragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());
                    appAnchorState = AppAnchorState.HOSTING; // Add this line.
                    showMessageSnackBar("Now hosting anchor..."); // Add this line.


                    if (anchorNode != null) {
                        arFragment.getArSceneView().getScene().removeChild(anchorNode);
                        anchorNode = null;
                    }

                    anchorNode = new AnchorNode(anchor);
                    // Create the transformable andy and add it to the anchor.

                    andy = new TransformableNode(arFragment.getTransformationSystem());
                    andy.setParent(anchorNode);
                    andy.setRenderable(myRenderable);
                    arFragment.getArSceneView().getScene().addChild(anchorNode);
                    andy.select();


                    isHostingAnchorCompleted = false ;


                    // Add some UI to show you that the anchor is being hosted.


                    checkUpdatedAnchor(false, false);

                    record.show();


                    //on tap

                });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!shouldShowRequestPermissionRationale()) {
                // Permission denied with checking "Do not ask again".
                launchPermissionSettings(this);
            }
            finish();
        }

        if (requestCode == CustomRecorder.RECORDING_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Recording permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
                if (!shouldShowRequestAudioPermissionRationale()) {
                    // Permission denied with checking "Do not ask again".
                    launchPermissionSettings(this);
                }
                finish();
            } else {

                Toast.makeText(MainActivity.this, "Now you can Record Audio", Toast.LENGTH_LONG).show();

            }
        }


        if (requestCode == CustomRecorder.WRITE_REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External Storage permission is needed to run this application", Toast.LENGTH_LONG)
                        .show();
                if (!shouldShowRequestWritePermissionRationale()) {
                    // Permission denied with checking "Do not ask again".
                    launchPermissionSettings(this);
                }
                finish();
            } else {

                Toast.makeText(MainActivity.this, "Now you can Record Audio", Toast.LENGTH_LONG).show();

            }
        }
    }

    private boolean shouldShowRequestWritePermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    }


    private void checkCameraPermission() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }


    }

    private boolean shouldShowRequestPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
    }

    private boolean shouldShowRequestAudioPermissionRationale() {
        return ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO);
    }

    /**
     * Launch Application Setting to grant permission.
     */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }


    void showMessageSnackBar(String message) {
        messageSnackbar =
                Snackbar.make(
                        findViewById(android.R.id.content),
                        message,
                        Snackbar.LENGTH_INDEFINITE);
        messageSnackbar.getView().setBackgroundColor(Color.BLUE);

        messageSnackbar.setAction(
                "Dismiss",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        messageSnackbar.dismiss();
                    }
                });

        messageSnackbar.addCallback(
                new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        super.onDismissed(transientBottomBar, event);
                    }
                });


        messageSnackbar.show();
    }

    private void checkUpdatedAnchor(boolean isReadyForRecoding, boolean isReadyToSendObject) {

        if (isHostingAnchorCompleted) {

            if (isReadyForRecoding) {
                if (anchor != null && anchor.getCloudAnchorId() != null) {

                    myRecorder.startRecording();


                }
            }

            if (isReadyToSendObject) {
                if (anchor != null && anchor.getCloudAnchorId() != null) {
                    record.setEnabled(true);


                    Log.d("Mohamed_tag", anchor.getCloudAnchorId());

                    communicateWithRecorderInterface.shareObject(anchor.getCloudAnchorId(), toUserCode, fromUserCode);
                    visualizerContainer.setVisibility(View.GONE);
                    record.hide();

                    if (anchorNode != null) {
                        arFragment.getArSceneView().getScene().removeChild(anchorNode);
                        anchorNode = null;
                    }


                }
            }
        }
        Log.d("Mohamed_tag_status", String.valueOf(isHostingAnchorCompleted));

//            if (appAnchorState != AppAnchorState.HOSTING || appAnchorState != AppAnchorState.RESOLVING ) {
//                return;
//            }


        Anchor.CloudAnchorState cloudState = anchor.getCloudAnchorState();
        Log.d("Mohamed_update", cloudState.toString());
        if (cloudState.isError()) {

            showMessageSnackBar("Error hosting anchor: " + cloudState);
            appAnchorState = AppAnchorState.NONE;

            storageHelper.clearAnchorId();

        } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {

            if (appAnchorState == AppAnchorState.RESOLVING) {


                isHostingAnchorCompleted = true;

                Log.d("Mohamed_update", String.valueOf(isHostingAnchorCompleted));

                if (anchor != null && anchor.getCloudAnchorId() != null) {
                    storageHelper.saveAnchor(anchor.getCloudAnchorId());
                }

                showMessageSnackBar(
                        "Anchor resolved successfully! Cloud ID: " + anchor.getCloudAnchorId());
                appAnchorState = AppAnchorState.RESOLVED;

            } else if (appAnchorState == AppAnchorState.HOSTING) {

                isHostingAnchorCompleted = true;

                if (anchor != null && anchor.getCloudAnchorId() != null) {
                    storageHelper.saveAnchor(anchor.getCloudAnchorId());
                }

                showMessageSnackBar(
                        "Anchor hosted successfully! Cloud ID: " + anchor.getCloudAnchorId());
                appAnchorState = AppAnchorState.HOSTED;
            }
        }else if(cloudState == Anchor.CloudAnchorState.TASK_IN_PROGRESS){
            showMessageSnackBar(
                    "please wait .... till Anchor become ready !");
        }


    }


    private void showAlertDialog(String message, Dialog.OnClickListener onClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.title);
        builder.setMessage(message);
        builder.setPositiveButton("yes", onClickListener);
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }

        });
        dialog = builder.create();
        dialog.show();
    }

    private void showMessageAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View v = getLayoutInflater().inflate(R.layout.msg_dialog, null);
        Spinner usersSelector = v.findViewById(R.id.users_ids);
        MaterialButton shareButton = v.findViewById(R.id.share_button);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //here we will send message

                toUserCode = (usersSelector.getSelectedItem() != null) ? usersSelector.getSelectedItem().toString() : null;
                fromUserCode = String.valueOf(storageHelper.getShortcode());
                checkUpdatedAnchor(false, true);
                if (msgDialog != null && msgDialog.isShowing()) {
                    msgDialog.dismiss();
                }
            }
        });
        usersSelector.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, usersCodes));

        builder.setView(v);
        msgDialog = builder.create();
        msgDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        msgDialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (messageSnackbar != null && messageSnackbar.isShown()) {
            messageSnackbar.dismiss();
        }

        if (msgDialog != null && msgDialog.isShowing()) {
            msgDialog.dismiss();
        }
    }


}
