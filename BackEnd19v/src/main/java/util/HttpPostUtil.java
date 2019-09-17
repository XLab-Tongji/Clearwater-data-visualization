package util;

import okhttp3.*;


public class HttpPostUtil {

    public static String postData(String con1) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json,application/json");
        RequestBody body = RequestBody.create(mediaType, con1);
        Request request = new Request.Builder()
                .url("http://10.60.38.173:10081")
                .post(body)
                .addHeader("Content-Type", "application/json,application/json")
                .addHeader("Accept", "*/*")
                .addHeader("Cache-Control", "no-cache")
                .addHeader("Host", "10.60.38.173:10081")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        try {
            Response response = client.newCall(request).execute();
            System.out.println(response);
            if (response.code() == 200){
                return response.body().string();
            }
            else return null;
        }catch (Exception e){
            System.out.println(e);
            return null;
        }
    }

    public static void main(String[] args) {

    }
}