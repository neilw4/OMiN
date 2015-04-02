package neilw4.omin.controller;

import com.orm.SugarTransactionHelper;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.Collections;

import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.SecretKey;
import neilw4.omin.db.UserId;
import neilw4.omin.controller.fetch_key.FetchKey;

import static junit.framework.Assert.assertTrue;
import static neilw4.omin.Logger.*;

public class UnameController {

    static String TAG = UnameController.class.getSimpleName();


    public static UserId getUidFor(Message msg) {
        MessageUid msgUid = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).first();
        if (msgUid != null) {
            return msgUid.uid;
        }
        return null;
    }

    public static SecretKey getSecretKey() {
        Select<SecretKey> query = Select.from(SecretKey.class).where(Condition.prop("in_use").eq(1));
        assertTrue(query.count() <= 1);
        return query.first();
    }

    public static boolean setUname(final String uname) {
        if (uname != null && !UserId.valid(uname)) {
            return false;
        }

        SugarTransactionHelper.doInTansaction(new SugarTransactionHelper.Callback() {
            @Override
            public void manipulateInTransaction() {
                SecretKey oldKey = getSecretKey();
                if (oldKey == null || !oldKey.uid.uname.equals(uname)) {
                    if (oldKey != null) {
                        oldKey.inUse = false;
                        oldKey.save();
                    }
                    if (uname != null) {
                        UserId uid = makeUid(uname);
                        makeSecretKey(uid);
                        info(TAG, "new uname");
                    }
                }
            }
        });
        return true;
    }

    private static UserId makeUid(String uname) {
        UserId uid = Select.from(UserId.class).where(
                Condition.prop("uname").eq(uname)
        ).first();

        if (uid == null) {
            uid = new UserId(uname, null);
            uid.save();
            debug(TAG, "Created new uid " + uname);
        } else {
            debug(TAG, "Found existing uid " + uname);
        }
        return uid;
    }

    private static SecretKey makeSecretKey(UserId uid) {
        SecretKey key = Select.from(SecretKey.class).where(Condition.prop("uid").eq(uid.getId())).first();
        if (key == null) {
            key = new SecretKey(uid);
        }
        key.inUse = true;
        key.save();
        if (key.ps06Key == null) {
            FetchKey.asyncFetch(Collections.singletonList(key));
        }
        return key;
    }
}
