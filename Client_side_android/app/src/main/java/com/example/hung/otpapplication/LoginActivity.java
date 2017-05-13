package com.example.hung.otpapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hung on 5/1/17.
 */

public class LoginActivity extends Activity {
    EditText et_account,et_seed,et_pasword;
    Button bt_active;
    AlertDialogManager alert = new AlertDialogManager();
    public String account,seed,password;
    SessionManagement session;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://192.168.0.156:8080");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSocket.connect();
        mSocket.on("result",onResult);
        session = new SessionManagement(getApplicationContext());
        et_account = (EditText)findViewById(R.id.et_account);
        et_pasword = (EditText)findViewById(R.id.et_password);
        et_seed = (EditText)findViewById(R.id.et_seed);

        bt_active = (Button)findViewById(R.id.bt_active);
        bt_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                account = et_account.getText().toString();
                seed = et_seed.getText().toString();
                password = et_pasword.getText().toString();
                if(account.trim().length() > 0 && seed.trim().length() > 0 && password.trim().length() >0){

                    JSONObject user = new JSONObject();
                    try {
                        user.put("username",account.trim());
                        user.put("password",password.trim());
                        user.put("seed",seed.trim());
                        mSocket.emit("submit user", user);

                    } catch (JSONException e) {
                        Log.d("SEND MESSAGE","ERROR");
                        e.printStackTrace();
                    }
                    finish();
                }else{
                    alert.showAlertDialog(LoginActivity.this, "Login failed..", "Please enter account and seed", false);
                }

            }
        });
    }
    private Emitter.Listener onResult = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String result;
                    try {
                        result = data.getString("result");
                        System.out.println(result);
                        if(result.equals("True")){
                            session.createLoginSession(account,seed);
                            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                            startActivity(intent);
                            finish();
                        }else {
                            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                            startActivity(intent);
                            Toast.makeText(getApplicationContext(), "Thong tin tai khoan sai", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }catch (JSONException e){
                        return;
                    }
                }
            });
        }
    };
}
