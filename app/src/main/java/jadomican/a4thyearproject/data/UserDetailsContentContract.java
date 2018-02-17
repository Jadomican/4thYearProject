package jadomican.a4thyearproject.data;

        import android.content.ContentResolver;
        import android.net.Uri;
        import android.provider.BaseColumns;

        import java.util.Map;

        import static com.amazonaws.services.pinpoint.model.JobStatus.CREATED;

/**
 * Per the official Android documentation, this class defines all publically available
 * elements, like the authority, the content URIs, columns, and content types for each
 * element
 */
public class UserDetailsContentContract {
    /**
     * The authority of the user details content provider - this must match the authority
     * specified in the AndroidManifest.xml provider section
     */
    public static final String AUTHORITY = "jadomican.a4thyearproject.provider";

    /**
     * The content URI for the top-level userDetail authority
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Constants for the User Details table
     */
    public static final class UserDetails implements BaseColumns {
        /**
         * The Table Name
         */
        public static final String TABLE_NAME = "user-detail";

        /**
         * The internal ID
         */
        public static final String _ID = "id";

        /**
         * The profileId field
         */
        public static final String PROFILEID = "profileId";

        /**
         * The addedMedicines field
         */
        public static final String ADDEDMEDICINES = "addedMedicines";

        /**
         * The bio field
         */
        public static final String BIO = "bio";

        /**
         * The content field
         */
        public static final String DATEOFBIRTH = "dateOfBirth";

        /**
         * The created field
         */
        public static final String FIRSTNAME = "firstName";

        /**
         * The updated field
         */
        public static final String LASTNAME = "lastName";

        /**
         * The directory base-path
         */
        public static final String DIR_BASEPATH = "user-detail";

        /**
         * The items base-path
         */
        public static final String ITEM_BASEPATH = "user-detail/*";

        /**
         * The SQLite database command to create the table
         */
        public static final String CREATE_SQLITE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + PROFILEID + " TEXT UNIQUE NOT NULL, "
                        + ADDEDMEDICINES + " TEXT UNIQUE NOT NULL, "
                        + BIO + " TEXT NOT NULL DEFAULT '', "
                        + DATEOFBIRTH + " TEXT NOT NULL DEFAULT '', "
                        + FIRSTNAME + " TEXT NOT NULL DEFAULT '', "
                        + LASTNAME + " TEXT NOT NULL DEFAULT '')";

        /**
         * The content URI for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(UserDetailsContentContract.CONTENT_URI, TABLE_NAME);

        /**
         * The mime type of a directory of items
         */
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.jadomican.a4thyearproject";

        /**
         * The mime type of a single item
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.jadomican.a4thyearproject";

        /**
         * A projection of all columns in the items table
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                PROFILEID,
                ADDEDMEDICINES,
                BIO,
                DATEOFBIRTH,
                FIRSTNAME,
                LASTNAME,
        };

        /**
         * The default sort order (SQLite syntax)
         */
        public static final String SORT_ORDER_DEFAULT = CREATED + " ASC";

        /**
         * Build a URI for the provided details
         * @param profileId the ID of the provided details
         * @return the URI of the provided details
         */
        public static Uri uriBuilder(String profileId) {
            Uri item = new Uri.Builder()
                    .scheme("content")
                    .authority(UserDetailsContentContract.AUTHORITY)
                    .appendPath(UserDetailsContentContract.UserDetails.DIR_BASEPATH)
                    .appendPath(profileId)
                    .build();
            return item;
        }
    }
}

