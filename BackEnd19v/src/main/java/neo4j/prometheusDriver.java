package neo4j;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import static neo4j.Neo4jDriver.*;

@Component
public class prometheusDriver {

    public static boolean proTemp(String urlNode) {
//        query=APIServiceOpenAPIAggregationControllerQueue1_adds{instance="192.168.199.191:6443",job="kubernetes-apiservers"}
        String url = "http://10.60.38.181:31004/api/v1/query?query="+urlNode;
        InputStream is = null;
        try {
            is = new URL(url).openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            String jsonText = sb.toString();
            System.out.println(jsonText);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;

    }

    public static boolean newPrometheus(HashMap data){
        String pUrl = (String) data.get("url");
        String nameSpace = (String) data.get("namespace");
        String serviceName = (String) data.get("service");
        System.out.println("url: "+pUrl+", service name: "+serviceName);
        return storePrometheus(pUrl, nameSpace, serviceName);
    }

    public static void main(String[] args) throws IOException {
        String query = "APIServiceOpenAPIAggregationControllerQueue1_adds{instance=\"192.168.199.191:6443\",job=\"kubernetes-apiservers\"}";
        proTemp(query);
    }

}
