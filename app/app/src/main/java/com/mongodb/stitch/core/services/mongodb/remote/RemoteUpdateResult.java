package com.mongodb.stitch.core.services.mongodb.remote;

import org.bson.BsonValue;

public class RemoteUpdateResult {
    private long matchedCount;
    private long modifiedCount;
    private BsonValue upsertedId;

    public RemoteUpdateResult(long matchedCount, long modifiedCount, BsonValue upsertedId) {
        this.matchedCount = matchedCount;
        this.modifiedCount = modifiedCount;
        this.upsertedId = upsertedId;
    }

    public long getMatchedCount() { return matchedCount; }
    public long getModifiedCount() { return modifiedCount; }
    public BsonValue getUpsertedId() { return upsertedId; }
}
