package com.example.hung.otpapplication;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.HashMap;

import static com.example.hung.otpapplication.TOTP.generateTOTP;

public class MainActivity extends Activity {
    TextView tv_account,tv_result;
    Button bt_reactived,bt_generate, bt_shutdow;
    SessionManagement session;
    public static String seed;
    public static String account;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManagement(getApplicationContext());
        tv_account = (TextView)findViewById(R.id.tv_account);
        tv_result = (TextView)findViewById(R.id.tv_result);
        bt_reactived = (Button)findViewById(R.id.bt_reactived);
        bt_generate = (Button)findViewById(R.id.bt_generate);
        bt_shutdow = (Button)findViewById(R.id.bt_shutdow);

        session.checkLogin();

        HashMap<String, String> user = session.getUserDetails();

        account = user.get(SessionManagement.KEY_SEED);

        seed = user.get(SessionManagement.KEY_ACC);
//
        tv_account.setText("Bank account: "+account);

        bt_generate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                tv_result.setText(generate(seed));
            }
        });

        bt_reactived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                session.logoutUser();
            }
        });

        bt_shutdow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                System.exit(1);
            }
        });
    }

    protected String generate(String seed){

        long T0=0;
        long X=30;
        long T = (System.currentTimeMillis()/1000-T0)/X;

        String steps = Long.toHexString(T).toUpperCase();
        return generateTOTP(seed,steps,"6","HMACSHA1");
    }
}
