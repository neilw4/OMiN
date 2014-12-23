package neilw4.omin.db;

import com.orm.SugarRecord;

public class User extends SugarRecord<User> {
    public String name;
    private boolean interested;
}
