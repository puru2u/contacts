package com.contacts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddPhoneContactActivity extends AppCompatActivity {
    private EditText displayNameEditor;

    private EditText phoneNumberEditor;
    private Button savePhoneContactButton;
    private int PERMISSION_REQUEST_CODE_WRITE_CONTACTS = 200;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_phone_contact);


        displayNameEditor = findViewById(R.id.add_phone_contact_display_name);

        phoneNumberEditor = findViewById(R.id.add_phone_contact_number);

        savePhoneContactButton = (Button)findViewById(R.id.add_phone_contact_save_button);


        savePhoneContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get android phone contact content provider uri.
                //Uri addContactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                // Below uri can avoid java.lang.UnsupportedOperationException: URI: content://com.android.contacts/data/phones error.
                Uri addContactsUri = ContactsContract.Data.CONTENT_URI;

                // Add an empty contact and get the generated id.
                long rowContactId = getRawContactId();

                // Add contact name data.
                String displayName = displayNameEditor.getText().toString();
                insertContactDisplayName(addContactsUri, rowContactId, displayName);

                // Add contact phone data.
                String phoneNumber = phoneNumberEditor.getText().toString();

                insertContactPhoneNumber(addContactsUri, rowContactId, phoneNumber);

                Toast.makeText(getApplicationContext(),"New contact has been added, go back to previous page to see it in contacts list." , Toast.LENGTH_LONG).show();

                finish();
            }
        });

        if(!hasPhoneContactsPermission(Manifest.permission.WRITE_CONTACTS))
        {
            requestPermission(Manifest.permission.WRITE_CONTACTS, PERMISSION_REQUEST_CODE_WRITE_CONTACTS);
        }
//        else
//        {
//            AddPhoneContactActivity.start(getApplicationContext());
//        }
    }

    // This method will only insert an empty data to RawContacts.CONTENT_URI
    // The purpose is to get a system generated raw contact id.
    private long getRawContactId()
    {
        // Inser an empty contact.
        ContentValues contentValues = new ContentValues();
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, contentValues);
        // Get the newly created contact raw id.
        long ret = ContentUris.parseId(rawContactUri);
        return ret;
    }


    // Insert newly created contact display name.
    private void insertContactDisplayName(Uri addContactsUri, long rawContactId, String displayName)
    {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);

        // Put contact display name value.
        contentValues.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, displayName);

        getContentResolver().insert(addContactsUri, contentValues);

    }

    private void insertContactPhoneNumber(Uri addContactsUri, long rawContactId, String phoneNumber)
    {
        // Create a ContentValues object.
        ContentValues contentValues = new ContentValues();

        // Each contact must has an id to avoid java.lang.IllegalArgumentException: raw_contact_id is required error.
        contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);

        // Each contact must has an mime type to avoid java.lang.IllegalArgumentException: mimetype is required error.
        contentValues.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Put phone number value.
        contentValues.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber);



        // Insert new contact data into phone contact list.
        getContentResolver().insert(addContactsUri, contentValues);

    }

    // ListPhoneContactsActivity use this method to start this activity.
    public static void start(Context context)
    {
        Intent intent = new Intent(context, AddPhoneContactActivity.class);
        context.startActivity(intent);

    }

    private void requestPermission(String permission, int requestCode)
    {
        String requestPermissionArray[] = {permission};
        ActivityCompat.requestPermissions(this, requestPermissionArray, requestCode);
    }

    // After user select Allow or Deny button in request runtime permission dialog
    // , this method will be invoked.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int length = grantResults.length;
        if(length > 0)
        {
            int grantResult = grantResults[0];

            if(grantResult == PackageManager.PERMISSION_GRANTED) {

                if(requestCode==PERMISSION_REQUEST_CODE_WRITE_CONTACTS)
                {
                    AddPhoneContactActivity.start(getApplicationContext());
                }
            }else
            {
                Toast.makeText(getApplicationContext(), "You denied permission.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // Check whether user has phone contacts manipulation permission or not.
    private boolean hasPhoneContactsPermission(String permission)
    {
        boolean ret = false;

        // If android sdk version is bigger than 23 the need to check run time permission.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // return phone read contacts permission grant status.
            int hasPermission = ContextCompat.checkSelfPermission(getApplicationContext(), permission);
            // If permission is granted then return true.
            if (hasPermission == PackageManager.PERMISSION_GRANTED) {
                ret = true;
            }
        }else
        {
            ret = true;
        }
        return ret;
    }

}