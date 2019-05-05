package neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.csvreader.CsvReader;
import com.github.jsonldjava.utils.Obj;
import neo4jentities.DataAccessor;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import util.CommonUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.File;
import java.io.IOException;

import static org.neo4j.driver.v1.Values.parameters;
@Component
public class Neo4jDriver {
    @Cacheable(cacheNames = "query",cacheManager = "cacheManager")
    public HashMap<String, List<HashMap<String,Object>>> getresult(String name1, String name2, int step1, int step2) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        //初始化驱动器
        Map<Long,String> map = new HashMap<>();
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()){
                StatementResult result = tx.run("Match p=(n:" + CommonUtil.getlabel(name1) + "{Name:$name1 })-[r*" + step1 + ".." + step2 + "]-(m:" + CommonUtil.getlabel(name2) + "{Name:$name2}) return p as nodesrelation",
                        parameters("name1",name1,"name2",name2));
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("name",node.get("Name").toString().replace("\"",""));
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("source",relationship.startNodeId());
                        rela.put("target",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        allrelations.add(rela);
                    }
                }
                resultgraph.put("nodes",allnodes);
                resultgraph.put("links",allrelations);

            }
        }
        driver.close();
        System.out.println(resultgraph);
        return resultgraph;

    }
    public HashMap<String, List<HashMap<String,Object>>> getOneNoderesult(String name1, int step1, int step2) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        Map<Long,String> map = new HashMap<>();
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()){
            try (Transaction tx = session.beginTransaction()){
                StatementResult result = tx.run("Match p=(n:" + CommonUtil.getlabel(name1) + "{Name:$name1 })-[r*" + step1 + ".." + step2 + "]-(m) return p as nodesrelation",
                        parameters("name1",name1));
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    System.out.println(1);
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("name",node.get("Name").toString().replace("\"",""));
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("source",relationship.startNodeId());
                        rela.put("target",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        allrelations.add(rela);
                    }

                }
                resultgraph.put("nodes",allnodes);
                resultgraph.put("links",allrelations);

            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getAllNodes() {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        Map<Long,String> map = new HashMap<>();
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("Match p=(n)-[r]-(m) return p as nodesrelation");
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("name",node.get("name").toString().replace("\"",""));
                        nod.put("type",node.get("type").toString().replace("\"",""));
                        nod.put("layer",node.get("layer").toString().replace("\"",""));
                        nod.put("performance",node.get("performance").toString().replace("\"",""));
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("source",relationship.startNodeId());
                        rela.put("target",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        allrelations.add(rela);
                    }
                }
                resultgraph.put("nodes",allnodes);
                resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String,List<String>> getAllLabel(){
        HashMap<String,List<String>> hashMap = new HashMap<>();
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {
            StatementResult statementResult = session.run("MATCH (n) RETURN distinct labels(n)");
            System.out.println(statementResult);
            List<String> list = new ArrayList<>();
            while (statementResult.hasNext()){
                Record record = statementResult.next();
                System.out.println(record.get("labels(n)").toString());
                list.add(record.get("labels(n)").toString().replace("'","").replace("[\"","").replace("\"]",""));
            }
            //System.out.println(relations);
            //for(int i=0; i<relations.size(); i++) {
            //    session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            //}
            result.remove("state");
            result.put("state", "Operation succeeded");
            hashMap.put("Label",list);
        }
        driver.close();
        return hashMap;
    }
    public HashMap<String, Object> addOneNode(String label, int id, String name, String type, String layer, String performance, ArrayList<Integer> relations) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: "+label+") "+
                            "SET n.id = $id "+"SET n.name = $name "+"SET n.type = $type "+"SET n.layer = $layer "+
                            "SET n.performance = $performance "+"return id(n)",
                    parameters("id",id,"name",name,"type",type,"layer",layer,"performance",performance));
            int trueId = newID.single().get(0).asInt();
            System.out.println(relations);
            for(int i=0; i<relations.size(); i++) {
                session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            }
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        driver.close();
        return result;
    }
    public int AddDeploymentNode(String label, String containerPort, String name, String nameSpace, String image) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        int trueId=0;
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: "+label+") "+
                            "SET n.containerPort = $containerPort "+"SET n.name = $name "+"SET n.nameSpace = $nameSpace "+"SET n.image = $image "+ "SET n.type = 'Deployment_Node' "+"SET n.performance=$performance "+
                            " return id(n)",
                    parameters("containerPort",containerPort,"name",name,"nameSpace",nameSpace,"image",image,
                            "performance","name:"+name+";nameSpace:"+nameSpace+";containerPort:"+containerPort+";image:"+image+";type:Deployment_Node"));
            trueId = newID.single().get(0).asInt();
            System.out.println("trueId:"+trueId);
            //System.out.println(relations);
            //for(int i=0; i<relations.size(); i++) {
            //    session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            //}
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        driver.close();
        return trueId;
    }
    public int AddServiceNode(String label, String port, String name, String nameSpace, String targetPort) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        int trueId=0;
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: "+label+") "+
                            "SET n.port = $port "+"SET n.name = $name "+"SET n.nameSpace = $nameSpace "+"SET n.targetPort = $targetPort "+ "SET n.type = 'Service_Node' "+"SET n.performance = $performance "+
                            "return id(n)",
                    parameters("port",port,"name",name,"nameSpace",nameSpace,"targetPort",targetPort,
                            "performance","name:"+name+";port:"+port+";nameSpace:"+nameSpace+";targetPort:"+targetPort+";type:Service_Node"));
            trueId = newID.single().get(0).asInt();
            System.out.println("trueId:"+trueId);
            //System.out.println(relations);
            //for(int i=0; i<relations.size(); i++) {
            //    session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            //}
            result.remove("state");
            result.put("state", "Operation succeeded");

        }
        driver.close();
        return trueId;
    }
    public int AddContainerNode(String label, String name, String volumeMount, ArrayList arrayListAdd, ArrayList arrayListDrop) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        int trueId=0;
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: "+label+") "+
                            "SET n.volumeMount = $volumeMount "+"SET n.name = $name "+"SET n.arrayListAdd = $arrayListAdd "+"SET n.arrayListDrop = $arrayListDrop "+ "SET n.type = 'Container_Node' "+"SET n.performance = $performance "+
                            "return id(n)",
                    parameters("volumeMount",volumeMount,"name",name,"arrayListAdd",arrayListAdd,"arrayListDrop",arrayListDrop,
                    "performance","name:"+name+";volumeMount:"+volumeMount+";arrayListAdd:"+arrayListAdd+";arrayListDrop:"+arrayListDrop+";type:Container_Node"));
            trueId = newID.single().get(0).asInt();
            System.out.println("trueId:"+trueId);
            //System.out.println(relations);
            //for(int i=0; i<relations.size(); i++) {
            //    session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            //}
            result.remove("state");
            result.put("state", "Operation succeeded");

        }
        driver.close();
        return trueId;
    }
    public static Boolean AddDataSetNode(String filePath,String name,String system){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        ArrayList arrayList = csvTimestamp(filePath);
        String from = (String)arrayList.get(0);
        String to = (String)arrayList.get(arrayList.size()-1);
        //System.out.println();
        result.put("state", "Operation failed");
        int trueId=0;
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: Dataset)"+
                            "SET n.path = $path "+"SET n.from = $from "+"SET n.to = $to "+"SET n.name= $name "+"SET n.type = $type "+"SET n.performance = $performance "+
                            "return id(n)",
                    parameters("path",filePath,"from",from,"to",to,"name",system+"/"+name,"type","Dataset",
                            "performance","name:"+system+"/"+name+";path:"+filePath+";from:"+from+";to:"+to+";type:Dataset"));
            trueId = newID.single().get(0).asInt();
            System.out.println("trueId:"+trueId);
            //System.out.println(relations);
            //for(int i=0; i<relations.size(); i++) {
            //    session.run("Start a=node("+trueId+"),b=node("+relations.get(i)+") Merge (a)-[r:rel{type:'Have'}]->(b)");
            //}
            newID = session.run("Match (n: System)"+
                            "Where n.name = $name "+
                            "return id(n)",
                    parameters("name",system));
            int id = newID.single().get(0).asInt();
            session.run("Start a=node("+trueId+"),b=node("+id+") Merge (a)-[r:records{type:'Records'}]->(b)");
            result.remove("state");
            result.put("state", "Operation succeeded");

        }
        driver.close();
        return true;
    }
    public static int AddSystemNode(String name){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        int trueId=0;
        try(Session session = driver.session()) {
            StatementResult newID = session.run("CREATE (n: System)"+
                            "SET n.name= $name "+"SET n.type = $type"+" SET n.performance = $performance"+
                            " return id(n)",
                    parameters("name",name,"type","System","performance","name:"+name+";type:System"));
            trueId = newID.single().get(0).asInt();
            System.out.println("trueId:"+trueId);
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        driver.close();
        return trueId;
    }
    public static HashMap AddMetricNode(String type,String podName,String metricName,String datasetName,String relationName,String nodeName,String nodeType){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        int trueId=0;
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        String name = type+"/"+podName+"/"+metricName;
        String tempName = "";
        Boolean flag = true;
        try(Session session = driver.session()) {
            StatementResult newID = session.run("Match (n: Metric)"+
                            "WHERE n.row = $row AND n.dataset = $dataset "+
                            "return n",
                    parameters("row",name,"dataset",datasetName));

            if (!newID.hasNext()) {
                newID = session.run("CREATE (n: Metric)" +
                                "SET n.name = $name " +"SET n.type = $type "+"SET n.dataset = $dataset "+ "SET n.row = $row "+"SET n.performance = $performance "+
                                "return id(n)",
                        parameters("name", relationName,"type","Metric","dataset",datasetName,"row",name,
                                "performance","name:"+relationName+";type:Metric;dataset:"+datasetName+";row:"+name));
                trueId = newID.single().get(0).asInt();
                System.out.println("trueId:" + trueId);
                flag = PodToMetric(nodeType,trueId, nodeName);
                if (!flag) {
                    driver.close();
                    result.remove("state");
                    result.put("state", "podName not found");
                    return result;
                }
                flag = MetricToDataset(trueId, datasetName, name);
                if (!flag) {
                    driver.close();
                    result.remove("state");
                    result.put("state", "Dataset not found");
                    return result;
                }
                result.remove("state");
                result.put("state", "Operation succeeded");
            }else{
                //driver.close();
                result.remove("state");
                result.put("state", "metric already exist");
                return result;
            }
        }
        driver.close();
        return result;
    }
    public static Boolean PodToMetric(String type,int ID,String podName){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        int trueId=0;
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        Boolean flag = true;
        try(Session session = driver.session()) {
            if (type.equals("Service_Node")) {
                StatementResult result0 = session.run("Match (n:Service_Node) where n.name = $name return ID(n)"
                        , parameters("name", podName));
                if (!result0.hasNext()) {
                    return false;
                }
                while (result0.hasNext()) {
                    Record record = result0.next();
                    System.out.println(record);
                    int curID = record.get(0).asInt();
                    System.out.println(curID);
                    session.run("Start a=node(" + curID + "),b=node(" + ID + ") Merge (a)-[r:indicator{type:'indicator'}]->(b)");
                }
            }
            if (type.equals("Container_Node")) {
                StatementResult result0 = session.run("Match (n:Container_Node) where n.name = $name return ID(n)"
                        , parameters("name", podName));
                if (!result0.hasNext()) {
                    return false;
                }
                while (result0.hasNext()) {
                    Record record = result0.next();
                    System.out.println(record);
                    int curID = record.get(0).asInt();
                    System.out.println(curID);
                    session.run("Start a=node(" + curID + "),b=node(" + ID + ") Merge (a)-[r:indicator{type:'indicator'}]->(b)");
                }
            }
            if (type.equals("Deployment_Node")) {
                StatementResult result0 = session.run("Match (n:Deployment_Node) where n.name = $name return ID(n)"
                        , parameters("name", podName));
                if (!result0.hasNext()) {
                    return false;
                }
                while (result0.hasNext()) {
                    Record record = result0.next();
                    System.out.println(record);
                    int curID = record.get(0).asInt();
                    System.out.println(curID);
                    session.run("Start a=node(" + curID + "),b=node(" + ID + ") Merge (a)-[r:indicator{type:'indicator'}]->(b)");
                }
            }

        }
        return true;
    }
    public static Boolean MetricToDataset(int ID,String dataBaseName,String resultName){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        int trueId=0;
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        Boolean flag = true;
        try(Session session = driver.session()) {

                StatementResult result0 = session.run("Match (n:Dataset) where n.name = $name return ID(n)"
                        ,parameters("name",dataBaseName));
                if (!result0.hasNext()){
                    return false;
                }
                while (result0.hasNext()){
                    Record record = result0.next();
                    System.out.println(record);
                    int curID = record.get(0).asInt();
                    System.out.println(curID);
                    session.run( "Start a=node("+ID+"),b=node("+curID+") Merge (a)-[r:collected_by{type:'collected_by',name:$name}]->(b)"
                            ,parameters("name",resultName));
                }

        }
        return true;
    }
    public HashMap<String, Object> AddYamlRelation(int deploymentID,int containerID,int serviceID,int systemID){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {

            session.run( "Start a=node("+containerID+"),b=node("+deploymentID+") Merge (a)-[r:deploy{type:'Deployed By'}]->(b)");
            session.run( "Start a=node("+serviceID+"),b=node("+containerID+") Merge (a)-[r:deploy{type:'Deployed At'}]->(b)");
            session.run( "Start a=node("+systemID+"),b=node("+serviceID+") Merge (a)-[r:exposes{type:'Exposes'}]->(b)");
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        System.out.println("dcd");

        return result;
    }
    private static int AddCausationNode(String name) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        int trueId=0;
        try(Session session = driver.session()) {
            StatementResult newID = session.run("MATCH (n: "+"Metric"+") "+
                            "WHERE n.row = $name " + "return id(n)",
                    parameters("name",name));

            if (!newID.hasNext()) {
                trueId = 0;
                System.out.println("not exist");
            } else trueId = newID.single().get(0).asInt();

            System.out.println("trueId:"+trueId);
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        driver.close();
        return trueId;
    }
    private static Boolean DeploymentToNode(String timeStamp,String hostIP,
                                            String name,String nameSpace,String podIP,String deploymentName){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        int trueId=0;
        HashMap<String, Object> result = new HashMap<>();
        result.put("state", "Operation failed");
        Boolean flag = true;
        try(Session session = driver.session()) {
            StatementResult result0 = session.run("Match (n:Deployment_Node) where n.name = $name return ID(n)"
                        , parameters("name", deploymentName));
            if (!result0.hasNext()) {
                return false;
            }
            while (result0.hasNext()) {
                Record record = result0.next();
                //System.out.println(record);
                int curID = record.get(0).asInt();
                System.out.println(name);
                StatementResult result1 = session.run("Match (n:Node) where n.name = $name return ID(n)", parameters("name", name));
                if (!result1.hasNext()){
                    result1 =  session.run("CREATE (n: Node ) "+
                                    "SET n.timeStamp = $timeStamp "+"SET n.hostIP = $hostIP "+
                                    "SET n.nameSpace = $nameSpace "+"SET n.name = $name "+
                                    "SET n.podIP = $podIP "+ "SET n.type = 'Node' "+"SET n.performance = $performance "+
                                    "return id(n)",
                            parameters("timeStamp",timeStamp,"hostIP",hostIP,"nameSpace",nameSpace,
                                    "name",name,"podIP",podIP,
                                    "performance","name:"+name+";timeStamp:"+timeStamp+";hostIP:"+hostIP+";nameSpace:"+nameSpace+
                                    ";podIP:"+podIP+";type:Node"));
                }
                int ID = result1.next().get(0).asInt();
                session.run("Start a=node(" + curID + "),b=node(" + ID + ") Merge (a)-[r:deploys_at{type:'deploys_at'}]->(b)");
            }

        }
        return true;
    }
    public static Boolean AddNode(String url){
        String username="lab";
        String password="409";
        String[] command = {"curl", "-H", "Accept:application/json", "-u", username+":"+password , url};
        ProcessBuilder process = new ProcessBuilder(command);
        Process p;
        try
        {
            p = process.start();
            BufferedReader reader =  new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ( (line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.print(result);
            Map maps = (Map) com.alibaba.fastjson.JSON.parse(result);
            com.alibaba.fastjson.JSONArray arrayList = (com.alibaba.fastjson.JSONArray) maps.get("success");
            for (int i=0;i<arrayList.size();i++){
                Map map = (Map)arrayList.get(i);
                String timeStamp = (String)map.get("creationTimestamp");
                String hostIP = (String)map.get("hostIP");
//                String name = (String)map.get("name");
                String nameSpace = (String)map.get("namespace");
                String name = (String)map.get("nodeName");
                String podIP = (String)map.get("podIP");
                Map map1 = (Map)map.get("labels");
                String deploymentName = (String)map1.get("name");
                System.out.print(name);
                Boolean flag = DeploymentToNode(timeStamp,hostIP,name,nameSpace,podIP,deploymentName);
                if (!flag){
                    return false;
                }
            }
        }
        catch (IOException e)
        {
            System.out.print("error");
            e.printStackTrace();
        }
        return true;
    }

    public static HashMap<String, Object> AddMetricRelation(int sourceID,int targetID,String relationship){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        try(Session session = driver.session()) {
            if (relationship.equals("Cause"))
                session.run( "Start a=node("+sourceID+"),b=node("+targetID+") Merge (a)-[r:Cause{type:$relation}]->(b)"
                        ,parameters("relation",relationship));
            else
                session.run( "Start a=node("+sourceID+"),b=node("+targetID+") Merge (a)-[r:Cause{type:$relation}]->(b)"
                        ,parameters("relation",relationship));
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        System.out.println("dcd");

        return result;
    }
    public HashMap<String, List<HashMap<String,Object>>> getDeploymentNodes() {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        Map<Long, String> map = new HashMap<>();
        System.out.println("sdfghjkl");
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            //try (Transaction tx = session.beginTransaction()) {
            StatementResult result = session.run("match (n:Deployment_Node) return n");
            List<HashMap<String, Object>> allnodes = new ArrayList<>();
            //List<HashMap<String,Object>> allrelations = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                //Path path = record.get("nodesrelation").asPath();
                HashMap<String, Object> nod = new HashMap();
                nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                nod.put("containerPort", record.get("n").get("containerPort").toString().replace("\"",""));
                nod.put("nameSpace", record.get("n").get("nameSpace").toString().replace("\"",""));
                nod.put("image", record.get("n").get("image").toString().replace("\"",""));
                //nod.put("performance","name:"+nod.get("name")+";containerPort:"+nod.get("containerPort")+";nameSpace:"+nod.get("nameSpace")+";image:"+nod.get("image")+";type:Deployment_Node");
                System.out.println(record.get("n").keys());
                if (!allnodes.contains(nod))
                    allnodes.add(nod);
                //Iterable<Relationship> relations = path.relationships();
                //for(Relationship relationship:relations) {
                //    HashMap<String,Object> rela = new HashMap();
                //    rela.put("source",relationship.startNodeId());
                //    rela.put("target",relationship.endNodeId());
                //    rela.put("type",relationship.type());
                //    allrelations.add(rela);
                //}
            }
            resultgraph.put("nodes", allnodes);
            //resultgraph.put("links",allrelations);
            //}

        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getServiceNodes() {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        System.out.println("sdfghjkl");
        Map<Long, String> map = new HashMap<>();
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(n:Service_Node) return n");

                List<HashMap<String, Object>> allnodes = new ArrayList<>();
                //List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    //Path path = record.get("nodesrelation").asPath();
                    HashMap<String, Object> nod = new HashMap();
                    nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                    nod.put("port", record.get("n").get("port").toString().replace("\"",""));
                    nod.put("nameSpace", record.get("n").get("nameSpace").toString().replace("\"",""));
                    nod.put("targetPort", record.get("n").get("targetPort").toString().replace("\"",""));
                    //nod.put("performance","name:"+nod.get("name")+";port:"+nod.get("port")+";nameSpace:"+nod.get("nameSpace")+";targetPort:"+nod.get("targetPort")+";type:Service_Node");
                    System.out.println( record);
                    if (!allnodes.contains(nod))
                        allnodes.add(nod);
                    //Iterable<Relationship> relations = path.relationships();
                    //for(Relationship relationship:relations) {
                    //    HashMap<String,Object> rela = new HashMap();
                    //    rela.put("source",relationship.startNodeId());
                    //    rela.put("target",relationship.endNodeId());
                    //    rela.put("type",relationship.type());
                    //    allrelations.add(rela);
                    //}
                }
                resultgraph.put("nodes", allnodes);
                //resultgraph.put("links",allrelations);
            }

        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getContainerNodes() {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        Map<Long, String> map = new HashMap<>();
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(n:Container_Node) return n");
                List<HashMap<String, Object>> allnodes = new ArrayList<>();
                System.out.println(result.next().get("n").get("name")+"34");
                //List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    //Path path = record.get("nodesrelation").asPath();
                    HashMap<String, Object> nod = new HashMap();
                    nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                    nod.put("volumeMount", record.get("n").get("volumeMount").toString().replace("\"",""));
                    nod.put("arrayListAdd", record.get("n").get("arrayListAdd").toString().replace("\"",""));
                    nod.put("arrayListDrop", record.get("n").get("arrayListDrop").toString().replace("\"",""));
                    //nod.put("performance","name:"+nod.get("name")+";volumeMount:"+nod.get("volumeMount")+";arrayListAdd:"+nod.get("arrayListAdd")+";arrayListDrop:"+nod.get("arrayListDrop")+";type:Container_Node");

                    System.out.println(record);
                    if (!allnodes.contains(nod))
                        allnodes.add(nod);
                    //Iterable<Relationship> relations = path.relationships();
                    //for(Relationship relationship:relations) {
                    //    HashMap<String,Object> rela = new HashMap();
                    //    rela.put("source",relationship.startNodeId());
                    //    rela.put("target",relationship.endNodeId());
                    //    rela.put("type",relationship.type());
                    //    allrelations.add(rela);
                    //}
                }
                resultgraph.put("nodes", allnodes);
                //resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getCausationNodes() {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        Map<Long, String> map = new HashMap<>();
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(n:Causation_Node) return n");
                List<HashMap<String, Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    //Path path = record.get("Cause").asPath();

                    HashMap<String, Object> nod = new HashMap();
                    //System.out.println(record.get("n").get("n").get(0));
                    nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                    nod.put("type", "Metric");
                    //nod.put("performance","name:"+nod.get("name")+";type:Causation_Node");
                    //nod.put("id",record.get("n").get("id")).toString();
                    System.out.println(record);
                    if (!allnodes.contains(nod))
                        allnodes.add(nod);
                    //Iterable<Relationship> relations = path.relationships();
                    //for(Relationship relationship:relations) {
                    //    HashMap<String,Object> rela = new HashMap();
                    //    rela.put("source",relationship.startNodeId());
                    //    rela.put("target",relationship.endNodeId());
                    //    rela.put("type",relationship.type());
                    //    allrelations.add(rela);
                    //}
                }
                resultgraph.put("nodes", allnodes);
                //resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getSystemNodes(){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        Map<Long, String> map = new HashMap<>();
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(n:System) return n");
                List<HashMap<String, Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    //Path path = record.get("Cause").asPath();

                    HashMap<String, Object> nod = new HashMap();
                    //System.out.println(record.get("n").get("n").get(0));
                    nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                    nod.put("type", "System");
                    //nod.put("performance","name:"+nod.get("name")+";type:System");
                    //nod.put("id",record.get("n").get("id")).toString();
                    System.out.println(record);
                    if (!allnodes.contains(nod))
                        allnodes.add(nod);
                    //Iterable<Relationship> relations = path.relationships();
                    //for(Relationship relationship:relations) {
                    //    HashMap<String,Object> rela = new HashMap();
                    //    rela.put("source",relationship.startNodeId());
                    //    rela.put("target",relationship.endNodeId());
                    //    rela.put("type",relationship.type());
                    //    allrelations.add(rela);
                    //}
                }
                resultgraph.put("nodes", allnodes);
                //resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getDataset(){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));

        Map<Long, String> map = new HashMap<>();
        HashMap<String, List<HashMap<String, Object>>> resultgraph = new HashMap<>();
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("match(n:Dataset) return n");
                List<HashMap<String, Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while (result.hasNext()) {
                    Record record = result.next();
                    //Path path = record.get("Cause").asPath();

                    HashMap<String, Object> nod = new HashMap();
                    //System.out.println(record.get("n").get("n").get(0));
                    nod.put("name", record.get("n").get("name").toString().replace("\"",""));
                    nod.put("type", "Dataset");
                    //nod.put("performance","name:"+nod.get("name")+";type:System");
                    //nod.put("id",record.get("n").get("id")).toString();
                    System.out.println(record);
                    if (!allnodes.contains(nod))
                        allnodes.add(nod);
                    //Iterable<Relationship> relations = path.relationships();
                    //for(Relationship relationship:relations) {
                    //    HashMap<String,Object> rela = new HashMap();
                    //    rela.put("source",relationship.startNodeId());
                    //    rela.put("target",relationship.endNodeId());
                    //    rela.put("type",relationship.type());
                    //    allrelations.add(rela);
                    //}
                }
                resultgraph.put("nodes", allnodes);
                //resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }

    public HashMap<String, List<HashMap<String,Object>>> getNeighbors(String name) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        Map<Long,String> map = new HashMap<>();
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("Match p=(n{name:$name})-[r]-(m) return p as nodesrelation", parameters("name",name));
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("name",node.get("name").toString().replace("\"",""));
                        nod.put("type",node.get("type").toString().replace("\"",""));
                        nod.put("layer",node.get("layer").toString().replace("\"",""));
                        nod.put("performance",node.get("performance").toString().replace("\"",""));
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("source",relationship.startNodeId());
                        rela.put("target",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        allrelations.add(rela);
                    }
                }
                resultgraph.put("nodes",allnodes);
                resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public HashMap<String, List<HashMap<String,Object>>> getLabel(String label) {
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j","1234"));
        Map<Long,String> map = new HashMap<>();
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("Match p=(n: "+label+")-[r]-(m) return p as nodesrelation");
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("name",node.get("name").toString().replace("\"",""));
                        nod.put("type",node.get("type").toString().replace("\"",""));
                        nod.put("layer",node.get("layer").toString().replace("\"",""));
                        nod.put("performance",node.get("performance").toString().replace("\"",""));
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("source",relationship.startNodeId());
                        rela.put("target",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        allrelations.add(rela);
                    }
                }
                resultgraph.put("nodes",allnodes);
                resultgraph.put("links",allrelations);
            }
        }
        driver.close();
        return resultgraph;
    }
    public static Boolean Deal(String keyName, Map<String, Object> map, HashMap hashMap) {
        //System.out.println(map);
        try {
            Iterator iter = map.entrySet().iterator();
            String curKeyName = keyName;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                keyName = curKeyName;
                if (!keyName.equals(""))
                    keyName = keyName + "-" + (String) entry.getKey();
                else keyName = keyName + (String) entry.getKey();
                boolean flag = true;
                Map<String, Object> map1;
                try {
                    map1 = (Map<String, Object>) entry.getValue();
                    Deal(keyName, map1, hashMap);
                } catch (ClassCastException e) {
                    flag = false;
                }
                if (!flag) {
                    boolean flag2 = true;
                    String val = "";
                    int value = -999;
                    try {
                        try {
                            val = (String) entry.getValue();
                        } catch (ClassCastException e) {
                            value = (int) entry.getValue();
                        }
                    } catch (ClassCastException e) {
                        flag2 = false;
                    }
                    if (flag2 && value == -999) {
                        //System.out.println(keyName + "  " + val);
                        hashMap.put(keyName, val);
                    } else if (flag2 && value != -999) {
                        //System.out.println(keyName + "  " + value);
                        hashMap.put(keyName, value);
                    } else {
                        try {
                            ArrayList arrayList = (ArrayList) entry.getValue();
                            try {
                                for (int i = 0; i < arrayList.size(); i++) {
                                    Deal(keyName, (Map<String, Object>) arrayList.get(i), hashMap);
                                }
                            } catch (ClassCastException e) {
                                hashMap.put(keyName, arrayList);
                            }
                        } catch (ClassCastException e) {
                            //System.out.println("sss");
                        }
                    }

                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return true;
    }
    public static ArrayList csvTimestamp(String filePath) {
        ArrayList arrayList = new ArrayList();
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            csvReader.readHeaders();
            while (csvReader.readRecord()){
                //System.out.println(csvReader.get("timestamp"));
                String string = csvReader.get("timestamp");
                if (!string.equals(""))
                    arrayList.add(string);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return arrayList;
    }
    public static ArrayList CurrentArray(String header,String filePath){
        try {
            ArrayList arrayList = new ArrayList();
            CsvReader csvReader = new CsvReader(filePath);
            csvReader.readHeaders();
            while (csvReader.readRecord()){
                String string = csvReader.get(header);
                if (!string.equals("")){
                    arrayList.add(string);
                }
            }
            return arrayList;
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static HashMap<String,ArrayList> csvOperationData(String filePath) {
        HashMap<String,ArrayList> hashMap = new HashMap();
        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            String header[] = null;
            while (csvReader.readRecord()){
                header = csvReader.getRawRecord().split(",");
                break;
            }
            csvReader.close();
            csvReader = new CsvReader(filePath);
            csvReader.readHeaders();
            try {
                for (int i = 1; i < header.length; i++) {
                    hashMap.put(header[i],CurrentArray(header[i],filePath));
                    //hashMap.put(header[i], arrayList);
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hashMap;

    }
    public static HashMap<String,ArrayList<HashMap<String,Object>>> causationData(String[] strings){
        try {
            HashMap<String, ArrayList<HashMap<String, Object>>> resultgraph = new HashMap<>();
            ArrayList<HashMap<String, Object>> allnodes = new ArrayList<>();
            ArrayList<HashMap<String, Object>> allrelations = new ArrayList<>();
//            File file = new File(filePath);
            for (int i=0;i<strings.length;i++){
                System.out.println(strings[i]);
            }
            int lineNum = 1;

            String line = strings[lineNum];
            int source = 0;
            int target = 0;

            String node[] = line.split(",");
            //System.out.println(line);

            for (int i = 1; i < node.length; i++) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("Name", node[i]);
                if (!allnodes.contains(map))
                    allnodes.add(map);
            }

            lineNum=4;
            line = strings[lineNum];

            while (lineNum<strings.length-1) {
                //System.out.println(line);
                String string[] = line.split(" ");
                HashMap<String, Object> map = new HashMap<>();
                System.out.println(string[1]+" "+string[3]);

                map.put("source", string[1]);
                map.put("target", string[3]);
                source = AddCausationNode(string[1]);
                target = AddCausationNode(string[3]);
                if (source!=0 && target!=0) {
                    String relationship = "";
                    if (string[2].equals("-->") || string[2].equals("o->"))
                        relationship = "casues";
                    if (string[2].equals("o-o"))
                        relationship = "has_causation";
                    if (string[2].equals("<->"))
                        relationship = "has_latent_variable";
                    if (relationship.equals("has_latent_variable")) {
                        AddMetricRelation(source, target, "has_latent_variable");
                        AddMetricRelation(target, source, "has_latent_variable");
                    } else {
                        AddMetricRelation(source, target, relationship);
                    }
                    map.put("type", relationship);
                    if (!allrelations.contains(map))
                        allrelations.add(map);
                }
                source=0;
                target=0;
                lineNum++;
                if (lineNum<strings.length)
                    line = strings[lineNum];
            }

            resultgraph.put("Relationship", allrelations);
            resultgraph.put("Nodes", allnodes);
            return resultgraph;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static Boolean AddServiceRelation(String name, ArrayList<String> arrayList){
        Driver driver = GraphDatabase.driver("bolt://10.60.38.173:7687",
                AuthTokens.basic("neo4j", "1234"));
        HashMap<String, Object> result = new HashMap<>();
        //System.out.println();
        result.put("state", "Operation failed");
        int fromID=0;
        int toID = 0;
        try(Session session = driver.session()) {
            StatementResult from = session.run("MATCH (n: "+"Service_Node"+") "+
                            "WHERE n.name = $name" + " return id(n)",
                    parameters("name", name.toLowerCase().replace(" ","")));
            //System.out.println(name.toLowerCase().replace(" ",""));
            if (from.hasNext())
                fromID = from.single().get(0).asInt();
            //System.out.println(fromID);

            for (int i=0;i<arrayList.size();i++) {
                StatementResult to = session.run("MATCH (n: " + "Service_Node" + ") " +
                                "WHERE n.name = $name " + "return id(n)",
                        parameters("name", arrayList.get(i).toLowerCase().replace(" ","")));
                System.out.println(arrayList.get(i).toLowerCase().replace(" ",""));
                System.out.println(to);
                if (fromID!=0 && to.hasNext()){
                    toID = to.single().get(0).asInt();
                    System.out.println(fromID + " "+toID);
                    session.run( "Start a=node("+fromID+"),b=node("+toID+") Merge (a)-[r:Calls{type:'Calls'}]->(b)");
                }
            }
            result.remove("state");
            result.put("state", "Operation succeeded");
        }
        return true;
    }
    public static HashMap<String, HashMap<String,Object>> metricInfo(String filePath){
        HashMap<String, HashMap<String,Object>> hashMap = new HashMap<>();

        try {
            // 创建CSV读对象
            CsvReader csvReader = new CsvReader(filePath);

            // 读表头
            String header[] = null;
            while (csvReader.readRecord()) {
                header = csvReader.getRawRecord().split(",");
                break;
            }
            //container和dataset下次改
            HashMap<String,Object> hashMapTemp = new HashMap<>();
            List<String> list = new ArrayList<>();
            list.add("service");
            list.add("deployment");
            list.add("container");
            hashMapTemp.put("name",list);
            hashMap.put("Node",hashMapTemp);
            hashMap.put("service",new HashMap<>());
            hashMap.put("deployment",new HashMap<>());
            hashMap.put("container",new HashMap<>());
            hashMap.get("container").put("place",new ArrayList<>());
            hashMap.get("service").put("place",new ArrayList<>());
            hashMap.get("deployment").put("place",new ArrayList<>());

            for (int i=0;i<header.length;i++){
                try{
                    String temp[] = header[i].split("/");
                    if (temp[0].equals("container")){
                        HashMap<String,Object> hashMap1 = hashMap.get("container");
                        Boolean flag = hashMap1.containsKey(temp[1]);
                        if (!flag){
                            ArrayList arrayList = (ArrayList)hashMap1.get("place");
                            arrayList.add(temp[1]);

                            ArrayList arrayList1 = new ArrayList();
                            hashMap1.put(temp[1],arrayList1);
                            arrayList1.add(temp[2]);

                        }else{
                            ArrayList arrayList = (ArrayList)hashMap1.get(temp[1]);
                            arrayList.add(temp[2]);
                        }
                    }else if (temp[0].equals("service")){
                        HashMap<String,Object> hashMap1 = hashMap.get("service");
                        String temp2[] = temp[1].split("_");
                        Boolean flag = hashMap1.containsKey(temp2[0]);
                        if (!flag){
                            ArrayList arrayList = (ArrayList)hashMap1.get("place");
                            arrayList.add(temp2[0]);

                            ArrayList arrayList1 = new ArrayList();
                            hashMap1.put(temp2[0],arrayList1);
                            arrayList1.add(temp[2]);
                        }else{
                            ArrayList arrayList = (ArrayList)hashMap1.get(temp2[0]);
                            arrayList.add(temp[2]);
                        }
                    }else if (temp[0].equals("deployment")){

                    }

                }catch (Exception e){
                    e.printStackTrace();
                }
            }


//            listContainer.add(":80");
//            listContainer.add("docker-compose");
//            hashMapTemp.put("name",listContainer);
//            hashMap.put("Container",hashMapTemp);
//
//            HashMap<String,Object> hashMapDataset = new HashMap<>();
//            hashMapDataset.put("name","result.csv");
//            hashMap.put("Dataset",hashMapDataset);
//
//
//            HashMap<String,Object> hashMapDocker = new HashMap<>();
//            HashMap<String,Object> hashMap80 = new HashMap<>();
//            List<String> service = new ArrayList<>();
//            List<String> list = new ArrayList<>();
//            List<String> service80 = new ArrayList<>();
//            List<String> list80 = new ArrayList<>();
//            hashMapDocker.put("service",service);
//            hashMap80.put("service",service80);
//            String curString = "";
//            String curString80 = "";
//            for (int i=1;i<header.length;i++){
//                try {
//                    String temp[] = header[i].split("/");
//                    if (!curString.equals(temp[0].split("_")[1])) {
//                        curString = temp[0].split("_")[1];
//                        hashMapDocker.put(curString, list);
//                        list.clear();
//                        service.add(curString);
//                    }
//                    list.add(temp[1]);
//                }catch (Exception e){
//                    String temp[] = header[i].split("/");
//                    if (!curString80.equals(temp[0].split(":")[0])) {
//                        curString80 = temp[0].split(":")[0];
//                        hashMap80.put(curString80, list80);
//                        list80.clear();
//                        service80.add(curString80);
//                    }
//                    list80.add(temp[1]);
//                }
//            }
//            hashMap.put("docker-compose",hashMapDocker);
//            hashMap.put(":80",hashMap80);
        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }
    public static HashMap<String,ArrayList> getElement(){
        HashMap<String,ArrayList> hashMap = new HashMap<>();
        ArrayList arrayList = new ArrayList();
        hashMap.put("Element",arrayList);
        String username="lab";
        String password="409";
        String[] command = {"curl", "-H", "Accept:application/json", "-u", username+":"+password , "http://10.60.38.182:5525/tool/api/v1.0/get_namespace"};
        ProcessBuilder process = new ProcessBuilder(command);
        Process p;
        try {
            p = process.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();
            System.out.println(result);
            Map maps = (Map) com.alibaba.fastjson.JSON.parse(result);
            com.alibaba.fastjson.JSONArray arrayList1 = (com.alibaba.fastjson.JSONArray) maps.get("success");
            for (int i=0;i<arrayList1.size();i++){
                Map map = (Map)arrayList1.get(i);
                String name = (String)map.get("name");
                arrayList.add(name);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return hashMap;
    }
    public static Map<String, Object> csvPost(String algorithm,String filepath) throws IOException {
        String sURL = "http://10.60.38.182:10080/causality";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(sURL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("algorithm", algorithm, ContentType.TEXT_PLAIN);
        builder.addTextBody("format", "json", ContentType.TEXT_PLAIN);

        // 把文件加到HTTP的post请求中
        File f = new File(filepath);
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String sResponse= EntityUtils.toString(responseEntity, "UTF-8");
        Map maps = (Map) com.alibaba.fastjson.JSON.parse(sResponse);
        Map maps2 = (Map) com.alibaba.fastjson.JSON.parse(jsonPost(algorithm,filepath));
        System.out.println(maps2);
        maps.put("txt",maps2.get("data"));
        return maps;
    }
    public static String jsonPost(String algorithm,String filepath) throws IOException {
        String sURL = "http://10.60.38.182:10080/causality";
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost uploadFile = new HttpPost(sURL);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("algorithm", algorithm, ContentType.TEXT_PLAIN);
        builder.addTextBody("format", "text", ContentType.TEXT_PLAIN);

        // 把文件加到HTTP的post请求中
        File f = new File(filepath);
        builder.addBinaryBody(
                "file",
                new FileInputStream(f),
                ContentType.APPLICATION_OCTET_STREAM,
                f.getName()
        );

        HttpEntity multipart = builder.build();
        uploadFile.setEntity(multipart);
        CloseableHttpResponse response = httpClient.execute(uploadFile);
        HttpEntity responseEntity = response.getEntity();
        String sResponse= EntityUtils.toString(responseEntity, "UTF-8");
        return sResponse;
    }




    /**
     * this semester below
     */
    public static String getData(String address) {
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

    public static boolean storeNamespace() {
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_namespace";

        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            Model model = ModelFactory.createDefaultModel();
            String url = "http://namespace/" + address + "/";
            Resource resource = model.createResource(url);

            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    String name = (String) jObject.getJSONObject("metadata").get("name");
//                if (name.equals("monitoring") || name.equals("sock-shop")) {
                    resource.addProperty(model.createProperty(url + name),
                            model.createResource().addProperty(model.createProperty(url + name + "/status"), (String) jObject.getJSONObject("status").get("phase")));
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            //存储fuseki
            DataAccessor.getInstance().add(model);
            model.write(System.out, "RDF/XML-ABBREV");
            model.write(System.out, "N-TRIPLE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeNamespaceName(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            // get address string
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            try {
                // create model
                Model model = ModelFactory.createDefaultModel();
                // namespace string
                String np1 = "http://namespace/"+address+"/monitoring";
                String np2 = "http://namespace/"+address+"/sock-shop";
                Resource res = model.createResource(np1);
                Resource resource = model.createResource(np2);
                res.addProperty(model.createProperty(np1+"/name"), "monitoring");
                resource.addProperty(model.createProperty(np2+"/name"), "sock-shop");
                res.addProperty(model.createProperty(np1, "/supervises"), resource);
                //存储fuseki
                model.write(System.out, "RDF/XML-ABBREV");
                DataAccessor.getInstance().add(model);
                model.write(System.out, "N-TRIPLE");

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeServerName(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            String nodeName = "";
            for (int i=0;i<jsonArray.size();i++){
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");

                    Model model = ModelFactory.createDefaultModel();

                    String store = "http://backup/"+address;
                    Resource res = model.createResource(store);
                    res.addProperty(model.createProperty(store+"/server"), (String) jMetaData.get("name"));

                    String names = "http://server/" + address + "/"+(String) jMetaData.get("name");
                    Resource resource = model.createResource(names);
                    resource.addProperty(model.createProperty(names+"/name"), (String) jMetaData.get("name"));
                    resource.addProperty(model.createProperty(names, "/labels"),model.createResource()
                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/arch"),
                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/arch"))
                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/fluentd-ds-ready"),
                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/fluentd-ds-ready"))
                            .addProperty(model.createProperty(names,"/labels/beta.kubernetes.io/os"),
                                    (String) jMetaData.getJSONObject("labels").get("beta.kubernetes.io/os"))
                            .addProperty(model.createProperty(names,"/labels/kubernetes.io/hostname"),
                                    (String) jMetaData.getJSONObject("labels").get("kubernetes.io/hostname"))
                            .addProperty(model.createProperty(names,"/labels/kubernetes.io/role"),
                                    (String) jMetaData.getJSONObject("labels").get("kubernetes.io/role")));
                    resource.addProperty(model.createProperty(names, "/annotations"),model.createResource()
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/backend-data"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/backend-data"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/backend-type"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/backend-type"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/kube-subnet-manager"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/kube-subnet-manager"))
                            .addProperty(model.createProperty(names,"/annotations/flannel.alpha.coreos.com/public-ip"),
                                    (String) jMetaData.getJSONObject("annotations").get("flannel.alpha.coreos.com/public-ip"))
                            .addProperty(model.createProperty(names,"/annotations/node.alpha.kubernetes.io/ttl"),
                                    (String) jMetaData.getJSONObject("annotations").get("node.alpha.kubernetes.io/ttl"))
                            .addProperty(model.createProperty(names,"/annotations/volumes.kubernetes.io/controller-managed-attach-detach"),
                                    (String) jMetaData.getJSONObject("annotations").get("volumes.kubernetes.io/controller-managed-attach-detach")));
                    resource.addProperty(model.createProperty(names, "/podCIDR"), (String) jSpec.get("podCIDR"));
                    resource.addProperty(model.createProperty(names, "/creationTimestamp"), (String) jMetaData.get("creationTimestamp"));
                    List<JSONObject> jConditions = (List<JSONObject>) jStatus.get("conditions");
//                    System.out.println(jConditions.size());
                    for (JSONObject json: jConditions){
//                        System.out.println(json);
                        String add = "/condition_" + (String) json.get("type");
                        String name1 = add + "/lastHeartbeatTime";
                        String name2 = add + "/lastTransitionTime";
                        String name3 = add + "/message";
                        String name4 = add + "/reason";
                        String name5 = add + "/status";
                        resource.addProperty(model.createProperty(names, add), model.createResource()
                                .addProperty(model.createProperty(names, name1),
                                        (String) json.get("lastHeartbeatTime"))
                                .addProperty(model.createProperty(names,name2),
                                        (String) json.get("lastTransitionTime"))
                                .addProperty(model.createProperty(names,name3),
                                        (String) json.get("message"))
                                .addProperty(model.createProperty(names,name4),
                                        (String) json.get("reason"))
                                .addProperty(model.createProperty(names,name5),
                                        (String) json.get("status")));
                    }
                    //存储fuseki
                    model.write(System.out, "RDF/XML-ABBREV");
                    DataAccessor.getInstance().add(model);
                    model.write(System.out, "N-TRIPLE");

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeEnvironment(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            System.out.println("storing env");
            // get address string
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            String envName = "http://environment/"+address;
            System.out.println("env name: "+envName);
            Model model = ModelFactory.createDefaultModel();
            try {
                Resource res = model.createResource(envName);
                res.addProperty(model.createProperty(envName+"/name"), "env");
                //存储fuseki
                model.write(System.out, "RDF/XML-ABBREV");
                DataAccessor.getInstance().add(model);
                model.write(System.out, "N-TRIPLE");

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeEnvRelation(){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        try {
            // get address string
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            String envName = "http://environment/"+address;
            RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                    .destination("http://10.60.38.181:30300/DevKGData/query");
            Query query = QueryFactory.create("SELECT ?p ?o " +
                    "WHERE { <http://backup/"+address+"> ?p ?o }");
            try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
                QueryExecution qExec = conn.query(query) ;
                ResultSet rs = qExec.execSelect() ;
                while(rs.hasNext()) {
                    QuerySolution qs = rs.next() ;
                    String subject = qs.get("o").toString();
                    System.out.println("adding env, server name: " + subject);
                    String urlNode = "http://server/"+address+"/"+subject;

                    String addRelation = "PREFIX j0:<"+envName+"/>\n" +
                            "INSERT DATA{\n" +
                            "<"+envName+"> j0:has "+"<"+urlNode+">\n" +
                            "}";
                    RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                            .destination("http://10.60.38.181:30300/DevKGData/update");

                    try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                        connAddRelation.update(addRelation);
                    }
                }
                qExec.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeMasterNode(String masterName){
        // get address
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        // save master url
        String urlMasterNode = "http://server/"+address+"/"+masterName;
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        // execute query, get all nodes
        Query query = QueryFactory.create("SELECT ?p ?o " +
                "WHERE { <http://backup/"+address+"> ?p ?o }");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query) ;
            ResultSet rs = qExec.execSelect() ;
            while(rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("o").toString();
                System.out.println("server name: " + subject);
                // add relations between master and other nodes
                if(!subject.equals(masterName)){
                    String urlNode = "http://server/"+address+"/"+subject;

                    String addRelation = "PREFIX j0:<"+urlMasterNode+"/>\n" +
                            "INSERT DATA{\n" +
                            "<"+urlMasterNode+"> j0:manage "+"<"+urlNode+">\n" +
                            "}";
                    RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                            .destination("http://10.60.38.181:30300/DevKGData/update");

                    try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                        connAddRelation.update(addRelation);
                    }
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storePodName(String podName){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_pods/"+podName;
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            for (int i=0;i<jsonArray.size();i++){
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");

                    Model model = ModelFactory.createDefaultModel();
                    String names = "http://pods/" + address + "/"+podName+"/"+(String) jMetaData.get("name");
                    Resource resource = model.createResource(names);
                    String namePod = names;
                    String temp = "http://pods/" + address + "/"+podName;
                    resource.addProperty(model.createProperty(temp,"/name"), (String) jMetaData.get("name"));
                    resource.addProperty(model.createProperty(names, "/namespace"), (String) jMetaData.get("namespace"));
                    resource.addProperty(model.createProperty(names, "/labels"),
                            model.createResource().addProperty(model.createProperty(names, "/labels/name"), (String) jMetaData.getJSONObject("labels").get("name"))
                                    .addProperty(model.createProperty(names, "/labels/pod-template-hash"), (String) jMetaData.getJSONObject("labels").get("pod-template-hash")));
                    resource.addProperty(model.createProperty(names, "/nodeName"), (String) jSpec.get("nodeName"));
                    resource.addProperty(model.createProperty(names, "/nodeSelector"),
                            model.createResource().addProperty(model.createProperty(names,"nodeSelector/beta.kubernetes.io-os"), (String) jSpec.getJSONObject("nodeSelector").get("beta.kubernetes.io/os")));

                    resource.addProperty(model.createProperty(names, "/hostIP"), (String) jStatus.get("hostIP"));
                    resource.addProperty(model.createProperty(names, "/podIP"), (String) jStatus.get("podIP"));
                    resource.addProperty(model.createProperty(names, "/qosClass"), (String) jStatus.get("qosClass"));
                    resource.addProperty(model.createProperty(names, "/startTime"), (String) jStatus.get("startTime"));
                    resource.addProperty(model.createProperty(names, "/phase"), (String) jStatus.get("phase"));

                    //以上POD信息完整
                    //以下CONTAINER
                    JSONArray jCont = jSpec.getJSONArray("containers");
                    JSONObject jContainer = jCont.getJSONObject(0);
                    JSONObject jContainerStatus = jsonArray.getJSONObject(i).getJSONObject("status").getJSONArray("containerStatuses").getJSONObject(0);
                    Model modelContainer = ModelFactory.createDefaultModel();
                    names = "http://containers/" + address + "/"+podName+"/"+(String)jContainer.get("name");
                    Resource resourceContainer = modelContainer.createResource(names);
                    resourceContainer.addProperty(modelContainer.createProperty("http://containers/" + address + "/"+podName+"/"+"name"),(String)jContainer.get("name"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/image"),(String)jContainer.get("image"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ports"),
                            modelContainer.createResource().addProperty(modelContainer.createProperty(names,"/ports/containerPort"),(String)jContainer.getJSONArray("ports").getJSONObject(0).get("containerPort").toString())
                                    .addProperty(modelContainer.createProperty(names,"/ports/protocol"),(String)jContainer.getJSONArray("ports").getJSONObject(0).get("protocol")));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/containerID"),(String)jContainerStatus.get("containerID"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/imageID"),(String)jContainerStatus.get("imageID"));
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ready"),(String) jContainerStatus.get("ready").toString());
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/restartCount"),(String)jContainerStatus.get("restartCount").toString());
//                    JSONObject jsonObject1 = jContainerStatus.getJSONObject("lastState");
//                    if (jsonObject1.isEmpty())
//                        resourceContainer.addProperty(modelContainer.createProperty(names+"lastState"),"null");
//                    else{
//                        resourceContainer.addProperty(modelContainer.createProperty(names+"lastState"),(String)jContainerStatus.getJSONObject("lastState").get(0));
//                    }
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/ports"),
                            modelContainer.createResource().addProperty(modelContainer.createProperty(names,"/ports/curstate"),(String)jContainerStatus.getJSONObject("state").keySet().toArray()[0])
                                    .addProperty(modelContainer.createProperty(names,"/ports/startedAt"),(String)jContainerStatus.getJSONObject("state").getJSONObject((String)jContainerStatus.getJSONObject("state").keySet().toArray()[0]).get("startedAt")));
                    resource.addProperty(model.createProperty(namePod,"/contains"),resourceContainer);

                    Model modelContainerStorage = ModelFactory.createDefaultModel();
//                    String nameStorage = names+"/containerStorage";
                    String nameStorage1 = names+"/container_fs_io_current";
                    String nameStorage2 = names+"/container_fs_usage_bytes";
                    String nameStorage3 = names+"/container_fs_reads_bytes_total";
                    String nameStorage4 = names+"/container_fs_writes_bytes_total";
                    Resource resourceStorage1 = modelContainerStorage.createResource(nameStorage1);
                    resourceStorage1.addProperty(modelContainerStorage.createProperty(nameStorage1,"/value"),"");
                    Resource resourceStorage2 = modelContainerStorage.createResource(nameStorage2);
                    resourceStorage2.addProperty(modelContainerStorage.createProperty(nameStorage2,"/value"),"");
                    Resource resourceStorage3 = modelContainerStorage.createResource(nameStorage3);
                    resourceStorage3.addProperty(modelContainerStorage.createProperty(nameStorage3,"/value"),"");
                    Resource resourceStorage4 = modelContainerStorage.createResource(nameStorage4);
                    resourceStorage4.addProperty(modelContainerStorage.createProperty(nameStorage4,"/value"),"");
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage1);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage2);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage3);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceStorage4);

                    Model modelContainerNetwork = ModelFactory.createDefaultModel();
//                    String nameNetwork = names+"/containerNetwork";
                    String nameNetwork1 = names+"/network_receive_bytes";
                    String nameNetwork2 = names+"/network_transmit_bytes";
                    Resource resourceNetwork1 = modelContainerNetwork.createResource(nameNetwork1);
                    Resource resourceNetwork2 = modelContainerNetwork.createResource(nameNetwork2);
                    resourceNetwork1.addProperty(modelContainerNetwork.createProperty(nameNetwork1,"/value"),"");
                    resourceNetwork2.addProperty(modelContainerNetwork.createProperty(nameNetwork2,"/value"),"");
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork1);
                    resourceContainer.addProperty(modelContainer.createProperty(names,"/profile"),resourceNetwork2);

                    //以上CONTAINER信息完整
                    //以下添加关系

                    model.write(System.out, "RDF/XML-ABBREV");
                    modelContainer.write(System.out, "RDF/XML-ABBREV");
                    model.write(System.out, "N-TRIPLE");
                    modelContainer.write(System.out, "N-TRIPLE");
                    //存储fuseki
                    DataAccessor.getInstance().add(model);
                    DataAccessor.getInstance().add(modelContainer);
                    DataAccessor.getInstance().add(modelContainerStorage);
                    DataAccessor.getInstance().add(modelContainerNetwork);
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
            //model.write(System.out, "N-TRIPLE");
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean storeServiceName(String serviceName){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_svc/"+serviceName;
        try {
            String jsonString = getData(httpUrl);
            JSONObject jsonObject = JSONObject.parseObject(jsonString);
            String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
            JSONArray jsonArray = jsonObject.getJSONObject("detail").getJSONObject(address).getJSONArray("resources");
            for (int i=0;i<jsonArray.size();i++) {
                try {
                    JSONObject jMetaData = jsonArray.getJSONObject(i).getJSONObject("metadata");
                    JSONObject jSpec = jsonArray.getJSONObject(i).getJSONObject("spec");
                    JSONObject jStatus = jsonArray.getJSONObject(i).getJSONObject("status");

                    Model model = ModelFactory.createDefaultModel();
                    String names = "http://services/" + address + "/" + serviceName + "/" + (String) jMetaData.get("name");
                    Resource resource = model.createResource(names);
                    resource.addProperty(model.createProperty("http://services/" + address + "/" + serviceName+"/name"),(String) jMetaData.get("name"));
                    resource.addProperty(model.createProperty(names ,"/annotations"),model.createResource()
                            .addProperty(model.createProperty(names,"/annotations/kubectl.kubernetes.io/last-applied-configuration"),
                                    (String) jMetaData.getJSONObject("annotations").get("kubectl.kubernetes.io/last-applied-configuration")));
                    resource.addProperty(model.createProperty(names, "/creationTimestamp"), (String) jMetaData.get("creationTimestamp"));
                    resource.addProperty(model.createProperty(names, "/namespace"), (String) jMetaData.get("namespace"));
                    resource.addProperty(model.createProperty(names, "/resourceVersion"), (String) jMetaData.get("resourceVersion"));
                    resource.addProperty(model.createProperty(names, "/labels"),model.createResource()
                            .addProperty(model.createProperty(names,"/labels/name"),
                                    (String) jMetaData.getJSONObject("labels").get("name")));
                    resource.addProperty(model.createProperty(names, "/clusterIP"), (String) jSpec.get("clusterIP"));
                    resource.addProperty(model.createProperty(names, "/type"), (String) jSpec.get("type"));
                    resource.addProperty(model.createProperty(names, "/selector"),model.createResource()
                            .addProperty(model.createProperty(names,"/selector/name"),
                                    (String) jSpec.getJSONObject("selector").get("name")));
                    resource.addProperty(model.createProperty(names, "/ports"),model.createResource()
                            .addProperty(model.createProperty(names,"/ports/port"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("port").toString())
                            .addProperty(model.createProperty(names,"/ports/protocol"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("protocol"))
                            .addProperty(model.createProperty(names,"/ports/targetPort"), (String)jSpec.getJSONArray("ports").getJSONObject(0).get("targetPort").toString()));
                    //存储fuseki
                    Model modelService = ModelFactory.createDefaultModel();
                    Model modelDatabase = ModelFactory.createDefaultModel();
                    if (!((String) jMetaData.get("name")).contains("db")){
//                        String nameService = names+"/serviceProfile";
                        String nameService1 = names+"/response_time";
                        String nameService2 = names+"/success_rate";
                        Resource resourceService1 = modelService.createResource(nameService1);
                        Resource resourceService2 = modelService.createResource(nameService2);
                        resourceService1.addProperty(modelService.createProperty(nameService1,"/value"),"");
                        resourceService2.addProperty(modelService.createProperty(nameService2,"/value"),"");
                        resource.addProperty(model.createProperty(names,"/profile"),resourceService1);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceService2);
                    }else{
                        String nameDatabase1 = names+"/throughput";
                        String nameDatabase2 = names+"/response_time";
                        Resource resourceDatabase1 = modelDatabase.createResource(nameDatabase1);
                        Resource resourceDatabase2 = modelDatabase.createResource(nameDatabase2);
                        resourceDatabase1.addProperty(modelDatabase.createProperty(nameDatabase1,"/value"),"");
                        resourceDatabase2.addProperty(modelDatabase.createProperty(nameDatabase2,"/value"),"");
                        resource.addProperty(model.createProperty(names,"/profile"),resourceDatabase1);
                        resource.addProperty(model.createProperty(names,"/profile"),resourceDatabase2);
                    }

                    model.write(System.out, "RDF/XML-ABBREV");
                    DataAccessor.getInstance().add(model);
                    DataAccessor.getInstance().add(modelService);
                    DataAccessor.getInstance().add(modelDatabase);
                    model.write(System.out, "N-TRIPLE");
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean podToService(String address, String namespace) {
        String urlPod = "http://pods/" + address + "/" + namespace + "/";
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");

        Query query = QueryFactory.create("PREFIX j0:<" + urlPod + ">\n" +
                "SELECT ?o WHERE {\n" +
                "\t?s j0:name ?o\n" +
                "}");
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            QueryExecution qExec = conn.query(query);
            ResultSet rs = qExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next();
                String subject = qs.get("o").toString();
                System.out.println("Subject1: " + subject);
                String name = urlPod + subject + "/labels/";

                query = QueryFactory.create("PREFIX j0:<" + name + ">\n" +
                        "SELECT ?o WHERE {\n" +
                        "\t?s j0:name ?o\n" +
                        "}");

                try (RDFConnectionFuseki connServiceName = (RDFConnectionFuseki) builder.build()) {

                    QueryExecution qExecServiceName = connServiceName.query(query);
                    ResultSet rsServiceName = qExecServiceName.execSelect();
                    while (rsServiceName.hasNext()) {
                        QuerySolution qsServiceName = rsServiceName.next();
                        String subjectServiceName = qsServiceName.get("o").toString();
                        String urlService = "http://services/" + address + "/" + namespace + "/" + subjectServiceName;
                        String temp = subject.replace("/","");
                        System.out.println("**"+temp);

                        String addRelation = "PREFIX j0:<" + urlPod + subject + "/>\n" +
                                "INSERT DATA{\n" +
                                "<" + urlPod + temp + ">  j0:provides " + "<" + urlService + ">\n" +
                                "}";
                        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                                .destination("http://10.60.38.181:30300/DevKGData/update");

                        try (RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki) builderAddRelation.build()) {
                            connAddRelation.update(addRelation);
                        }

                    }
                    qExecServiceName.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            qExec.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static boolean podToServer(String address,String namespace){
        String urlPod = "http://pods/"+address+"/"+namespace+"/";
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");

        Query query = QueryFactory.create("PREFIX j0:<"+urlPod+">\n" +
                "SELECT ?o WHERE {\n" +
                "\t?s j0:name ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query) ;
            ResultSet rs = qExec.execSelect() ;
            while(rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("o").toString();
                System.out.println("Subject: " + subject);
                String name = urlPod+subject+"/";

//                查找当前Pod所在的node name
                query = QueryFactory.create("PREFIX j0:<"+name+">\n" +
                        "SELECT ?o WHERE {\n" +
                        "\t?s j0:nodeName ?o\n" +
                        "}");

                try ( RDFConnectionFuseki connServiceName = (RDFConnectionFuseki)builder.build() ) {

                    QueryExecution qExecServiceName = connServiceName.query(query);
                    ResultSet rsServiceName = qExecServiceName.execSelect();
                    while (rsServiceName.hasNext()) {
                        QuerySolution qsServiceName = rsServiceName.next();
                        String nodeName = qsServiceName.get("o").toString();
                        String urlNode = "http://server/"+address+"/"+ nodeName;
                        System.out.println(urlNode);

                        String addRelation = "PREFIX j0:<"+urlPod+subject+"/>\n" +
                                "INSERT DATA{\n" +
                                "<"+urlPod+subject+">  j0:deployed_in "+"<"+urlNode+">\n" +
                                "}";
                        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                                .destination("http://10.60.38.181:30300/DevKGData/update");

                        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                            connAddRelation.update(addRelation);
                        }

                    }
                    qExecServiceName.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static Map<String, Object> getAllNodesAndLinks(){
        Map<String, Object> final_list = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> linkList = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");

        Query query = QueryFactory.create("SELECT distinct ?s WHERE {\n" +
                "\t?s ?p ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qExec = conn.query(query);
            ResultSet rs = qExec.execSelect();
            while (rs.hasNext()) {
                QuerySolution qs = rs.next() ;
                String subject = qs.get("s").toString();
                if(subject.contains("http")){
                    System.out.println("Subject: " + subject);
                    if(subject.contains("server")){
                        result.add(getServer(subject));
                        linkList.addAll(getLink(subject, "manage"));
                    }
                    else if(subject.contains("namespace")){
                        result.add(getNamespace(subject));
                        linkList.addAll(getLink(subject, "supervises"));
                    }
                    else if(subject.contains("service")){
                        if(subject.contains("db")){
                            if (subject.contains("response_time")||subject.contains("throughput"))
                                result.add(getPropertyNodes(subject, "serviceDatabase"));
                            else{
                                result.add(getService(subject));
                                linkList.addAll(getLink(subject, "profile"));
                            }
                        }
                        else {
                            if (subject.contains("success_rate")||subject.contains("response_time"))
                                result.add(getPropertyNodes(subject, "serviceServer"));
                            else{
                                result.add(getService(subject));
                                linkList.addAll(getLink(subject, "profile"));
                            }
                        }
                    }
                    else if(subject.contains("pods")){
                        result.add(getPod(subject));
                        linkList.addAll(getLink(subject, "deployed_in"));
                        linkList.addAll(getLink(subject, "contains"));
                        linkList.addAll(getLink(subject, "provides"));
                    }
                    else if(subject.contains("containers")){
                        if (subject.contains("container_fs_io_current")||subject.contains("container_fs_usage_bytes")||subject.contains("container_fs_reads_bytes_total")||subject.contains("container_fs_writes_bytes_total")){
                            result.add(getPropertyNodes(subject, "containerStorage"));
                        }else if (subject.contains("network_receive_bytes")||subject.contains("network_transmit_bytes")){
                            result.add(getPropertyNodes(subject, "containerNetwork"));
                        }else {
                            result.add(getContainer(subject));
                            linkList.addAll(getLink(subject, "profile"));
                        }
                    }
                }
            }
            qExec.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        final_list.put("nodeList", result);
        final_list.put("linkList", linkList);
        return final_list;
    }

    public static Map<String, Object> getNamespace(String urlNode){
        String[] l = urlNode.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", urlNode);
        node.put("name", l[l.length-1]);
        node.put("type", "namespace");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+urlNode+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("name")){
                    pro.put("name", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getServer(String urlNode){
        String[] l = urlNode.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", urlNode);
        node.put("name", l[l.length-1]);
        if(urlNode.contains("192.168.199.191"))
            node.put("type", "masterServer");
        else
            node.put("type", "server");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+urlNode+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            // error
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
//                System.out.println(p+", "+o);
                if(p.equals("creationTimestamp")){
                    pro.put("creationTimestamp", o);
                }
                else if(p.equals("podCIDR")){
                    pro.put("podCIDR", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getService(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "service");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("creationTimestamp")){
                    pro.put("creationTimestamp", o);
                }
                else if(p.equals("resourceVersion")){
                    pro.put("resourceVersion", o);
                }
                else if(p.equals("namespace")){
                    node.put("namespace", o);
                }
                else if(p.equals("clusterIP")){
                    pro.put("clusterIP", o);
                }
                else if(p.equals("type")){
                    pro.put("service_type", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getPropertyNodes(String url, String type){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", type);
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("value")){
                    pro.put("value", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getPod(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "pod");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next();
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length - 1];
                String o = q.get("o").toString();
                if (p.equals("deployed_in")) {
                    pro.put("deployed_in", o);
                } else if (p.equals("contains")) {
                    pro.put("contains", o);
                } else if (p.equals("qosClass")) {
                    pro.put("qosClass", o);
                } else if (p.equals("podIP")) {
                    pro.put("podIP", o);
                } else if (p.equals("startTime")) {
                    pro.put("startTime", o);
                } else if (p.equals("hostIP")) {
                    pro.put("hostIP", o);
                } else if (p.equals("phase")) {
                    pro.put("phase", o);
                } else if (p.equals("namespace")) {
                    pro.put("namespace", o);
                } else if (p.equals("nodeName")) {
                    pro.put("nodeName", o);
                }
            }
            qE.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static Map<String, Object> getContainer(String url){
        String[] l = url.split("/");
        Map<String, Object> node = new HashMap<>();
        node.put("id", url);
        node.put("name", l[l.length-1]);
        node.put("type", "container");
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?p ?o WHERE {\n" +
                "\t<"+url+"> ?p ?o\n" +
                "}");
        Map<String, Object> pro = new HashMap<>();
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {
            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                QuerySolution q = rs.next() ;
                String[] plist = q.get("p").toString().split("/");
                String p = plist[plist.length-1];
                String o = q.get("o").toString();
                if(p.equals("restartCount")){
                    pro.put("restartCount", o);
                }
                else if(p.equals("image")){
                    pro.put("image", o);
                }
                else if(p.equals("ready")){
                    pro.put("ready", o);
                }
                else if(p.equals("containerID")){
                    pro.put("containerID", o);
                }
                else if(p.equals("imageID")){
                    pro.put("imageID", o);
                }
            }
            qE.close();
        }
        node.put("property", pro);
        System.out.println(node);
        return node;
    }

    public static List<Map<String, Object>> getLink(String url, String linkType){
        List<Map<String, Object>> list = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/query");
        Query qNode = QueryFactory.create("SELECT ?s ?o WHERE {\n" +
                "\t?s <"+url+"/"+linkType+"> ?o\n" +
                "}");
        try ( RDFConnectionFuseki conn = (RDFConnectionFuseki)builder.build() ) {

            QueryExecution qE = conn.query(qNode);
            ResultSet rs = qE.execSelect();
            while (rs.hasNext()) {
                Map<String, Object> link = new HashMap<>();
                QuerySolution q = rs.next() ;
                link.put("sid", q.get("s").toString());
                link.put("tid", q.get("o").toString());
                link.put("name", linkType);
                link.put("type", linkType);
                list.add(link);
            }
            qE.close();
        }
        System.out.println(list);
        return list;
    }

    public static boolean addNode(HashMap data){
        try {
            String url = data.get("id").toString();
            HashMap property = (HashMap) data.get("property");

            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(url);
            for (Object key : property.keySet()) {
                resource.addProperty(model.createProperty(url, "/" + (String) key), (String) property.get(key));
            }
            DataAccessor.getInstance().add(model);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean addLink(HashMap data){
        String fromUrl = (String)data.get("tid");
        String toUrl = (String)data.get("sid");
        String type = (String)data.get("type");
        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"> j0:"+type +"<"+toUrl+">\n" +
                "}";
        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                .destination("http://10.60.38.181:30300/DevKGData/update");

        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
            connAddRelation.update(addRelation);
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteAll(){
        try {
            RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                    .destination("http://10.60.38.181:30300/DevKGData/update");
            String deleteAll = "DELETE WHERE\n" +
                    "{\n" +
                    "\t?s ?p ?o .\n" +
                    "}";
            try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                connAddRelation.update(deleteAll);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        storeNamespaceName();
        storeEnvironment();
        storeServerName();
        storeMasterNode("192.168.199.191");
        storeServiceName("sock-shop");
        storePodName("sock-shop");
        podToService("10.60.38.181","sock-shop");
        podToServer("10.60.38.181","sock-shop");
//        Map<String, Object> result = getAllNodesAndLinks();
//        System.out.println(result);
    }
}
