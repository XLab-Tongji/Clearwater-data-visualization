package neo4j;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neo4jentities.DataAccessor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.commons.codec.binary.Base64;

import org.apache.jena.riot.web.HttpOp;
import org.springframework.stereotype.Component;
import org.terracotta.statistics.Time;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static neo4j.Neo4jDriver.*;
import static neo4j.prometheusDriver.getProInfor;


@Component
public class FusekiDriver {

    public static Map<String, Object> getAllNodesAndLinks(){
        Map<String, Object> final_list = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        List<Map<String, Object>> eventList = new ArrayList<>();
        List<Map<String, Object>> linkList = new ArrayList<>();
        List<String> timeList = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create()
                .destination("http://10.60.38.173:3030/DevKGData/query");

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
                    else if(subject.contains("timestamp")){
                        result.add(getTimestamp(subject));
                    }
                    else if(subject.contains("environment")){
                        result.add(getEnv(subject));
                        linkList.addAll(getLink(subject, "has"));
                    }
                    else if(subject.contains("namespace")){
                        result.add(getNamespace(subject));
                        linkList.addAll(getLink(subject, "supervises"));
                    }
                    else if(subject.contains("event")){
                        Map<String, Object> event = getEventNodes(subject);
                        result.add(event);
                        eventList.add(event);
                        linkList.addAll(getLink(subject, "starts_at"));
                        linkList.addAll(getLink(subject, "ends_at"));
                    }
                    else if(subject.contains("service")){
                        if(subject.contains("db")){
                            if (subject.contains("latency")||subject.contains("throughput"))
                                result.add(getPropertyNodes(subject, "serviceDatabase"));
                            else{
                                result.add(getService(subject));
                                linkList.addAll(getLink(subject, "profile"));
                            }
                        }
                        else {
                            if (subject.contains("success_rate")||subject.contains("latency"))
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
                        if (subject.contains("CPU_Usage")||subject.contains("MEM_Usage")){
                            result.add(getPropertyNodes(subject, "containerStorage"));
                        }else if (subject.contains("Network_Input_Bytes")||subject.contains("Network_Output_Bytes")||subject.contains("Network_Input_Packets")||subject.contains("Network_Output_Packets")){
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
        try {
            timeList = getTimesFromMongo();
        } catch (Exception e){
            e.printStackTrace();
        }
        final_list.put("nodeList", result);
        final_list.put("eventList", eventList);
        final_list.put("linkList", linkList);
        final_list.put("timeList", timeList);
        return final_list;
    }

    public static Map<String, Object> getAllByTime(String time){
        return getOneFromMongo(time);
    }

    public static boolean addNode(HashMap data){
        try {
            String url = data.get("id").toString();
            String[] temp = url.split("/");
            String urltemp = url.replace("/"+temp[temp.length-1],"");
            System.out.println(urltemp);
            HashMap property = (HashMap) data.get("property");

            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(url);
            try{
                if (judgeExist(url)){
                    resource.addProperty(model.createProperty(urltemp, "/name"),(String)data.get("name"));
                    resource.addProperty(model.createProperty(url,"/type"),(String)data.get("type"));
                    System.out.println(data.get("name"));
                    for (Object key : property.keySet()) {
                        resource.addProperty(model.createProperty(url, "/" + key), (String) property.get(key));
                    }
                    DataAccessor.getInstance().add(model);
                }
                else {
                    System.out.println("same name");
                    return false;
                }
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean readJekins(HashMap data){

        String fullurl = ((HashMap)data.get("build")).get("full_url").toString();
        String[] list = fullurl.split("/");
        String name = data.get("name").toString();
        name += "_";
        name += list[list.length-1];
        System.out.println(name);
        String number = String.valueOf((int)((HashMap)data.get("build")).get("number"));
        String status = ((HashMap)data.get("build")).get("status").toString();
        String phase= ((HashMap)data.get("build")).get("phase").toString();
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        String serviceUrl = "http://event/"+address+"/"+name;
        if (phase.equals("FINALIZED")){
            // 创建Timestamp节点 返回id
            Date day=new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(day);
            // 创建Timestamp节点 返回id
            String timeID = storeTimestamp(time);
//            String eventID = findEvent(serviceUrl);
            String eventID = serviceUrl;
            System.out.println("-----------"+eventID);
            //连接
            HashMap hashMap = new HashMap();
            hashMap.put("sid",eventID);
            hashMap.put("tid",timeID);
            hashMap.put("type","ends_at");
            addLink2(hashMap, true, time);
        }else{
            Date day=new Date();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(day);
            // 创建Timestamp节点 返回id
            String timeID = storeTimestamp(time);
            HashMap map = new HashMap();
            map.put("full_url",fullurl);
            map.put("name",name);
            map.put("number",number);
            map.put("status",status);
            String eventID = addEvent(map);
            //连接
            HashMap hashMap = new HashMap();
            hashMap.put("sid",eventID);
            hashMap.put("tid",timeID);
            hashMap.put("type","starts_at");
            addLink2(hashMap, true, time);

        }
        return true;
    }

    public static boolean readKapacitor(HashMap data){
        String address = "10.60.38.173";
        String message = data.get("message").toString();
        //判断有没有type字段，没有则加一个
        if (!data.containsKey("type")) {
            data.put("type", "alert");
        }
        String serviceUrl = "http://event/" + address + "/" + message;
        Date day=new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(day);
        // 创建Timestamp节点 返回id
        String timeID = storeTimestamp(time);
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(serviceUrl);
            System.out.println(serviceUrl);
            System.out.println("-------");
            for (Object key : data.keySet().toArray()
                 ) {
                key = key.toString();
                resource.addProperty(model.createProperty(serviceUrl, "/" + key), data.get(key).toString() );
            }
            DataAccessor.getInstance().add(model);
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        HashMap hashMap = new HashMap();
        hashMap.put("sid",serviceUrl);
        hashMap.put("tid",timeID);
        hashMap.put("type","time");
        addLink2(hashMap, true, time);
        return true;
    }

    public static String addEvent(HashMap data){
        String httpUrl = "http://10.60.38.173:5530/tool/api/v1.0/get_node";
        String jsonString = getData(httpUrl);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String address = (String) jsonObject.getJSONObject("detail").keySet().toArray()[0];
        String full_url = data.get("full_url").toString();
        String name = data.get("name").toString();
        String number = data.get("number").toString();
        String status = data.get("status").toString();
        String serviceUrl = "http://event/"+address+"/"+name;
        try {
            Model model = ModelFactory.createDefaultModel();
            Resource resource = model.createResource(serviceUrl);
            System.out.println(serviceUrl);
            System.out.println("-------");
            resource.addProperty(model.createProperty(serviceUrl, "/full_url"), full_url);
            resource.addProperty(model.createProperty(serviceUrl, "/name"), name);
            resource.addProperty(model.createProperty(serviceUrl, "/nubmer"), number);
            resource.addProperty(model.createProperty(serviceUrl, "/status"), status);
            DataAccessor.getInstance().add(model);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return serviceUrl;
    }

    public static boolean addLink2(HashMap data, boolean flag, String time){
        String fromUrl = (String)data.get("sid");
        String toUrl = (String)data.get("tid");
        String type = (String)data.get("type");
        System.out.println(fromUrl);
        System.out.println(toUrl);
        System.out.println(type);
        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"> j0:"+type +" <"+toUrl+">\n" +
                "}";
        System.out.println(addRelation);

        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                .destination("http://10.60.38.173:3030/DevKGData/update");

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpOp.setDefaultHttpClient(httpclient);
        builderAddRelation.httpClient(httpclient);

        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
            connAddRelation.update(addRelation);
            Map<String, Object> result = getAllNodesAndLinks();
            if (flag){
                return save2MongoByTime(result, time);
            }
            else {
                return save2Mongo(result);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addLink(HashMap data){
        return addLink2(data, false, "");
//        String fromUrl = (String)data.get("sid");
//        String toUrl = (String)data.get("tid");
//        String type = (String)data.get("type");
//        System.out.println(fromUrl);
//        System.out.println(toUrl);
//        System.out.println(type);
//        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
//                "INSERT DATA{\n" +
//                "<"+fromUrl+"> j0:"+type +" <"+toUrl+">\n" +
//                "}";
//        System.out.println(addRelation);
//
//        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
//                .destination("http://10.60.38.173:3030/DevKGData/update");
//
//        CredentialsProvider credsProvider = new BasicCredentialsProvider();
//        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
//        credsProvider.setCredentials(AuthScope.ANY, credentials);
//        HttpClient httpclient = HttpClients.custom()
//                .setDefaultCredentialsProvider(credsProvider)
//                .build();
//        HttpOp.setDefaultHttpClient(httpclient);
//        builderAddRelation.httpClient(httpclient);
//
//        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
//            connAddRelation.update(addRelation);
//            Map<String, Object> result = getAllNodesAndLinks();
//            if(!save2Mongo(result)) return false;
//        }catch (Exception e){
//            e.printStackTrace();
//            return false;
//        }
//        return true;

    }

    public static boolean deleteLinks(List<HashMap> data){
        try {
            for (HashMap link: data) {
                String sid = (String)link.get("sid");
                String tid = (String)link.get("tid");
                if(!deleteOneLink(sid, tid)) return false;
            }
            Map<String, Object> result = getAllNodesAndLinks();
            if(!save2Mongo(result)) return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteNodes(List<HashMap> data){
        try {
            for (HashMap link: data) {
                String id = (String)link.get("id");
                if(!deleteOneNode(id)) return false;
            }
            Map<String, Object> result = getAllNodesAndLinks();
            if(!save2Mongo(result)) return false;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean deleteAll(){
        try {
            RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                    .destination("http://10.60.38.173:3030/DevKGData/update");
            String deleteAll = "DELETE WHERE\n" +
                    "{\n" +
                    "\t?s ?p ?o .\n" +
                    "}";

//// first we use a method on HttpOp to log in and get our cookie
//            Params params = new Params();
//            params.addParam("username", "admin");
//            params.addParam("password", "D0rlghQl5IAgYOm");
//            HttpOp.execHttpPostForm("http://10.60.38.173:3030", params , null, null, null, httpContext);
//
//// now our cookie is stored in httpContext
//            CookieStore cookieStore = httpContext.getCookieStore();
//
//// lastly we build a client that uses that cookie
//            HttpClient httpclient = HttpClients.custom()
//                            .setDefaultCookieStore(cookieStore)
//                            .build();
//            HttpOp.setDefaultHttpClient(httpclient);

            System.out.println(Base64.class.getProtectionDomain().getCodeSource().getLocation());
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
            credsProvider.setCredentials(AuthScope.ANY, credentials);
            HttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credsProvider)
                    .build();
            HttpOp.setDefaultHttpClient(httpclient);
            builderAddRelation.httpClient(httpclient);


            try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
                connAddRelation.update(deleteAll);
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean modifyNode(HashMap data){
        try {
            String url = data.get("id").toString();
            boolean flag = deleteOneNode(url);
            if (!flag){
                return flag;
            }else if(addNode(data)){
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
            else return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static boolean modifyLink(HashMap data){
        try {
            String fromUrl = (String)data.get("sid");
            String toUrl = (String)data.get("tid");
            boolean flag = deleteOneLink(fromUrl,toUrl);
            if (!flag){
                return flag;
            }else if(addLink(data)){
                Map<String, Object> result = getAllNodesAndLinks();
                if(!save2Mongo(result)) return false;
            }
            else return false;
        }catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    public static ArrayList getResourcesWithQuery(){
        ArrayList result = new ArrayList();
        Model model = DataAccessor.getInstance().getModel();
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (r.hasProperty(model.createProperty(r.toString()+"/query"))){
                result.add(r);
            }
        }
        System.out.println(result);
        return result;
    }

    //将event和实体的属性对应起来的接口（添加事件和指标之间的关联度）
    public static void addLinkEvent2S(){
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        Date start = new Date();
        Date end = new Date();
        try {
            start = DateFormat.parse("2019-08-06 00:00:00");
            end = DateFormat.parse("2019-08-06 23:59:59");
        } catch(ParseException px) {
            px.printStackTrace();
        }
        Model model = DataAccessor.getInstance().getModel();
        ArrayList<Resource> resources = getResourcesWithQuery();
        //受到时区的影响
        Map dates = getEventInFuseki(start, end);
        List times = new ArrayList<>();
        for (Date i:(ArrayList<Date>)dates.get("Date")
        ) {
            times.add(i.getTime()/1000);
        }
        Collections.sort(times);
        for (Resource i:resources
        ) {
            Statement statement = i.getProperty(model.createProperty(i.toString()+"/query"));
            System.out.println(start.getTime()/1000 + " " + end.getTime()/1000);

            JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),start.getTime()/1000 + "", end.getTime()/1000 + "");
            //JSONArray proInfor = getProInfor(statement.getString().replace(" ",""),times.get(0)+"", times.get(times.size()-1)+"");
            if (proInfor == null)continue;
            List timeList = new ArrayList();
            for (Object j:times
                 ) {
                for (Object k:proInfor
                     ) {
                    if ((Long)j <= ((JSONArray)k).getLong(0) ){
                        timeList.add(((JSONArray)k).getLong(0));
                        break;
                    }
                }
            }
            System.out.println("request---------");
            System.out.println(timeList);
            System.out.println(proInfor);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("file1",timeList);
            jsonObject.put("file2", proInfor);
            String re =  util.HttpPostUtil.postData(jsonObject.toJSONString());
            if (re != null){
                for (String ev:(ArrayList<String>)dates.get("Event")
                     ) {
                    addCorrelation(ev, i.toString(), re);
                }
            }
        }



    }


    //查询指定范围内发生的时间
    public static Map<String, ArrayList> getEventInFuseki(Date startTime,Date endTime)
    {
        SimpleDateFormat DateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //加上时间
        String start = DateFormat.format(startTime);
        String end = DateFormat.format(endTime);
        Map result = new HashMap<>();
        result.put("Date", new ArrayList<>());
        result.put("Event", new ArrayList<>());
        Model model = DataAccessor.getInstance().getModel();
        ResIterator iter = model.listSubjects();
        while (iter.hasNext()) {
            Resource r = iter.nextResource();
            if (r.toString().contains("event"))
            {
                String timeProperty=r.getProperty(model.createProperty(r.toString()+"/starts_at")).getResource().toString();
                //截取出时间字符串，去掉中间的“-”
                int length=timeProperty.length();
                String time=timeProperty.substring(length-19,length-9)+" "+timeProperty.substring(length-8);
                if(time.compareTo(start)>=0&&time.compareTo(end)<=0)
                {
                    try {
                        Date dateResult = DateFormat.parse(time);
                        ((ArrayList)result.get("Date")).add(dateResult);
                        ((ArrayList)result.get("Event")).add(r.toString());
                    }catch(ParseException px) {
                        px.printStackTrace();
                    }
                }
            }
        }
        System.out.println(result);
        return result;
    }

    public static boolean addCorrelation(String fromUrl, String toUrl, String correlation){
        String addRelation = "PREFIX j0:<"+fromUrl+"/>\n" +
                "INSERT DATA{\n" +
                "<"+fromUrl+"> j0:"+ correlation +" <"+toUrl+">\n" +
                "}";
        System.out.println(addRelation);
        RDFConnectionRemoteBuilder builderAddRelation = RDFConnectionFuseki.create()
                .destination("http://10.60.38.173:3030/DevKGData/update");
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials credentials = new UsernamePasswordCredentials("admin", "D0rlghQl5IAgYOm");
        credsProvider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        HttpOp.setDefaultHttpClient(httpclient);
        builderAddRelation.httpClient(httpclient);
        try ( RDFConnectionFuseki connAddRelation = (RDFConnectionFuseki)builderAddRelation.build() ) {
            connAddRelation.update(addRelation);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }


    public static void main(String[] args) {
        //addLinkEvent2S();
    }
}
