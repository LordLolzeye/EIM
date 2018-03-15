package ro.pub.cs.systems.eim.lab04.contactsmanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class ContactsManagerActivity extends AppCompatActivity {

    private EditText name, phone, email, address, job, company, website, skype;

    private Button showAdditionalFields, saveButton, cancelButton;

    private LinearLayout additionalFieldsContainer;

    private ShowAdditionalFieldsListener showAdditionalFieldsListener = new ShowAdditionalFieldsListener();
    private class ShowAdditionalFieldsListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.save_button) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
                if (name != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.NAME, name.getText().toString());
                }
                if (phone != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone.getText().toString());
                }
                if (email != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.EMAIL, email.getText().toString());
                }
                if (address != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.POSTAL, address.getText().toString());
                }
                if (job != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, job.getText().toString());
                }
                if (company != null) {
                    intent.putExtra(ContactsContract.Intents.Insert.COMPANY, company.getText().toString());
                }
                ArrayList<ContentValues> contactData = new ArrayList<ContentValues>();
                if (website != null) {
                    ContentValues websiteRow = new ContentValues();
                    websiteRow.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
                    websiteRow.put(ContactsContract.CommonDataKinds.Website.URL, website.getText().toString());
                    contactData.add(websiteRow);
                }
                if (skype != null) {
                    ContentValues imRow = new ContentValues();
                    imRow.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
                    imRow.put(ContactsContract.CommonDataKinds.Im.DATA, skype.getText().toString());
                    contactData.add(imRow);
                }
                intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);
                startActivityForResult(intent, 666);
            } else if(view.getId() == R.id.cancel_button) {
                setResult(Activity.RESULT_CANCELED, new Intent());
            } else {
                switch (additionalFieldsContainer.getVisibility()) {
                    case View.VISIBLE:
                        showAdditionalFields.setText("SHOW ADDITIONAL FIELDS");
                        additionalFieldsContainer.setVisibility(View.INVISIBLE);
                        break;
                    case View.INVISIBLE:
                        showAdditionalFields.setText("HIDE ADDITIONAL FIELDS");
                        additionalFieldsContainer.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_manager);

        name = (EditText)findViewById(R.id.name);
        phone = (EditText)findViewById(R.id.phone);
        email = (EditText)findViewById(R.id.email);
        address = (EditText)findViewById(R.id.address);


        job = (EditText)findViewById(R.id.job);
        company = (EditText)findViewById(R.id.company);
        website = (EditText)findViewById(R.id.website);
        skype = (EditText)findViewById(R.id.skype);

        showAdditionalFields = (Button)findViewById(R.id.button);
        showAdditionalFields.setOnClickListener(showAdditionalFieldsListener);

        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(showAdditionalFieldsListener);

        cancelButton = (Button)findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(showAdditionalFieldsListener);

        additionalFieldsContainer = (LinearLayout)findViewById(R.id.linearLayoutAdditionalFields);

        Intent intent = getIntent();
        if (intent != null) {
            String phoneNr = intent.getStringExtra("ro.pub.cs.systems.eim.lab04.contactsmanager.PHONE_NUMBER_KEY");
            if (phoneNr != null) {
                phone.setText(phoneNr);
            } else {
                Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch(requestCode) {
            case 666:
                setResult(resultCode, new Intent());
                finish();
                break;
        }
    }
}
