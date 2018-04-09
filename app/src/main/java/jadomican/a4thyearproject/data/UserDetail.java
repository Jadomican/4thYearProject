package jadomican.a4thyearproject.data;

/**
 * Created by jadom_000 on 27/01/2018.
 */

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jadomican.a4thyearproject.AWSProvider;
import jadomican.a4thyearproject.MedicineListActivity;

/**
 * The User Profile model
 *
 * _id              The internal ID - only relevant to the current device
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
     * Create a new UserDetail from a Cursor object. Providing default values hopefully
     * ensures that the method never crashes the app.
     *
     * @param c the cursor to read from - it must be set to the right position.
     * @return the profile stored in the cursor.
     */
    public static UserDetail fromCursor(Cursor c) {
        UserDetail userDetail = new UserDetail();
        userDetail.setId(getLong(c, UserDetailsContentContract.UserDetails._ID, -1));
        userDetail.setProfileId(getString(c, UserDetailsContentContract.UserDetails.PROFILEID, ""));

        String medicinesValue = getString(c, UserDetailsContentContract.UserDetails.ADDEDMEDICINES, "");

        //A list representing the user's added medicines
        List<Medicine> listMedicines = new ArrayList<>();

        // If addedMedicines is not equal to the default value
        if (!medicinesValue.equals("")) {

            try {
                JSONArray array = new JSONArray(medicinesValue);

                // For each medicine, add to the list
                for (int i = 0; i < array.length(); i++) {
                    JSONObject element = array.getJSONObject(i);
                    Medicine medicine = new Medicine();
                    medicine.setMedicineName(element.get(MedicineListActivity.KEY_NAME).toString());
                    medicine.setMedicineOnsetAction(element.get(MedicineListActivity.KEY_ONSETACTION).toString());
                    medicine.setMedicineId(element.get(MedicineListActivity.KEY_ID).toString());
                    medicine.setMedicineType(element.get(MedicineListActivity.KEY_TYPE).toString());
                    medicine.setMedicineImageUrl(element.get(MedicineListActivity.KEY_IMAGEURL).toString());
                    medicine.setMedicineConflict(element.get(MedicineListActivity.KEY_CONFLICT).toString());
                    medicine.setMedicineDate(element.get(MedicineListActivity.KEY_DATE).toString());
                    listMedicines.add(medicine);
                }
            } catch (JSONException e) {
                Log.d("UserDetail", "A JSONException has occurred: " + e.toString());
            }
        }

        userDetail.setAddedMedicines(listMedicines);
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
        List<Medicine> defaultList = new ArrayList<>();
        setAddedMedicines(defaultList);
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


    /**
     * Returns the profileId
     */
    public String getProfileId() { return profileId; }

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
     * Updates the profile
     * @param dateOfBirth the new age
     * @param bio the new bio
     */

    public void updateUserDetail(List<Medicine> addedMedicines, String bio, String dateOfBirth, String firstName, String lastName) {
        setAddedMedicines(addedMedicines);
        setBio(bio);
        setDateOfBirth(dateOfBirth);
        setFirstName(firstName);
        setLastName(lastName);
    }

    /**
     * The string version of the class
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
