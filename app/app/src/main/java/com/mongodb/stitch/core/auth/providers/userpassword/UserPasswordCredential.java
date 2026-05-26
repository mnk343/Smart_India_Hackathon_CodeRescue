package com.mongodb.stitch.core.auth.providers.userpassword;

public class UserPasswordCredential {
    private String username;
    private String password;

    public UserPasswordCredential(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}
