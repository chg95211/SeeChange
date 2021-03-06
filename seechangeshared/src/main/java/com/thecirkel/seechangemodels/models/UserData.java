package com.thecirkel.seechangemodels.models;

public class UserData {

    private String username, bio, avatarurl;
    private Integer satoshi;

    public Integer getSatoshi() {
        return satoshi;
    }

    public void setSatoshi(Integer satoshi) {
        this.satoshi = satoshi;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAvatarurl() {
        return avatarurl;
    }

    public void setAvatarurl(String avatarurl) {
        this.avatarurl = avatarurl;
    }

}
