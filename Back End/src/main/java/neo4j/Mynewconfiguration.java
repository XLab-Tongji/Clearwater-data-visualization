package neo4j;


import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/*
@Configuration
@EnableNeo4jRepositories(basePackages = "neo4jrepository")
@EnableTransactionManagement
public class Mynewconfiguration {
    @Bean
    public SessionFactory sessionFactory() {
        // with domain entity base package(s)
        return new SessionFactory(configuration(), "neo4jentities");
    }

    @Bean
    public org.neo4j.ogm.config.Configuration configuration() {
        org.neo4j.ogm.config.Configuration config = new org.neo4j.ogm.config.Configuration();
        config.driverConfiguration() .setDriverClassName("org.neo4j.ogm.drivers.http.driver.HttpDriver") .setURI("http://neo4j:pzkpfw38t@149.28.125.166:7474");
        return config;

}

    @Bean
    public Neo4jTransactionManager transactionManager() {
        return new Neo4jTransactionManager(sessionFactory());
    }
}
*/