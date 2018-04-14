package jadomican.a4thyearproject.data;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */


import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import jadomican.a4thyearproject.AWSProvider;
import jadomican.a4thyearproject.MediApp;

/**
 * The User Profile model
 * <p>
 * _id              The internal ID - only relevant to the current device user
 * profileId        The global ID - should be unique globally
 * addedMedicines   The JSON Array storing added medicine details
 */
public class UserDetail {
    private long id = -1;
    private String profileId;
    private List<Medicine> addedMedicines = new ArrayList<Medicine>();
    private String dateOfBirth;
    private String bio;
    private String firstName;
    private String lastName;

    /**
     * Overloaded constructor to create a new User object
     */
    public UserDetail(long id, String profileId, List<Medicine> addedMedicines, String dateOfBirth,
                      String bio, String firstName, String lastName) {
        this.id = id;
        this.profileId = profileId;
        this.addedMedicines = addedMedicines;
        this.dateOfBirth = dateOfBirth;
        this.bio = bio;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Create a new UserDetail from a Cursor object. Providing default values hopefully
     * ensures that the method never crashes the app.
     *
     * @param c the cursor to read from - it must be set to the right position.
     * @return the profile stored in the cursor.
     */
    public static UserDetail fromCursor(Cursor c) {

        // If the cursor is empty, return a default empty user
        if (c.isAfterLast()) {
            Log.d("CURSOR", "IS AFTER LAST");
            return new UserDetail();
        }

        // Otherwise populate the user and return
        return new UserDetail(
                getLong(c, UserDetailsContentContract.UserDetails._ID, -1),
                getString(c, UserDetailsContentContract.UserDetails.PROFILEID, ""),
                MediApp.medicineStringToList(getString(c, UserDetailsContentContract.UserDetails.ADDEDMEDICINES, "")),
                getString(c, UserDetailsContentContract.UserDetails.DATEOFBIRTH, ""),
                getString(c, UserDetailsContentContract.UserDetails.BIO, ""),
                getString(c, UserDetailsContentContract.UserDetails.FIRSTNAME, ""),
                getString(c, UserDetailsContentContract.UserDetails.LASTNAME, "")
        );
    }

    /**
     * Read a string from a column key in the cursor. If the key doesn't exist, return a default value
     */
    private static String getString(Cursor c, String col, String defaultValue) {
        if (c.getColumnIndex(col) >= 0 && c.getString(c.getColumnIndex(col)) != null) {
            return c.getString(c.getColumnIndex(col));
        } else {
            return defaultValue;
        }
    }

    /**
     * Read a long value from a column key in the cursor. If the key doesn't exist, return a default value
     */
    private static long getLong(Cursor c, String col, long defaultValue) {
        if (c.getColumnIndex(col) >= 0) {
            return c.getLong(c.getColumnIndex(col));
        } else {
            return defaultValue;
        }
    }

    /**
     * Create a new blank profile object
     */
    public UserDetail() {
        setProfileId(AWSProvider.getInstance().getIdentityManager().getCachedUserID());
        setDateOfBirth("");
        setBio("");
        setFirstName("");
        setLastName("");
        List<Medicine> defaultList = new ArrayList<>();
        setAddedMedicines(defaultList);
    }

    /**
     * Returns the internal ID
     *
     * @return the internal ID
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the internal ID
     *
     * @param id the new internal ID
     */
    public void setId(long id) {
        this.id = id;
    }


    /**
     * Returns the profileId
     */
    public String getProfileId() {
        return profileId;
    }

    /**
     * Sets the profileId
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }


    public List<Medicine> getAddedMedicines() {
        return addedMedicines;
    }

    public void setAddedMedicines(List<Medicine> addedMedicines) {

        this.addedMedicines = addedMedicines;
    }

    /**
     * Returns the age
     *
     * @return the age
     */
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Sets the age
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    /**
     * Returns the bio
     *
     * @return the bio
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the bio
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Returns the fname
     *
     * @return the First name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the First name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the Surname
     *
     * @return the Surname
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the Surname
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * The string version of the class
     *
     * @return the class unique descriptor
     */
    @Override
    public String toString() {
        return String.format("[profile#%s] %s %s", profileId, firstName, lastName);
    }

    /**
     * Return the ContentValue object for this record.  Doesn't include the User ID as it
     * is stored locally for us
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        String jsonAddedMedicines = new Gson().toJson(addedMedicines);
        values.put(UserDetailsContentContract.UserDetails.PROFILEID, profileId);
        values.put(UserDetailsContentContract.UserDetails.ADDEDMEDICINES, jsonAddedMedicines);
        values.put(UserDetailsContentContract.UserDetails.DATEOFBIRTH, dateOfBirth);
        values.put(UserDetailsContentContract.UserDetails.BIO, bio);
        values.put(UserDetailsContentContract.UserDetails.FIRSTNAME, firstName);
        values.put(UserDetailsContentContract.UserDetails.LASTNAME, lastName);
        return values;
    }

}
