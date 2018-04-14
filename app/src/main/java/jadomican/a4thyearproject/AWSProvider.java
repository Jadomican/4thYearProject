package jadomican.a4thyearproject;

/*
 * Jason Domican
 * Final Year Project
 * Institute of Technology Tallaght
 */

import android.content.Context;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider;
import com.amazonaws.mobile.config.AWSConfiguration;


import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;

// Import DynamoDBMapper and AmazonDynamoDBClient to support data access methods
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * A central place to add code that accesses AWS resources
 */
public class AWSProvider {
    private static AWSProvider instance = null;
    private Context context;
    private AWSConfiguration awsConfiguration;
    private PinpointManager pinpointManager;

    // Declare DynamoDBMapper and AmazonDynamoDBClient private variables
    // to support data access methods
    private AmazonDynamoDBClient dbClient = null;
    private DynamoDBMapper dbMapper = null;

    public static AWSProvider getInstance() {
        return instance;
    }

    public static void initialize(Context context) {
        if (instance == null) {
            instance = new AWSProvider(context);
        }
    }

    private AWSProvider(Context context) {
        this.context = context;
        this.awsConfiguration = new AWSConfiguration(context);

        IdentityManager identityManager = new IdentityManager(context, awsConfiguration);
        IdentityManager.setDefaultIdentityManager(identityManager);
        identityManager.addSignInProvider(CognitoUserPoolsSignInProvider.class);
    }

    public Context getContext() {
        return this.context;
    }

    public AWSConfiguration getConfiguration() {
        return this.awsConfiguration;
    }

    /**
     * Return the AWS Identity Manager. Called when signing in, logging out etc.
     */
    public IdentityManager getIdentityManager() {
        return IdentityManager.getDefaultIdentityManager();
    }

    /**
     * Get the AWS Pinpoint Manager. Used to enable logging of custom statistics
     */
    public PinpointManager getPinpointManager() {
        if (pinpointManager == null) {
            final AWSCredentialsProvider cp = getIdentityManager().getCredentialsProvider();
            PinpointConfiguration config = new PinpointConfiguration(
                    getContext(), cp, getConfiguration());
            pinpointManager = new PinpointManager(config);
        }
        return pinpointManager;
    }

    /**
     * Return a DynamoDBMapper, allowing JSON objects to be mapped to Dynamo objects for storing in
     * the backend database
     */
    public DynamoDBMapper getDynamoDBMapper() {
        if (dbMapper == null) {
            final AWSCredentialsProvider cp = getIdentityManager().getCredentialsProvider();
            dbClient = new AmazonDynamoDBClient(cp);
            dbMapper = DynamoDBMapper.builder()
                    .awsConfiguration(getConfiguration())
                    .dynamoDBClient(dbClient)
                    .build();
        }
        return dbMapper;
    }
}