package neilw4.omin.db;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.List;

import neilw4.omin.datastructure.BloomFilter;

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

    public static List<User> interestedUsers() {
        return Select.from(User.class).where(Condition.prop("interested").eq(true)).list();
    }

}
