package com.greenmagics.ar.models;

public class user {

    String shortcode ;
    String anchorId ;

    public user(){

    }
    public user(String shortcode, String anchorId) {
        this.shortcode = shortcode;
        this.anchorId = anchorId;
    }

    public String getShortcode() {
        return shortcode;
    }

    public void setShortcode(String shortcode) {
        this.shortcode = shortcode;
    }

    public String getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }
}
