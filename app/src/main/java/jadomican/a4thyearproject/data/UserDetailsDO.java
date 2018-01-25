package jadomican.a4thyearproject.data;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "yearproject-mobilehub-87068747-user-details")

public class UserDetailsDO {
    private String _userId;
    private Map<String, String> _addedMedicines;
    private Double _age;
    private String _bio;
    private String _firstName;
    private String _lastName;

    @DynamoDBHashKey(attributeName = "userId")
    @DynamoDBIndexRangeKey(attributeName = "userId", globalSecondaryIndexNames = {"firstName-userId","lastName-userId",})
    public String getUserId() {
        return _userId;
    }

    public void setUserId(final String _userId) {
        this._userId = _userId;
    }
    @DynamoDBAttribute(attributeName = "addedMedicines")
    public Map<String, String> getAddedMedicines() {
        return _addedMedicines;
    }

    public void setAddedMedicines(final Map<String, String> _addedMedicines) {
        this._addedMedicines = _addedMedicines;
    }
    @DynamoDBAttribute(attributeName = "age")
    public Double getAge() {
        return _age;
    }

    public void setAge(final Double _age) {
        this._age = _age;
    }
    @DynamoDBAttribute(attributeName = "bio")
    public String getBio() {
        return _bio;
    }

    public void setBio(final String _bio) {
        this._bio = _bio;
    }
    @DynamoDBIndexHashKey(attributeName = "firstName", globalSecondaryIndexName = "firstName-userId")
    public String getFirstName() {
        return _firstName;
    }

    public void setFirstName(final String _firstName) {
        this._firstName = _firstName;
    }
    @DynamoDBIndexHashKey(attributeName = "lastName", globalSecondaryIndexName = "lastName-userId")
    public String getLastName() {
        return _lastName;
    }

    public void setLastName(final String _lastName) {
        this._lastName = _lastName;
    }

}
