package neo4j;

import org.neo4j.driver.v1.*;

import static org.neo4j.driver.v1.Values.parameters;

public class Neo4jTest {
    public static void main(String[] args) {
        Driver driver = GraphDatabase.driver( "bolt://localhost:7687", AuthTokens.basic( "neo4j", "1234" ) );
        Session session = driver.session();

        session.run( "CREATE (a:Person {name: {name}, title: {title}})",
                parameters( "name", "Arthur", "title", "King" ) );

        StatementResult result = session.run( "MATCH (a:Person) WHERE a.name = {name} " +
                        "RETURN a.name AS name, a.title AS title",
                parameters( "name", "Arthur" ) );
        while ( result.hasNext() )
        {
            Record record = result.next();
            System.out.println( record.get( "title" ).asString() + " " + record.get( "name" ).asString() );
        }

        session.close();
        driver.close();
    }
}
