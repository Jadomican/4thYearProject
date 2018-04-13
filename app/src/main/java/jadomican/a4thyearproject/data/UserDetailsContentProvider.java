package jadomican.a4thyearproject.data;

/**
 * Created by jadom_000 on 21/01/2018.
 */

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;
import java.util.List;

import jadomican.a4thyearproject.AWSProvider;
import jadomican.a4thyearproject.MediApp;

/**
 * The Content Provider for the internal user-detail database
 */
public class UserDetailsContentProvider extends ContentProvider {
    /**
     * Creates a UriMatcher for matching the path elements for this content provider
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * The code for the UriMatch matching all userDetails
     */
    private static final int ALL_ITEMS = 10;

    /**
     * The code for the UriMatch matching a single userDetail
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
     * @param uri           The URI to query
     * @param projection    The fields to return
     * @param selection     The WHERE clause
     * @param selectionArgs Any arguments to the WHERE clause
     * @param sortOrder     the sort order for the returned records
     * @return a Cursor that can iterate over the results
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        int uriType = sUriMatcher.match(uri);

        DynamoDBMapper dbMapper = AWSProvider.getInstance().getDynamoDBMapper();
        MatrixCursor cursor = new MatrixCursor(UserDetailsContentContract.UserDetails.PROJECTION_ALL);
        String userId = AWSProvider.getInstance().getIdentityManager().getCachedUserID();

        switch (uriType) {
            case ALL_ITEMS:
                // In this (simplified) version of a content provider, we only allow searching
                // for all records that the user owns.  The first step to this is establishing
                // a template record that has the partition key pre-populated.
                UserDetailDO template = new UserDetailDO();
                template.setUserId(userId);
                // Now create a query expression that is based on the template record.
                DynamoDBQueryExpression<UserDetailDO> queryExpression;
                queryExpression = new DynamoDBQueryExpression<UserDetailDO>()
                        .withHashKeyValues(template);
                // Finally, do the query with that query expression.
                List<UserDetailDO> result = dbMapper.query(UserDetailDO.class, queryExpression);
                Iterator<UserDetailDO> iterator = result.iterator();
                while (iterator.hasNext()) {
                    final UserDetailDO userDetail = iterator.next();
                    Object[] columnValues = fromUserDetailDO(userDetail);
                    cursor.addRow(columnValues);
                }

                break;
            case ONE_ITEM:
                // In this (simplified) version of a content provider, we only allow searching
                // for the specific record that was requested
                final UserDetailDO userDetails = dbMapper.load(UserDetailDO.class, userId, uri.getLastPathSegment());
                if (userDetails != null) {
                    Object[] columnValues = fromUserDetailDO(userDetails);
                    cursor.addRow(columnValues);
                }
                break;
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    //Method to convert from a DynamoDB Object to add to Android Cursor
    private Object[] fromUserDetailDO(UserDetailDO userDetail) {
        String[] fields = UserDetailsContentContract.UserDetails.PROJECTION_ALL;
        Object[] r = new Object[fields.length];

        for (int i = 0; i < fields.length; i++) {
            if (fields[i].equals(UserDetailsContentContract.UserDetails.ADDEDMEDICINES)) {

                //Try to convert addedMedicines String to a JSON array
                try {
                    String json = new Gson().toJson(userDetail.getAddedMedicines());
                    JSONArray array = new JSONArray(json);
                    r[i] = array.toString();
                } catch (JSONException e) {
                    r[i] = "";
                }

            } else if (fields[i].equals(UserDetailsContentContract.UserDetails.DATEOFBIRTH)) {
                r[i] = userDetail.getDateOfBirth();
            } else if (fields[i].equals(UserDetailsContentContract.UserDetails.BIO)) {
                r[i] = userDetail.getBio();
            } else if (fields[i].equals(UserDetailsContentContract.UserDetails.FIRSTNAME)) {
                r[i] = userDetail.getFirstName();
            } else if (fields[i].equals(UserDetailsContentContract.UserDetails.LASTNAME)) {
                r[i] = userDetail.getLastName();
            } else if (fields[i].equals(UserDetailsContentContract.UserDetails.PROFILEID)) {
                r[i] = userDetail.getProfileId();
            } else {
                r[i] = new Integer(0);
            }
        }
        return r;
    }

    private UserDetailDO toUserDetailDO(ContentValues values) {
        final UserDetailDO userDetail = new UserDetailDO();
        userDetail.setAddedMedicines(MediApp.medicineStringToList(values.getAsString(UserDetailsContentContract.UserDetails.ADDEDMEDICINES)));
        userDetail.setBio(values.getAsString(UserDetailsContentContract.UserDetails.BIO));
        userDetail.setDateOfBirth(values.getAsString(UserDetailsContentContract.UserDetails.DATEOFBIRTH));
        userDetail.setFirstName(values.getAsString(UserDetailsContentContract.UserDetails.FIRSTNAME));
        userDetail.setLastName(values.getAsString(UserDetailsContentContract.UserDetails.LASTNAME));
        userDetail.setProfileId(values.getAsString(UserDetailsContentContract.UserDetails.PROFILEID));
        userDetail.setUserId(AWSProvider.getInstance().getIdentityManager().getCachedUserID());
        return userDetail;
    }

    /**
     * The content provider must return the content type for its supported URIs.  The supported
     * URIs are defined in the UriMatcher and the types are stored in the UserDetailsContentContract.
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
     * @param uri    the base URI to insert at (must be a directory-based URI)
     * @param values the values to be inserted
     * @return the URI of the inserted item
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case ALL_ITEMS:
                DynamoDBMapper dbMapper = AWSProvider.getInstance().getDynamoDBMapper();
                final UserDetailDO newUserDetail = toUserDetailDO(values);
                dbMapper.save(newUserDetail);
                Uri item = new Uri.Builder()
                        .appendPath(UserDetailsContentContract.CONTENT_URI.toString())
                        .appendPath(newUserDetail.getProfileId())
                        .build();
                notifyAllListeners(item);
                return item;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    /**
     * Delete one or more records from the SQLite database.
     *
     * @param uri           the URI of the record(s) to delete
     * @param selection     A WHERE clause to use for the deletion
     * @param selectionArgs Any arguments to replace the ? in the selection
     * @return the number of rows deleted.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;

        switch (uriType) {
            case ONE_ITEM:
                DynamoDBMapper dbMapper = AWSProvider.getInstance().getDynamoDBMapper();
                final UserDetailDO userDetail = new UserDetailDO();
                userDetail.setProfileId(uri.getLastPathSegment());
                userDetail.setUserId(AWSProvider.getInstance().getIdentityManager().getCachedUserID());
                dbMapper.delete(userDetail);
                rows = 1;
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
     * @param uri           The URI of the record(s)
     * @param values        The new values for the record(s)
     * @param selection     If the URI is a directory, the WHERE clause
     * @param selectionArgs Arguments for the WHERE clause
     * @return the number of rows updated
     */
    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        int uriType = sUriMatcher.match(uri);
        int rows;

        switch (uriType) {
            case ONE_ITEM:
                DynamoDBMapper dbMapper = AWSProvider.getInstance().getDynamoDBMapper();
                final UserDetailDO updatedUserDetail = toUserDetailDO(values);
                dbMapper.save(updatedUserDetail);
                rows = 1;
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
     * Notify all listeners that the specified URI has changed
     *
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
