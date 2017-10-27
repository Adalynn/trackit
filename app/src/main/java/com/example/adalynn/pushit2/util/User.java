package com.example.adalynn.pushit2.util;

/**
 * Created by adalynn on 21/7/17.
 */

public class User {

    public String dbid;
    public String fbid;


    public String getDbId() {
        return dbid;
    }

    public void setDbId(String dbid) {
        this.dbid = dbid;
    }

    public String getFbId() {
        return fbid;
    }

    public void setFbId(String fbid) {
        this.fbid = fbid;
    }


    @Override
    public String toString() {
        return "User[fbid=" + fbid + "]";
    }

}
