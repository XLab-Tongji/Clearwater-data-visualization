package web;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

import static service.CsvService.*;

@RestController
public class CsvController {
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

}
