package com.example.ntut.weshare.member;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.ntut.weshare.Common;
import com.example.ntut.weshare.MainActivity;
import com.example.ntut.weshare.R;

import java.util.List;

public class MemberLoginActivity extends AppCompatActivity {
    private final static String TAG = "UserLoginActivity";

    private EditText etAccount;
    private EditText etPassword;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.member_login_fragment);
        findViews();
    }

    private void findViews() {
        etAccount = (EditText) findViewById(R.id.etAccount);
        etPassword = (EditText) findViewById(R.id.etPassword);
    }


    public void onLoginClick(View view) {
        String account = etAccount.getText().toString().trim();
        if (account.length() <= 0) {
            Toast.makeText(this, R.string.msg_accountNull,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String password = etPassword.getText().toString().trim();
        if (password.length() <= 0) {
            Toast.makeText(this, R.string.msg_passwordNull,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (Common.networkConnected(this)) {//傳送到server端
            String url = Common.URL + "UserServlet";
            User user = new User(account, password);//傳送文字資料
            String action = "userLogin";
            //int count = 0;
            String userName = null;
            List<User> users = null;
            try {
                userName = new UserLoginTask().execute(url, action, user).get();
//                users = new UserGetAllTask().execute(url, account).get();
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
            if (userName.equalsIgnoreCase("-1")) {
                Common.showToast(MemberLoginActivity.this, R.string.msg_LoginFailAndCheck);
            } else {
                Common.showToast(MemberLoginActivity.this, R.string.msg_LoginSuccess);
                try {
                    users = new UserGetAllTask().execute(url, account).get();
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
                SharedPreferences pref = getSharedPreferences(Common.PREF_FILE,
                        MODE_PRIVATE);
                pref.edit()
                        .putBoolean("login", true)
                        .putString("user", account)
                        .putString("password", password)
                        .putString("name", users.get(0).getName())
                        .putInt("type", users.get(0).getIdType())
                        .apply();
                setResult(RESULT_OK);

                finish();

                Intent updateIntent = new Intent();
                updateIntent.setClass(MemberLoginActivity.this, MainActivity.class);
                startActivity(updateIntent);
                //finish();
            }
        } else {
            Common.showToast(this, R.string.msg_NoNetwork);
        }

    }
//

    public void onRegisterClick(View view) {
        Intent updateIntent;
        updateIntent = new Intent();
        updateIntent.setClass(MemberLoginActivity.this, MemberRegisterTypeActivity.class);
        startActivity(updateIntent);
    }

    public void onHelpIndClick(View view) {
        etAccount.setText("kt40909");
        etPassword.setText("123456");
    }

    public void onHelpOrgClick(View view) {
        etAccount.setText("jack");
        etPassword.setText("123456");
    }
}