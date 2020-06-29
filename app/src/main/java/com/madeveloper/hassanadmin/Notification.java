package com.madeveloper.hassanadmin;

public class Notification {
    String id ;
    String title ;
    String imageUrl ;
    String body ;
    Object time;

    public Notification() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setBody(String body) {
        this.body = body;
    }



    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getBody() {
        return body;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }
}
