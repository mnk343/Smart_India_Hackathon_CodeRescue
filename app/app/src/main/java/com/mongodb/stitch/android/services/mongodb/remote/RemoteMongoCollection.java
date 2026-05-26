package com.mongodb.stitch.android.services.mongodb.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteUpdateResult;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;

public class RemoteMongoCollection<T> {
    private String dbName;
    private String collectionName;

    public RemoteMongoCollection(String dbName, String collectionName) {
        this.dbName = dbName;
        this.collectionName = collectionName;
    }

    public RemoteFindIterable<T> find() {
        return new RemoteFindIterable<>();
    }

    public RemoteFindIterable<T> find(Bson filter) {
        return new RemoteFindIterable<>();
    }

    public Task<RemoteInsertOneResult> insertOne(T document) {
        return Tasks.forResult(new RemoteInsertOneResult(null));
    }

    public Task<RemoteUpdateResult> updateOne(Bson filter, Bson update) {
        return Tasks.forResult(new RemoteUpdateResult(0, 0, null));
    }

    public Task<RemoteUpdateResult> updateMany(Bson filter, Bson update) {
        return Tasks.forResult(new RemoteUpdateResult(0, 0, null));
    }
}
