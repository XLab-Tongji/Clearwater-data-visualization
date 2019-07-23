# BusinessIntelligenceProject

## Build
```
mvn clean package -U -Dmaven.test.skip=true tomcat7:redeploy
```

## Run
```
docker run -p 9999:9999 -d okg/backend
```
