package service;

import neo4j.Neo4jDriver;
import org.apache.commons.fileupload.disk.DiskFileItem;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static neo4j.Neo4jDriver.*;



public class Neo4jService {
    private Neo4jDriver neo4jDriver;
    //处理yaml文件接口
    public ArrayList yamldealService(MultipartFile file,String system) throws IOException {
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

    //添加服务之间关系接口 目前文件类型：json
    public Boolean addServiceRelationService(HttpServletRequest request,MultipartFile multipartFile) {
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

    //上传文件接口
    public String fileUpdateService(HttpServletRequest request,
                             HttpServletResponse response,  MultipartFile file, String system) {
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
}
