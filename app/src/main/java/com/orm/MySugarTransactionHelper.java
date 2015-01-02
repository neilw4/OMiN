package com.orm;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.IOException;

// Modified version of SugarTransactionHelper.
// https://github.com/satyan/sugar/blob/v1.3/library/src/com/orm/MySugarTransactionHelper.java
public class MySugarTransactionHelper {

    public static <T> T doInTransaction(Callback<T> callback) throws IOException {

        SQLiteDatabase database = SugarApp.getSugarContext().getDatabase().getDB();

        database.beginTransaction();
        try {
            Log.d(SugarTransactionHelper.class.getSimpleName(), "callback executing within transaction");
            T retval = callback.manipulateInTransaction();
            database.setTransactionSuccessful();
            Log.d(SugarTransactionHelper.class.getSimpleName(), "callback successfully executed within transaction");
            return retval;
        } finally {
            database.endTransaction();
        }
    }

    public static interface Callback<T> {
        T manipulateInTransaction() throws IOException;
    }
}