package service;

import neo4j.MongoDriver;

import static neo4j.FusekiDriver.*;

public class MongoService {
    public Boolean storeAllService(String masterName, String podName,  String serviceName,  String address,  String namespace){
        boolean result = true;
        MongoDriver mongoDriver = new MongoDriver();
        result &= mongoDriver.clear_db();
        result &= storeNamespaceName();
        result &= storeServerName();
        result &= storeMasterNode(masterName);
        result &= storeEnvironment(address);
        result &= storePodName(podName);
        result &= storeEnvRelation(address);
        result &= storeServiceName(serviceName);
        result &= podToService(address, namespace);
        result &= podToServer(address, namespace);
        return result;
    }
}
