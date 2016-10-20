package com.android.nanodegree.jrobbins.popularmovies.app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.android.nanodegree.jrobbins.popularmovies.app.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/*
    Students: These are functions and some test data to make it easier to test your database and
    Content Provider.  Note that you'll want your MoviesContract class to exactly match the one
    in our solution to use these as-given.
 */
public class TestUtilities extends AndroidTestCase {
    public static final String TEST_MOVIE_ID = "271110";
    public static final String TEST_MOVIE_FILTER = "popular";

    static void validateCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();
        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Column '" + columnName + "' not found. " + error, idx == -1);
            String expectedValue = entry.getValue().toString();
            assertEquals("Value '" + entry.getValue().toString() +
                    "' did not match the expected value '" +
                    expectedValue + "'. " + error, expectedValue, valueCursor.getString(idx));
        }
    }

    /*
        Use this to create some default movie values for your database tests.
     */
    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, 2016-04-27);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TITLE, "Captain America: Civil War");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POSTER, "/5N20rQURev5CNDcMjHVUZhpoCNC.jpg");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_OVERVIEW, "Following the events of Age of Ultron, ...");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_BACKDROP_PATH, "/m5O3SZvQ6EgD5XXXLPIP1wLppeW.jpg");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RELEASE_DATE, "2016-04-27");

        return movieValues;
    }

    public static ContentValues createListTypeValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MoviesContract.MovieListsEntry.COLUMN_LIST_ID, TEST_MOVIE_FILTER);
        contentValues.put(MoviesContract.MovieListsEntry.COLUMN_MOVIE_ID, TEST_MOVIE_ID);

        return contentValues;
    }

    static ContentValues createFavoritesValues() {
        // Create a new map of values, where column names are the keys
        ContentValues testValues = new ContentValues();
        testValues.put(MoviesContract.FavoritesEntry.COLUMN_IS_FAVORITE, TEST_MOVIE_ID);

        return testValues;
    }

    static ContentValues createMovieDetailsValues() {
        // Create a new map of values, where column names are the keys
        ContentValues movieValues = new ContentValues();
        movieValues.put(MoviesContract.MovieEntry.COLUMN_HOMEPAGE, "http://marvel.com/captainamericapremiere");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_IMDB_ID, "http://www.imdb.com/tt3498820");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_POPULARITY, 25.0625);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_PRODUCTION_COMPANIES, "Marvel Studios");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_RUNTIME, 146);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_COUNT, 3280);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_VOTE_AVG, 6.78);
        movieValues.put(MoviesContract.MovieEntry.COLUMN_GENRES, "Action, Science Fiction, Thriller");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_TRAILERS, "{\"id\":271110,\"results\":[{\"id\":\"5794ccaa9251414236001173\",\"iso_639_1\":\"en\",\"iso_3166_1\":\"US\",\"key\":\"43NWzay3W4s\",\"name\":\"Official Trailer #1\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"},{\"id\":\"5738f0ac92514166fe000fb6\",\"iso_639_1\":\"en\",\"iso_3166_1\":\"US\",\"key\":\"dKrVegVI0Us\",\"name\":\"Official Trailer 2\",\"site\":\"YouTube\",\"size\":1080,\"type\":\"Trailer\"}]}");
        movieValues.put(MoviesContract.MovieEntry.COLUMN_REVIEWS, "{\"id\":271110,\"page\":1,\"results\":[{\"id\":\"572d7bc1c3a3680fdb001d69\",\"author\":\"Frank Ochieng\",\"content\":\"Well another super-sized Marvel Comics superhero saga hits the big screen with the selected savior Captain America taking the top billing on the marquee. Thus, the pulsating popcorn pleaser **Captain America: Civil War** arrives on the scene to giddy audiences that have been loyal and fixated on the successful spring of mighty Marvel heroes that have been paraded to viewers throughout the last few years. Thankfully, **Captain America: Civil War** carries on the tradition of spry superhero-studded spectacles that have been glorious and adventurous from the stable of Marvel-based movies guaranteed to win over the enthusiastic hearts of avid comic book fanboys everywhere. Joyously overstuffed and convincingly extensive with a super team armed with crime-fighting excess, **Civil War** seizes the moment to introduce a noteworthy twist: two factions of rescuing superpowers engaging in some explosive in-house fighting led by two of Marvel Comics main standouts in Chris Evans’s Captain America and Robert Downey Jr.’s Iron Man.\\r\\n\\r\\nCo-directors Anthony Russo and Rene Russo were instinctively crafty to link the overly successful Iron Man film franchise to the **Captain America: Civil War** inner circle to ensure an even more treasured toxic atmosphere. Superhero fans will get a thrill of witnessing the extra add-ons concerning other dynamic titans to join the action-packed festivities involving Team Captain America versus Team Iron Man. True, **Civil War** has its share of flaws but that does not take away from this heroes-in-crisis flick demonstrating its ambitious overtones.\\r\\n\\r\\nSo what has caused the bad blood among the great and grand good guys known for protecting the world from evil dominance and destruction? Essentially the theme of collateral damage, the involvement regarding civilian-related deaths and injuries plus the world-wide backlash has created a controversy pitted against The Avengers. Specifically, Captain America (a.k.a. Steve Rogers) and Scarlet Witch/Wanda Maximoff (Elizabeth Olsen) are responsible for the boisterous battle that caused such negative sensation resulting in political turmoil. Avengers head honcho Iron Man (a.k.a. Tony Stark) is dealing with his own personal regrets in the ill-advised creation of the unpredictable Ultron. The political authority want to hold Iron Man, second-in-command Captain America and the rest of the Avengers accountable for the global devastation that have taking its toll when trying to oversee the potential harm wreaking havoc on humanity.\\r\\n\\r\\nLeading the charge in putting a watchful eye on the labeled reckless Avengers is Secretary of State Thaddeus Ross (William Hurt) that suggests the super group be monitored by the United Nations. Naturally friction develops between the superheroes that either agree with Ross’s UN restriction policies or disagree with being placed under a microscope that threatens to handcuff their free-wheeling heroic duties. All these contrasting beliefs eventually turn into epic back-and-forth confrontations where the raging Avengers are at odds with each other.\\r\\n\\r\\nFor Stark/Iron Man’s stance, he is willing to toe the line and ultimately agree that his crew needs to tone down their tenacious tactics as crime-stoppers. Iron Man’s consciousness, particularly in the case of a disillusioned mother (Alfre Woodward) making him feel guilt-ridden over her son’s death during an intense Battle of Sokovia, is probably the main factor behind his decision to have his team reigned in a bit from the political watchdogs. Siding with Iron Man’s viewpoint are the likes of Black Widow (Scarlett Johansson), Vision (Paul Bettany) and War Machine (Don Cheadle). As for Rogers/Captain America, he is not too thrilled being put in check by the intrusive governmental pencil-pushers that want to scrutinize the team’s every move. Standing with Captain America firmly are Falcon (Anthony Mackie) and the aforementioned Scarlet Witch.\\r\\n\\r\\nOn top of Captain America’s current Avengers-oriented strife in his contentious mingling with Iron Man and his ardent backers, he now finds himself trying to defend his old buddy-turned-wanted man Winter Soldier (Sebastian Stan) who is accused of killing civilians. Iron Man believes in Winter Soldier’s innocence and goes so far as to help him escape. Yes…Winter Soldier does come with more baggage attached to him–mainly in the form of the menacing Zemo (Daniel Bruhl).\\r\\n\\r\\nThe Russos and screenwriters Christopher Marckus and Stephen McFeely (all attached to the previous “Captain America: Winter Soldier”) provide the eyeful visual effects that predictably stimulate and effectively add to the overload of frenzied frolicking in this boisterous blockbuster. Certainly the deepened angst among this bombastic bunch works far more solidly than what was displayed in the stiffened and problematic Batman v. Superman. The notion that the entire globe and its leaders are weary of all the collective chaos at the hands of the Avengers trying to save their hides is a bit ridiculous. Besides, why are not the foes of the Avengers put on the hot coals for the societal ruination? It seems rather counterproductive to chastise the noble superpowers offering the safety of mankind yet the detractors not being grateful for the services that the Avengers bring to the table. It is somewhat convoluted to think that the global community are sour on our heralded heroes or that the heat generated within the walls will completely destroy the Avengers and their colorful, capable colleagues.\\r\\n\\r\\nThe movie’s aptly entitled **Civil War** does invite more punch to the proceedings especially when a who’s who of superhero showstoppers join the feisty fray at hand. The noted inclusion of defiant do-gooders are packed with the likes of Spider-Man (Tom Holland), the retired returnee Hawkeye (Jeremy Renner), Ant-Man (Paul Rudd), and nifty newcomer Black Panther (Chadwick Boseman). No doubt Captain America: Civil War is the impish and energizing launch pad for upcoming Marvel-induced movies waiting to make their future distinctive arrival on the big screen. Although Evans’s steady and charismatic Captain America more than holds his own as the solo act billed in the film’s title one cannot overlook Downey’s compelling Iron Man as the reliable source that lifts the profile of Evans’s Masked Wonder. This is indeed a collaborative big score for the glorified costumed cast but special kudos are reserved for Stan’s killing culprit as well as Boseman’s African president assuming the slick and resourceful Black Panther.\\r\\n\\r\\nYeah, **Captain America: Civil War** is true to its frenetic form as revved up entertainment preparing moviegoers for the upcoming summertime sizzle at the box office. After all, the on-screen Marvel Comics gravy train keeps moving merrily along so stay tuned.\\r\\n\\r\\n**Captain America: Civil War (2016)**\\r\\n\\r\\nWalt Disney Pictures\\r\\n\\r\\n2 hrs 26 mins.\\r\\n\\r\\nStarring: Chris Evans, Robert Downey, Jr., Scarlett Johansson, Sebastian Stan, Anthony Mackie, Don Cheadle, Jeremy Renner, Chadwick Boseman, Paul Bettany, Elizabeth Olsen, Paul Rudd, Daniel Bruhl, Alfre Woodward, William Hurt\\r\\n\\r\\nDirected by: Anthony Russo and Joe Russo\\r\\n\\r\\nMPAA Rating: PG-13\\r\\n\\r\\nGenre: Superheroes Saga/Action and Adventure/Science Fiction/Fantasy\\r\\n\\r\\nCritic’s Rating: *** stars (out of 4 stars)\\r\\n\\r\\n**(c) Frank Ochieng**\",\"url\":\"https://www.themoviedb.org/review/572d7bc1c3a3680fdb001d69\"},{\"id\":\"57acf05cc3a36820750001c8\",\"author\":\"Austin Singleton\",\"content\":\"The Russo Brother's and Marvel did it again! Read my full review here!\\r\\n\\r\\nhttp://www.hweird1reviews.com/allreviews/captain-america-civil-war-review\",\"url\":\"https://www.themoviedb.org/review/57acf05cc3a36820750001c8\"},{\"id\":\"57f40f319251417fe300171a\",\"author\":\"Reno\",\"content\":\"**The heroes're divided and so the fans!**\\r\\n\\r\\nBefore watching this I thought what the Avengers are doing here. I believed they got the title wrong. Once I watched it, I'm still the same. Because it did not look like the Captain America's film, he never was dominated, so calling it as his film totally inappropriate. Technically, it is a third Avengers film and well done. I liked it, it was entertaining. But the Civil War means it's nothing a nation's war within, just these super-guys fight for themselves against what kind of administration they want to come under.\\r\\n\\r\\nI think they have heard that people's complaint about blowing up the buildings during reviewing other films by bringing up this one. So they finger pointed those events in this narration and that is one of the reasons for the Civil War to take place. Particularly disturbing the public and loss of many lives when these powerful men fighting the bad guys. The fun part is they are off the street and had a most important confrontation of the film in a deserted airfield. So does it make people who were whining about that be happy?\\r\\n\\r\\nI don't know, but as an entertaining film, it did everything it can give the best for the viewers. Like the title and posters reveal, the heroes are divided here and probably you're going to take a side. Like usual, I'm with the Stark. So if you're like me, then you would feel the annoyance with the opposite team, particularly like Antman and obviously Captain America's rebellious decision. It was like the world versus United States, because only Americans are behind Captain America. Anyway, I did not like dividing the fans and what this film has been a serious damage. I hope the fans won't take it seriously.\\r\\n\\r\\n_8/10_\",\"url\":\"https://www.themoviedb.org/review/57f40f319251417fe300171a\"}],\"total_pages\":1,\"total_results\":3}");

        return movieValues;
    }

    static long insertTestFavoriteValues(Context context) {
        // insert our test records into the database
        MoviesDbHelper dbHelper = new MoviesDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testValues = TestUtilities.createFavoritesValues();

        long favoritesRowId;
        favoritesRowId = db.insert(MoviesContract.FavoritesEntry.TABLE_NAME, null, testValues);

        // Verify we got a row back.
        assertTrue("Error: Failure to insert favorites Values", favoritesRowId != -1);

        return favoritesRowId;
    }

    /*
        Test the ContentObserver callbacks using the PollingCheck class that we grabbed from the Android
        CTS tests.

        Note that this only tests that the onChange function is called; it does not test that the
        correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
