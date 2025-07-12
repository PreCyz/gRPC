# Tech stack
* Maven 3.9.9
* maven.compiler.source 21
* maven.compiler.target 21
* grpc.version 1.72.0
* protobuf.version 4.30.2
* protobuf-plugin.version 0.6.1

# Build the whole project
```powershell
mvn clean install
```

# Run server
```powershell
cd greeting-server
set java_path=$ENV:JAVA11_HOME\bin\java.exe
$java_path -jar target/greeting-server-1.0-SNAPSHOT.jar
```

# Run client
```powershell
cd greeting-client
set java_path=$ENV:JAVA11_HOME\bin\java.exe
$java_path -jar target/greeting-client-1.0-SNAPSHOT.jar
```

# AWS Env variables
```
MONGODB_PASSWORD=mSzEh0sWLEWHVDR6
MONGODB_DATABASE=gipter-test
MONGODB_USERNAME=gipter-test
MONGODB_HOST=gipter-test.ruzxs.mongodb.net
SPRING_PROFILES_ACTIVE=mongo
```
