# BusinessIntelligenceProject

## Build
```
mvn clean package dockerfile:build -DskipTests=true
```

## Run
```
docker run -p 9999:9999 -d okg/backend
```