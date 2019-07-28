package com.greenmagics.ar.models;

public class Message {
    String audioFileName ;
    String receiverCode ;
    String senderCode ;
    String lastTimeUpdated ;
    String status ;

    public Message(){

    }

    public Message(String audioFileName, String receiverCode, String senderCode, String lastTimeUpdated, String status) {
        this.audioFileName = audioFileName;
        this.receiverCode = receiverCode;
        this.senderCode = senderCode;
        this.lastTimeUpdated = lastTimeUpdated;
        this.status = status;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getReceiverCode() {
        return receiverCode;
    }

    public void setReceiverCode(String receiverCode) {
        this.receiverCode = receiverCode;
    }

    public String getSenderCode() {
        return senderCode;
    }

    public void setSenderCode(String senderCode) {
        this.senderCode = senderCode;
    }

    public String getLastTimeUpdated() {
        return lastTimeUpdated;
    }

    public void setLastTimeUpdated(String lastTimeUpdated) {
        this.lastTimeUpdated = lastTimeUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
