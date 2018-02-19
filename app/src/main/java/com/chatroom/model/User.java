package com.chatroom.model;

import android.support.annotation.NonNull;

import java.io.Serializable;


public class User implements Serializable,Comparable<User> {

    private String uid,phoneNumber, name, bio, image;

    public User(String uid,String phoneNumber, String name, String bio, String image) {
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;

        User user = (User) o;

        return getUid().equals(user.getUid()) && getPhoneNumber().equals(user.getPhoneNumber()) && getName().equals(user.getName()) && getBio().equals(user.getBio()) && (getImage() != null ? getImage().equals(user.getImage()) : user.getImage() == null);
    }

    @Override
    public int hashCode() {
        int result = getUid().hashCode();
        result = 31 * result + getPhoneNumber().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getBio().hashCode();
        result = 31 * result + (getImage() != null ? getImage().hashCode() : 0);
        return result;
    }

    public User() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public int compareTo(@NonNull User user) {
        return getName().compareTo(user.getName());
    }
}
