package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TablesActivity extends AppCompatActivity
    //easier OnClickListener and OnLongClickListener for each button
        implements View.OnClickListener, View.OnLongClickListener {
    private static SQLiteDatabase myDB;
    private final String getInfo = "http://mad.mywork.gr/get_coffee_data.php?t=429822";
    private static final Button[] coffeeTables = new Button[9];
    private Document shopInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tables);

        //prevent screen rotation and activity restart
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //loop around buttons, assign ID, and set onclick and onlongclick listeners
        for(int i=0; i<coffeeTables.length; i++) {
                String buttonID = "button" + (i+1);
                int resID = getResources().getIdentifier(buttonID, "id", getPackageName());
                coffeeTables[i] = findViewById(resID);
                coffeeTables[i].setOnClickListener(this);
                coffeeTables[i].setOnLongClickListener(this);
        }

        //create db if it doesn't exist
        myDB = this.openOrCreateDatabase("CoffeeShop", MODE_PRIVATE, null);

        //initialize tables or delete old values
        initializeDB();

        //asynctask method for contacting server
        TablesActivity.contactCTower contact = new contactCTower();
            contact.execute(getInfo);
    }

    private void initializeDB() {
        /* Create Tables table */
        myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                "tables " +
                "(table_id integer primary key, " +
                "table_status integer)");

        /* Create Products table */
        myDB.execSQL("CREATE TABLE IF NOT EXISTS " +
                "products " +
                "(product_id integer primary key, " +
                "product_title text not null, " +
                "product_price real)");
        /* Delete old data */
        myDB.delete("tables", null, null);
        myDB.delete("products", null, null);
    }

    private class contactCTower extends AsyncTask<String, Void, String> {
        //connect to CTower
        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            String response = "";
            try {
                shopInfo = Jsoup.connect(url).get();
                Element statusShop = shopInfo.select("status").first();
                response = statusShop.text();
            } catch (Exception e) {
                String statusError = "Error : " + e.getMessage() + "\n";
                Toast.makeText(TablesActivity.this, statusError, Toast.LENGTH_SHORT).show();
            }
            return response;
        }

        //check CTower response
        @Override
        protected void onPostExecute(String s) {
            if (s.equals("3-OK"))
                saveData();
            else {
                Toast.makeText(TablesActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                Intent toLoginActivity = new Intent(TablesActivity.this, LoginActivity.class);
                startActivity(toLoginActivity);
                }
        }
    }

    private void saveData() { //save data from CTower
        Elements allTables = shopInfo.select("tables > id");
        Elements tableStatus = shopInfo.select("tables > status");

        //update button colors and numbers first
        for (int i=0; i<allTables.size(); i++){
            int id = Integer.parseInt(allTables.get(i).text());
            int status = Integer.parseInt(tableStatus.get(i).text());
            coffeeTables[i].setText(Integer.toString(id));
            if (status == 0)
                coffeeTables[i].setBackground(new PaintDrawable(Color.parseColor("#FF8BC34A")));
            else
                coffeeTables[i].setBackground(new PaintDrawable(Color.RED));
            }

        //save table data from CTower to TABLES db
        for (int i=0; i<allTables.size(); i++){
            ContentValues cv1 = new ContentValues();
            int id = Integer.parseInt(allTables.get(i).text());
            int status = Integer.parseInt(tableStatus.get(i).text());
            cv1.put("table_id", id);
            cv1.put("table_status", status);
            myDB.insert("tables", null, cv1);
        }

        //save products data from CTower to PRODUCTS db
        Elements productIDs = shopInfo.select("product > id");
        Elements productTitles = shopInfo.select("product > title");
        Elements productPrices = shopInfo.select("product > price");
        for (int i=0; i<productIDs.size(); i++){
            ContentValues cv2 = new ContentValues();
            cv2.put("product_id", Integer.parseInt(productIDs.get(i).text()));
            cv2.put("product_title", productTitles.get(i).text());
            cv2.put("product_price", Double.parseDouble(productPrices.get(i).text()));
            myDB.insert("products", null, cv2);
        }
    }

    @Override
    public void onClick(View v) { //find the button that user clicks
        String tableID = "";
        for (Button coffeeTable : coffeeTables) {
            if (coffeeTable.getId() == v.getId()) {
                tableID = coffeeTable.getText().toString();
                break;
            }
        }

        //open OrderActivity
        Intent toOrder = new Intent(TablesActivity.this, OrderActivity.class);
        toOrder.putExtra("tableNum", tableID);
        startActivity(toOrder);
    }

    @Override
    public boolean onLongClick(View v) { //find the button that user long clicks
        String tableID = "";
        int color = 0;
        for (Button coffeeTable : coffeeTables) {
            if (coffeeTable.getId() == v.getId()) {
                tableID = coffeeTable.getText().toString();
                color = ((PaintDrawable) coffeeTable.getBackground()).getPaint().getColor();
                break;
            }
        }

        //open PaymentActivity if table has order
        if (color == -65536) {
        Intent toPayment = new Intent(TablesActivity.this, PaymentActivity.class);
        toPayment.putExtra("tableNum", tableID);
        startActivity(toPayment);
        }
        return true;
    }

    //update database and button color if table status changed
    public static void updateTableStatus(int tabNum, int stat){
        if (stat == 1) { //change to red after placing first order
            for (int i = 0; i < coffeeTables.length; i++) {
                int num = Integer.parseInt(coffeeTables[i].getText().toString());
                if (num == tabNum)
                    coffeeTables[i].setBackground(new PaintDrawable(Color.RED));
            }

        }else if (stat == 0){ //change to green after full payment
            for (int i = 0; i < coffeeTables.length; i++) {
                int num = Integer.parseInt(coffeeTables[i].getText().toString());
                if (num == tabNum)
                    coffeeTables[i].setBackground(new PaintDrawable(Color.parseColor("#FF8BC34A")));
            }
        }

        //save status change on database
        String sqlStatement = "UPDATE tables SET table_status = "+stat+" WHERE table_id = \""+ tabNum +"\"";
        myDB.execSQL(sqlStatement);
    }
}