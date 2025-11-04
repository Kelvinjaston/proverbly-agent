package com.example.proverb.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class ExternalQuote {

    @JsonAlias("q")
    private String content;

    @JsonAlias("a")
    private String author;

    private String h;

    public ExternalQuote() {}

    public ExternalQuote(String content, String author) {
        this.content = content;
        this.author = author;
    }

    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public String getH() {
        return h;
    }
    public void setH(String h) {
        this.h = h;
    }
    @Override
    public String toString() {
        return "Quote: \"" + content + "\" — " + author;
    }
}
