package com.itachi46.autoredirect.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 *
 * This class handles the receiving of a cancel allForward global broadcast intent.
 *
 * Created by itachi46 on 27/04/2015.
 */
public class CancelAllForwardReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(MainActivity.TAG, "broadcast received!");
        String disableAllForwardString = "#21#";
        Intent intentCallForward = new Intent(Intent.ACTION_CALL); // ACTION_CALL
        // this allows activities to be started outside of an activity
        intentCallForward.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("tel", disableAllForwardString, "");
        intentCallForward.setData(uri);
        context.startActivity(intentCallForward);
    }




}
