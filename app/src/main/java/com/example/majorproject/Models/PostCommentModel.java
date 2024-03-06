package com.example.majorproject.Models;

public class PostCommentModel {
    String user_id;
    String user_name;
    String user_email;
    String user_image;
    String comment;
    String comment_date_time;

    public PostCommentModel() {
    }

    public PostCommentModel(String user_id, String user_name, String user_email, String user_image, String comment, String comment_date_time) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.user_email = user_email;
        this.user_image = user_image;
        this.comment = comment;
        this.comment_date_time = comment_date_time;
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

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment_date_time() {
        return comment_date_time;
    }

    public void setComment_date_time(String comment_date_time) {
        this.comment_date_time = comment_date_time;
    }
}
