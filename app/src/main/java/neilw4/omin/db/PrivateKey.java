package neilw4.omin.db;

import com.orm.SugarRecord;

public class PrivateKey extends SugarRecord<PrivateKey> {
    public UserId uid;
    public String key;

    public PrivateKey(UserId uid, String key) {
        this.uid = uid;
        this.key = key;
    }

    @SuppressWarnings("unused")
    public PrivateKey() {
        // Sugar ORM requires an empty constructor.
    }

}
