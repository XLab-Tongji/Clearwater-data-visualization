package neo4j;


import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Path;
import org.neo4j.driver.v1.types.Relationship;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import util.CommonUtil;

import java.io.*;
import java.util.*;

import static global.globalvalue.neo4japi;
import static org.neo4j.driver.v1.Values.parameters;
import static service.CsvService.csvTimestamp;

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
        Driver driver = GraphDatabase.driver("bolt://localhost:7687",
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
        System.out.println(resultgraph);
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

            String node[] = line.split(";");
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
//                System.out.println(string[1]+"|"+string[3]);
//                System.out.println(string[2]);

                map.put("source", string[1]);
                map.put("target", string[3]);
                //source = AddCausationNode(string[1]);
                //target = AddCausationNode(string[3]);
                source = 1;
                target = 1;
                if (source!=0 && target!=0) {
                    String relationship = "";
                    if (string[2].equals("-->") || string[2].equals("o->"))
                        relationship = "casues";
                    if (string[2].equals("o-o"))
                        relationship = "has_causation";
                    if (string[2].equals("<->"))
                        relationship = "has_latent_variable";
//                    if (relationship.equals("has_latent_variable")) {
//                        AddMetricRelation(source, target, "has_latent_variable");
//                        AddMetricRelation(target, source, "has_latent_variable");
//                    } else {
//                        AddMetricRelation(source, target, relationship);
//                    }
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
            resultgraph.put("nodes", allnodes);
            resultgraph.put("relations", allrelations);
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


    //第一次适用需要运行 CREATE INDEX ON :Resource(uri)
    public static void importTtl(String typePath, String systemPath) {
        Driver driver = GraphDatabase.driver(neo4japi+":7687",
                AuthTokens.basic("neo4j","1234"));
        //初始化驱动器
        try (Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                String namespace="CREATE (:NamespacePrefixDefinition " +
                        "{`http://localhost/KGns/#`:''," +
                        "`http://www.w3.org/1999/02/22-rdf-syntax-ns#`:'rdf'," +
                        "`http://www.w3.org/2000/01/rdf-schema#`:'rdfs'," +
                        "`http://xmlns.com/foaf/0.1/`:'foaf'," +
                        "`http://localhost/KGns/relationship#`:'rel'," +
                        "`http://localhost/KGns/Container_attributes#`:'Container_attributes'," +
                        "`http://localhost/KGns/Service_attributes#`:'Service_attributes'})";
                String ontology="CALL semantics.importRDF(\"file://" + typePath + "\", \"Turtle\")";
                String system="CALL semantics.importRDF(\"file://" + systemPath + "\", \"Turtle\")";
                String systemFileName=systemPath.substring(systemPath.lastIndexOf("/")+1);
                String systemName=systemFileName.substring(0,systemFileName.lastIndexOf("."));
                String createSystemNode="create(n:System{name:'"+systemName+"',uri:'www.tongji.edu.cn/"+systemName+"'})return n";
                String addRelation= "match(a:System),(b)where(not b:System and not (a)-->(b))create (a)-[r:has]->(b) return r";
                System.out.println(ontology);
                System.out.println(system);

                tx.run(namespace);
                StatementResult result2 = tx.run(ontology);
                tx.run(system);
                tx.run(createSystemNode);
                tx.run(addRelation);

                System.out.println(result2.toString());
                tx.success();
            }
        }
        driver.close();
    }

    public static HashMap<String, List<HashMap<String,Object>>> getAllNodesandlinks(String systemName) {
        Driver driver = GraphDatabase.driver(neo4japi+":7687",
                AuthTokens.basic("neo4j","1234"));
        HashMap<String, List<HashMap<String,Object>>> resultgraph = new HashMap<>();
        try(Session session = driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                StatementResult result = tx.run("Match p=(n)-[r]-(m) , (a:System)-[e:has]-(n) where a.name = '"+systemName+"' return p as nodesrelation");
               // StatementResult result = tx.run("match (:System{name:'"+systemName+"'})--(n)return n as nodesrelation");
                List<HashMap<String,Object>> allnodes = new ArrayList<>();
                List<HashMap<String,Object>> allrelations = new ArrayList<>();
                while(result.hasNext()){
                    Record record = result.next();
                    Path path = record.get("nodesrelation").asPath();
                    Iterable<Node> nodes = path.nodes();
                    for(Node node:nodes) {
                        HashMap<String,Object> nod = new HashMap();
                        nod.put("id",node.id());
                        nod.put("properties", node.asMap());
                        String[] name = node.asMap().get("uri").toString().split("#");
                        nod.put("name", name[name.length-1]);
                        Iterator iterator = node.labels().iterator();
                        iterator.next();
                        if (iterator.hasNext()){
                            String type = iterator.next().toString();
                            if (type.contains("owl")) continue;
                            nod.put("type",type.substring(2));
                        } else {
//                            nod.put("type", "null");
                            continue;
                        }
                        if(!allnodes.contains(nod))
                            allnodes.add(nod);
                    }
                    Iterable<Relationship> relations = path.relationships();
                    for(Relationship relationship:relations) {
                        HashMap<String,Object> rela = new HashMap();
                        rela.put("sid",relationship.startNodeId());
                        rela.put("tid",relationship.endNodeId());
                        rela.put("type",relationship.type());
                        rela.put("name",relationship.type());
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


    public static void main(String[] args) {

        new Neo4jDriver().getAllNodesandlinks("0000");
//        importTtl("F:/Xlab/ontology.ttl",
//                "F:/Xlab/system.ttl");

    }
}