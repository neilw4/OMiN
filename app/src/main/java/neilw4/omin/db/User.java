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

    protected static User consolidateUserIds(List<UserId> uids) {
        // Find the user associated with the uids.
        User user = null;
        for (UserId uid : uids) {
            if (uid.user != null) {
                if (user == null) {
                    user = uid.user;
                } else {
                    // combine multiple users by deleting one.
                    if (user.name == null) {
                        deleteUser(user, uid.user);
                        user = uid.user;
                    } else {
                        deleteUser(uid.user, user);
                    }
                }
            }
        }

        // If there is no user for the uids, make one.
        if (user == null) {
            user = new User();
            user.save();
        }

        // Change all of the uids to the same user.
        for (UserId uid : uids) {
            if (uid.user != user) {
                uid.user = user;
                uid.save();
            }
        }
        return user;
    }


    private static void deleteUser(User toDelete, User changeTo) {
        for (UserId uid: Select.from(UserId.class).where(Condition.prop("user").eq(toDelete.getId())).list()) {
            uid.user = changeTo;
            uid.save();
        }
        toDelete.delete();
    }
}
