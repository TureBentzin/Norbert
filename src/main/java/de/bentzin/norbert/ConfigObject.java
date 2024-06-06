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

    private boolean writeEnabled = true;

    @NotNull
    public static ConfigObject defaultConfig() {
        ConfigObject configObject = new ConfigObject();
        configObject.setSqlitePath("data.sqlite");
        configObject.setAdminIds(List.of());
        configObject.setUrl("https://testat.etechnik.fh-aachen.de");
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
}
