package com.mongodb.stitch.android.services.mongodb.remote;

public class RemoteMongoClient {
    public static final Object factory = new Object();

    public RemoteMongoDatabase getDatabase(String databaseName) {
        return new RemoteMongoDatabase(databaseName);
    }
}
