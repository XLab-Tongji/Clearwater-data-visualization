package service;

import com.alibaba.fastjson.*;
import neo4j.MongoDriver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static neo4j.FusekiDriver.*;
import static neo4j.MongoDriver.getAllTypeAndName;

public class MongoService {
    public Boolean storeAllService(String masterName, String podName,  String serviceName,  String address,  String namespace){
        boolean result = true;
        MongoDriver mongoDriver = new MongoDriver();
        result &= mongoDriver.clear_db();
        result &= storeNamespaceName();
        result &= storeServerName();
        result &= storeMasterNode(masterName);
        result &= storeEnvironment(address);
        result &= storePodName(podName);
        result &= storeEnvRelation(address);
        result &= storeServiceName(serviceName);
        result &= podToService(address, namespace);
        result &= podToServer(address, namespace);
        return result;
    }

    public static Map jsonarray2Map(){
        Map re = new HashMap();
        ArrayList opts = new ArrayList();
        ArrayList types = new ArrayList();
        JSONArray jsonArray = getAllTypeAndName();
        for (int i = 0; i < jsonArray.size(); i++) {
            String typename = jsonArray.getJSONObject(i).getString("type");
            List<String> list = JSONObject.parseArray(jsonArray.getJSONObject(i).getString("name"), String.class);
            List children = new ArrayList();
            for (String j:list
                 ) {
                HashMap<String,String> child = new HashMap<>();
                child.put("value",j);
                child.put("label",j);
                children.add(child);
            }
            HashMap op = new HashMap();
            op.put("value",typename);
            op.put("label",typename);
            op.put("children",children);
            opts.add(op);
            HashMap type = new HashMap();
            type.put("value",typename);
            type.put("label",typename);
            types.add(type);
        }
        re.put("options",opts);
        re.put("types",types);
        return re;
    }

    public static void main(String[] args) {
        System.out.println(jsonarray2Map());
    }
}
