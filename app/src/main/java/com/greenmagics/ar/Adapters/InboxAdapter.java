package com.greenmagics.ar.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.greenmagics.ar.R;
import com.greenmagics.ar.Utils.FireBaseConnectHelperClass;
import com.greenmagics.ar.models.Message;

import java.util.ArrayList;
import java.util.List;


public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {

    Context ctx ;
    FireBaseConnectHelperClass fireBaseConnectHelperClass ;

    List<Message> list = new ArrayList<>();
    public InboxAdapter(Context ctx, List<Message> list){
        this.ctx = ctx ;
        this.list = list ;
        fireBaseConnectHelperClass = new FireBaseConnectHelperClass(ctx);
    }


    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new InboxViewHolder(LayoutInflater.from(ctx).inflate(R.layout.inbox_message_item,null));
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder inboxViewHolder, int i) {

        inboxViewHolder.user_name_message.setText(list.get(i).getSenderCode());
        inboxViewHolder.openMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fireBaseConnectHelperClass.startNewSessionWithMessage(list.get(i));
            }
        });


    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    class InboxViewHolder extends RecyclerView.ViewHolder {

        TextView user_name_message ;
        Button openMessage ;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            user_name_message = itemView.findViewById(R.id.user_name);
            openMessage = itemView.findViewById(R.id.message_open);
        }
    }
}
