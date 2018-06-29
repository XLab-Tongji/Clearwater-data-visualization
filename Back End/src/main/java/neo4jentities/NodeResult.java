package neo4jentities;

import org.springframework.data.neo4j.annotation.QueryResult;

@QueryResult
public class NodeResult {
    String nodename;
    String type;
    Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getNodename() {
        return nodename;
    }

    public void setNodename(String nodename) {
        this.nodename = nodename;
    }

    public String getType() {
        return type;
    }


    public void setType(String type) {
        this.type = type;
    }
}
