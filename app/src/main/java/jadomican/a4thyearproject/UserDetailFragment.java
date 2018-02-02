package jadomican.a4thyearproject;

/**
 * Created by jadom_000 on 27/01/2018.
 */

        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.database.Cursor;
        import android.net.Uri;
        import android.os.Handler;
        import android.os.Bundle;
        import android.support.v4.app.Fragment;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.EditText;

        import jadomican.a4thyearproject.data.UserDetail;
        import jadomican.a4thyearproject.data.UserDetailsContentContract;
        import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsClient;
        import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;

/**
 * A fragment representing a single Note detail screen.
 * This fragment is either contained in a { NoteListActivity}
 * in two-pane mode (on tablets) or a { NoteDetailActivity}
 * on handsets.
 */
public class UserDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "noteId";

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
    EditText editAge;
    EditText editBio;

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
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the ContentResolver
        contentResolver = getContext().getContentResolver();

        // Unbundle the arguments if any.  If there is an argument, load the data from
        // the content resolver aka the content provider.
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_ITEM_ID)) {
            String itemId = getArguments().getString(ARG_ITEM_ID);
            itemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);
            Cursor data = contentResolver.query(itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
            if (data != null) {
                data.moveToFirst();
                mItem = UserDetail.fromCursor(data);
                isUpdate = true;
            }
        } else {
            mItem = new UserDetail();
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
        if (!mItem.getAge().equals(editAge.getText().toString().trim())) {
            mItem.setAge(editAge.getText().toString().trim());
            isUpdated = true;
        }
        if (!mItem.getBio().equals(editBio.getText().toString().trim())) {
            mItem.setBio(editBio.getText().toString().trim());
            isUpdated = true;
        }

        /*
        MORE HERE

        */
        // Convert to ContentValues and store in the database.
        if (isUpdated) {
            ContentValues values = mItem.toContentValues();
            if (isUpdate) {
                contentResolver.update(itemUri, values, null, null);
            } else {
                itemUri = contentResolver.insert(UserDetailsContentContract.UserDetails.CONTENT_URI, values);
                isUpdate = true;    // Anything from now on is an update

                // Send Custom Event to Amazon Pinpoint
                final AnalyticsClient mgr = AWSProvider.getInstance()
                        .getPinpointManager()
                        .getAnalyticsClient();
                final AnalyticsEvent evt = mgr.createEvent("AddNote")
                        .withAttribute("bio", mItem.getBio());
                mgr.recordEvent(evt);
                mgr.submitEvents();            }
        }
    }

    /**
     * Returns the current note.
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
        editAge = (EditText) rootView.findViewById(R.id.edit_title);
        editBio = (EditText) rootView.findViewById(R.id.edit_content);

        editAge.setText(mItem.getAge());
        editBio.setText(mItem.getBio());

        return rootView;
    }
}