package com.itachi46.autoredirect.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {

    private Switch forwardToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //read status of forwarding
        TelephonyManager manager = (TelephonyManager)
                this.getSystemService(TELEPHONY_SERVICE);
        manager.listen(new MyPhoneStateListener(),
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);

        //add set on value change listener for the switch
        forwardToggle = (Switch) findViewById(R.id.forward_toggle);
        forwardToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                forwardCalls(compoundButton);
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

        return true;

//        return super.onOptionsItemSelected(item);
    }

    public void forwardCalls(View view) {
        Switch forwardSwitch;
        forwardSwitch = (Switch) view;
        boolean on = forwardSwitch.isChecked();
        if (!on) {
            cancelAllForward();
            Toast.makeText(this, "off", Toast.LENGTH_SHORT).show();
        } else {
            EditText editBox = (EditText) findViewById(R.id.forward_number);
            String phoneNumber = editBox.getText().toString();
            if (phoneNumber.isEmpty() || !phoneNumber.startsWith("+")) {
                forwardSwitch.setChecked(false);
            } else {
                forwardAll(phoneNumber);
            }
        }
    }

    private void cancelAllForward() {
        String disableAllForwardString;
        disableAllForwardString = "#21#";
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
            forwardToggle.setChecked(cfi);
        }

    }

}
