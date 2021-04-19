package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class MainActivity extends AppCompatActivity {
    private TextView resultTextView;
    private String statusString, msgString, savedToken;
    private String urlAuthenticate = "http://mad.mywork.gr/authenticate.php?t=429822";
    public static final String userToken = "userToken";
    public static final String sharedPrefs = "sharedPreferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultTextView = findViewById(R.id.result);

        /* SharedPreferences sharedPrefMAD = getSharedPreferences(sharedPrefs, MODE_PRIVATE);
        savedToken = sharedPrefMAD.getString(userToken, null);

        if (savedToken != null) {
            String urlWithToken = urlAuthenticate.replaceAll("XYZ", savedToken);
            urlAuthenticate = urlWithToken;
        } */
        goToAuthenticate();

    }
    private void goToAuthenticate() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(urlAuthenticate).get();
                    Elements statusDoc = doc.getElementsByTag("status") ;
                    statusString = statusDoc.text();
                    Elements msgDoc = doc.getElementsByTag("msg") ;
                    msgString = msgDoc.text();
                } catch (Exception e) {
                    msgString = "Error : " + e.getMessage() + "\n";
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultTextView.setText(msgString);
                        if (statusString.equals("0-FAIL")) {
                            startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        }
                        else {
                            Intent toMenuActivity = new Intent(MainActivity.this, MenuActivity.class);
                            toMenuActivity.putExtra("helloMsg", msgString);
                            startActivity(toMenuActivity);
                        }
                    }
                });
            }
        }).start();
    }
}