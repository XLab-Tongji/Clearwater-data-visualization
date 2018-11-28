package web;

import com.csvreader.CsvReader;
import neo4j.Neo4jDriver;
import org.neo4j.ogm.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static neo4j.Neo4jDriver.csvOperationData;
import static neo4j.Neo4jDriver.csvTimestamp;


@RestController
public class IndexRestController {
    @Autowired
    private Neo4jDriver neo4jDriver;
    @Cacheable(cacheNames = "getdata")
    @RequestMapping(value = "/api/getData" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getData(@RequestParam("name_1") String name1,@RequestParam("name_2") String name2,@RequestParam("edge") int edge) {
        //HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = HashMap<String, List<HashMap<String,Object>>>
        HashMap<String, List<HashMap<String,Object>>> hashMap = neo4jDriver.getresult(name1,name2,1,edge);
        return hashMap;
    }
    @RequestMapping(value = "/api/getOneNodeData" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getOneNodeData(@RequestParam("name_1") String name1,@RequestParam("edge") int edge) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getOneNoderesult(name1,1,edge);
    }
    @Cacheable(cacheNames = "getdatapart")
    @RequestMapping(value = "/api/getDataPart" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getDataPart(@RequestParam("name_1") String name1,@RequestParam("name_2") String name2,@RequestParam("edge_1") int edge1,@RequestParam("edge_2") int edge2) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getresult(name1,name2,edge1,edge2);
    }
    @Cacheable(cacheNames = "getonenodedatapart")
    @RequestMapping(value = "/api/getOneNodeDataPart" , method = RequestMethod.POST ,produces = "application/json")
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
    @RequestMapping(value = "/api/getNeighbors" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getNeighbors(@RequestParam("name") String name) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getNeighbors(name);
    }
    @RequestMapping(value = "/api/getLabel" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getLabel(@RequestParam("label") String label) {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getLabel(label);
    }
//    @PostMapping("/yamldeal")
//    @ResponseBody
    @RequestMapping(value = "/api/getDeployment" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getDeployment() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        System.out.println("1243568");
        return neo4jDriver.getDeploymentNodes();
    }
    @RequestMapping(value = "/api/getService" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getService() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getServiceNodes();
    }
    @RequestMapping(value = "/api/getContainer" , method = RequestMethod.GET ,produces = "application/json")
    public HashMap<String, List<HashMap<String,Object>>> getContainer() {
        HashMap<String, HashMap<String, List<HashMap<String,Object>>>> hashMap = new HashMap();
        return neo4jDriver.getContainerNodes();
    }
    @RequestMapping(value = "/api/getTimestamp" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String,ArrayList> getTimestamp(HttpServletRequest request,
                                  HttpServletResponse response,@RequestParam("filename") String filename) {
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        ArrayList arrayList = csvTimestamp(filePath);
        HashMap<String,ArrayList> map = new HashMap<>();
        map.put("Timestamp",arrayList);
        return map;
    }
    @RequestMapping(value = "/api/getOperationData" , method = RequestMethod.POST ,produces = "application/json")
    public HashMap<String,HashMap<String,ArrayList>> getOperationData(HttpServletRequest request,
                                              HttpServletResponse response,@RequestParam("filename") String filename) {
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");
        savePath = savePath.replace("file:", "");
        String filePath = savePath + "/"  + filename;
        HashMap<String,ArrayList> hashMap = csvOperationData(filePath);
        HashMap<String,HashMap<String,ArrayList>> map = new HashMap<>();
        map.put("Operations",hashMap);
        return map;
    }


    @RequestMapping(value = "/api/yamldeal",method = RequestMethod.POST)
    public ArrayList yamldeal(@RequestParam("file") MultipartFile file) {
        Yaml yaml = new Yaml();
        //Object load = yaml.loadAll(new FileInputStream(file));
        Map<String, Object> map = null;
        //System.out.println(yaml.dump(load));
        ArrayList arrayList = new ArrayList();
        try {
            for (Object data : yaml.loadAll(new FileInputStream(file.getOriginalFilename()))) {
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
                    String containerPort= hashMap.get("spec-template-spec-containers-ports-containerPort").toString();
                    String name = (String)hashMap.get("metadata-name");
                    String nameSpace = (String)hashMap.get("metadata-namespace");
                    String image = (String)hashMap.get("spec-template-spec-containers-image");

                    String containerName = (String)hashMap.get("spec-template-spec-containers-name");
                    String volumeMount = (String)hashMap.get("spec-template-spec-containers-volumeMounts-name");
                    ArrayList arrayListAdd = (ArrayList)hashMap.get("spec-template-spec-containers-securityContext-capabilities-add");
                    ArrayList arrayListDrop = (ArrayList)hashMap.get("spec-template-spec-containers-securityContext-capabilities-drop");
                    deploymentID = neo4jDriver.AddDeploymentNode("Deployment_Node",containerPort,name,nameSpace,image);
                    containerID = neo4jDriver.AddContainerNode("Container_Node",containerName,volumeMount,arrayListAdd,arrayListDrop);
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
                    neo4jDriver.AddYamlRelation(deploymentID,containerID,serviceID);
                }
                System.out.println("---------------");
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        return arrayList;

    }
    @RequestMapping(value = "/api/csvdeal",method = RequestMethod.POST)
    public String csvdeal(HttpServletRequest request,
                             HttpServletResponse response, @RequestParam("file") MultipartFile file) {
        String savePath = request.getSession().getServletContext().getRealPath("/WEB-INF/upload");
        savePath = savePath.replace("file:", "");
        File file1 = new File(savePath);
        if (!file1.exists() && !file1.isDirectory()) {
            file1.mkdir();
        }
        //delAllFil(savePath);

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
            String filePath = savePath + "/"  + file.getOriginalFilename();

            File file2 = new File(filePath);
            if (file2.exists()){
                return "file already upload";
            }
            if (!file2.exists()) {
                //file2.createNewFile();
                //log.info("临时文件保存路径：" + savePath + "/" + "temp" + "_" + buildInfo[0].getOriginalFilename());
               // arrayList.add(filePath);
                OutputStream os = new FileOutputStream(new File(savePath + "/" + file.getOriginalFilename()));// 文件原名,如a.txt
                os.write(b);
                os.flush();
                os.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "success";
    }

}
