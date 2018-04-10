package jadomican.a4thyearproject;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.amazonaws.mobile.auth.core.DefaultSignInResultHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.IdentityProvider;
import com.amazonaws.mobile.auth.ui.AuthUIConfiguration;
import com.amazonaws.mobile.auth.ui.SignInActivity;

import jadomican.a4thyearproject.data.UserDetail;
import jadomican.a4thyearproject.data.UserDetailsContentContract;


public class AuthenticatorActivity extends AppCompatActivity {

    private UserDetail mItem;
    private Uri itemUri;

    private ContentResolver contentResolver;
    private static final int QUERY_TOKEN = 1001;
    private static final int UPDATE_TOKEN = 1002;
    private static final int INSERT_TOKEN = 1003;
    private Activity successfulSignInActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);
        final IdentityManager identityManager = AWSProvider.getInstance().getIdentityManager();
        // Set up the callbacks to handle the authentication response
        identityManager.setUpToAuthenticate(this, new DefaultSignInResultHandler() {
            @Override
            public void onSuccess(Activity activity, IdentityProvider identityProvider) {
                successfulSignInActivity = activity;
                itemUri = UserDetailsContentContract.UserDetails.uriBuilder(AWSProvider.getInstance().getIdentityManager().getCachedUserID());
                syncUser();
            }

            @Override
            public boolean onCancel(Activity activity) {
                Log.d("ON CANCEL", "CANCEL");
                return false;
            }
        });

        // Start the authentication UI
        AuthUIConfiguration config = new AuthUIConfiguration.Builder()
                .userPools(true).logoResId(R.drawable.mediapp_sign_in_round_alt)
                .backgroundColor(Color.rgb(42, 112, 132))
                .build();
        SignInActivity.startSignInActivity(this, config);
        //AuthenticatorActivity.this.finish();
    }

    private void syncUser() {
        contentResolver = getApplicationContext().getContentResolver();
        // Asynchronously query the backend database. Here a check is performed to see if the user
        // is a brand new user to the app, i.e. they have no entry in the database. If this is the
        // case an insert is made to the database to store the users details
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
                super.onQueryComplete(token, cookie, cursor);

                cursor.moveToFirst();
                // Due to the way cursors work, they still exist after reading through each column of data
                // isAfterLast determines if the cursor passed in has any data or not
                if (cursor.isAfterLast()) {
                    mItem = UserDetail.fromCursor(cursor);
                    saveData();
                } else {
                    goToMainActivity();
                }
            }
        };
        queryHandler.startQuery(QUERY_TOKEN, null, itemUri, UserDetailsContentContract.UserDetails.PROJECTION_ALL, null, null, null);
    }

    private void saveData() {
        AsyncQueryHandler queryHandler = new AsyncQueryHandler(contentResolver) {
            @Override
            protected void onInsertComplete(int token, Object cookie, Uri uri) {
                super.onInsertComplete(token, cookie, uri);
                Log.d("UserDetailFragment", "insert completed");
                goToMainActivity();
            }
        };
        ContentValues values = mItem.toContentValues();
        queryHandler.startInsert(INSERT_TOKEN, null, UserDetailsContentContract.UserDetails.CONTENT_URI, values);

    }

    private void goToMainActivity() {
        MediApp.customToast(getResources().getString(R.string.logged_in));
        final Intent intent = new Intent(this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        successfulSignInActivity.startActivity(intent);
        successfulSignInActivity.finish();
    }


}
