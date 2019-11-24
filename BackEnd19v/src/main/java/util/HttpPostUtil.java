package util;

import okhttp3.*;
import web.FileController;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;


public class HttpPostUtil {

    public static String postData(String con1) {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json,application/json");
        RequestBody body = RequestBody.create(mediaType, con1);
        Request request = new Request.Builder()
                .url("http://10.60.38.173:10081")
                //.url("http://192.168.31.205:8080")
                .post(body)
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

    public static String getImage(String urlString) throws Exception {
        //new一个URL对象
        URL url = new URL(urlString);
        //打开链接
        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        //设置请求方式为"GET"
        conn.setRequestMethod("GET");
        //超时响应时间为5秒
        conn.setConnectTimeout(5 * 1000);
        //通过输入流获取图片数据
        InputStream inStream = conn.getInputStream();
        //得到图片的二进制数据，以二进制封装得到数据，具有通用性
        byte[] data = readInputStream(inStream);
        //new一个文件对象用来保存图片，默认保存当前工程根目录
        String savePath = FileController.class.getResource("/").getPath().replace("classes","hierarchy");
        File folder = new File(savePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        File imageFile = new File(savePath + "hierarchy" + new Date().toString() + ".png");
        //判断路径是否存在，如果不存在就创建一个
        if(!imageFile.exists()){
            imageFile.createNewFile();
        }
        //创建输出流
        FileOutputStream outStream = new FileOutputStream(imageFile);
        //写入数据
        outStream.write(data);
        //关闭输出流
        outStream.close();

        return imageFile.getName();
    }

    public static byte[] readInputStream(InputStream inStream) throws Exception{
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        //创建一个Buffer字符串
        byte[] buffer = new byte[1024];
        //每次读取的字符串长度，如果为-1，代表全部读取完毕
        int len = 0;
        //使用一个输入流从buffer里把数据读取出来
        while( (len=inStream.read(buffer)) != -1 ){
            //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
            outStream.write(buffer, 0, len);
        }
        //关闭输入流
        inStream.close();
        //把outStream里的数据写入内存
        return outStream.toByteArray();
    }



    public static void main(String[] args) {
        try {
            getImage("https://www.baidu.com/img/bd_logo1.png?where=super");
        }catch (Exception e){
            System.out.println(e);
        }
    }
}