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

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView bookListView = (ListView) findViewById(R.id.list);
        mLoadProgressBar = findViewById(R.id.loading_spinner);
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);
        bookListView.setEmptyView(mEmptyStateTextView);

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
                        // Build final request URL by adding REQUEST_URL_BASE and query
                        String requestUrl = REQUEST_URL_BASE + query;
                        // Start displaying progress bar
                        mLoadProgressBar.setVisibility(View.VISIBLE);

                        SharedPreferences sharePrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        String orderBy = sharePrefs.getString(
                            getString(R.string.settings_order_by_key),
                            getString(R.string.settings_order_by_default)
                        );

                        String maxNumOfBooks = sharePrefs.getString(
                            getString(R.string.settings_maxresults_by_key),
                            getString(R.string.settings_maxresults_default)
                        );

                        boolean showOnlyFreeEBooks = sharePrefs.getBoolean(
                            getString(R.string.settings_only_free_ebooks_key), false);

                        Uri baseUri = Uri.parse(REQUEST_URL_BASE);
                        Uri.Builder uriBuilder = baseUri.buildUpon();

                        uriBuilder.appendQueryParameter("q", query);
                        uriBuilder.appendQueryParameter("maxResults", maxNumOfBooks);
                        uriBuilder.appendQueryParameter("orderBy", orderBy);

                        if (showOnlyFreeEBooks) {
                            uriBuilder.appendQueryParameter("filter", "free-ebooks");
                        }

                        Log.i(LOG_TAG, "URL to fetch books data = " + uriBuilder.toString());

                        // Create AsyncTask to start loading books
                        new BookLoadTask().execute(uriBuilder.toString());
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

        protected List<Book> doInBackground(String... urls) {

            // Don't perform the request if there are no URLs, or the first URL is null.
            if (TextUtils.isEmpty(urls[0])) {
                return null;
            }
            // Clear list of books from previously loaded data and fill with the new data
            bookList.clear();
            bookList = QueryUtils.fetchBooks(urls[0]);
            return bookList;
        }

        protected void onPostExecute(List<Book> books) {

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

    // Internal method that checks if there is a network connection on the user's phone
    private boolean hasInternetConnection() {

        //Determine if there is a network connection to the Internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    // Save loaded books data to show it again when user rotates screen
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        return bookList;
    }

}