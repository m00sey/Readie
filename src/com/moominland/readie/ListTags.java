package com.moominland.readie;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.TextView;

public class ListTags extends Activity {

	private NfcAdapter nfcAdapter;
	private PendingIntent pendingIntent;
	private String[][] techLists;

	private TextView textview;

	private static final String TAG = "com.moominland.activities.ListTags";
	private IntentFilter[] intentFilters;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "create " + getIntent().getAction());
		super.onCreate(savedInstanceState);
		textview = new TextView(this);
		textview.setText("Scan a tag");
		setContentView(textview);

		nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
//				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndefFilter = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefFilter.addDataType("text/foo");
		} catch (MalformedMimeTypeException e) {
			Log.e(TAG,"MalformedMimeTypeException setting mime type");
		}
		
		IntentFilter techFilter = new IntentFilter(
				NfcAdapter.ACTION_TECH_DISCOVERED);
		intentFilters = new IntentFilter[] { ndefFilter, techFilter };

		techLists = new String[][] { { MifareClassic.class.getName() },
				{ MifareUltralight.class.getName() },
				{ IsoDep.class.getName() }, { NfcA.class.getName() },
				{ NfcB.class.getName() }, { NfcF.class.getName() },
				{ NfcV.class.getName() }, { Ndef.class.getName() },
				{ NdefFormatable.class.getName() } };
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "**** RESUME");
//		Log.i(TAG, getIntent().getAction());
		nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,
				techLists);
		String action = getIntent().getAction();
//		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
//			Log.i(TAG, "Action detected");
//			updateTagDetailsFromIntent(getIntent());
//		}
		
		if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Log.i(TAG, "Action detected");
			updateTagDetailsFromIntent(getIntent());
			setIntent(new Intent());
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		Log.i(TAG, "NEW INTENT");
		updateTagDetailsFromIntent(intent);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "pasuing " + getIntent().getAction());
		nfcAdapter.disableForegroundDispatch(this);
	}

	private void updateTagDetailsFromIntent(Intent intent) {
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(100);
		
		Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		
		textview.setText("");
		textview.setText("Discovered tag supporting the following tech:\n");
		
		String techs = "";
		
		for (String s : detectedTag.getTechList()) {
			techs += (s + "\n");
			Log.i(TAG, s);
		}
		textview.append(techs);		
	}
}