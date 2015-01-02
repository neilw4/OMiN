package neilw4.omin.db;

import com.orm.SugarRecord;

public class User extends SugarRecord<User> {
    public String name;
    public boolean interested = false;

    public User(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public User() {
        // Sugar ORM requires an empty constructor.
    }


}
