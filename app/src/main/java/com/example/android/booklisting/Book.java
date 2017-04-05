package com.example.android.booklisting;

public class Book {

    //Fields to consider: canonicalVolumeLink

    private final String mSmallThumbnailUrl;

    private final String mTitle;

    private final String mAuthor;

    private final String mPublishedDate;

    private final String mPreviewLink;

    public Book(String smallThumbnailUrl, String title, String author, String publishedDate, String previewLink) {
        this.mSmallThumbnailUrl = smallThumbnailUrl;
        this.mTitle = title;
        this.mAuthor = author;
        this.mPublishedDate = publishedDate;
        this.mPreviewLink = previewLink;
    }

    public String getSmallThumbnailUrl() {
        return mSmallThumbnailUrl;
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
