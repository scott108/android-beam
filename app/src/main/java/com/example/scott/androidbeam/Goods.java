package com.example.scott.androidbeam;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Scott on 15/4/27.
 */
public class Goods {
    private int price;
    private int quantity;
    private boolean isCheck = false;
    private EditText editText;

    public EditText getEditText() {
        return editText;
    }

    public void setEditText(EditText editText) {
        this.editText = editText;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }
}
