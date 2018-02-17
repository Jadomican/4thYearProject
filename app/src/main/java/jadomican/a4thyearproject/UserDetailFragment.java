package jadomican.a4thyearproject;

/**
 * Created by jadom_000 on 27/01/2018.
 */

        import android.content.AsyncQueryHandler;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Handler;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.EditText;

        import jadomican.a4thyearproject.data.UserDetail;
        import jadomican.a4thyearproject.data.UserDetailsContentContract;
        import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsClient;
        import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;

/**
 * A fragment representing a single Profile detail screen.
 * This fragment is either contained in a { ListActivity}
 * in two-pane mode (on tablets) or a { UserDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "profileId";

    /**
     * The dummy content this fragment is presenting.
     */
    private UserDetail mItem;
    private Uri itemUri;

    /**
     * Content Resolver
     */
    private ContentResolver contentResolver;

    /**
     * Is this an insert or an update?
     */
    private boolean isUpdate;

    /**
     * The component bindings
     */
    EditText editBio;
    EditText editDateOfBirth;
    EditText editFirstName;
    EditText editLastName;


    /**
     * The timer for saving the record back to SQL
     */
    Handler timer = new Handler();
    Runnable timerTask = new Runnable() {
        @Override
        public void run() {
            saveData();                             // Save the data
            timer.postDelayed(timerTask, 5000);     // Every 5 seconds
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public UserDetailFragment() {
    }

    /**
     * Lifecycle event handler - called when the fragment is created.
     * @param savedInstanceState the saved state
     */
// Constants used for async data operations
    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    private static final int INSERT_TOKEN = 1003;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ContentResolver
        contentResolver = getContext().getContentResolver();

        // Unbundle the arguments if any.  If there is an argument, load the data from
        // the content resolver aka the content provider.
        Bundle arguments = getArguments();
        mItem = new UserDetail();
        if (arguments != null && arguments.containsKey(ARG_ITEM_ID)) {
            String itemId = getArguments().getString(ARG_ITEM_ID);
            itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);


            // Replace local cursor methods with async query handling
            AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                @Override
                protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                    super.onQueryComplete(token, cookie, cursor);
                    cursor.moveToFirst();
                    mItem = UserDetail.fromCursor(cursor);
                    isUpdate = true;

                    editDateOfBirth.setText(mItem.getDateOfBirth());
                    editBio.setText(mItem.getBio());
                    editFirstName.setText(mItem.getFirstName());
                    editLastName.setText(mItem.getLastName());
                }
            };
            queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);

        } else {
            isUpdate = false;
        }

        // Start the timer for the delayed start
        timer.postDelayed(timerTask, 5000);
    }


    /**
     * Lifecycle event handler - called when the fragment is paused.  Use this to do any
     * saving of data as it is the last opportunity to reliably do so.
     */
    @Override
    public void onPause() {
        super.onPause();
        timer.removeCallbacks(timerTask);
        saveData();
    }

    /**
     * Save the data from the form back into the database.
     */
    private void saveData() {
        // Save the edited text back to the item.
        boolean isUpdated = false;
        if (!mItem.getDateOfBirth().equals(editDateOfBirth.getText().toString().trim())) {
            mItem.setDateOfBirth(editDateOfBirth.getText().toString().trim());
            isUpdated = true;
        }
        if (!mItem.getBio().equals(editBio.getText().toString().trim())) {
            mItem.setBio(editBio.getText().toString().trim());
            isUpdated = true;
        }
        if (!mItem.getFirstName().equals(editFirstName.getText().toString().trim())) {
            mItem.setFirstName(editFirstName.getText().toString().trim());
            isUpdated = true;
        }
        if (!mItem.getLastName().equals(editLastName.getText().toString().trim())) {
            mItem.setLastName(editLastName.getText().toString().trim());
            isUpdated = true;
        }


        // Convert to ContentValues and store in the database.
        if (isUpdated) {
            ContentValues values = mItem.toContentValues();

            AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                @Override
                protected void onInsertComplete(int token, Object cookie, Uri uri) {
                    super.onInsertComplete(token, cookie, uri);
                    Log.d("UserDetailFragment", "insert completed");
                }

                @Override
                protected void onUpdateComplete(int token, Object cookie, int result) {
                    super.onUpdateComplete(token, cookie, result);
                    Log.d("UserDetailFragment", "update completed");
                }
            };
            if (isUpdate) {

                queryHandler.startUpdate(UPDATE_TOKEN, null, itemUri, values, null, null);
            } else {
                queryHandler.startInsert(INSERT_TOKEN, null, UserDetailsContentContract.UserDetails.CONTENT_URI, values);
                //isUpdate = true;    // Anything from now on is an update

                // Send Custom Event to Amazon Pinpoint
                final AnalyticsClient mgr = AWSProvider.getInstance()
                        .getPinpointManager()
                        .getAnalyticsClient();
                final AnalyticsEvent evt = mgr.createEvent("AddProfile")
                        .withAttribute("profileId", mItem.getProfileId());
                mgr.recordEvent(evt);
                mgr.submitEvents();
            }


        }
    }

    /**
     * Returns the current profile.
     * @return the current data
     */
    public UserDetail getUserDetails() {
        return mItem;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get a reference to the root view
        View rootView = inflater.inflate(R.layout.user_detail, container, false);

        // Update the text in the editor
        editDateOfBirth = (EditText) rootView.findViewById(R.id.edit_dob);
        editBio = (EditText) rootView.findViewById(R.id.edit_bio);
        editFirstName = (EditText) rootView.findViewById(R.id.edit_first_name);
        editLastName = (EditText) rootView.findViewById(R.id.edit_last_name);

        editDateOfBirth.setText(mItem.getDateOfBirth());
        editBio.setText(mItem.getBio());
        editFirstName.setText(mItem.getFirstName());
        editLastName.setText(mItem.getLastName());

        return rootView;
    }
}