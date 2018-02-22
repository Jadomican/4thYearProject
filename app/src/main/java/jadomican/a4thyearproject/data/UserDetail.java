package jadomican.a4thyearproject.data;

/**
 * Created by jadom_000 on 27/01/2018.
 */

import android.content.ContentValues;
import android.database.Cursor;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.Map;
import java.util.UUID;

import jadomican.a4thyearproject.AWSProvider;

/**
 * The User Profile model
 *
 * _id      The internal ID - only relevant to the current device
 * profileId   The global ID - should be unique globally
 * addedMedicines    The JSON-like document storing medicine details
 */

public class UserDetail {
    private long id = -1;
    private String profileId;
    //private Map<String, String> addedMedicines;
    private String addedMedicines;
    private String dateOfBirth;
    private String bio;
    private String firstName;
    private String lastName;

    /**
     * Create a new UserDetail from a Cursor object.  This version provides default values for
     * any information that is missing; hopefully, this ensures that the method never crashes
     * the app.
     *
     * @param c the cursor to read from - it must be set to the right position.
     * @return the note stored in the cursor.
     */
    public static UserDetail fromCursor(Cursor c) {
        UserDetail userDetail = new UserDetail();

        userDetail.setId(getLong(c, UserDetailsContentContract.UserDetails._ID, -1));
        userDetail.setProfileId(getString(c, UserDetailsContentContract.UserDetails.PROFILEID, ""));
        userDetail.setDateOfBirth(getString(c, UserDetailsContentContract.UserDetails.DATEOFBIRTH, ""));
        userDetail.setBio(getString(c, UserDetailsContentContract.UserDetails.BIO, ""));
        userDetail.setFirstName(getString(c, UserDetailsContentContract.UserDetails.FIRSTNAME, ""));
        userDetail.setLastName(getString(c, UserDetailsContentContract.UserDetails.LASTNAME, ""));

        return userDetail;
    }

    /**
     * Read a string from a key in the cursor
     *
     * @param c the cursor to read from
     * @param col the column key
     * @param defaultValue the default value if the column key does not exist in the cursor
     * @return the value of the key
     */
    private static String getString(Cursor c, String col, String defaultValue) {
        if (c.getColumnIndex(col) >= 0) {
            return c.getString(c.getColumnIndex(col));
        } else {
            return defaultValue;
        }
    }

    /**
     * Read a long value from a key in the cursor
     *
     * @param c the cursor to read from
     * @param col the column key
     * @param defaultValue the default value if the column key does not exist in the cursor
     * @return the value of the key
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
    }

    /**
     * Returns the internal ID
     * @return the internal ID
     */
    public long getId() { return id; }

    /**
     * Sets the internal ID
     * @param id the new internal ID
     */
    public void setId(long id) { this.id = id;}


/*    *//**
     * Returns the noteId
     * @return the note ID
     */
    public String getProfileId() { return profileId; }

    /*
     * Sets the noteId
     * @param noteId the new note ID
     */
    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    /**
     * Returns the age
     * @return the age
     */
    public String getDateOfBirth() { return dateOfBirth; }

    /**
     * Sets the age
     * @param dateOfBirth the new age
     */
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }


    /**
     * Returns the bio
     * @return the bio
     */
    public String getBio() { return bio; }

    /**
     * Sets the bio
     * @param bio the new title
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Returns the fname
     * @return the fname
     */
    public String getFirstName() { return firstName; }

    /**
     * Sets the fname
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the fname
     * @return the fname
     */
    public String getLastName() { return lastName; }

    /**
     * Sets the lname
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Updates the note
     * @param dateOfBirth the new age
     * @param bio the new bio
     */
    public void updateUserDetail(String dateOfBirth, String bio, String firstName, String lastName) {
        setDateOfBirth(dateOfBirth);
        setBio(bio);
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * The string version of the class
     * @return the class unique descriptor
     */
    @Override
    public String toString() {
        return String.format("[note#%s] %s", profileId, bio);
    }

    /**
     * Return the ContentValue object for this record.  This should not include the _id
     * field as it is stored for us.
     */
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();

        values.put(UserDetailsContentContract.UserDetails.PROFILEID, profileId);
        values.put(UserDetailsContentContract.UserDetails.ADDEDMEDICINES, addedMedicines);
        values.put(UserDetailsContentContract.UserDetails.DATEOFBIRTH, dateOfBirth);
        values.put(UserDetailsContentContract.UserDetails.BIO, bio);
        values.put(UserDetailsContentContract.UserDetails.FIRSTNAME, firstName);
        values.put(UserDetailsContentContract.UserDetails.LASTNAME, lastName);

        return values;
    }
}
