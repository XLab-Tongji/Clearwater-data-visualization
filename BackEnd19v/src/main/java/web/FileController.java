package web;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static neo4j.MongoDriver.saveSystemTypeAndNameFile;
import static neo4j.Neo4jDriver.importTtl;
import static util.TurtleUtil.*;

@RestController
public class FileController {

    @RequestMapping(value = "/api/uploadTypeFile",method = RequestMethod.POST,produces = "application/json")
    //上传系统的tpye文件
    public Map<String, Object> postType(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name){
        String savePath = FileController.class.getResource("/").getPath().replace("classes","upload/type");
        Map<String, Object> res = new HashMap<>();
        try{
            if (springUpload(request, savePath, name)) {
                res.put("succees",1);
                readOntologyJson(name);
                saveSystemTypeAndNameFile(name,"");
            }
        }catch (Exception e) {
            e.printStackTrace();
            res.put("succees", 0);
            res.put("Reason",e.toString());
        }
        return res;
    }



    @RequestMapping(value = "/api/uploadSystemFile",method = RequestMethod.POST,produces = "application/json")
    //上传系统的system文件
    public Map<String, Object> postSystem(HttpServletRequest request, HttpServletResponse response, @RequestParam("name") String name, @RequestParam("type") String type){
        String savePath = FileController.class.getResource("/").getPath().replace("classes","upload/system");
        Map<String, Object> res = new HashMap<>();
        try{
            if (springUpload(request, savePath, name)) {
                res.put("succees",1);
                readSystemJson(name);
                saveSystemTypeAndNameFile(type,name);
            }
        }catch (Exception e) {
            e.printStackTrace();
            res.put("succees", 0);
            res.put("Reason",e.toString());
        }
        importTtl(FileController.class.getResource("/").getPath().replace("classes","turtle/type")+type+".ttl",
                FileController.class.getResource("/").getPath().replace("classes","turtle/system")+name+".ttl");
        return res;
    }

    private boolean springUpload(HttpServletRequest request, String savePath, String fileName) throws IllegalStateException, IOException
    {
        //将当前上下文初始化给  CommonsMutipartResolver （多部分解析器）
        CommonsMultipartResolver multipartResolver=new CommonsMultipartResolver(
                request.getSession().getServletContext());
        //检查form中是否有enctype="multipart/form-data"
        if(multipartResolver.isMultipart(request))
        {
            //将request变成多部分request
            MultipartHttpServletRequest multiRequest=(MultipartHttpServletRequest)request;
            //获取multiRequest 中所有的文件名
            Iterator iter=multiRequest.getFileNames();

            while(iter.hasNext())
            {

                //一次遍历所有文件
                MultipartFile file=multiRequest.getFile(iter.next().toString());
                if(file!=null)
                {
                    String oldName = file.getOriginalFilename();
                    String path = savePath + fileName + oldName.substring(oldName.lastIndexOf("."));
                    System.out.println(path);
                    File folder = new File(savePath);
                    //文件夹路径不存在
                    if (!folder.exists() && !folder.isDirectory()) {
                        folder.mkdirs();
                    }
                    File newFile = new File(path);
                    //判断路径是否存在，如果不存在就创建一个
                    if(!newFile.exists()){
                        newFile.mkdir();
                    }
                    //上传
                    file.transferTo(newFile);

                }

            }

        }
        return true;
    }

}
