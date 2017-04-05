package com.example.android.booklisting;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.util.List;

public class BookLoader extends AsyncTaskLoader<List<Book>> {

    /** Tag for log messages */
    private static final String LOG_TAG = BookLoader.class.getName();

    /** Query URL */
    private String mLoadUrl;

    public BookLoader(Context context, String loadUrl) {
        super(context);
        this.mLoadUrl = loadUrl;
        Log.v(LOG_TAG, "BookLoader constructor invoked");
    }

    @Override
    protected void onStartLoading() {
        Log.v(LOG_TAG, "onStartLoading method invoked");
        forceLoad();
    }

    @Override
    public List<Book> loadInBackground() {
        Log.v(LOG_TAG, "loadInBackground() method invoked");
        // Don't perform the request if there are no URLs, or the first URL is null.
        if (mLoadUrl == null || mLoadUrl == "") {
            return null;
        }
        List<Book> books = QueryUtils.fetchBooks(mLoadUrl);
        return books;
    }

    public void updateLoadUrl(String newLoadUrl) {
        this.mLoadUrl = newLoadUrl;
    }

}
