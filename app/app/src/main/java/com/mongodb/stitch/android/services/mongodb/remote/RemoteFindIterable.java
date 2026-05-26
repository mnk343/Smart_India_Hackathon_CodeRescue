package com.mongodb.stitch.android.services.mongodb.remote;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

public class RemoteFindIterable<T> {

    public RemoteFindIterable<T> filter(Object filter) {
        return this;
    }

    public RemoteFindIterable<T> sort(Object sort) {
        return this;
    }

    public RemoteFindIterable<T> limit(int limit) {
        return this;
    }

    public Task<List<T>> into(ArrayList<T> target) {
        // Return empty list - stub implementation
        return Tasks.forResult(new ArrayList<T>());
    }

    public Task<T> first() {
        return Tasks.forResult(null);
    }
}
