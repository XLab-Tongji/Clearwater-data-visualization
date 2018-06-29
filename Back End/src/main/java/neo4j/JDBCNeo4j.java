package neo4j;

import java.sql.*;

public class JDBCNeo4j {
    public static void main(String[] args) throws SQLException,ClassNotFoundException{
          //这里可以使用localhost,最好还是使用ip地址,
        Class.forName("org.neo4j.jdbc.Driver");
        Connection con = DriverManager.getConnection("jdbc:neo4j://149.28.125.166:7474","neo4j","pzkpfw38t");
        //Querying
        try(Statement stmt = con.createStatement())
        {

            ResultSet rs = stmt.executeQuery("Match p=(n {Name:{0} })-[r*1..3]-(m {Name:{1}}) return p as nodesrelation");
            while(rs.next())
            {
                System.out.println(rs.getString("nodesrelation"));
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
