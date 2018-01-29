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
     * The authority of the notes content provider - this must match the authority
     * specified in the AndroidManifest.xml provider section
     */
    public static final String AUTHORITY = "jadomican.a4thyearproject.provider";

    /**
     * The content URI for the top-level notes authority
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Constants for the Notes table
     */
    public static final class UserDetails implements BaseColumns {
        /**
         * The Table Name
         */
        public static final String TABLE_NAME = "user-details";

        /**
         * The internal ID
         */
        public static final String _ID = "id";

        /**
         * The noteId field
         */
        public static final String ADDEDMEDICINES = "addedMedicines";

        /**
         * The bio field
         */
        public static final String BIO = "bio";

        /**
         * The content field
         */
        public static final String AGE = "age";

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
        public static final String DIR_BASEPATH = "notes";

        /**
         * The items base-path
         */
        public static final String ITEM_BASEPATH = "notes/*";

        /**
         * The SQLite database command to create the table
         */
        public static final String CREATE_SQLITE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "("
                        + _ID + " INTEGER PRIMARY KEY, "
                        + ADDEDMEDICINES + " TEXT UNIQUE NOT NULL, "
                        + AGE + " INTEGER NOT NULL DEFAULT '', "
                        + BIO + " TEXT NOT NULL DEFAULT '', "
                        + FIRSTNAME + " TEXT NOT NULL DEFAULT, "
                        + LASTNAME + " TEXT NOT NULL DEFAULT)";

        /**
         * The content URI for this table
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(UserDetailsContentContract.CONTENT_URI, TABLE_NAME);

        /**
         * The mime type of a directory of items
         */
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.amazonaws.mobile.samples.notes";

        /**
         * The mime type of a single item
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.amazonaws.mobile.samples.notes";

        /**
         * A projection of all columns in the items table
         */
        public static final String[] PROJECTION_ALL = {
                _ID,
                ADDEDMEDICINES,
                AGE,
                BIO,
                FIRSTNAME,
                LASTNAME,
        };

        /**
         * The default sort order (SQLite syntax)
         */
        public static final String SORT_ORDER_DEFAULT = CREATED + " ASC";

        /**
         * Build a URI for the provided note
         * @param noteId the ID of the provided note
         * @return the URI of the provided note
         */
        public static Uri uriBuilder(String noteId) {
            Uri item = new Uri.Builder()
                    .scheme("content")
                    .authority(UserDetailsContentContract.AUTHORITY)
                    .appendPath(UserDetailsContentContract.UserDetails.DIR_BASEPATH)
                    .appendPath(noteId)
                    .build();
            return item;
        }
    }
}

