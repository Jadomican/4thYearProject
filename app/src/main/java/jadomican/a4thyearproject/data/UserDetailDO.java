package jadomican.a4thyearproject.data;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;


/**
 * The model representing a DynamoDB user profile object, which can be stored/ queried from an existing
 * user record
 */
@DynamoDBTable(tableName = "yearproject-mobilehub-87068747-user-detail")
public class UserDetailDO {
    private String _userId;
    private String _profileId;
    private List<Medicine> _addedMedicines;
    private String _bio;
    private String _dateOfBirth;
    private String _firstName;
    private String _lastName;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexHashKey(attributeName = "userId", globalSecondaryIndexNames = {"userId-firstName","userId-profileId","userId-lastName",})
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBRangeKey(attributeName = "profileId")
    @DynamoDBIndexRangeKey(attributeName = "profileId", globalSecondaryIndexName = "userId-profileId")
    public String getProfileId() {
        return _profileId;
    }

    public void setProfileId(final String _profileId) {
        this._profileId = _profileId;
    }
    @DynamoDBAttribute(attributeName = "addedMedicines")
    public List<Medicine> getAddedMedicines() {
        return _addedMedicines;
    }

    public void setAddedMedicines(final List<Medicine> _addedMedicines) {
        this._addedMedicines = _addedMedicines;
    }

    @DynamoDBAttribute(attributeName = "bio")
    public String getBio() {
        return _bio;
    }

    public void setBio(final String _bio) {
        this._bio = _bio;
    }

    @DynamoDBAttribute(attributeName = "dateOfBirth")
    public String getDateOfBirth() {
        return _dateOfBirth;
    }

    public void setDateOfBirth(final String _dateOfBirth) {
        this._dateOfBirth = _dateOfBirth;
    }

    @DynamoDBIndexRangeKey(attributeName = "firstName", globalSecondaryIndexName = "userId-firstName")
    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(final String _firstName) {
        this._firstName = _firstName;
    }
    @DynamoDBIndexRangeKey(attributeName = "lastName", globalSecondaryIndexName = "userId-lastName")
    public String getLastName() {
        return _lastName;
    }

    public void setLastName(final String _lastName) {
        this._lastName = _lastName;
    }

}
