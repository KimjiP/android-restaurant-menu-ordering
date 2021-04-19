package com.example.madclassproj;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.LinkedList;

public class PaymentActivity extends AppCompatActivity {
    private TextView tableNum, tv_cost, tv_paid, tv_balance;
    private String url;
    private Document responseCT;
    public RecyclerView mRecyclerView;
    LinkedList<Product> OrderList;
    public ArrayList<Integer> orderedQtys;
    private ImageView sendPayment;
    private EditText et_am;
    private String tableBalance, tableID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        //prevent screen rotation and activity restart
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        orderedQtys = new ArrayList<>();
        tableNum = findViewById(R.id.tv_tableIDPay);
        tv_cost = findViewById(R.id.tv_cost);
        tv_paid = findViewById(R.id.tv_paid);
        tv_balance = findViewById(R.id.tv_balance);

        //get table number from TablesActivity
        Bundle b = getIntent().getExtras();
        tableID = b.getString("tableNum");
        tableNum.setText(tableID);

        sendPayment = findViewById(R.id.btnPay);
        et_am = findViewById(R.id.et_am);
        OrderList = new LinkedList<Product>();
        mRecyclerView = findViewById(R.id.order_RecyclerPay);
        url = "http://mad.mywork.gr/get_order.php?t=429822&tid=" + tableID;

        //declare and initialize adapter
        OrderListAdapter mAdapter = new OrderListAdapter(this, OrderList);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        //asynctask to get order details from CTower
        getOrders get = new getOrders();
            get.execute(url);

            //send payment to Ctower when button is clicked
        sendPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendBalance();
            }
        });
    }

    //this method gets balance and order details from CTower
    //it also gets response when payment is sent to CTower
    private class getOrders extends AsyncTask<String, Void, String[]>{
        @Override
        protected String[] doInBackground(String... strings) {
            String url = strings[0];
            String[] ctResponse = {"", ""};

            try {
                responseCT = Jsoup.connect(url).get();
                Elements getStatus = responseCT.getElementsByTag("status");
                ctResponse[0] = getStatus.text();
                Elements getMessage = responseCT.getElementsByTag("msg");
                ctResponse[1] = getMessage.text();
            } catch (Exception e){
                String error = "Error: " + e.getMessage();
                Toast.makeText(PaymentActivity.this, error, Toast.LENGTH_SHORT).show();
            }
            return ctResponse;
        }

        @Override
        protected void onPostExecute(String[] s) {

            //this runs after longclick of a button on TablesActivity
            if (s[0].equals("5-OK")) {

                //displaying order details on the screen
                Elements getCost = responseCT.getElementsByTag("cost");
                String tableCost = getCost.text();
                tv_cost.setText(tableCost);
                Elements getPayment = responseCT.getElementsByTag("payment");
                String tablePayment = getPayment.text();
                tv_paid.setText(tablePayment);
                Elements getBalance = responseCT.getElementsByTag("balance");
                tableBalance = getBalance.text();
                tv_balance.setText(tableBalance);
                et_am.setText(tableBalance);
                displayOrders();
            }

            //if CTower confirms amount is valid
            else if (s[0].equals("6-OK")) {

                //check if table status changed after payment
                int stat=0;
                Elements getNewBal = responseCT.getElementsByTag("new_balance");
                Float newBal = Float.parseFloat(getNewBal.text());
                if (newBal >= 0) {
                    Toast.makeText(PaymentActivity.this, "Payment recorded", Toast.LENGTH_SHORT).show();
                    if (newBal == 0)
                        stat = 0;
                    else
                        stat = 1;

                    //go back to TablesActivity then update table status and db
                    TablesActivity.updateTableStatus(Integer.parseInt(tableID), stat);
                    finish();
                }
            }
            else {
                Toast.makeText(PaymentActivity.this, "Error: " + s[1], Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //displaying ordered items on recyclerview
    private void displayOrders() {
        Elements productIDs = responseCT.select("product > id");
        Elements productTitles = responseCT.select("product > title");
        Elements productPrices = responseCT.select("product > price");
        Elements productQtys = responseCT.select("product > quantity");
        for (int i=0; i<productIDs.size(); i++){
            int productID = Integer.parseInt(productIDs.get(i).text());
            String productTitle = productTitles.get(i).text();
            Float productPrice = Float.parseFloat(productPrices.get(i).text());
            int qty = Integer.parseInt(productQtys.get(i).text());
            OrderList.add( new Product(productID, productTitle, productPrice, qty));
        }
    }

    //sending amount when pay button is clicked
    private void sendBalance() {

        //compare balance and amount entered
        Float floatTemp = Float.parseFloat(et_am.getText().toString());
        Float toSend = Float.parseFloat(String.format("%.2f", floatTemp));
        Float balance = Float.parseFloat(tableBalance);

        //check if amount entered is valid
        //non-numerical values are blocked using activity_payment.xml
        if (Float.compare(toSend, balance) < 0.001 && toSend>0 ) { //toSend <= balance
            String urlPay = "http://mad.mywork.gr/send_payment.php?t=429822&tid="
                + tableID + "&a=" + toSend.toString();

            //send amount to asynctask method
            getOrders getResponse = new getOrders();
                getResponse.execute(urlPay);
        }
        else {
            Toast.makeText(PaymentActivity.this, "Error: invalid amount entered", Toast.LENGTH_SHORT).show();
        }
    }
}