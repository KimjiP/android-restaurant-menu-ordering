package com.example.madclassproj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import java.util.LinkedList;

import static java.lang.Integer.parseInt;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListAdapter.OrderListViewHolder> {
    Context ct;
    LayoutInflater mInflater;
    LinkedList<Product> mProductList;

    public static class OrderListViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View item_layout;
        public TextView tv_product_id, tv_product_title, tv_product_price, tv_ProductQty;
        public ImageButton btn_inc, btn_dec;

        public OrderListViewHolder(View v , OrderListAdapter adapter) {
            super(v);
            item_layout = v;
            tv_product_id = v.findViewById(R.id.tv_product_id);
            tv_product_title = v.findViewById(R.id.tv_product_name);
            tv_product_price = v.findViewById(R.id.tv_price);
            tv_ProductQty = v.findViewById(R.id.tv_product_qty);
            btn_inc = v.findViewById(R.id.btn_inc);
            btn_dec = v.findViewById(R.id.btn_dec);
        }
    }

    public OrderListAdapter(Context context, LinkedList<Product> ProductList) {
        ct = context;
        mInflater = LayoutInflater.from(context);
        this.mProductList = ProductList;
    }

    @Override
    public OrderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create view from layout
        View mItemView = mInflater.from(ct).inflate(R.layout.order_layout, parent, false);
        return new OrderListViewHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(final OrderListViewHolder holder, final int position) {
        // Retrieve the data for that position
        Product mCurrent = mProductList.get(position);
        // Add the data to the view
        holder.tv_product_id.setText(String.valueOf(mCurrent.getId()));
        holder.tv_product_title.setText(mCurrent.getTitle());
        holder.tv_product_price.setText(String.format("%.2f", mCurrent.getPrice()));

        //updates HashMap in OrderActivity when plus sign is clicked
        //also updates displayed total cost
        holder.btn_inc.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    int upd_qty = parseInt(holder.tv_ProductQty.getText().toString()) + 1;
                    holder.tv_ProductQty.setText(String.valueOf(upd_qty));
                    float getProductPrice = Float.parseFloat(holder.tv_product_price.getText().toString());
                    int prodID = parseInt(holder.tv_product_id.getText().toString());
                    OrderActivity.addTotal(getProductPrice, prodID, upd_qty);
                }
        });

        //updates HashMap in OrderActivity when minus sign is clicked
        //also updates displayed total cost
        holder.btn_dec.setOnClickListener(new View.OnClickListener(){
            //new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int upd_qty = parseInt(holder.tv_ProductQty.getText().toString()) - 1;
                if (upd_qty >= 0) {
                    holder.tv_ProductQty.setText(String.valueOf(upd_qty));
                    float getProductPrice = Float.parseFloat(holder.tv_product_price.getText().toString());
                    int prodID = parseInt(holder.tv_product_id.getText().toString());
                    OrderActivity.subtractTotal(getProductPrice, prodID, upd_qty);
                }
            }
        });

        //removes the plus and sign buttons on PaymentActivity
        if (ct.getClass().getSimpleName().equals("PaymentActivity")) {
            holder.btn_inc.setVisibility(View.GONE);
            holder.btn_dec.setVisibility(View.GONE);
            int qty = (int)  mCurrent.getQuantity();
            holder.tv_ProductQty.setText(String.valueOf(qty));
        }
    }

    @Override
    public int getItemCount() {
        // Return the number of data items to display
        return mProductList.size();
    }

    //these two overrides prevent the recyclerview from resetting values when scrolling
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

}
