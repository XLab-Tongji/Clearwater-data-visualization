package web;

import neo4j.Neo4jDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import service.Neo4jService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static neo4j.FusekiDriver.*;
import static neo4j.Neo4jDriver.*;

import static neo4j.prometheusDriver.newPrometheus;

@RestController
public class Neo4jController {
  //  @Autowired
    private Neo4jDriver neo4jDriver;
    private Neo4jService neo4jService;
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
    @RequestMapping(value = "/api/yamldeal",method = RequestMethod.POST)
    //处理yaml文件接口
    public ArrayList yamldeal(@RequestParam("file") MultipartFile file,@RequestParam("systemName") String system) throws IOException {
        return neo4jService.yamldealService(file,system);
    }
    @RequestMapping(value = "/api/addServiceRelation" , method = RequestMethod.POST)
    //添加服务之间关系接口 目前文件类型：json
    public Boolean addServiceRelation(HttpServletRequest request,@RequestParam("file") MultipartFile multipartFile) {
        return neo4jService.addServiceRelationService(request,multipartFile);
    }
    @RequestMapping(value = "/api/fileupdate",method = RequestMethod.POST)
    //上传文件接口
    public String fileupdate(HttpServletRequest request,
                             HttpServletResponse response, @RequestParam("file") MultipartFile file,@RequestParam("systemName") String system) {
        return neo4jService.fileUpdateService(request,response,file,system);
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

    //获取结点、连接
    @RequestMapping(value = "/api/getSystemNodesAndLinks",method = RequestMethod.GET,produces = "application/json")
    public Map getNodeAndLink(@RequestParam("systemName")String systemName){
        return getAllNodesandlinks(systemName);
    }


    //以下为Fuseki部分
    @RequestMapping(value = "/api/getNodesAndLinks",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getNodesAndLinks(){
        return getAllNodesAndLinks();
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

    //添加事件和指标之间的关联度
    @RequestMapping(value = "/api/addEventLinkToProfile", method = RequestMethod.GET, produces = "application/json")
    public void addEventLinkToProfile(){
        addLinkEvent2S();
    }



}
