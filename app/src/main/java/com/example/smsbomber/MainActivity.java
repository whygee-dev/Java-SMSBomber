package com.example.smsbomber;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static MainActivity mainActivityRunningInstance;
    private String lastNumberClick = "-1";

    public static MainActivity getInstance(){
        return mainActivityRunningInstance;
    }

    private ListView listNames;
    private List<Contact> contacts;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    HashMap<String, String> contactMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityRunningInstance = this;
        setContentView(R.layout.activity_main);
        this.listNames = (ListView) findViewById(R.id.listNames);
        this.contacts = getContactNames();
        checkForSmsReceivePermissions();
        checkForSmsSendPermissions();
        showContacts();
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            List<String> displayContacts = new ArrayList<String>();

            for (Contact contact:
                 contacts) {
                displayContacts.add(contact.name + " " + contact.number + " (" + contact.exchangedSmsCount + ")");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, displayContacts);
            listNames.setAdapter(adapter);
            listNames.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Dialog dialog= new CustomDialog(MainActivity.this);
                    dialog.show();
                    lastNumberClick = contacts.get(position).number;
                    Log.i("click", contacts.get(position).number);
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void bomb(int n, String body) {
        Log.i("bombN", String.valueOf(n));
        Log.i("bombNumber", lastNumberClick);
        Log.i("bombBody", body);

        if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            for(int i = 0; i < n; i++) {

                Log.i("bombSms", "sending i" + i);
                sendSms(lastNumberClick, body);
            }

            Toast.makeText(this, "Bombed " + lastNumberClick + " " + n + " fois", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Permission non accordée", Toast.LENGTH_LONG).show();
        }

    }

    public void sendSms(String number, String body) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(number, null, body, null, null);
    }

    void checkForSmsReceivePermissions(){
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.RECEIVE_SMS") == PackageManager.PERMISSION_GRANTED) {
            Log.d("smsReceivePerm", "checkForSmsReceivePermissions: Allowed");
        } else {
            Log.d("smsReceivePerm", "checkForSmsReceivePermissions: Denied");

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECEIVE_SMS}, 43391);
        }
    }

    void checkForSmsSendPermissions(){
        if(ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.SEND_SMS") == PackageManager.PERMISSION_GRANTED) {
            Log.d("smsSendPerm", "checkForSmsSendPermissions: Allowed");
        } else {
            Log.d("smsSendPerm", "checkForSmsSendPermissions: Denied");

            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.SEND_SMS}, 43392);
        }
    }



    void updateSmsCount(String address) {
        String withCountryCodeRemoved = address.replace("+33", "0");

        for (Contact contact:
             contacts) {
            if (address.equals(contact.number) || withCountryCodeRemoved.equals(contact.number)) {
                contact.exchangedSmsCount += 1;
                showContacts();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showContacts();
            } else {
                Toast.makeText(this, "Permission non accordée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private List<Contact> getContactNames() {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String phoneNumber = "";

                if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0)
                {
                    // Query phone here. Covered next
                    Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,null, null);
                    while (phones.moveToNext()) {
                        phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    phones.close();
                }
                Log.i("Number", phoneNumber);

                contacts.add(new Contact(name, id, phoneNumber));

            } while (cursor.moveToNext());
        }

        cursor.close();

        return contacts;
    }
}