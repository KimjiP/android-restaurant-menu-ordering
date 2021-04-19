package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MenuActivity extends AppCompatActivity {

    private String loginHello;
    private TextView helloUserTxt;
    private Button jukebox;
    private Button shop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        helloUserTxt = findViewById(R.id.txtMenu);
        jukebox = findViewById(R.id.jukeboxBtn);
        loginHello = getIntent().getStringExtra("helloMsg");
        helloUserTxt.setText(loginHello);
        jukebox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toJukeboxActivity = new Intent(MenuActivity.this, JukeboxActivity.class);
                startActivity(toJukeboxActivity);
            }
        });
        shop = findViewById(R.id.shopBtn);
        shop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toCoffeeShop = new Intent(MenuActivity.this, TablesActivity.class);
                startActivity(toCoffeeShop);
            }
        });
    }
}
