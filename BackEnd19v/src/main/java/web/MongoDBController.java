package web;


import org.springframework.web.bind.annotation.*;
import service.MongoService;
import util.TimerUtil;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static neo4j.FusekiDriver.*;
import static neo4j.MongoDriver.*;
import static neo4j.prometheusDriver.DealPrometheusRequest;

@RestController
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
                Map<String, Object> result = getAllNodesAndLinks();
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

    @RequestMapping(value = "/api/addVariousEvent", method = RequestMethod.POST, produces = "application/json")
    public Boolean addEvent(@RequestParam("Source") String source, @RequestParam("Content") String content){
        return saveEvent2Mongo(content, source);
    }

    @RequestMapping(value = "/api/getSystemTypeAndNameFile",method = RequestMethod.GET,produces = "application/json")
    public Map<String, Object> getAllSystemTypeAndNameFile(){
        return MongoService.jsonarray2Map();
    }

}
