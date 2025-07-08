package pawg.grpc.springgrpcserver;

import java.io.Serializable;
import java.util.Set;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "statistics")
public class StatisticEntity implements Serializable {

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
    public String status;

    @Override
    public String toString() {
        return "StatisticEntity{" +
               "id='" + id + '\'' +
               ", username='" + username + '\'' +
               ", lastExecutionDate='" + lastExecutionDate + '\'' +
               ", firstExecutionDate='" + firstExecutionDate + '\'' +
               ", lastSuccessDate='" + lastSuccessDate + '\'' +
               ", lastFailedDate='" + lastFailedDate + '\'' +
               ", javaVersion='" + javaVersion + '\'' +
               ", lastUpdateStatus='" + lastUpdateStatus + '\'' +
               ", lastRunType='" + lastRunType + '\'' +
               ", systemUsers=" + systemUsers +
               ", applicationVersion='" + applicationVersion + '\'' +
               ", status='" + status + '\'' +
               '}';
    }
}
