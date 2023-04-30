package com.jentsch.voicegpt.views;

import android.content.Context;

public class ViewBuilder implements ViewBuilderInterface {

    public MessageView buildRecvView(Context context) {
        MessageView view = new ItemRecvView(context);
        return view;
    }

    public MessageView buildSentView(Context context) {
        MessageView view = new ItemSentView(context);
        return view;
    }
}
