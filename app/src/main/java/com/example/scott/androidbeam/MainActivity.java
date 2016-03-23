package com.example.scott.androidbeam;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;

import com.example.scott.androidbeam.rsa.RSA;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;


public class MainActivity extends Activity implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
    private NfcAdapter mNfcAdapter;
    private NdefMessage ndefMessage;
    protected PendingIntent nfcPendingIntent;
    private IntentFilter[] tagFilters;
    private Spinner spinner;
    private ArrayAdapter<String> lunchList;
    private String[] lunch = {"7-ELEVEN", "FamilyMart", "全聯福利中心"};
    private String[] goodsName = {"熱狗", "麥香奶茶", "立頓奶茶", "冰淇淋", "口香糖"};
    private String[] goodsPrice = {"25", "15", "15", "30", "20"};
    private String storeChoice;
    private EditText payEditText;
    private CheckBox checkBox1, checkBox2, checkBox3, checkBox4, checkBox5;
    private EditText editText1, editText2, editText3, editText4, editText5;
    private ArrayList<EditText> editTextsList;
    HashMap<String, Goods> goodsHashMap;
    JSONObject object;
    String domain = "com.example.scott.androidbream";
    String type = "icheedata";
    Intent originIntent;

    //Key generate
    /*KeyPair keyPair = RSA.generateKeyPair();
    final RSAPublicKey publicKey1 = (RSAPublicKey) keyPair.getPublic();
    final BigInteger publicExponent1 = publicKey1.getPublicExponent();
    final BigInteger modulus1 = publicKey1.getModulus();
    RSAPrivateKey privateKey1 = (RSAPrivateKey) keyPair.getPrivate();
    BigInteger privateExponent1 = privateKey1.getPrivateExponent();*/

    final BigInteger modulusCoupon = new BigInteger("107241757999324904109676237233998063372282760155581141342752413692887583048814821221030706707167921452158792708120548149058657917192427214697842032427487630966828799686728050147100820423608156536978307513903790660036654957441997168808342303029956119479061610128933276777366502321051631391540621213726816239821");
    final BigInteger publicExponentCoupon = new BigInteger("65537");
    RSAPublicKey publicKeyCoupon = RSA.getPublicKey(modulusCoupon, publicExponentCoupon);


    final BigInteger modulus = new BigInteger("143854915996257127934881054745501985707406774855370276858724537089683610417734462144878408350385351936928074881758294135729179360855649990564573120807132735483900454934423182320371509048850680540170791226428301729715812898784678966973762034475582944989766417228246806000963630333109717268688718033060706449657");

    final BigInteger publicExponent = new BigInteger("65537");
    final BigInteger privateExponent = new BigInteger("27185301960932672070334343394159666951282983758544957488064809067484497536103900295471551755794781325645882591674572727940032140381726736547938387494031442245291417169190942465705529780352379141236168165150771928861302163385990246421318121822084918398051777257938447118498512912096832071229487004572270401153");

    final RSAPrivateKey privateKey = RSA.getPrivateKey(modulus, privateExponent);
    final RSAPublicKey publicKey = RSA.getPublicKey(modulus, publicExponent);

    Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextsList = new ArrayList<EditText>();

        Button sendJsonBtn = (Button) findViewById(R.id.sendJsonBtn);
        payEditText = (EditText) findViewById(R.id.payEditText);
        goodsHashMap = new HashMap<String, Goods>();

        //System.out.println("publicExponent : " + publicExponent1);
        //System.out.println("modulus : " + modulus1);
        //System.out.println("privateExponent : " + privateExponent1);

        checkBox1 = (CheckBox) findViewById(R.id.checkBox);
        checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkBox3 = (CheckBox) findViewById(R.id.checkBox3);
        checkBox4 = (CheckBox) findViewById(R.id.checkBox4);
        checkBox5 = (CheckBox) findViewById(R.id.checkBox5);
        checkBox1.setOnCheckedChangeListener(chklistener);
        checkBox2.setOnCheckedChangeListener(chklistener);
        checkBox3.setOnCheckedChangeListener(chklistener);
        checkBox4.setOnCheckedChangeListener(chklistener);
        checkBox5.setOnCheckedChangeListener(chklistener);

        editText1 = (EditText) findViewById(R.id.editText2);
        editText2 = (EditText) findViewById(R.id.editText3);
        editText3 = (EditText) findViewById(R.id.editText4);
        editText4 = (EditText) findViewById(R.id.editText5);
        editText5 = (EditText) findViewById(R.id.editText6);

        editTextsList.add(editText1);
        editTextsList.add(editText2);
        editTextsList.add(editText3);
        editTextsList.add(editText4);
        editTextsList.add(editText5);

        gson = new Gson();

        for(int i = 0; i < 5; i++) {
            Goods goods = new Goods();
            goods.setPrice(Integer.valueOf(goodsPrice[i]));
            goods.setQuantity(1);
            goods.setEditText(editTextsList.get(i));
            goodsHashMap.put(goodsName[i], goods);
        }

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


        spinner = (Spinner)findViewById(R.id.mySpinner);
        lunchList = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lunch);
        spinner.setAdapter(lunchList);

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

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,int position, long arg3) {
                storeChoice = lunch[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



    }

    private CheckBox.OnCheckedChangeListener chklistener = new CheckBox.OnCheckedChangeListener(){
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            // TODO Auto-generated method stub
            if(checkBox1.isChecked()){
                goodsHashMap.get("熱狗").setCheck(true);
            } else {
                goodsHashMap.get("熱狗").setCheck(false);
            }
            if(checkBox2.isChecked()){
                goodsHashMap.get("麥香奶茶").setCheck(true);
            } else {
                goodsHashMap.get("麥香奶茶").setCheck(false);
            }
            if(checkBox3.isChecked()){
                goodsHashMap.get("立頓奶茶").setCheck(true);
            } else {
                goodsHashMap.get("立頓奶茶").setCheck(false);
            }
            if(checkBox4.isChecked()){
                goodsHashMap.get("冰淇淋").setCheck(true);
            } else {
                goodsHashMap.get("冰淇淋").setCheck(false);
            }
            if(checkBox5.isChecked()){
                goodsHashMap.get("口香糖").setCheck(true);
            } else {
                goodsHashMap.get("口香糖").setCheck(false);
            }
        }
    };

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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentTime = sdf.format(new Date());
        String invoiceNum = "AA-" + Math.round(Math.random()* 9999) + Math.round(Math.random()* 9999);
        String deadline = "104年04-05月";
        String storePhone = "000000-機01-序00000000";
        String storeNum = Math.round(Math.random()* 9999)+"" + Math.round(Math.random()* 9999) + "";
        try {
            object.put("StoreName", storeChoice);
            object.put("Deadline", deadline);
            object.put("InvoiceNum", invoiceNum);
            object.put("CurrentTime", currentTime);
            object.put("StoreNum", storeNum);
            object.put("StorePhone", storePhone);
            int totalGoodsPrice = 0;
            int totalGoods = 0;
            int k = 0;
            String goodsList = null;
            for(int j = 0; j < 5; j++) {
                for(int i = k; i < 5; i++) {
                    if(goodsHashMap.get(goodsName[i]).isCheck()) {
                        int price = goodsHashMap.get(goodsName[i]).getPrice();
                        int quantity;
                        if(!goodsHashMap.get(goodsName[i]).getEditText().getText().toString().equals("")) {
                            quantity = Integer.valueOf(goodsHashMap.get(goodsName[i]).getEditText().getText().toString());
                        }else {
                            quantity = goodsHashMap.get(goodsName[i]).getQuantity();
                        }
                        int totalPrice = price * quantity;
                        object.put("Goods" + j, goodsName[i] +"," + price + "," + quantity +"," + totalPrice);
                        goodsList += goodsName[i] + price + quantity + totalPrice;
                        k = i + 1;
                        totalGoods++;
                        totalGoodsPrice += totalPrice;
                        break;
                    }
                }
            }
            object.put("GoodsQuantity", totalGoods);
            object.put("TotalMoney", totalGoodsPrice);
            int pay = Integer.valueOf(payEditText.getText().toString());
            int payBack = pay - totalGoodsPrice;
            object.put("PayDetail", pay);
            object.put("PayBack", payBack);

            String m = storeChoice + deadline + invoiceNum + currentTime + storeNum + storePhone + RSA.SHA1(goodsList) + totalGoods + totalGoodsPrice + pay + payBack;

            final byte[] signed = RSA.sign(m, privateKey);
            String signedString = Base64.encodeToString(signed, Base64.DEFAULT);

            object.put("Signature", signedString);
            object.put("GoodsHash", RSA.SHA1(goodsList));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
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

        System.out.println(new String(msg.getRecords()[0].getPayload()));

        CouponItem couponItem = gson.fromJson(new String(msg.getRecords()[0].getPayload()), CouponItem.class);
        verifyCoupon(couponItem);
        // record 0 contains the MIME type, record 1 is the AAR, if present
        Toast.makeText(this, new String(msg.getRecords()[0].getPayload()), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {
        System.out.println("Finish Pushed");
        ndefMessage = null;
    }

    public void verifyCoupon(CouponItem couponItem) {

        String couponContent = couponItem.getCouponID() + couponItem.getCouponName() + couponItem.getCouponContent() + couponItem.getStoreName() +
                couponItem.getCouponBonus() + couponItem.getStartTime() + couponItem.getEndTime();

        byte[] sign = Base64.decode(couponItem.getSignature(), Base64.DEFAULT);

        boolean result = RSA.verify(couponContent, sign, publicKeyCoupon);
        String resultMessage = "";
        if (result) {
            resultMessage = "簽章：" + couponItem.getSignature() + "\n核銷成功!!";
        } else {
            resultMessage = "簽章：" + couponItem.getSignature() + "\n核銷失敗!!";
        }

        new AlertDialog.Builder(MainActivity.this)
                .setTitle("核銷結果")
                .setMessage(resultMessage)
                .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }
}
