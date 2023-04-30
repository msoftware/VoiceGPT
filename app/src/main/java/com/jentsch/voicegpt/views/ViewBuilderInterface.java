package com.jentsch.voicegpt.views;

import android.content.Context;

public interface ViewBuilderInterface {

    MessageView buildRecvView(Context context);
    MessageView buildSentView(Context context);

}
