package jadomican.a4thyearproject.data;

/**
 * Created by jadom_000 on 21/01/2018.
 */

import android.content.ContentProvider;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.UriMatcher;
        import android.database.Cursor;
        import android.database.SQLException;
        import android.database.sqlite.SQLiteDatabase;
        import android.database.sqlite.SQLiteQueryBuilder;
        import android.net.Uri;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.text.TextUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jadomican.a4thyearproject.AWSProvider;

/**
 * The Content Provider for the internal Notes database
 */
public class UserDetailsContentProvider extends ContentProvider {
    /**
     * Creates a UriMatcher for matching the path elements for this content provider
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * The code for the UriMatch matching all notes
     */
    private static final int ALL_ITEMS = 10;

    /**
     * The code for the UriMatch matching a single note
     */
    private static final int ONE_ITEM = 20;

    /**
     * The database helper for this content provider
     */
    private DatabaseHelper databaseHelper;

    /*
     * Initialize the UriMatcher with the URIs that this content provider handles
     */
    static {
        sUriMatcher.addURI(
                UserDetailsContentContract.AUTHORITY,
                UserDetailsContentContract.UserDetails.DIR_BASEPATH,
                ALL_ITEMS);
        sUriMatcher.addURI(
                UserDetailsContentContract.AUTHORITY,
                UserDetailsContentContract.UserDetails.ITEM_BASEPATH,
                ONE_ITEM);
    }

    /**
     * Part of the Content Provider interface.  The system calls onCreate() when it starts up
     * the provider.  You should only perform fast-running initialization tasks in this method.
     * Defer database creation and data loading until the provider actually receives a request
     * for the data.  This runs on the UI thread.
     *
     * @return true if the provider was successfully loaded; false otherwise
     */
    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    /**
     * Query for a (number of) records.
     *
     * @param uri The URI to query
     * @param projection The fields to return
     * @param selection The WHERE clause
     * @param selectionArgs Any arguments to the WHERE clause
     * @param sortOrder the sort order for the returned records
     * @return a Cursor that can iterate over the results
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriType = sUriMatcher.match(uri);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        switch (uriType) {
            case ALL_ITEMS:
                queryBuilder.setTables(UserDetailsContentContract.UserDetails.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = UserDetailsContentContract.UserDetails.SORT_ORDER_DEFAULT;
                }
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                queryBuilder.setTables(UserDetailsContentContract.UserDetails.TABLE_NAME);
                queryBuilder.appendWhere(where);
                break;
        }

        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    /**
     * The content provider must return the content type for its supported URIs.  The supported
     * URIs are defined in the UriMatcher and the types are stored in the NotesContentContract.
     *
     * @param uri the URI for typing
     * @return the type of the URI
     */
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ALL_ITEMS:
                return UserDetailsContentContract.UserDetails.CONTENT_DIR_TYPE;
            case ONE_ITEM:
                return UserDetailsContentContract.UserDetails.CONTENT_ITEM_TYPE;
            default:
                return null;
        }
    }

    /**
     * Insert a new record into the database.
     *
     * @param uri the base URI to insert at (must be a directory-based URI)
     * @param values the values to be inserted
     * @return the URI of the inserted item
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ALL_ITEMS:
                SQLiteDatabase db = databaseHelper.getWritableDatabase();
                long id = db.insert(UserDetailsContentContract.UserDetails.TABLE_NAME, null, values);
                if (id > 0) {
                    String noteId = values.getAsString(UserDetailsContentContract.UserDetails._ID);
                    Uri item = UserDetailsContentContract.UserDetails.uriBuilder(noteId);
                    notifyAllListeners(item);
                    return item;
                }
                throw new SQLException(String.format(Locale.US, "Error inserting for URI %s - id = %d", uri, id));
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Delete one or more records from the SQLite database.
     *
     * @param uri the URI of the record(s) to delete
     * @param selection A WHERE clause to use for the deletion
     * @param selectionArgs Any arguments to replace the ? in the selection
     * @return the number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (uriType) {
            case ALL_ITEMS:
                rows = db.delete(
                        UserDetailsContentContract.UserDetails.TABLE_NAME,  // The table name
                        selection, selectionArgs);              // The WHERE clause
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rows = db.delete(
                        UserDetailsContentContract.UserDetails.TABLE_NAME,  // The table name
                        where, selectionArgs);                  // The WHERE clause
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rows > 0) {
            notifyAllListeners(uri);
        }
        return rows;
    }

    /**
     * Part of the ContentProvider implementation.  Updates the record (based on the record URI)
     * with the specified ContentValues
     *
     * @param uri The URI of the record(s)
     * @param values The new values for the record(s)
     * @param selection If the URI is a directory, the WHERE clause
     * @param selectionArgs Arguments for the WHERE clause
     * @return the number of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        switch (uriType) {
            case ALL_ITEMS:
                rows = db.update(
                        UserDetailsContentContract.UserDetails.TABLE_NAME,  // The table name
                        values,                                 // The values to replace
                        selection, selectionArgs);              // The WHERE clause
                break;
            case ONE_ITEM:
                String where = getOneItemClause(uri.getLastPathSegment());
                if (!TextUtils.isEmpty(selection)) {
                    where += " AND " + selection;
                }
                rows = db.update(
                        UserDetailsContentContract.UserDetails.TABLE_NAME,  // The table name
                        values,                                 // The values to replace
                        where, selectionArgs);                  // The WHERE clause
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        if (rows > 0) {
            notifyAllListeners(uri);
        }
        return rows;
    }


    private UserDetailsDO toUserDetailsDO(ContentValues values) {
        final UserDetailsDO note = new UserDetailsDO();

        //MUST BE FIXED
        Map<String, String> test = new HashMap<>();
        note.setAddedMedicines(test);

        note.setAge(values.getAsDouble(UserDetailsContentContract.UserDetails.AGE));
        note.setBio(values.getAsString(UserDetailsContentContract.UserDetails.BIO));
        note.setFirstName(values.getAsString(UserDetailsContentContract.UserDetails.FIRSTNAME));
        note.setLastName(values.getAsString(UserDetailsContentContract.UserDetails.LASTNAME));
        note.setUserId(AWSProvider.getInstance().getIdentityManager().getCachedUserID());
        return note;
    }


    /**
     * Notify all listeners that the specified URI has changed
     * @param uri the URI that changed
     */
    private void notifyAllListeners(Uri uri) {
        ContentResolver resolver = getContext().getContentResolver();
        if (resolver != null) {
            resolver.notifyChange(uri, null);
        }
    }

    private String getOneItemClause(String id) {
        return String.format("%s = \"%s\"", UserDetailsContentContract.UserDetails._ID, id);
    }
}
