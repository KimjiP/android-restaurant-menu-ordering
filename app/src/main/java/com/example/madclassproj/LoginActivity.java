package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class LoginActivity extends AppCompatActivity {
    private TextView resultAuth, loginText;// = findViewById(R.id.textAlert);
    private String urlEmail = "http://mad.mywork.gr/generate_token.php?e=your_email";
    private String urlForToken, statusStringAuth, msgStringAuth, token;
    private EditText email;
    private Button submit;
    //public static final String userToken = "userToken";
    //public static final String sharedPrefs = "sharedPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        resultAuth = findViewById(R.id.textAlert);
        loginText = findViewById(R.id.loginText);
        email = findViewById(R.id.editEmail);
        submit = findViewById(R.id.buttonSubmit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (email.getText().toString().trim().length() == 0) {
                    Toast.makeText(LoginActivity.this, "You did not enter an e-mail", Toast.LENGTH_SHORT).show();
                }
                else {
                    urlForToken = urlEmail.replaceAll("your_email", email.getText().toString());
                    getToken();
                }
            }
        });
    }

    private void getToken() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document docToken = Jsoup.connect(urlForToken).get();
                    Elements statusAuth = docToken.getElementsByTag("status");
                    statusStringAuth = statusAuth.text();
                    Elements statusMsg = docToken.getElementsByTag("msg");
                    msgStringAuth = statusMsg.text();
                } catch (Exception e) {
                    msgStringAuth = "Error : " + e.getMessage() + "\n";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultAuth.setText(msgStringAuth);
                        token = msgStringAuth.replaceAll("[^0-9]", "");
                        if (statusStringAuth.equals("1-FAIL")) {
                            Toast.makeText(LoginActivity.this, "Authentication Failed", Toast.LENGTH_SHORT).show();
                        } else {
                            msgStringAuth = msgStringAuth + "\n Please terminate app and relaunch.";
                            resultAuth.setText(msgStringAuth);
                            Toast.makeText(LoginActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                            //SharedPreferences sharedPrefMAD = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
                            //SharedPreferences.Editor prefsEditor = sharedPrefMAD.edit();
                            //prefsEditor.putString(userToken, token);
                            //prefsEditor.commit();
                            email.setVisibility(View.INVISIBLE);
                            submit.setVisibility(View.INVISIBLE);
                            loginText.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        }).start();
    }
}