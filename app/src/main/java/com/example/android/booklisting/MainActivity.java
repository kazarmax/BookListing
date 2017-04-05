package com.example.android.booklisting;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Book>> {

    /** URL for earthquake data from the USGS dataset */
    private String requestUrl;

    /** Adapter for the list of books */
    private BookAdapter mBookAdapter;

    private View mLoadProgressBar;

    /**
     * Constant value for the book loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int BOOK_LOADER_ID = 1;

    private static final String LOG_TAG = MainActivity.class.getName();

    /** TextView that is displayed when the list is empty */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the {@link ListView} in the layout
        ListView bookListView = (ListView) findViewById(R.id.list);

        mLoadProgressBar = findViewById(R.id.loading_spinner);

        mEmptyStateTextView = (TextView) findViewById(R.id.empty_list_view);
        bookListView.setEmptyView(mEmptyStateTextView);

        mBookAdapter = new BookAdapter(this, new ArrayList<Book>());
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
            Log.v(LOG_TAG, "In onCreate method - getLoaderManager().initLoader invoked");
            // Get a reference to the LoaderManager, in order to interact with loaders.

            Button searchButton = (Button) findViewById(R.id.book_search_btn);
            searchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    SearchView searchView = (SearchView) findViewById(R.id.search_view);
                    if (searchView.getQuery() != "") {
                        requestUrl = "https://www.googleapis.com/books/v1/volumes?maxResults=10&q="
                                + searchView.getQuery();
                    }

                    mLoadProgressBar.setVisibility(View.VISIBLE);
                    LoaderManager loaderManager = getLoaderManager();

                    // Initialize the loader. Pass in the int ID constant defined above and pass in null for
                    // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
                    // because this activity implements the LoaderCallbacks interface).
                    loaderManager.initLoader(BOOK_LOADER_ID, null, MainActivity.this);
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
    public Loader<List<Book>> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader callback triggered");
        return new BookLoader(this, requestUrl);
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        Log.v(LOG_TAG, "onLoadFinished callback triggered");

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

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        Log.v(LOG_TAG, "onLoaderReset callback triggered");
        mBookAdapter.clear();
    }

    private boolean hasInternetConnection() {
        //Determine if there is a network connection to the Internet
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        Log.v(LOG_TAG, "ConnectivityManager object = " + cm.toString());

        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}