package de.bentzin.norbert;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Ture Bentzin
 * @since 26-03-2024
 */
public class ConfigObject {

    @NotNull
    private String sqlitePath;
    @NotNull
    private List<Long> adminIds;

    @NotNull
    private String url;

    private long channelId;
    protected long guildId;

    private boolean writeEnabled = true;

    @NotNull
    public static ConfigObject defaultConfig() {
        ConfigObject configObject = new ConfigObject();
        configObject.setSqlitePath("data.sqlite");
        configObject.setAdminIds(List.of());
        configObject.setUrl("https://testat.etechnik.fh-aachen.de");
        configObject.setChannelId(-1);
        return configObject;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @NotNull ConfigObject setUrl(@NotNull String url) {
        this.url = url;
        return this;
    }

    @NotNull
    public String getSqlitePath() {
        return sqlitePath;
    }

    public void setSqlitePath(@NotNull String sqlitePath) {
        this.sqlitePath = sqlitePath;
    }

    @NotNull
    public List<Long> getAdminIds() {
        return adminIds;
    }

    public void setAdminIds(@NotNull List<Long> adminIds) {
        this.adminIds = adminIds;
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public long getChannelId() {
        return channelId;
    }

    public void setChannelId(long channelId) {
        this.channelId = channelId;
    }

    public long getGuildId() {
        return guildId;
    }

    public void setGuildId(long guildId) {
        this.guildId = guildId;
    }
}
