package com.example.scott.androidbeam;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by Administrator on 2015/3/25.
 */
public class MessageActivity extends Activity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        textView = (TextView) findViewById(R.id.textView);
        //取的intent中的bundle物件
        Bundle bundle =this.getIntent().getExtras();

        String message = bundle.getString("message");
        textView.setText(message);

    }
}
