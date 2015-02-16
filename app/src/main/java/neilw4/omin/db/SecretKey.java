package neilw4.omin.db;

import com.orm.SugarRecord;

import static junit.framework.Assert.assertTrue;

public class SecretKey extends SugarRecord<SecretKey> {
    public UserId uid;
    public String ps06Key = null;

    public SecretKey(UserId uid) {
        this.uid = uid;
    }

    public SecretKey(UserId uid, String ps06Key) {
        this.uid = uid;
        this.ps06Key = ps06Key;
    }

    @SuppressWarnings("unused")
    public SecretKey() {
        // Sugar ORM requires an empty constructor.
    }

    @Override
    public void save() {
        assertTrue(uid != null);
        super.save();
    }

}
