package com.example.cafemanagement.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AiForecastModel {

    // Danh sách dự báo doanh thu 7 ngày tiếp theo
    @SerializedName("predictedRevenue")
    private List<Double> predictedRevenue;

    // Lời khuyên kinh doanh bằng văn bản từ AI
    @SerializedName("businessAdvice")
    private String businessAdvice;

    public AiForecastModel() {}

    public List<Double> getPredictedRevenue() {
        return predictedRevenue;
    }

    public void setPredictedRevenue(List<Double> predictedRevenue) {
        this.predictedRevenue = predictedRevenue;
    }

    public String getBusinessAdvice() {
        return businessAdvice;
    }

    public void setBusinessAdvice(String businessAdvice) {
        this.businessAdvice = businessAdvice;
    }
}