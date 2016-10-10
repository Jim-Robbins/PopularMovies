package com.android.nanodegree.jrobbins.popularmovies.app;

import android.content.Intent;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.android.nanodegree.jrobbins.popularmovies.app.fragments.DetailFragment;
import com.android.nanodegree.jrobbins.popularmovies.app.fragments.MovieFragment;
import com.android.nanodegree.jrobbins.popularmovies.app.utils.Utility;
import com.facebook.stetho.Stetho;

public class MainActivity extends AppCompatActivity {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mSortBy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Stetho.initializeWithDefaults(this);

        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MovieFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            // Skip the headers in PreferenceActivity when there's only one header -  http://stackoverflow.com/questions/10802640/skip-the-headers-in-preferenceactivity-when-theres-only-one-header
            intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
            intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
            this.startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String sortBy = Utility.getPreferredMovieList( this );
        // update the location in our second pane using the fragment manager
        if (sortBy != null && !sortBy.equals(mSortBy)) {
//            MovieFragment mf = (MovieFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_main);
//            if ( null != mf ) {
//                mf.onListTypeChanged();
//            }
//            DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
//            if ( null != df ) {
//                df.onListTypeChanged(sortBy);
//            }
            mSortBy = sortBy;
        }
    }
}
