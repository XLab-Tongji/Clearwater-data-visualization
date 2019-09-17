package service;

import neo4j.MongoDriver;
<<<<<<< Updated upstream
import neo4j.Neo4jDriver;
import org.apache.commons.fileupload.disk.DiskFileItem;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.yaml.snakeyaml.Yaml;
import util.TimerUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static neo4j.FusekiDriver.*;
import static neo4j.Neo4jDriver.*;
import static neo4j.prometheusDriver.DealPrometheusRequest;
import static neo4j.prometheusDriver.newPrometheus;
=======

import static neo4j.FusekiDriver.*;

>>>>>>> Stashed changes


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
}
