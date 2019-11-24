package util;

import web.FileController;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

public class FileUtil {

    public static void saveClusterResult(String re, String name){
        try {

            String savePath = FileController.class.getResource("/").getPath().replace("classes","clusterResult");
            File folder = new File(savePath);
            //文件夹路径不存在
            if (!folder.exists() && !folder.isDirectory()) {
                folder.mkdirs();
            }
            File file = new File(savePath + name + new Date().getTime() + ".json");
            //判断路径是否存在，如果不存在就创建一个
            if(!file.exists()){
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(re.getBytes());
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
