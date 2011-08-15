package com.moominland.readie;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ListTags extends Activity {

    private NfcAdapter nfcAdapter;
    private String[][] techLists = new String[][]{};

    private TextView textview;

    private static final String TAG = "Readie";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        textview = new TextView(this);
        textview.setText("Scan a tag");
        setContentView(textview);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == 1000) {
            setIntent(data);
            updateTagDetailsFromIntent();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onStart() {
        super.onDestroy();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        nfcAdapter = NfcAdapter.getDefaultAdapter(ListTags.this);
        PendingIntent pendingIntent = createPendingResult(1000, new Intent(), 0);

        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);

        try {
            ndefFilter.addDataType("text/foo");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "MalformedMimeTypeException setting mime type");
        }

        IntentFilter[] intentFilters = new IntentFilter[]{ndefFilter};

        if (getIntent() != null) {
            updateTagDetailsFromIntent();
        }

        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,
                techLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "onNewIntent");
        /**
         * http://developer.android.com/reference/android/app/Activity.html#onNewIntent(android.content.Intent)
         * Note that getIntent() still returns the original Intent.
         * You can use setIntent(Intent) to update it to this new Intent.
         */
        setIntent(intent);
        updateTagDetailsFromIntent();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void updateTagDetailsFromIntent() {
        Log.d(TAG, "reading tag");
        Log.d(TAG, getIntent().toString());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            textview.setText("");
            Parcelable[] rawData = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawData != null) {
                msgs = new NdefMessage[rawData.length];
                for (int i = 0; i < rawData.length; i++) {
                    msgs[i] = (NdefMessage) rawData[i];
                }
            } else {
                // Empty tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_EMPTY, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                msgs = new NdefMessage[]{msg};
            }

            for (NdefMessage message : msgs) {
                for (NdefRecord record : message.getRecords()) {
                    textview.append(new String(record.getPayload()));
                }
            }
            //we've processed this intent, lets remove it.
            Log.i(TAG, "clearing intent");
            setIntent(new Intent());
            Toast.makeText(this, "Scanned tag", Toast.LENGTH_SHORT).show();
        }
    }
}