package de.j4velin.pedometer.config;

/**
 * Created by roma on 03.05.2018.
 */

public class Achievement {

    public static enum Type {
        stepsTotal,
        stepsDaily
    }

    private String name;
    private String description;
    private String imageUrl;
    private  Type type;
    private int value;

    public Achievement() {
    }

    public Achievement(String name, String description, String imageUrl, Type type, int value) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
