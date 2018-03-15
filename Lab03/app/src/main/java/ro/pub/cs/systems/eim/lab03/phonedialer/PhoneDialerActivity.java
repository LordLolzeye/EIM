package ro.pub.cs.systems.eim.lab03.phonedialer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class PhoneDialerActivity extends AppCompatActivity {

    private EditText editText;
    private ImageButton callImageButton;
    private ImageButton hangupImageButton;
    private ImageButton backspaceImageButton;

    private Button add_to_contact;

    public static int all_buttons[] = {
            R.id.button,
            R.id.button2,
            R.id.button3,
            R.id.button4,
            R.id.button5,
            R.id.button6,
            R.id.button7,
            R.id.button8,
            R.id.button9,
            R.id.button10, // 0
            R.id.button11, // *
            R.id.button12 // #
    };

    private KeyBoardListener keyboardListener = new KeyBoardListener();
    private BackspaceListener backspaceListener = new BackspaceListener();
    private CallListener callListener = new CallListener();
    private HangupListener hangupListener = new HangupListener();
    private AddToContactListener addToContactListener = new AddToContactListener();

    private class KeyBoardListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            editText.setText(editText.getText().toString() + ((Button)view).getText().toString());
        }
    }

    private class BackspaceListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String number = editText.getText().toString();
            int pn_len = number.length();

            if (pn_len > 0) {
                editText.setText(number.substring(0, pn_len - 1));
            }
        }
    }

    private class CallListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (ContextCompat.checkSelfPermission(PhoneDialerActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        PhoneDialerActivity.this,
                        new String[] { Manifest.permission.CALL_PHONE },
                        1);
            } else {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + editText.getText().toString()));
                startActivity(callIntent);
            }
        }
    }

    private class HangupListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            finish();
        }
    }

    private class AddToContactListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String phoneNumber = editText.getText().toString();
            if (phoneNumber.length() > 0) {
                Intent intent = new Intent("ro.pub.cs.systems.eim.lab04.contactsmanager.intent.action.ContactsManagerActivity");
                intent.putExtra("ro.pub.cs.systems.eim.lab04.contactsmanager.PHONE_NUMBER_KEY", phoneNumber);
                startActivityForResult(intent, 666);
            } else {
                Toast.makeText(getApplication(), "ERROR!", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_dialer);

        editText = (EditText) findViewById(R.id.editText);

        for (int i = 0; i < all_buttons.length; i++) {
            Button keyboardButton = (Button)findViewById(all_buttons[i]);
            keyboardButton.setOnClickListener(keyboardListener);
        }

        backspaceImageButton = (ImageButton) findViewById(R.id.imageButton);
        backspaceImageButton.setOnClickListener(backspaceListener);

        callImageButton = (ImageButton) findViewById(R.id.imageButton2);
        callImageButton.setOnClickListener(callListener);

        hangupImageButton = (ImageButton) findViewById(R.id.imageButton3);
        hangupImageButton.setOnClickListener(hangupListener);

        add_to_contact = (Button) findViewById(R.id.add_to_contact);
        add_to_contact.setOnClickListener(addToContactListener);
    }


}
