package com.mongodb.stitch.android.core.auth;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.mongodb.stitch.core.auth.providers.userpassword.UserPasswordCredential;

public class StitchAuth {
    public Task<StitchUser> loginWithCredential(UserPasswordCredential credential) {
        return Tasks.forResult(new StitchUser());
    }

    public StitchUser getUser() {
        return new StitchUser();
    }

    public boolean isLoggedIn() {
        return true;
    }
}
