package neilw4.omin.db;

import com.orm.SugarRecord;

public class PrivateKey extends SugarRecord<PrivateKey> {
    public UserId uid;
    public byte[][] ps06Key = null;

    public PrivateKey(UserId uid) {
        this.uid = uid;
    }

    public PrivateKey(UserId uid, byte[][] ps06Key) {
        this.uid = uid;
        this.ps06Key = ps06Key;
    }

    @SuppressWarnings("unused")
    public PrivateKey() {
        // Sugar ORM requires an empty constructor.
    }

}
