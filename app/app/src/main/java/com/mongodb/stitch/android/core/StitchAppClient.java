package com.mongodb.stitch.android.core;

import com.mongodb.stitch.android.core.auth.StitchAuth;

public class StitchAppClient {
    private StitchAuth auth = new StitchAuth();

    public StitchAuth getAuth() {
        return auth;
    }

    @SuppressWarnings("unchecked")
    public <T> T getServiceClient(Object factory, String serviceName) {
        return (T) new com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient();
    }
}
