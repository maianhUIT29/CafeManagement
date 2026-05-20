package com.example.cafemanagement.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class PriceFormatter {
    public static String formatPrice(double price) {
        // Cấu hình dấu phân cách hàng nghìn là dấu chấm, dấu thập phân là dấu phẩy
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        // Định dạng #,##0.## ép buộc hệ thống tự động chèn dấu chấm mỗi 3 chữ số
        DecimalFormat formatter = new DecimalFormat("#,##0.##", symbols);

        return formatter.format(price) + "đ";
    }
}