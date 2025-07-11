package pawg.grpc.type;

import java.util.Set;

public class StatisticDTO {

    public StatisticDTO() {}

    public StatisticDTO(String id, String username) {
        this.id = id;
        this.username = username;
    }

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
               '}';
    }
}
