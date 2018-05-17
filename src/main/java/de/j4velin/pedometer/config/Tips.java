package de.j4velin.pedometer.config;

/**
 * Created by roma on 10.05.2018.
 */

public class Tips {

    private String name;
    private String url;

    public Tips(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
