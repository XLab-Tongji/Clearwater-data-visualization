package service;

import neo4j.Neo4jDriver;
import neo4jentities.LineResult;
import neo4jentities.Node;
import neo4jentities.NodeResult;
import org.neo4j.ogm.annotation.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.neo4j.ogm.annotation.Relationship;
import java.util.List;
/*
@Component
public class NodeService {
    @Autowired
    private Neo4jDriver neo4jDriver;

    public void  findbytwoentity(String name1, String name2){
        List<LineResult> results = neo4jDriver.get(name1, name2);
        for (LineResult lineResult : results) {
            List<Node> nodes = lineResult.getNodesrelation();
            for (Node node : nodes) {
                System.out.println(node.getName());
            }

        }

    }

    public void findnodebyname(String name1) {
        Iterable<NodeResult> properties = nodeRepository.getThingPropertyByThingName(name1);
        for (NodeResult nodeResult : properties) {
            System.out.println(nodeResult.getNodename());
            System.out.println(nodeResult.getType());
            System.out.println(nodeResult.getId());
        }
        List<Node> nodes = nodeRepository.findByName("The_New_England_Journal_of_Medicine");
        for(Node node : nodes) {
            System.out.println(node.getName());
        }

        //System.out.println(nodeRepository.findByName("The_New_England_Journal_of_Medicine"));
    }
}
*/