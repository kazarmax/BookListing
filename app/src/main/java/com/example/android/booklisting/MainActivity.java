package com.example.android.booklisting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
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
            "https://www.googleapis.com/books/v1/volumes";

    /** Object for storing a list of Books loaded using Google Books API */
    private ArrayList<Book> bookList;

    /** Adapter for the list of books */
    private BookAdapter mBookAdapter;

    /** Progress bar to be shown while books are being loaded from server */
    private View mLoadProgressBar;

    private static final String LOG_TAG = MainActivity.class.getName();

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    /** Set of vars for auto loading books using OnScrollListener */
    // The minimum amount of items to have below your current scroll position, before loading more.
    private static final int BOOK_LOAD_THRESHOLD = 5;

    // Index in the list of books on the remote server to star loading from
    private int bookLoadStartIndex = 0;

    // Number of books to load per one request to server
    private static final int BOOK_LOAD_PORTION = 10;

    // Flag indicating that we are still waiting for the last set of data to load - if True
    private boolean loadInProgress = true;

    private String currentQueryText;

    // Creates OnScrollListener used to start loading new portion of books when list is scrolled to the end
    private AbsListView.OnScrollListener booksOnScrollListener = new AbsListView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            if (!loadInProgress && totalItemCount > 0 && (totalItemCount - visibleItemCount) <= (firstVisibleItem + BOOK_LOAD_THRESHOLD)) {
                new BookLoadTask(false).execute(buildBookLoadUrlString(currentQueryText));
                loadInProgress = true;
                bookLoadStartIndex += BOOK_LOAD_PORTION;
                Log.i(LOG_TAG, "Async task created from onScroll");
            }

        }
    };

    // Save loaded books data to show it again when user rotates screen
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return bookList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView) findViewById(R.id.list);

        mLoadProgressBar = findViewById(R.id.loading_spinner);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);

        bookListView.setEmptyView(mEmptyStateTextView);
        bookListView.setOnScrollListener(booksOnScrollListener);

        // Load previously saved data of loaded books
        if (getLastCustomNonConfigurationInstance() == null) {
            bookList = new ArrayList<Book>();
        } else {
            bookList = (ArrayList<Book>) getLastCustomNonConfigurationInstance();
        }

        // Create custom ArrayAdapter and link it to the ListView
        mBookAdapter = new BookAdapter(this, bookList);
        bookListView.setAdapter(mBookAdapter);

        // Set OnCLickListener to the ListView to be able to open a link to view info about selected book
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current book that was clicked on
                Book currentBook = mBookAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bookUri = Uri.parse(currentBook.getPreviewLink());

                // Create a new intent to view the book URI
                Intent intent = new Intent(Intent.ACTION_VIEW, bookUri);

                // Send the intent to launch a new activity
                startActivity(intent);
            }
        });

        // If there is a network connection, fetch data
        if (hasInternetConnection()) {
            SearchView searchView = (SearchView) findViewById(R.id.search_view);

            // Set listener to the SearchView to process search query inputs from users
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Perform request only if search query is not empty (null or 0-length)
                    if (!TextUtils.isEmpty(query)) {

                        currentQueryText = query;

                        // Reset startIndex to load books from the beginning of the list because of new search query
                        bookLoadStartIndex = 0;

                        // Start displaying progress bar
                        mLoadProgressBar.setVisibility(View.VISIBLE);

                        // Create AsyncTask to start loading books with True parameter indicating that it is a new search query
                        new BookLoadTask(true).execute(buildBookLoadUrlString(query));
                        loadInProgress = true;
                        bookLoadStartIndex += BOOK_LOAD_PORTION;
                        Log.i(LOG_TAG, "Async task created from onQueryTextSubmit");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * AsyncTask class for loading books data in background thread
     */
    private class BookLoadTask extends AsyncTask<String, Void, List<Book>> {

        private boolean isSearchQueryChanged = true;

        private BookLoadTask(boolean isSearchQueryChanged) {
            this.isSearchQueryChanged = isSearchQueryChanged;
        }

        protected List<Book> doInBackground(String... urls) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (TextUtils.isEmpty(urls[0])) {
                return null;
            }

            // If user search query has changed, clear list of books from old data
            if (isSearchQueryChanged) {
                bookList.clear();
            }

            bookList = QueryUtils.fetchBooks(urls[0]);
            return bookList;
        }

        protected void onPostExecute(List<Book> books) {
            // Load completed, so turning flag off
            loadInProgress = false;

            Log.i(LOG_TAG, "Async task completed");

            // Hide loading indicator because the data has been loaded
            mLoadProgressBar.setVisibility(ProgressBar.GONE);

            // Set empty state text to display "No books found."
            mEmptyStateTextView.setText(R.string.no_books_found);

            // If user search query has changed, clear list of books in adapter from old data
            if (isSearchQueryChanged) {
                mBookAdapter.clear();
            }

            // If there is a valid list of {@link Book}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (books != null && !books.isEmpty()) {
                mBookAdapter.addAll(books);
            }

        }
    }

    // Internal method that checks if there is a network connection on the user's phone
    private boolean hasInternetConnection() {

        //Determine if there is a network connection to the Internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    private String buildBookLoadUrlString(String userQueryText) {
        SharedPreferences sharePrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String orderBy = sharePrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        boolean showOnlyFreeEBooks = sharePrefs.getBoolean(
                getString(R.string.settings_only_free_ebooks_key), false);

        Uri baseUri = Uri.parse(REQUEST_URL_BASE);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("q", userQueryText);
        uriBuilder.appendQueryParameter("maxResults", String.valueOf(BOOK_LOAD_PORTION));
        uriBuilder.appendQueryParameter("orderBy", orderBy);
        uriBuilder.appendQueryParameter("startIndex", String.valueOf(bookLoadStartIndex));

        if (showOnlyFreeEBooks) {
            uriBuilder.appendQueryParameter("filter", "free-ebooks");
        }

        Log.i(LOG_TAG, "URL to fetch books data = " + uriBuilder.toString());

        return uriBuilder.toString();
    }

}