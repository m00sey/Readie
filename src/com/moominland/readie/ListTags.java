package com.moominland.readie;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class ListTags extends Activity {

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private String[][] techLists = new String[][] {};

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
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndefFilter = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefFilter.addDataType("text/foo");
		} catch (MalformedMimeTypeException e) {
			Log.e(TAG,"MalformedMimeTypeException setting mime type");
		}

		intentFilters = new IntentFilter[] { ndefFilter };

        if (getIntent() != null) {
            updateTagDetailsFromIntent();
        }
    }

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "resumed " + pendingIntent + " " + intentFilters + " " + techLists);
        for (IntentFilter intentFilter : intentFilters) {
            Log.d(TAG, "actions " + intentFilter.getAction(0));
            Log.d(TAG, "data type " + intentFilter.getDataType(0));
        }
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,
				techLists);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "I see the correct NDEF intent here, in the log");
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
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(100);
            Toast.makeText(this, "Scanned tag", Toast.LENGTH_SHORT).show();

            Tag detectedTag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);

            textview.setText("");
            textview.setText("Discovered tag supporting the following tech:\n");

            String techs = "";

            for (String s : detectedTag.getTechList()) {
                techs += (s + "\n");
                Log.i(TAG, s);
            }
            textview.append(techs);
            //we've processed this intent, lets remove it.
            Log.i(TAG, "clearing intent");
            setIntent(new Intent());
        }
	}
}