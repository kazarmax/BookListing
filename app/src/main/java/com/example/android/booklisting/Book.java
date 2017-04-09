package com.example.android.booklisting;

public class Book {

    private final String mThumbnailUrl;

    private final String mTitle;

    private final String mAuthor;

    private final String mPublishedDate;

    private final String mPreviewLink;

    public Book(String thumbnailUrl, String title, String author, String publishedDate,
                String previewLink) {
        this.mThumbnailUrl = thumbnailUrl;
        this.mTitle = title;
        this.mAuthor = author;
        this.mPublishedDate = publishedDate;
        this.mPreviewLink = previewLink;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getPublishedDate() {
        return mPublishedDate;
    }

    public String getPreviewLink() {
        return mPreviewLink;
    }

}