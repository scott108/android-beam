package com.example.scott.androidbeam;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;


public class MainActivity extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;
    private NdefMessage ndefMessage;
    protected PendingIntent nfcPendingIntent;
    private IntentFilter[] tagFilters;
    TextView textView;
    JSONObject object;
    String domain = "com.example.scott.androidbream";
    String type = "icheedata";
    Intent originIntent;
    Base64 base64;

    public TextView getTextView() {
        return textView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);
        Button sendSignBtn = (Button) findViewById(R.id.sendSignBtn);
        Button sendJsonBtn = (Button) findViewById(R.id.sendJsonBtn);

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Register Android Beam callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        mNfcAdapter.setOnNdefPushCompleteCallback(this, this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            this.finish();
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        sendSignBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sign = "ds5g465ds4g65kgjmew";
                byte[] signData = sign.getBytes();
                setNfcMessage(signData);
            }
        });

        sendJsonBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] mimeData = createJSONObject();
                setNfcMessage(mimeData);
            }
        });

        nfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        tagDetected.addDataScheme("vnd.android.nfc");
        tagDetected.addDataAuthority("ext", null);
        tagDetected.addDataPath("/" + domain + ":" + type, 0);
        tagFilters = new IntentFilter[]{tagDetected};

        originIntent  = getIntent();
    }

    public void setNfcMessage(byte[] mimeData) {
        ndefMessage = new NdefMessage(new NdefRecord[]{NdefRecord.createExternal(domain, type, mimeData)});
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefMessage msg = ndefMessage;
        System.out.println("detected");
        return msg;
    }

    @Override
    public void onNewIntent(Intent intent) {
        System.out.println("On New Inten Thread : " + Thread.currentThread().getName().toString());
        String appSpecificPath = intent.getDataString();
        System.out.println("appSpecificPath :" + appSpecificPath);
        setIntent(intent);
        /*
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction()) && getIntent().getDataString().equals("vnd.android.nfc://ext/" + domain + ":" + type)) {
            processIntent(getIntent());
        }
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("On Resume Thread : " + Thread.currentThread().getName().toString());
        mNfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, tagFilters, null);
        // Check to see that the Activity started due to an Android Beam
        System.out.println("intent :" + getIntent().getAction().toString());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
            setIntent(originIntent);
        }
    }

    private byte[] createJSONObject() {
        object = new JSONObject();
        try {
            object.put("StoreName", "7-11");
            object.put("Dateline", "104年01-02月");
            object.put("InvoiceNum", "AA-87654321");
            object.put("CurrentTime", "2015-05-01 00:00:00");
            object.put("StoreNum", "賣方87654321");
            object.put("StorePhone", "店號:000000-機01-序00000000");
            object.put("GoodsList", "熱巧克    45*    1    45T\n" +
                                    "紅豆麵包    30*    1    30T");
            object.put("TotalMoney", "2項    合計75");
            object.put("PayDetail", "現金    $100找零    $25");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String base64String = null;
        try {
            base64String = Base64.encodeToString(object.toString().getBytes("UTF-8"), Base64.DEFAULT);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        byte[] base64Byte = Base64.decode(base64String, Base64.DEFAULT);

        return base64Byte;
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        // record 0 contains the MIME type, record 1 is the AAR, if present
        textView.setText(new String(msg.getRecords()[0].getPayload()));
        Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        System.out.println("Finish Pushed");
        ndefMessage = null;
    }
}
