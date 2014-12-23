package neilw4.omin.db;

import com.orm.SugarRecord;

public class UserId extends SugarRecord<UserId> {
    public User user;
    private UserId parentId;
    private String id;
}
