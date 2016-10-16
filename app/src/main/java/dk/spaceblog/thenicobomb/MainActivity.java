/*
* Small Android App for sending floods or delayed SMS.
* Copyright (C) 2016  Nicolas Obriot
*
* This program is free software: you can redistribute it and/or modify it under
* the terms of the GNU General Public License as published by the
* Free Software Foundation, either version 3 of the License,
* or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
* Created on Mon Feb 22 20:50:06 2016
* */

package dk.spaceblog.thenicobomb;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //Strange but I haven't found any other way to access the instance of this activity from other classes :
    public static MainActivity mainActivityInstance = null;
    //Class attributes
    private Spinner repeatSpinner, delaySpinner, intervalSpinner;
    private int delay, interval, repeat;
    private String textMessage, phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityInstance = this; //Initialize the static instance with the current instance
        // Set the main menu view
        setContentView(R.layout.activity_main);

        // Configure the default values for delay, repeat and intervals.
        delay = interval = repeat = 1;
        textMessage = phoneNumber= null;

        // Populate the spinners (http://www.mkyong.com/android/android-spinner-drop-down-list-example/)
        // Repeat spinner
        repeatSpinner = (Spinner) findViewById(R.id.repeatSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sms_repeat_count, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        repeatSpinner.setAdapter(adapter);
        repeatSpinner.setOnItemSelectedListener(new SpinnerOnItemSelectedListener(1));
        repeatSpinner.setSelection(0);

        // Delay spinner
        delaySpinner = (Spinner) findViewById(R.id.delaySpinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.sms_delay, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        delaySpinner.setAdapter(adapter2);
        delaySpinner.setOnItemSelectedListener(new SpinnerOnItemSelectedListener(2));
        delaySpinner.setSelection(0);

        // Interval spinner
        intervalSpinner = (Spinner) findViewById(R.id.intervalSpinner);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.sms_interval, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        intervalSpinner.setAdapter(adapter3);
        intervalSpinner.setOnItemSelectedListener(new SpinnerOnItemSelectedListener(3));
        intervalSpinner.setSelection(0);

        //Activate the contact button :
        ((ImageButton)findViewById(R.id.pick_contact)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user BoD suggests using Intent.ACTION_PICK instead of .ACTION_GET_CONTENT to avoid the chooser
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // BoD con't: CONTENT_TYPE instead of CONTENT_ITEM_TYPE
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                startActivityForResult(intent, 1);
            }
        });

        //Check whether the app has the permission to send SMS :
        this.checkSMSPermission();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            System.exit(0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Setters for the parameters
    public void setDelay(int newDelay){
        Log.d("NicoBomb:MainActivity", "New delay configured:" +newDelay +"ms" );
        this.delay=newDelay;
    }
    public void setInterval(int newInterval){
        Log.d("NicoBomb:MainActivity", "New message frequency configured:" +newInterval +"ms" );
        this.interval=newInterval;
    }
    public void setRepeat(int newRepeat){
        Log.d("NicoBomb:MainActivity", "New repeat count configured:" +newRepeat +" messages" );
        this.repeat=newRepeat;
    }
    public void setPhoneNumber(String newPhoneNumber){
        Log.d("NicoBomb:MainActivity", "New Phone Number configured:" +newPhoneNumber +" messages" );
        this.phoneNumber=newPhoneNumber;
    }

    //What happens when the radio buttons are clicked
    public void onRadioButtonClicked(View view){
        Log.d("NicoBomb:MainActivity", "Radio button has been clicked");
    }

    // When the pick contact button has been selected
    public void pickContact(View view){
        Log.d("NicoBomb:MainActivity", "Pick contact clicked");
        // TODO: something like this: http://www.c-sharpcorner.com/UploadFile/ef3808/how-to-pick-a-contact-from-contact-list-in-android/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                Cursor c = null;
                try {
                    c = getContentResolver().query(uri, new String[]{
                                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                                    ContactsContract.CommonDataKinds.Phone.TYPE },
                            null, null, null);

                    if (c != null && c.moveToFirst()) {
                        String number = c.getString(0);
                        int type = c.getInt(1);
                        showSelectedNumber(type, number); //Show a toast for the phone number
                        setPhoneNumber(number);// Put the number in the field
                        // Also copy the data into the editText
                        EditText editText = (EditText)findViewById(R.id.phone_number);
                        editText.setText(number);
                    }
                } finally {
                    if (c != null) {
                        c.close();
                    }
                }
            }
        }
    }



    /** Called when the user clicks the Send button */
    public void sendNicoBomb(View view) {
        // Do something in response to button
        Log.d("NicoBomb:MainActivity", "Nico Bomb activated!");

        // First check whether there is any text entered ..
        EditText editText = (EditText) findViewById(R.id.edit_message);
        if(editText.getText().toString().length()>=1){
            textMessage = editText.getText().toString();
        }else{
            Toast.makeText(this, "You have to enter a message, dickhead !", Toast.LENGTH_SHORT).show();
        }

        // First check whether there is any number entered ..
        EditText editText2 = (EditText) findViewById(R.id.phone_number);
        if(editText2.getText().toString().length()>=1){
            phoneNumber = editText2.getText().toString();
        }else{
            Toast.makeText(this, "You have to enter a phone number, you idiot!", Toast.LENGTH_SHORT).show();
        }
        // We display a notification if there is a message
        if (null != textMessage && null != phoneNumber) {
            for (int i = 0; i < repeat; i++) {
                // Execute some code after 2 seconds have passed
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        Log.d("NicoBomb:MainActivity", "Delay loop entered");
                        sendSMSMessage();
                    }
                }, delay + i * interval);
            }
        }
    }


    /** Called when the user clicks the Send button */
    public void sendSMSMessage(){
        //Intent sentIntent = new Intent("SMS_SENT");
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(this.phoneNumber, null, this.textMessage, null, null);
    }

    /* Function used to show a toast with selected phone number */
    public void showSelectedNumber(int type, String number) {
        Toast.makeText(this, type + ": " + number, Toast.LENGTH_LONG).show();
    }


    /**/
    public void checkSMSPermission(){
    // Here, "this" is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            //if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            //} else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.SEND_SMS}, 0); //MY_PERMISSIONS_REQUEST_SEND_SMS

                // MY_PERMISSIONS_REQUEST_SEND_SMS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            //}
        }
    }

}
