package xyz.n7mn.dev.nanamiplayersystem.data;

import java.util.UUID;

public class ServerPermData {
// UUID 	ServerName 	ServerDefaultJoinPerm 	ServerDefaultPerm 	Is_Active
    private UUID uuid;
    private String ServerName;
    private String[] ServerJoinPerm;
    private String[] ServerOpPerm;
    private boolean Active;

    public ServerPermData(UUID uuid, String serverName, String[] serverJoinPerm, String[] serverOpPerm, boolean active){
        this.uuid = uuid;
        this.ServerName = serverName;
        this.ServerJoinPerm = serverJoinPerm;
        this.ServerOpPerm = serverOpPerm;
        this.Active = active;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getServerName() {
        return ServerName;
    }

    public void setServerName(String serverName) {
        ServerName = serverName;
    }

    public String[] getServerJoinPerm() {
        return ServerJoinPerm;
    }

    public void setServerJoinPerm(String[] serverJoinPerm) {
        ServerJoinPerm = serverJoinPerm;
    }

    public String[] getServerOpPerm() {
        return ServerOpPerm;
    }

    public void setServerOpPerm(String[] serverOpPerm) {
        ServerOpPerm = serverOpPerm;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }
}
