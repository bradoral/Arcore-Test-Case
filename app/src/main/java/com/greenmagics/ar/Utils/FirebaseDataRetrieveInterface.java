package com.greenmagics.ar.Utils;

import com.greenmagics.ar.models.Message;

import java.util.List;

public interface FirebaseDataRetrieveInterface {
    void setUsersCodes(List<String> usersCodes);
    void isMessageSent(boolean isSent);

    void updateCurrentMessages(List<Message> list);

    void updateInboxItemState(boolean isAnyNewMsgs);

    void startNewSessionWithThisMessage(Message message);
}
