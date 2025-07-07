package pawg.grpc.springgrpcserver;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Set;

@Document(collection = "statistics")
public class Statistic implements Serializable {

    @Id
    public String id;
    public String username;
    public String lastExecutionDate;
    public String firstExecutionDate;
    public String lastSuccessDate;
    public String lastFailedDate;
    public String javaVersion;
    public String lastUpdateStatus;
    public String lastRunType;
    public Set<String> systemUsers;
    public String applicationVersion;

}
