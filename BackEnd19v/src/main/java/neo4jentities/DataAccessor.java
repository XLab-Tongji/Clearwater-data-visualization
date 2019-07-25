package neo4jentities;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;

public class DataAccessor {
    public static String serviceURI = "http://10.60.38.173:3030/DevKGData";
    public static DatasetAccessorFactory factory = null;
    public static DatasetAccessor accessor;

    public static DatasetAccessor getAccessor() {
        return accessor;
    }

    public static void setAccessor(DatasetAccessor accessor) {
        DataAccessor.accessor = accessor;
    }

    public static DatasetAccessor getInstance(){
        if(accessor == null){
            accessor = factory.createHTTP(serviceURI);
        }
        return accessor;
    }
}
