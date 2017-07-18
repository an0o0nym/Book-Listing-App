package com.example.android.booklistingapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Book>> {

    private static final String LOG_TAG = BookActivity.class.getName();
    private static final String REQUEST_URL = "https://www.googleapis.com/books/v1/volumes";
    private static final int BOOK_LOADER_ID = 1;

    private EditText searchText;
    private ProgressBar loadView;
    private TextView emptyView;

    private BookAdapter mAdapter;

    private String bookTitle;
    private ConnectivityManager connMgr;
    private NetworkInfo netInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_activity);

        final ListView bookListView = (ListView) findViewById(R.id.list);
        searchText = (EditText) findViewById(R.id.search_text);
        Button searchButton = (Button) findViewById(R.id.search_button);
        loadView = (ProgressBar) findViewById(R.id.load_view);
        emptyView = (TextView) findViewById(R.id.empty_view);
        mAdapter = new BookAdapter(this, new ArrayList<Book>());

        bookListView.setAdapter(mAdapter);

        loadView.setVisibility(View.GONE);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasInternetConn()) {
                    emptyView.setText("");
                    loadView.setVisibility(View.VISIBLE);
                    hideKeyboard();
                    getLoaderManager().restartLoader(BOOK_LOADER_ID, null, BookActivity.this);
                }
            }
        });

        if (hasInternetConn()) {
            getLoaderManager().initLoader(BOOK_LOADER_ID, null, BookActivity.this);
        }

    }

    @Override
    public Loader<List<Book>> onCreateLoader(int i, Bundle bundle) {
        Uri baseUri = Uri.parse(REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        bookTitle = searchText.getText().toString().trim();

        uriBuilder.appendQueryParameter("q", bookTitle);
        uriBuilder.appendQueryParameter("maxResults", "10");
        return new BookLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Book>> loader, List<Book> books) {
        mAdapter.clear();
        loadView.setVisibility(View.GONE);

        if (books != null && !books.isEmpty()) {
            mAdapter.addAll(books);
        } else if (TextUtils.isEmpty(bookTitle)) {
            // if user didn't enter any search phrase
            emptyView.setText(getString(R.string.start_search));
        } else {
            // if user did enter search phrase
            emptyView.setText(getString(R.string.no_books_found));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Book>> loader) {
        mAdapter.clear();
    }

    /**
     * Helper method used to determine whether device has internet connection
     *
     * @return true if device has internet connection, false otherwise.
     */
    private boolean hasInternetConn() {
        connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        netInfo = connMgr.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected()) {
            return true;
        } else {
            mAdapter.clear();
            emptyView.setText(getString(R.string.no_internet_connection));
            return false;
        }
    }

    /**
     * Helper method used to hide keyboard.
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow((null == getCurrentFocus())
                ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
