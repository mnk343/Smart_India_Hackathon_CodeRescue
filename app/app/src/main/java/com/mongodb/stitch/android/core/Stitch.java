package com.mongodb.stitch.android.core;

public final class Stitch {
    private static StitchAppClient defaultClient = new StitchAppClient();

    public static StitchAppClient initializeDefaultAppClient(String clientAppId) {
        return defaultClient;
    }

    public static StitchAppClient getDefaultAppClient() {
        return defaultClient;
    }

    public static boolean hasAppClient(String clientAppId) {
        return true;
    }
}
