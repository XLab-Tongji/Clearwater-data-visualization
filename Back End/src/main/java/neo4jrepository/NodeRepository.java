package neo4jrepository;

//import com.sun.corba.se.impl.orbutil.graph.Graph;
import neo4jentities.LineResult;
import neo4jentities.Node;
import neo4jentities.NodeResult;
import org.neo4j.ogm.annotation.Property;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.annotation.QueryResult;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
/*
@Repository
public interface NodeRepository extends GraphRepository<Node> {

    List<Node> findByName(String Name);

    @Query("Match (n {Name:{0} })-[r]-(m) return m.Name as nodename,type(r) as type, Id(m) as id")
    List<NodeResult> getThingPropertyByThingName(String thingName);

    @Query("Match p=(n {Name:{0} })-[r*1..3]-(m {Name:{1}}) return p as nodesrelation")
    List<LineResult> getPathByNames(String name1, String name2);

}*/
//"Match p=(n {Name:"The_New_England_Journal_of_Medicine" })-[r*1..3]-(m {Name:"Jeffrey_M._Drazen"}) return p"
//Match p=(n {Name:"The_New_England_Journal_of_Medicine" })-[r*1..3]-(m {Name:"Medicine"}) return p