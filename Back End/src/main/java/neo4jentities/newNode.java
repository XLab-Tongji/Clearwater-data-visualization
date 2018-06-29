package neo4jentities;

import com.sun.xml.internal.xsom.impl.scd.Iterators;

import java.util.ArrayList;

public class newNode {
    private String label;
    private int id;
    private String name;
    private String type;
    private String layer;
    private String performance;
    private ArrayList<Integer> relations;
    public String getLabel(){return label;}
    public int getId(){return id;}
}
