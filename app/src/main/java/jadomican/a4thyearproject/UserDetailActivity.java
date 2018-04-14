package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * An activity representing a single edit profile details screen.
 */
public class UserDetailActivity extends BaseAppCompatActivity {

    /**
     * Override the onCreate method in BaseAppCompatActivity with Activity-specific code
     *
     * @see BaseAppCompatActivity#onCreate(Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the user id which this screen is representing
        if (savedInstanceState == null) {
            Bundle arguments = new Bundle();
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey(UserDetailFragment.ARG_ITEM_ID)) {
                String userDetailId = extras.getString(UserDetailFragment.ARG_ITEM_ID);
                arguments.putString(UserDetailFragment.ARG_ITEM_ID, userDetailId);
            }

            // Begin the fragment to display user details
            UserDetailFragment fragment = new UserDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.user_detail_container, fragment)
                    .commit();
        }
    }

    // Return Activity-specific resources to the base class
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_user_detail;
    }

    @Override
    protected int getMenuItemResourceId() {
        return R.id.nav_edit_profile;
    }

    @Override
    protected int getMenuResourceId() {
        return R.menu.bar_menu;
    }

    @Override
    protected String getHelpTitle() {
        return getString(R.string.bar_edit_profile_help_title);
    }

    @Override
    protected String getHelpMessage() {
        return getString(R.string.bar_edit_profile_help_message);
    }

    /**
     * Hide on-screen keyboard when losing EditText focus, ensuring that the Side Bar (if used)
     * does not become obstructed
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

}