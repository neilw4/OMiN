package neilw4.omin.db;

import com.orm.SugarRecord;

// Singleton table.
public class MyUser extends SugarRecord<MyUser> {
    public User myUser;

    public MyUser(User myUser) {
        this.myUser = myUser;
    }

    @SuppressWarnings("unused")
    public MyUser() {
        // Sugar ORM requires an empty constructor.
    }

}
