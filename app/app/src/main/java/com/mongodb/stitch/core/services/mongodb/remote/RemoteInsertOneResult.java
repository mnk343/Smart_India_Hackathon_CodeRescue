package com.mongodb.stitch.core.services.mongodb.remote;

import org.bson.BsonValue;

public class RemoteInsertOneResult {
    private BsonValue insertedId;

    public RemoteInsertOneResult(BsonValue insertedId) {
        this.insertedId = insertedId;
    }

    public BsonValue getInsertedId() {
        return insertedId;
    }
}
