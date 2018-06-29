package neo4jentities;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public class Node {
    //@GraphId
    //private Long id;
    private String Name;

    /*@Relationship()
    private Set<Relationship> relationships;

    public Set<Relationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(Set<Relationship> relationships) {
        this.relationships = relationships;
    }*/

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        this.Name = name;
    }

    /*public Long getId() {
        return id;
    }

   public void setId(Long id) {
        this.id = id;
    }*/
}
