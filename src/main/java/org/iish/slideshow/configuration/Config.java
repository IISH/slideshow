package org.iish.slideshow.configuration;

public class Config {
    private int timeout;
    private String api;
    private Blacklist blacklist;

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public Blacklist getBlacklist() {
        return blacklist;
    }

    public void setBlacklist(Blacklist blacklist) {
        this.blacklist = blacklist;
    }
}
