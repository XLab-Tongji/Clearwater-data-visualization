package dao;

import neo4j.Neo4jDriver;
import neo4jentities.Node;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;


/*public class NodeRepositoryTest extends AbstractTransactionalTestNGSpringContextTests{


    @Test
    public void testquery() {
        Neo4jDriver neo4jDriver = new Neo4jDriver();
        neo4jDriver.getresult("The_New_England_Journal_of_Medicine","Jeffrey_M._Drazen",1,2);
    }

}*/
