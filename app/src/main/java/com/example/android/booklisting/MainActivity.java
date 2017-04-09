package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    /** URL base for books data from the Google Books API */
    private static final String REQUEST_URL_BASE =
            "https://www.googleapis.com/books/v1/volumes?maxResults=10&q=";

    /** Object for storing a list of Books loaded using Google Books API */
    private ArrayList<Book> bookList;

    /** Adapter for the list of books */
    private BookAdapter mBookAdapter;

    /** Progress bar to be shown while books are being loaded from server*/
    private View mLoadProgressBar;

    private static final String LOG_TAG = MainActivity.class.getName();

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.v(LOG_TAG, "OnCreate method invoked");

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        mLoadProgressBar = findViewById(R.id.loading_spinner);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);
        bookListView.setEmptyView(mEmptyStateTextView);

        if (getLastCustomNonConfigurationInstance() == null) {
            Log.v(LOG_TAG, "Inside If condition = (getLastCustomNonConfigurationInstance() == null)");
            bookList = new ArrayList<Book>();
        } else {
            Log.v(LOG_TAG, "Inside else condition = (getLastCustomNonConfigurationInstance() == null)");
            bookList = (ArrayList<Book>) getLastCustomNonConfigurationInstance();
        }

        mBookAdapter = new BookAdapter(this, bookList);
        bookListView.setAdapter(mBookAdapter);

        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                Book currentBook = mBookAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getPreviewLink());

                // Create a new intent to view the earthquake URI
                Intent intent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(intent);
            }
        });

        // If there is a network connection, fetch data
        if (hasInternetConnection()) {
            Log.v(LOG_TAG, "In hasInternetConnection() IF block");

            SearchView searchView = (SearchView) findViewById(R.id.search_view);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.v(LOG_TAG, "Inside searchButton.setOnClickListener");
                    if (query != "" || query != null) {
                        Log.v(LOG_TAG, "Inside searchView.getQuery IF block");
                        String requestUrl = REQUEST_URL_BASE + query;
                        mLoadProgressBar.setVisibility(View.VISIBLE);
                        new BookLoadTask().execute(requestUrl);
                    }
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return false;
                }
            });

        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadProgressBar.setVisibility(ProgressBar.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_internet);
        }
    }

    private class BookLoadTask extends AsyncTask<String, Void, List<Book>> {

        protected List<Book> doInBackground(String... urls) {
            // Don't perform the request if there are no URLs, or the first URL is null.
            if (urls[0] == null || urls[0] == "") {
                return null;
            }
            Log.v(LOG_TAG, "Inside BookLoadTask - doInBackground");
            bookList.clear();
            bookList = QueryUtils.fetchBooks(urls[0]);
            return bookList;
        }

        protected void onPostExecute(List<Book> books) {

            Log.v(LOG_TAG, "Inside BookLoadTask - onPostExecute");
            // Hide loading indicator because the data has been loaded
            mLoadProgressBar.setVisibility(ProgressBar.GONE);

            // Set empty state text to display "No books found."
            mEmptyStateTextView.setText(R.string.no_books_found);

            // Clear the adapter of previous book data
            mBookAdapter.clear();

            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (books != null && !books.isEmpty()) {
                mBookAdapter.addAll(books);
            }
        }
    }

    private boolean hasInternetConnection() {
        Log.v(LOG_TAG, "Inside hasInternetConnection() method");
        //Determine if there is a network connection to the Internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.v(LOG_TAG, "ConnectivityManager object = " + cm.toString());

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        Log.v(LOG_TAG, "onRetainCustomNonConfigurationInstance() method");
        return bookList;
    }

}