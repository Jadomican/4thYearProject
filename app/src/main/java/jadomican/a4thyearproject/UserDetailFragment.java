package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.app.DatePickerDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsClient;
import com.amazonaws.mobileconnectors.pinpoint.analytics.AnalyticsEvent;

import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;

import java.util.Calendar;

/**
 * A fragment representing a single user profile edit details screen.
 */
public class UserDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "profileId";

    /**
     * The content this fragment is presenting.
     */
    private UserDetail mItem;
    private Uri mItemUri;

    /**
     * Content Resolver which provides access to the content provider
     *
     * @see jadomican.a4thyearproject.data.UserDetailsContentProvider
     */
    private ContentResolver contentResolver;

    /**
     * Is this an insert or an update? This would handle a case in which the user has no previous
     * record in the database
     */
    private boolean isUpdate;

    /**
     * The component bindings
     */
    private EditText mEditBio;
    private EditText mEditDateOfBirth;
    private EditText mEditFirstName;
    private EditText mEditLastName;

    /**
     * Required empty constructor for the fragment manager to instantiate the
     * fragment
     */
    public UserDetailFragment() {
    }

    // Constants used for async data operations
    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    private static final int INSERT_TOKEN = 1003;

    /**
     * Unbundle the profile id that this screen is representing.
     */
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
            mItemUri = UserDetailsContentContract.UserDetails.uriBuilder(itemId);
            syncUser();
        } else {
            isUpdate = false;
        }
    }

    /**
     * Sync user profile with the latest details from the database
     */
    public void syncUser() {
        // Network tasks must not be performed in the UI thread. Hence the Async Task used here
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);
                cursor.moveToFirst();
                mItem = UserDetail.fromCursor(cursor);
                isUpdate = true;

                mEditDateOfBirth.setText(mItem.getDateOfBirth());
                mEditBio.setText(mItem.getBio());
                mEditFirstName.setText(mItem.getFirstName());
                mEditLastName.setText(mItem.getLastName());
            }
        };
        queryHandler.startQuery(QUERY_TOKEN, null, mItemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);

    }

    /**
     * Handle the fragment being paused
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Save the data from the form back into the database.
     */
    private void saveData() {
        boolean isUpdated = false;

        // Here we determine whether or not the user has updated the details in any of the profile
        // fields. If not we inform the user and abort the unnecessary update network operation
        if (!mEditFirstName.getText().toString().trim().equals(mItem.getFirstName())) {
            mItem.setFirstName(mEditFirstName.getText().toString().trim());
            isUpdated = true;
            Log.d("Profile Change", "fName");
        }
        if (!mEditLastName.getText().toString().trim().equals(mItem.getLastName().trim())) {
            mItem.setLastName(mEditLastName.getText().toString().trim());
            isUpdated = true;
            Log.d("Profile Change", "lName");
        }
        if (!mEditDateOfBirth.getText().toString().trim().equals(mItem.getDateOfBirth())) {
            mItem.setDateOfBirth(mEditDateOfBirth.getText().toString().trim());
            isUpdated = true;
            Log.d("Profile Change", "dob");

        }
        if (!mEditBio.getText().toString().trim().equals(mItem.getBio())) {
            mItem.setBio(mEditBio.getText().toString().trim());
            isUpdated = true;
            Log.d("Profile Change", "bio");
        }

        // Convert to ContentValues and store in the database.
        if (isUpdated) {
            ContentValues values = mItem.toContentValues();

            AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
                @Override
                protected void onInsertComplete(int token, Object cookie, Uri uri) {
                    super.onInsertComplete(token, cookie, uri);
                    MediApp.customToast("Details Saved", MediApp.KEY_POSITIVE);
                    Log.d("UserDetailFragment", "insert completed");
                }

                @Override
                protected void onUpdateComplete(int token, Object cookie, int result) {
                    super.onUpdateComplete(token, cookie, result);
                    MediApp.customToast("Details Saved", MediApp.KEY_POSITIVE);
                    Log.d("UserDetailFragment", "update completed");
                }
            };
            if (isUpdate) {

                queryHandler.startUpdate(UPDATE_TOKEN, null, mItemUri, values, null, null);
            } else {
                queryHandler.startInsert(INSERT_TOKEN, null, UserDetailsContentContract.UserDetails.CONTENT_URI, values);

                // Send Custom Event to Amazon Pinpoint
                final AnalyticsClient mgr = AWSProvider.getInstance()
                        .getPinpointManager()
                        .getAnalyticsClient();
                final AnalyticsEvent evt = mgr.createEvent("EditProfile")
                        .withAttribute("profileId", mItem.getProfileId());
                mgr.recordEvent(evt);
                mgr.submitEvents();
            }
        } else {
            MediApp.customToast("No Changes Made", MediApp.KEY_NEGATIVE);
        }
    }

    /**
     * Sync user on resume to maintain latest details
     */
    @Override
    public void onResume() {
        super.onResume();
        syncUser();
    }

    /**
     * Populate the editable text boxes on view creation.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Get a reference to the root view
        View rootView = inflater.inflate(R.layout.user_detail, container, false);

        // Update the text in the editor
        mEditDateOfBirth = (EditText) rootView.findViewById(R.id.edit_dob);
        mEditBio = (EditText) rootView.findViewById(R.id.edit_bio);
        mEditFirstName = (EditText) rootView.findViewById(R.id.edit_first_name);
        mEditLastName = (EditText) rootView.findViewById(R.id.edit_last_name);

        mEditDateOfBirth.setText(mItem.getDateOfBirth());
        mEditBio.setText(mItem.getBio());
        mEditFirstName.setText(mItem.getFirstName());
        mEditLastName.setText(mItem.getLastName());

        // Create a listener to allow user to choose date from a calendar
        mEditDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                mEditDateOfBirth.setText(String.format("%04d-%02d-%02d", year, (monthOfYear + 1), dayOfMonth));
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        // Button to allow user to save new details to profile
        Button updateProfileButton = (Button) rootView.findViewById(R.id.update_profile_button);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save data on button click
                saveData();
            }
        });
        return rootView;
    }
}