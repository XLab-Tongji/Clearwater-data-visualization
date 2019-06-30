package neo4j;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.text.SimpleDateFormat;
import java.util.*;

public class MongoDriver {

    //连接到mongodb服务
    private MongoClient mongoClient = new MongoClient("10.60.38.173", 27020);
    //连接到数据库
    private MongoDatabase mongoDatabase = mongoClient.getDatabase("knowledgegraph");
    private MongoCollection<Document> collection = mongoDatabase.getCollection("query_statements");

    public boolean save_data(Map<String, Object> data){
        try {
            Document document = new Document();
            for (String key : data.keySet()) {
                document.append(key, data.get(key));
            }
            collection.insertOne(document);
            System.out.println("文档插入成功");
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Document> get_data(){
        FindIterable<Document> findIterable = collection.find();
        MongoCursor<Document> mongoCursor = findIterable.iterator();
        List<Document> result = new ArrayList<>();
        while(mongoCursor.hasNext()){
            Document d = mongoCursor.next();
            result.add(d);
        }
        return result;
    }

    public boolean clear_db(){
        try {
            collection.deleteMany(new Document());
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
