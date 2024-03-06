package com.example.majorproject.Models;

import android.graphics.drawable.Drawable;

public class PostModel {
    String _id;
    String user_id;
    String user_name;
    String user_email;
    String user_image_link;
    Drawable user_image_drawable;
    String post_image;
    String post_description;
    String post_date_time;
    boolean post_block;

    public PostModel() {
    }

    public PostModel(String _id, String user_id, String user_name, String user_email, String user_image_link, Drawable user_image_drawable, String post_image, String post_description, String post_date_time, boolean post_block) {
        this._id = _id;
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_email = user_email;
        this.user_image_link = user_image_link;
        this.user_image_drawable = user_image_drawable;
        this.post_image = post_image;
        this.post_description = post_description;
        this.post_date_time = post_date_time;
        this.post_block = post_block;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_image_link() {
        return user_image_link;
    }

    public void setUser_image_link(String user_image_link) {
        this.user_image_link = user_image_link;
    }

    public Drawable getUser_image_drawable() {
        return user_image_drawable;
    }

    public void setUser_image_drawable(Drawable user_image_resource) {
        this.user_image_drawable = user_image_resource;
    }

    public String getPost_image() {
        return post_image;
    }

    public void setPost_image(String post_image) {
        this.post_image = post_image;
    }

    public String getPost_description() {
        return post_description;
    }

    public void setPost_description(String post_description) {
        this.post_description = post_description;
    }

    public String getPost_date_time() {
        return post_date_time;
    }

    public void setPost_date_time(String post_date_time) {
        this.post_date_time = post_date_time;
    }

    public boolean isPost_block() {
        return post_block;
    }

    public void setPost_block(boolean post_block) {
        this.post_block = post_block;
    }
}
