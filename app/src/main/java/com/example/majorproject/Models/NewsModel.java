package com.example.majorproject.Models;

public class NewsModel {

        String title;
        String category;
        String creator;
        String content;
        String country;
        String description;
        String imageurl;
        String categories;

        String tag;
        String url;
        String publish_on;
        String source_name;

    public NewsModel(String title, String category, String creator, String content, String country,String source_name, String description, String imageurl, String categories, String tag, String url, String publish_on) {
        this.title = title;
        this.category = category;
        this.creator = creator;
        this.content = content;
        this.country = country;
        this.description = description;
        this.imageurl = imageurl;
        this.categories = categories;
        this.tag = tag;
        this.url = url;
        this.publish_on = publish_on;
        this.source_name = source_name;
    }

    public NewsModel() {

        }

        public String getUrl() {
            return this.url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getCategories() {
            return categories;
        }

        public void setCategories(String categories) {
            this.categories = categories;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }



        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getImageurl() {
            return imageurl;
        }

        public void setImageurl(String imageurl) {
            this.imageurl = imageurl;
        }

        public String getDescription() {
            return description;
        }


        public void setDescription(String description) {
            this.description = description;
        }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getPublish_on() {
        return publish_on;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public void setPublish_on(String publish_on) {
        this.publish_on = publish_on;
    }
}
