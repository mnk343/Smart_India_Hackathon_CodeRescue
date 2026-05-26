package com.mongodb.stitch.android.services.mongodb.remote;

import org.bson.Document;

public class RemoteMongoDatabase {
    private String name;

    public RemoteMongoDatabase(String name) {
        this.name = name;
    }

    public RemoteMongoCollection<Document> getCollection(String collectionName) {
        return new RemoteMongoCollection<>(name, collectionName);
    }
}
