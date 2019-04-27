package neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csvreader.CsvReader;
import neo4jentities.DataAccessor;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import util.CommonUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static org.neo4j.driver.v1.Values.parameters;

@Component
public class FusekiDriver {

    // get address from uri
    public static String getAddress(String address) {
        URL url = null;
        HttpURLConnection httpConn = null;
        BufferedReader in = null;

        StringBuffer sb = new StringBuffer();
        try {
            url = new URL(address);
            in = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));
            String str = null;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (Exception ex) {
        } finally {
            try {
                if (in != null) {

                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        String data = sb.toString();
        return data;
    }

    // delete all on remote server
    public static boolean delete() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/update");

        //update string
        String updateString = "DELETE WHERE { ?s ?p ?o .}";

        try (RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki) builder.build()) {
            connAddRelation.update(updateString);
        }
        return true;
    }


    public static void main(String[] args) {
        delete();
    }
}
