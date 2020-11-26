package com.zjb.fileheader.entity;

import java.util.List;

/**
 * @author zhaojingbo(zjbhnay @ 163.com)
 * @date 2020-11-25 11:43:49
 */
public class Comment {
    private String author;
    private String date;
    /**
     * 其余的header
     */
    private List<String> other;

    public Comment() {
    }

    public Comment(String author, String date, List<String> other) {
        this.author = author;
        this.date = date;
        this.other = other;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<String> getOther() {
        return other;
    }

    public void setOther(List<String> other) {
        this.other = other;
    }
}

