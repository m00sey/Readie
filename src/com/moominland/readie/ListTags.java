package com.moominland.readie;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.text.MessageFormat;

public class ListTags extends Activity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private String[][] techLists = new String[][]{};

    private TextView textview;

    private static final String TAG = "Readie";
    private IntentFilter[] intentFilters;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create " + getIntent().getAction());
        textview = new TextView(this);
        textview.setText("Scan a tag");
        setContentView(textview);

        nfcAdapter = NfcAdapter.getDefaultAdapter(ListTags.this);
        pendingIntent = createPendingResult(1000, new Intent(this, getClass()), PendingIntent.FLAG_UPDATE_CURRENT);
//		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
//				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndefFilter = new IntentFilter(
                NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataType("text/foo");
        } catch (MalformedMimeTypeException e) {
            Log.e(TAG, "MalformedMimeTypeException setting mime type");
        }

        intentFilters = new IntentFilter[]{ndefFilter};

        if (getIntent() != null) {
            updateTagDetailsFromIntent();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == 1000) {
            Log.d(TAG, "winning");
            Log.d(TAG, data.getAction());
            setIntent(data);
            updateTagDetailsFromIntent();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, MessageFormat.format("resumed {0} {1} {2}", pendingIntent, intentFilters, techLists));
        for (IntentFilter intentFilter : intentFilters) {
            Log.d(TAG, "actions " + intentFilter.getAction(0));
            Log.d(TAG, "data type " + intentFilter.getDataType(0));
        }
        Log.d(TAG, getIntent().toString());
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,
                techLists);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i(TAG, "new intent");
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
        nfcAdapter.disableForegroundDispatch(this);
    }

    private void updateTagDetailsFromIntent() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            textview.setText("");
            Parcelable[] rawData = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawData != null) {
                msgs = new NdefMessage[rawData.length];
                for (int i = 0; i < rawData.length; i++) {
                    msgs[i] = (NdefMessage) rawData[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[]{};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
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