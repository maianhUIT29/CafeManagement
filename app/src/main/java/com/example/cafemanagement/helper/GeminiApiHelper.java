package com.example.cafemanagement.helper;

import android.util.Log;

import com.example.cafemanagement.model.AiForecastModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GeminiApiHelper {

    private static final String API_KEY = "AIzaSyAFJCR6O97ZneHi6cIxil3AK4x5APYKFTI";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();
    private final Gson gson = new Gson();

    public interface GeminiCallback {
        void onSuccess(AiForecastModel forecastResult);
        void onError(String errorMessage);
    }

    public void getRevenueForecast(String historicalData, GeminiCallback callback) {

        String prompt = "Vai trò: Chuyên gia phân tích dữ liệu kinh doanh quán cà phê.\n\n" +
                "Dữ liệu đầu vào: Lịch sử doanh thu các ngày qua như sau:\n" + historicalData + "\n\n" +
                "Nhiệm vụ:\n" +
                "1. Phân tích xu hướng tăng giảm từ dữ liệu đầu vào để dự báo mức doanh thu cho 7 ngày tiếp theo.\n" +
                "2. Dựa vào xu hướng vừa phân tích, tự suy luận và đưa ra 1 lời khuyên chiến lược kinh doanh phù hợp (độ dài tối đa 50 chữ).\n\n" +
                "Ràng buộc định dạng đầu ra:\n" +
                "Bắt buộc trả về duy nhất một đối tượng JSON hợp lệ. Tuyệt đối không sử dụng định dạng thẻ Markdown. Tuyệt đối không thêm bất kỳ văn bản giải thích nào trước hoặc sau khối JSON. Cấu trúc JSON phải tuân thủ nghiêm ngặt định dạng sau:\n" +
                "{\n" +
                "  \"predictedRevenue\": [1500000, 1600000, 1700000, 1550000, 1800000, 2000000, 2100000],\n" +
                "  \"businessAdvice\": \"Chuỗi văn bản chứa lời khuyên do bạn tự tạo ra\"\n" +
                "}";

        com.google.gson.JsonObject partObj = new com.google.gson.JsonObject();
        partObj.addProperty("text", prompt);

        com.google.gson.JsonArray partsArray = new com.google.gson.JsonArray();
        partsArray.add(partObj);

        com.google.gson.JsonObject contentObj = new com.google.gson.JsonObject();
        contentObj.add("parts", partsArray);

        com.google.gson.JsonArray contentsArray = new com.google.gson.JsonArray();
        contentsArray.add(contentObj);

        com.google.gson.JsonObject rootObj = new com.google.gson.JsonObject();
        rootObj.add("contents", contentsArray);

        String requestBodyJson = gson.toJson(rootObj);

        RequestBody body = RequestBody.create(requestBodyJson, JSON_MEDIA_TYPE);

        Log.d("GEMINI_API_DEBUG", "Đang gọi đến đường dẫn: " + API_URL);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-goog-api-key", API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError("Lỗi kết nối mạng: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("GEMINI_API_DEBUG", "Máy chủ trả về mã lỗi: " + response.code());
                    callback.onError("Lỗi máy chủ AI: " + response.code());
                    return;
                }

                try {
                    String responseData = response.body().string();
                    Log.d("GEMINI_API_DEBUG", "Dữ liệu thô từ máy chủ: " + responseData);

                    JsonObject jsonObject = new JsonParser().parse(responseData).getAsJsonObject();
                    String aiTextOutput = jsonObject.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();

                    AiForecastModel result = gson.fromJson(aiTextOutput.trim(), AiForecastModel.class);

                    if (result != null && result.getPredictedRevenue() != null) {
                        callback.onSuccess(result);
                    } else {
                        callback.onError("Hệ thống AI trả về dữ liệu không đúng cấu trúc.");
                    }
                } catch (Exception e) {
                    Log.e("GEMINI_API_DEBUG", "Lỗi giải mã: " + e.getMessage());
                    callback.onError("Lỗi giải mã JSON: " + e.getMessage());
                }
            }
        });
    }
}