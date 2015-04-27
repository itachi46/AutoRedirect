package com.itachi46.autoredirect.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "Wahoo";
    private Switch forwardToggle;
    private EditText forwardNumber;
    private boolean forwardingChangeDetected;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "oncreate!");
        setContentView(R.layout.activity_main);

        sharedPrefs = getSharedPreferences(getString(R.string.preference_key_file), Context.MODE_PRIVATE);

        addCallForwardListener();
        addSwitchListener();
        addEditTextListener();

        forwardingChangeDetected = false;
        String savedForwardingNumber = sharedPrefs.getString(getString(R.string.saved_forwarding_number), "");
        forwardNumber.setText(savedForwardingNumber);


    }

    @Override
    protected void onPause(){
        super.onPause();
        savePreferences();
    }

    @Override
    protected void onStop(){
        super.onStop();
        savePreferences();
    }

    private void addCallForwardListener() {
        //read status of forwarding
        TelephonyManager manager = (TelephonyManager)
                this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
    }

    private void addSwitchListener() {
        //add set on value change listener for the switch
        forwardToggle = (Switch) findViewById(R.id.forward_toggle);
        forwardToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                savePreferences();
                if (forwardingChangeDetected) {
                    forwardingChangeDetected = false;
                } else {
                    forwardCalls(compoundButton);
                }
            }
        });
    }

    private void addEditTextListener() {
        // add editText's on action listener.
        forwardNumber = (EditText) findViewById(R.id.forward_number);

        forwardNumber.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(getString(R.string.saved_forwarding_number), forwardNumber.getText().toString());
                editor.commit();
                return false;
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Toast.makeText(this, "Settings selected", Toast.LENGTH_SHORT)
                        .show();
                break;
            default:
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    private void savePreferences(){
        forwardNumber = (EditText) findViewById(R.id.forward_number);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getString(R.string.saved_forwarding_number), forwardNumber.getText().toString());
        editor.apply();

    }

    private void forwardCalls(View view) {
        Switch forwardSwitch;
        forwardSwitch = (Switch) view;
        boolean on = forwardSwitch.isChecked();
        if (!on) {
            forwardSwitch.setChecked(true); // preserve previous state and let call forwarding listener change the value.
            cancelAllForward();
        } else {
            forwardSwitch.setChecked(false);// this is set to false so that the toggle only stays at on if the forwarding was successful.
            String phoneNumber = forwardNumber.getText().toString();
            if (phoneNumber.isEmpty() || !phoneNumber.startsWith("+") || phoneNumber.length() < 8) {
                Toast.makeText(this, R.string.Invalid_number, Toast.LENGTH_SHORT).show();
            }
            else {
                forwardAll(phoneNumber);
                createNotification(phoneNumber);

            }
        }
    }

    /**
     * Creates the notification after a forward is made.
     * @param phoneNumber the Number to forward to.
     */
    private void createNotification(String phoneNumber){

        Intent cancelIntent = new Intent(this, CancelAllForwardReceiver.class);
        PendingIntent pendingCancelIntent = PendingIntent.getBroadcast(this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Call Forwarding Active")
                        .setContentText("All calls forwarded to " + phoneNumber)
                        .addAction(R.drawable.ic_launcher,"Snooze", null)
                        .addAction(R.drawable.ic_launcher, "Cancel", pendingCancelIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_MAX);

        // Creates an explicit intent for an Activity in your app
        Intent openActivityIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(openActivityIntent);
        // 0  is the request code which is used to retrieve this particular intent again.
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }

    private void cancelAllForward() {
        String disableAllForwardString = "#21#";
        Intent intentCallForward = new Intent(Intent.ACTION_CALL); // ACTION_CALL
        Uri uri = Uri.fromParts("tel", disableAllForwardString, "");
        intentCallForward.setData(uri);
        startActivity(intentCallForward);
    }

    private void forwardAll(String number) {
        String forwardString;
        forwardString = String.format("*21*%s#", number);
        Intent intentCallForward = new Intent(Intent.ACTION_CALL); // ACTION_CALL
        Uri uri = Uri.fromParts("tel", forwardString, "#");
        intentCallForward.setData(uri);
        startActivity(intentCallForward);
    }


    class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallForwardingIndicatorChanged(boolean cfi) {
            super.onCallForwardingIndicatorChanged(cfi);
            if (forwardToggle.isChecked() != cfi) {
                forwardingChangeDetected = true;
                forwardToggle.setChecked(cfi);
            }


        }

    }

}
