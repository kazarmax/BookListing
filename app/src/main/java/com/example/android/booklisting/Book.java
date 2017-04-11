package com.example.android.booklisting;

import android.graphics.Bitmap;

public class Book {

    private final String mThumbnailUrl;

    private final String mTitle;

    private final String mAuthor;

    private final String mPublishedDate;

    private final String mPreviewLink;

    private Bitmap mBookImageBitmap;

    public Book(String thumbnailUrl, String title, String author, String publishedDate,
                String previewLink, Bitmap bookImageBitmap) {
        this.mThumbnailUrl = thumbnailUrl;
        this.mTitle = title;
        this.mAuthor = author;
        this.mPublishedDate = publishedDate;
        this.mPreviewLink = previewLink;
        this.mBookImageBitmap = bookImageBitmap;
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

    public Bitmap getBookImageBitmap() {
        return mBookImageBitmap;
    }

    public void setBookImageBitmap(Bitmap bookImageBitmap) {
        this.mBookImageBitmap = bookImageBitmap;
    }

}