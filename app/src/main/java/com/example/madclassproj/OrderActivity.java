package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class OrderActivity extends AppCompatActivity {

    private SQLiteDatabase myDB;
    private TextView tableNum;
    private static TextView tv_totalCost;
    private ImageView sendOrder;
    private static HashMap<Integer, Integer> orders = new HashMap<>();
    private Document responseCT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //prevent screen rotation and activity restart
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        tableNum = findViewById(R.id.tv_tableID);
        RecyclerView mRecyclerView = findViewById(R.id.order_recycler);
        tv_totalCost = findViewById(R.id.tv_total_price);
        sendOrder = findViewById(R.id.btnSendOrder);

        //get table number from TablesActivity
        Bundle b = getIntent().getExtras();
        String tableID = b.getString("tableNum");
        tableNum.setText(tableID);

        //initialize then read database
        myDB = this.openOrCreateDatabase("CoffeeShop", MODE_PRIVATE, null);
        LinkedList<Product> ProductList = new LinkedList<Product>();
        Cursor c = myDB.rawQuery("SELECT * FROM products", null);
        try {
            while (c.moveToNext()) {
                int productID = c.getInt(c.getColumnIndex("product_id"));
                String productTitle = c.getString(c.getColumnIndex("product_title"));
                Float productPrice = c.getFloat(c.getColumnIndex("product_price"));
                ProductList.add( new Product(productID, productTitle, productPrice, 0));
            }
        } finally {
            c.close();
        }

        //initialize adapter
        OrderListAdapter mAdapter = new OrderListAdapter(this, ProductList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //construct URL string when send order button is clicked
        sendOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                constructURL();
            }
        });
    }

    //orders are saved in a HashMap for easier access
    //use product ID as key and order quantity as value
    private void constructURL() {

        //construct URL
        if (!orders.isEmpty()) {
            String orderingTable = tableNum.getText().toString();
            String toConcatenate = "";
            Iterator<Map.Entry<Integer, Integer>> i = orders.entrySet().iterator();

            //iterate around the HashMap to construct URL
            while (i.hasNext()) {
                Map.Entry<Integer,Integer> pairs = i.next();
                int k = pairs.getKey();
                int v =  pairs.getValue();

                //user may click minus resulting to 0 quantities
                if (v != 0)
                    toConcatenate += k + "," + v;
                //do not add ";" if it is the last order
                if (i.hasNext())
                    toConcatenate += ";";
            }

            String url = "http://mad.mywork.gr/send_order.php?t=429822&tid="
                    + orderingTable + "&oc=" + toConcatenate;

            if (toConcatenate != "") { //make sure orders are added to URL
                sendCTower s = new sendCTower();
                    s.execute(url); //asynctask to send order to CTower
            }
        }
    }

    //send orders to CTower
    private class sendCTower extends AsyncTask<String, Void, String[]> {
        @Override
        protected String[] doInBackground(String... strings) {
            String url = strings[0];
            String[] stringResponse = {"", ""};
            try {
                responseCT = Jsoup.connect(url).get();
                Elements getStatus = responseCT.getElementsByTag("status");
                stringResponse[0] = getStatus.text();
                Elements getMessage = responseCT.getElementsByTag("msg");
                stringResponse[1] = getMessage.text();
            } catch (Exception e){
                String error = "Error: " + e.getMessage();
                Toast.makeText(OrderActivity.this, error, Toast.LENGTH_SHORT).show();
            }
            return stringResponse;
        }

        @Override
        protected void onPostExecute(String[] s) {

            //destroy OrderActivity when order is successfully send
            if (s[0].equals("4-OK")) {
                Elements getTableID = responseCT.getElementsByTag("table_id");
                Toast.makeText(OrderActivity.this, "Order sent!", Toast.LENGTH_SHORT).show();
                int tabNum = Integer.parseInt(getTableID.text());

                //update table status and color in TablesActivity
                TablesActivity.updateTableStatus(tabNum, 1);
                finish();
            }
            else
                Toast.makeText(OrderActivity.this, s[1], Toast.LENGTH_SHORT).show();
        }
    }

    //update total cost displayed when plus sign is clicked
    //method for click detection can be found in OrderListAdapter.java
    public static void addTotal(float coffeePrice, int elementID, int elementQty){
        float totalCost = Float.parseFloat(tv_totalCost.getText().toString());
        totalCost = totalCost + coffeePrice;
        tv_totalCost.setText(Float.toString(totalCost));

        //also update HashMap
        orders.put(elementID, elementQty);
    }

    //update total cost displayed when minus sign is clicked
    //method for click detection can be found in OrderListAdapter.java
    public static void subtractTotal(float coffeePrice, int elementID, int elementQty) {
        float totalCost = Float.parseFloat(tv_totalCost.getText().toString());
        totalCost = totalCost - coffeePrice;
        tv_totalCost.setText(Float.toString(totalCost));

        //also update HashMap
        orders.put(elementID, elementQty);
    }

    //resets HashMap values when activity is reset
    protected void onStop() {
        super.onStop();
        orders.clear();
    }

}