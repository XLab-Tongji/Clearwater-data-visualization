package web;

<<<<<<< Updated upstream
import neo4j.MongoDriver;
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
import service.MongoService;
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

import org.springframework.web.bind.annotation.*;
import service.MongoService;
import util.TimerUtil;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static neo4j.FusekiDriver.*;
import static neo4j.MongoDriver.*;
import static neo4j.prometheusDriver.DealPrometheusRequest;
>>>>>>> Stashed changes

public class MongoDBController {
    private MongoService mongoService;
    @RequestMapping(value = "/api/storeEnvironment",method = RequestMethod.POST,produces = "application/json")
    public Boolean storeAll(@RequestParam("masterName") String masterName, @RequestParam("podName") String podName, @RequestParam("serviceName") String serviceName, @RequestParam("address") String address, @RequestParam("namespace") String namespace){
        return mongoService.storeAllService(masterName,podName,serviceName,address,namespace);
    }
    @RequestMapping(value = "/api/getAllByTime",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getAllNLByTime(@RequestParam String time){
        return getAllByTime(time);
    }

    @RequestMapping(value = "/api/getEventByTime", method = RequestMethod.POST, produces = "application/json")
    public List<List> getEventByTime(@RequestParam("startDate")String startDate, @RequestParam("endDate")String endDate,@RequestParam("startTime")String startTime, @RequestParam("endTime")String endTime){
        return getEventMongByTime(startDate,startTime,endDate, endTime);
    }

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

    @RequestMapping(value = "/api/addKapacitorEvent", method = RequestMethod.POST, produces = "application/json")
    public Boolean addKapacitorEvent(@RequestBody String message){
        //return readKapacitor(data);
        System.out.println(message);
        return saveKapacitor2Mongo(message);
    }

    //以下为Fuseki提供支持部分
    @RequestMapping(value = "/api/getNodesAndLinks",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getNodesAndLinks(){
        return getAllNodesAndLinks();
    }
}
