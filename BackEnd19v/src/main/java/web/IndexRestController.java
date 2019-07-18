package web;

import neo4j.MongoDriver;
import neo4j.Neo4jDriver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.fileupload.disk.DiskFileItem;
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


@RestController
public class IndexRestController {
    @Autowired
    private Neo4jDriver neo4jDriver;
    @Cacheable(cacheNames = "getdata")
    @RequestMapping(value = "/api/getData" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getData(@RequestParam("name_1") String name1,@RequestParam("name_2") String name2,@RequestParam("edge") int edge) {
        //HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = HashMap<String, List<HashMap<String,Object>>>
        HashMap<String, List<HashMap<String,Object>>> hashMap = neo4jDriver.getresult(name1,name2,1,edge);
        return hashMap;
    }
    @RequestMapping(value = "/api/getOneNodeData" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getOneNodeData(@RequestParam("name_1") String name1,@RequestParam("edge") int edge) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getOneNoderesult(name1,1,edge);
    }
    @Cacheable(cacheNames = "getdatapart")
    @RequestMapping(value = "/api/getDataPart" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getDataPart(@RequestParam("name_1") String name1,@RequestParam("name_2") String name2,@RequestParam("edge_1") int edge1,@RequestParam("edge_2") int edge2) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getresult(name1,name2,edge1,edge2);
    }
    @Cacheable(cacheNames = "getonenodedatapart")
    @RequestMapping(value = "/api/getOneNodeDataPart" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getOneNodeDataPart(@RequestParam("name_1") String name1,@RequestParam("edge_1") int edge1,@RequestParam("edge_2") int edge2) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getOneNoderesult(name1,edge1,edge2);
    }
    @RequestMapping(value = "/api/getAll" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getAll() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getAllNodes();
    }
    @RequestMapping(value = "/api/addOneNode" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, Object> addOneNode(@RequestBody Map<String, Object> map) {
        String label = (String)map.get("label");
        int id = (int)map.get("id");
        String name = (String)map.get("name");
        String type = (String)map.get("type");
        String layer = (String)map.get("layer");
        String performance = (String)map.get("performance");
        ArrayList<Integer> relations = (ArrayList<Integer>)map.get("relations");
        return neo4jDriver.addOneNode(label, id, name, type, layer, performance, relations);
    }

    @RequestMapping(value = "/api/getNeighbors" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getNeighbors(@RequestParam("name") String name) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getNeighbors(name);
    }
    @RequestMapping(value = "/api/getLabel" , method = RequestMethod.GET ,produces = "application/json")
    //获取特定标签节点
    public HashMap<String, List<HashMap<String,Object>>> getLabel(@RequestParam("label") String label) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getLabel(label);
    }
    @RequestMapping(value = "/api/getAllLabel" , method = RequestMethod.GET ,produces = "application/json")
    //获取所有标签
    public HashMap<String, List<String>> getAllLabel() {
        System.out.println("1243568");
        return neo4jDriver.getAllLabel();
    }

//    @PostMapping("/yamldeal")
//    @ResponseBody
    @RequestMapping(value = "/api/getDeployment" , method = RequestMethod.GET ,produces = "application/json")
    //获取deployment节点
    public HashMap<String, List<HashMap<String,Object>>> getDeployment() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        System.out.println("1243568");
        return neo4jDriver.getDeploymentNodes();
    }
    @RequestMapping(value = "/api/getService" , method = RequestMethod.GET ,produces = "application/json")
    //获取service节点
    public HashMap<String, List<HashMap<String,Object>>> getService() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getServiceNodes();
    }
    @RequestMapping(value = "/api/getContainer" , method = RequestMethod.GET ,produces = "application/json")
    //获取container节点
    public HashMap<String, List<HashMap<String,Object>>> getContainer() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getContainerNodes();
    }
    @RequestMapping(value = "/api/getCausation" , method = RequestMethod.GET ,produces = "application/json")
    //获取因果关系节点
    public HashMap<String, List<HashMap<String,Object>>> getCausation() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getCausationNodes();
    }
    @RequestMapping(value = "/api/getSystem" , method = RequestMethod.GET ,produces = "application/json")
    //获取因果关系节点
    public HashMap<String, List<HashMap<String,Object>>> getSystem() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getSystemNodes();
    }
    @RequestMapping(value = "/api/getDataset" , method = RequestMethod.GET ,produces = "application/json")
    //获取因果关系节点
    public HashMap<String, List<HashMap<String,Object>>> getDataset() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getDataset();
    }
    @RequestMapping(value = "/api/getCSV",method = RequestMethod.GET,produces = "application/json")
    //获取csv名称
    public HashMap<String,ArrayList> getCsv(HttpServletRequest request,
                                         HttpServletResponse response){
        HashMap<String,ArrayList> hashMap = new HashMap<>();
        ArrayList list = new ArrayList();
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        File file = new File(savePath);
        if (file.exists()) {
            File[] files = file.listFiles();
            try {
                for (File file2 : files) {
                    if (file2.getName().contains(".csv"))
                        list.add(file2.getName());
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        hashMap.put("CSV",list);
        return hashMap;
    }

    @RequestMapping(value = "/api/getOperationData" , method = RequestMethod.GET ,produces = "application/json")
    //处理运维数据 目前文件类型：csv
    public HashMap<String,HashMap<String,ArrayList>> getOperationData(HttpServletRequest request,
                                                                      HttpServletResponse response,@RequestParam("filename") String filename) {
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        HashMap<String,ArrayList> hashMap = csvOperationData(filePath);
        HashMap<String,HashMap<String,ArrayList>> map = new HashMap<>();
        map.put("Operations",hashMap);
        return map;
    }
    @RequestMapping(value = "/api/metricInfo",method = RequestMethod.GET,produces = "application/json")
    //获取csv属性
    public HashMap<String,HashMap<String,Object>> getMetricInfo(HttpServletRequest request,
                                                                HttpServletResponse response,@RequestParam("filename") String filename){
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        return metricInfo(filePath);
    }
    //service/carts:80/response_time contianer/docker_compose_carts/cpu_util


    @RequestMapping(value = "/api/getTimestamp" , method = RequestMethod.GET ,produces = "application/json")
    //获取时间戳 目前文件类型：csv
    public HashMap<String,ArrayList> getTimestamp(HttpServletRequest request,
                                  HttpServletResponse response,@RequestParam("filename") String filename) {
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        ArrayList arrayList = csvTimestamp(filePath);
        HashMap<String,ArrayList> map = new HashMap<>();
        map.put("Timestamp",arrayList);
        return map;
    }
    @RequestMapping(value = "/api/yamldeal",method = RequestMethod.POST)
    //处理yaml文件接口
    public ArrayList yamldeal(@RequestParam("file") MultipartFile file,@RequestParam("systemName") String system) throws IOException {
        Yaml yaml = new Yaml();
        //Object load = yaml.loadAll(new FileInputStream(file));
        Map<String, Object> map = null;
        //System.out.println(yaml.dump(load));
        CommonsMultipartFile cFile = (CommonsMultipartFile) file;
        DiskFileItem fileItem = (DiskFileItem) cFile.getFileItem();
        ArrayList arrayList = new ArrayList();
        try {
            int systemID = AddSystemNode(system);
            for (Object data : yaml.loadAll(fileItem.getInputStream())) {
                HashMap hashMap = new HashMap();
                map = (Map<String, Object>) data;
                neo4jDriver.Deal("", map, hashMap);
                arrayList.add(hashMap);
            }
            int deploymentID =0;
            int containerID =0;
            int serviceID = 0;
            for (int i = 0; i < arrayList.size(); i++) {
                HashMap hashMap = (HashMap) arrayList.get(i);
                for (Object key : hashMap.keySet()) {
                    System.out.println("Key: " + (String) key + " Value: " + hashMap.get(key));
                }
                if (i % 2 ==0){
                    deploymentID =0;
                    containerID =0;
                    serviceID = 0;
                }
                if (hashMap.get("kind").equals("Deployment")){
                    try {
                        String containerPort = hashMap.get("spec-template-spec-containers-ports-containerPort").toString();
                        String name = (String) hashMap.get("metadata-name");
                        String nameSpace = (String) hashMap.get("metadata-namespace");
                        String image = (String) hashMap.get("spec-template-spec-containers-image");

                        String containerName = (String) hashMap.get("spec-template-spec-containers-name");
                        String volumeMount = (String) hashMap.get("spec-template-spec-containers-volumeMounts-name");
                        ArrayList arrayListAdd = (ArrayList) hashMap.get("spec-template-spec-containers-securityContext-capabilities-add");
                        ArrayList arrayListDrop = (ArrayList) hashMap.get("spec-template-spec-containers-securityContext-capabilities-drop");
                        deploymentID = neo4jDriver.AddDeploymentNode("Deployment_Node", containerPort, name, nameSpace, image);
                        containerID = neo4jDriver.AddContainerNode("Container_Node", containerName, volumeMount, arrayListAdd, arrayListDrop);
                        if (containerName.equals(name) && !containerName.equals("") && !name.equals("")){
                            System.out.println("!!");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else if (hashMap.get("kind").equals("Service")){
                    String name = (String)hashMap.get("metadata-name");
                    String nameSpace = (String)hashMap.get("metadata-namespace");
                    String port = hashMap.get("spec-ports-port").toString();
                    String targetPort = hashMap.get("spec-ports-targetPort").toString();
                    serviceID = neo4jDriver.AddServiceNode("Service_Node",port,name,nameSpace,targetPort);
                }
                System.out.print(deploymentID+" "+containerID+" "+serviceID);
                if (deploymentID==0 || containerID==0 || serviceID==0) continue;
                else {
                    neo4jDriver.AddYamlRelation(deploymentID,containerID,serviceID,systemID);
                }
                System.out.println("---------------");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return arrayList;
    }

    @RequestMapping(value = "/api/addServiceRelation" , method = RequestMethod.POST)
    //添加服务之间关系接口 目前文件类型：json
    public Boolean addServiceRelation(HttpServletRequest request,@RequestParam("file") MultipartFile multipartFile) {
//        String label = (String)map.get("label");
//        int id = (int)map.get("id");
//        String name = (String)map.get("name");
//        String type = (String)map.get("type");
//        String layer = (String)map.get("layer");
//        String performance = (String)map.get("performance");
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/json");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + multipartFile.getOriginalFilename();
        File file = new File(filePath);

        try {
            FileUtils.copyInputStreamToFile(multipartFile.getInputStream(),file);
            String content = FileUtils.readFileToString(file);
            JSONObject obj = JSONObject.fromObject(content);
            System.out.println(obj);
            JSONArray jsonArray = obj.getJSONArray("Graph");
            Iterator<Object> graph =jsonArray.iterator();
            while (graph.hasNext()){
                JSONObject gh = (JSONObject) graph.next();
                String resourceName = (String)gh.get("ResourceName");
                JSONArray callsJ =(JSONArray)gh.get("Calls");
                try {
                    if (!callsJ.isEmpty()) {
                        Iterator<Object> callIter = callsJ.iterator();
                        ArrayList<String> calls = new ArrayList<>();
                        while (callIter.hasNext()) {
                            calls.add((String) callIter.next());
                        }
                        System.out.println(resourceName);
                        System.out.println(calls);
                        AddServiceRelation(resourceName, calls);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        finally {
            file.delete();
        }
        //ArrayList<Integer> relations = (ArrayList<Integer>)map.get("relations");
        return true;
    }

    @RequestMapping(value = "/api/fileupdate",method = RequestMethod.POST)
    //上传文件接口
    public String fileupdate(HttpServletRequest request,
                             HttpServletResponse response, @RequestParam("file") MultipartFile file,@RequestParam("systemName") String system) {
        String savePath = "";
        if (file.getOriginalFilename().contains("csv")){
            savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        }else if (file.getOriginalFilename().contains("yml")){
                savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/yml");
        }else if (file.getOriginalFilename().contains("txt")){
                savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/txt");
        }else if (file.getOriginalFilename().contains("json")) {
            savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/json");
        }
        savePath = savePath.replace("file:", "");
        File file1 = new File(savePath);
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();
        }
        try {
            InputStream is = file.getInputStream();
            byte[] b = new byte[(int) file.getSize()];
            int read = 0;
            int i = 0;
            while ((read = is.read()) != -1) {
                b[i] = (byte) read;
                i++;
            }
            is.close();
            Long timeMillis = System.currentTimeMillis();

            //String filePath = savePath + "/"  + timeMillis+file.getOriginalFilename();
            String filePath = savePath + "/"  + file.getOriginalFilename();

            System.out.println(timeMillis);

            File file2 = new File(filePath);
            if (file2.exists()){
                return "file already upload";
            }
            if (!file2.exists()) {
                //file2.createNewFile();
                //log.info("临时文件保存路径：" + savePath + "/" + "temp" + "_" + buildInfo[0].getOriginalFilename());
               // arrayList.add(filePath);
                //OutputStream os = new FileOutputStream(new File(savePath + "/" + timeMillis+file.getOriginalFilename()));// 文件原名,如a.txt
                OutputStream os = new FileOutputStream(new File(savePath + "/" + file.getOriginalFilename()));
                os.write(b);
                os.flush();
                os.close();
            }
            if (file.getOriginalFilename().contains("csv")){
                System.out.println("csv!");
                AddDataSetNode(filePath,file.getOriginalFilename(),system);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

    @RequestMapping(value = "/api/causationdeal",method = RequestMethod.POST,produces = "application/json")
    //处理因果关系接口 目前文件类型：txt
    public HashMap<String,ArrayList<HashMap<String,Object>>> causationdeal(HttpServletRequest request,
                                                                           HttpServletResponse response, @RequestParam("content") String content){

        String[] strings = content.split("\n");
        return causationData(strings);
    }

    @RequestMapping(value = "/api/addMetric",method = RequestMethod.POST,produces = "application/json")
    //添加metric节点
    public HashMap addMetric(HttpServletRequest request, HttpServletResponse response, @RequestParam("Type") String type,@RequestParam("pod") String podName,
                                                                       @RequestParam("metric") String metricName,@RequestParam("nodeType") String nodeType,@RequestParam("nodeName") String nodName,
                                                                       @RequestParam("dateset") String datasetName,@RequestParam("relation") String relationName){
        return AddMetricNode(type,podName,metricName,datasetName,relationName,nodName,nodeType);
    }

    @RequestMapping(value = "/api/addNods",method = RequestMethod.POST,produces = "application/json")
    //添加Nod节点
    public Boolean addNods(HttpServletRequest request, HttpServletResponse response, @RequestParam("Url") String url){
        return AddNode(url);
    }

    @RequestMapping(value = "/api/getElementName",method = RequestMethod.GET,produces = "application/json")
    //获取element值
    public HashMap<String, ArrayList> getElementName(HttpServletRequest request, HttpServletResponse response){
        return getElement();
    }

    @RequestMapping(value = "/api/postCSV",method = RequestMethod.POST,produces = "application/json")
    //获取上传csv 获取相应的json和txt文件
    public Map<String, Object> postCSV(HttpServletRequest request, HttpServletResponse response,
                                              @RequestParam("algorithm")String algorithm,@RequestParam("filename") String filename){
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        try{
           // File file = new File(filePath);
            System.out.println("66");
            return csvPost(algorithm,filePath);
        }catch (Exception e) {
            System.out.println("66");
            e.printStackTrace();
        }
        return null;
    }













    @RequestMapping(value = "/api/storeEnvironment",method = RequestMethod.POST,produces = "application/json")
    public Boolean storeAll(@RequestParam("masterName") String masterName, @RequestParam("podName") String podName, @RequestParam("serviceName") String serviceName, @RequestParam("address") String address, @RequestParam("namespace") String namespace){
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

    @RequestMapping(value = "/api/getNodesAndLinks",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getNodesAndLinks(){
        return getAllNodesAndLinks();
    }

    @RequestMapping(value = "/api/getAllByTime",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getAllNLByTime(@RequestParam String time){
        return getAllByTime(time);
    }

    @RequestMapping(value = "/api/addNewNode",method = RequestMethod.POST,produces = "application/json")
    public Boolean addNewNode(@RequestBody HashMap data){
        return addNode(data);
    }

    @RequestMapping(value = "/api/addNewLink",method = RequestMethod.POST,produces = "application/json")
    public Boolean addNewLink(@RequestBody HashMap data){
        return addLink(data);
    }

    @RequestMapping(value = "/api/addNewEvent",method = RequestMethod.POST,produces = "application/json")
    public Boolean addNewEvent(@RequestBody HashMap data){
        return readJekins(data);
    }

    @RequestMapping(value = "/api/deleteAll",method = RequestMethod.POST,produces = "application/json")
    public Boolean deleteAllNodes(){
        return deleteAll();
    }

    @RequestMapping(value = "/api/delNodes",method = RequestMethod.POST,produces = "application/json")
    public Boolean delNodes(@RequestBody List<HashMap> data){
        return deleteNodes(data);
    }

    @RequestMapping(value = "/api/delLinks",method = RequestMethod.POST,produces = "application/json")
    public Boolean delLinks(@RequestBody List<HashMap> data){
        return deleteLinks(data);
    }

    @RequestMapping(value = "/api/modifyOneNode",method = RequestMethod.POST,produces = "application/json")
    public Boolean modifyOneNode(@RequestBody HashMap data){
        return modifyNode(data);
    }

    @RequestMapping(value = "/api/modifyOneLink",method = RequestMethod.POST,produces = "application/json")
    public Boolean modifyOneLink(@RequestBody HashMap data){
        return modifyLink(data);
    }

    @RequestMapping(value = "/api/createPrometheus", method = RequestMethod.POST, produces = "application/json")
    public Boolean createPrometheus(@RequestBody HashMap data) { return newPrometheus(data);}

    private TimerUtil timerUtil = new TimerUtil();

    @RequestMapping(value = "/api/openTimer", method = RequestMethod.POST, produces = "application/json")
    public Boolean openTimer(@RequestBody Integer period){
        try {
            // 匿名方法
            Runnable runnable = () -> {
                // 把当前数据库中所有数据存到mongodb中
                Map<String, Object> result = getNodesAndLinks();
                save2Mongo(result);
            };
            final long time = 5;//延迟执行实际：5秒
            timerUtil.scheduleAtFixedRate(runnable,time,period);
            System.out.println("定时存储Mongo服务已开启！模拟数据变化频率："+period+"秒");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("调用timer失败");
            return false;
        }
        return true;
    }

    @RequestMapping(value = "/api/shutdownTimer", method = RequestMethod.POST, produces = "application/json")
    public Boolean shutdownTimer(){
        try {
            timerUtil.shutdown();
            System.out.println("定时存储Mongo服务已关闭");
        } catch (Exception e){
            e.printStackTrace();
            System.out.println("调用timer失败");
            return false;
        }
        return true;
    }

    @RequestMapping(value = "/api/StartCausation", method = RequestMethod.POST, produces = "application/json")
    public Boolean StartCausation(HttpServletRequest request,@RequestParam("Start") String start,@RequestParam("End") String end){
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload/csv");
        savePath = savePath.replace("file:", "");
        ArrayList arrayList = getAllQuery();
        DealPrometheusRequest(start,end,arrayList,savePath);
        return true;
    }


}
