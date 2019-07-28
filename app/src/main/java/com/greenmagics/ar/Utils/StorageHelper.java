package com.greenmagics.ar.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;

public class StorageHelper {

    SharedPreferences prefs ;
    Context ctx ;
    long shortcode  ;

    public  StorageHelper(Context ctx){
        this.ctx = ctx ;
        prefs = ctx.getSharedPreferences("ARSTORAGE",Context.MODE_PRIVATE);
    }

    public void saveAnchor(String anchorId){
        prefs.edit().putString("AnchorId",anchorId).apply();
    }

    public  String getAnchor(){
        return prefs.getString("AnchorId",null);
    }

    public  void clearAnchorId (){
        prefs.edit().remove("AnchorId").apply();
    }


    public long getShortcode() {

       long code  =  prefs.getLong("shortcode",-1);

       if(code == -1){

           generateShortCode();
       }

       return  prefs.getLong("shortcode",-1) ;
    }

    private  void generateShortCode(){

       shortcode =  System.currentTimeMillis()/1000;
       prefs.edit().putLong("shortcode",shortcode).apply();
    }

}
