package neo4j;


/*import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableNeo4jRepositories(basePackages = "neo4jrepository")
@EnableTransactionManagement
public class Myneo4jConfiguration extends Neo4jConfiguration {
    @Bean
    public org.neo4j.ogm.config.Configuration getConfiguration() {
        org.neo4j.ogm.config.Configuration config =
                new org.neo4j.ogm.config.Configuration();
        // TODO: Temporary uses the embedded driver. We need to switch to http
        // driver. Then we can horizontally scale neo4j
        config.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver")
                .setCredentials("neo4j","pzkpfw38t")
                .setURI("http://149.28.125.166:7474");
        return config;
    }
    @Override
    @Bean
    public SessionFactory getSessionFactory() {
        // Return the session factory which also includes the persistent entities
        return new SessionFactory(getConfiguration(), "node4jentities");
    }
}
*/