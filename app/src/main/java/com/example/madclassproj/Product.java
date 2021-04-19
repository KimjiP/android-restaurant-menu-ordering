package com.example.madclassproj;

//new attribute is added to store the quantity purchased
public class Product {
    private  int id;
    private String title;
    private float price;
    private int quantity;

    public Product() {
        id = 0;
        title = "Not Set";
        price = 0.0F;
        quantity = 0;
    }

    public Product(int i, String t, float p, int q) {
        id = i;
        title = t;
        price = p;
        quantity = q;
    }

    public void setId(int v) { id = v; }
    public void setTitle(String v) { title = v; }
    public void setPrice(float v) { price = v; }
    public void setQuantity(int v) { quantity = v; }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public float getPrice() { return price; }
    public float getQuantity() { return quantity; }

}
