package com.csipsimple.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.csipsimple.ui.SipHome;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, SipHome.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
    }

}
