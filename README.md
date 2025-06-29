# Tech stack
* Maven 3.9.9
* maven.compiler.source 11
* maven.compiler.target 11
* grpc.version 1.64.0
* protobuf.version 3.25.1
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
