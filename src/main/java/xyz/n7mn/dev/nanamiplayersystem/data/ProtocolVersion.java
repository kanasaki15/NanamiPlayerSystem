package xyz.n7mn.dev.nanamiplayersystem.data;

public class ProtocolVersion {

    private int ProtocolVersion;
    private String MinecraftVersion;

    public ProtocolVersion(int protocolVersion, String minecraftVersion){
        this.ProtocolVersion = protocolVersion;
        this.MinecraftVersion = minecraftVersion;
    }

    public int getProtocolVersion() {
        return ProtocolVersion;
    }

    public String getMinecraftVersion() {
        return MinecraftVersion;
    }

    public void setProtocolVersion(int protocolVersion) {
        ProtocolVersion = protocolVersion;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        MinecraftVersion = minecraftVersion;
    }
}
