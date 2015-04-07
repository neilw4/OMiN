package neilw4.omin.controller;

import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import neilw4.omin.db.Message;
import neilw4.omin.db.MessageUid;
import neilw4.omin.db.User;
import neilw4.omin.db.UserId;

import static neilw4.omin.Logger.*;

public class FollowController {

    private static final String TAG = FollowController.class.getSimpleName();

    public static List<User> getFollowing() {
        return Select.from(User.class).where(Condition.prop("following").eq(1)).orderBy("name").list();
    }

    public static void unfollow(User user) {
        user.following = false;
        user.save();
    }

    public static List<UserId> getUids(User user) {
        return Select.from(UserId.class).where(Condition.prop("user").eq(user.getId())).orderBy("uname").list();
    }

    public static boolean followUser(String name, String id) {
        if (!UserId.valid(id)) {
            return false;
        }

        UserId uid = Select.from(UserId.class).where(Condition.prop("uname").eq(id)).first();


        if (uid == null) {
            uid = new UserId(id, null);
        }
        if (uid.user == null) {
            uid.user = new User();
        }

        if (name.equals(uid.user.name) && uid.user.following) {
            warn(TAG, "You are already following " + name);
            return false;
        }
        if (name.isEmpty()) {
            name = null;
        }
        uid.user.name = name;
        uid.user.following = true;
        uid.user.save();
        uid.save();
        return true;
    }

    public static int countMessages(User user) {
        Set<Message> messages = new HashSet<>();
        for (UserId uid: Select.from(UserId.class).where(Condition.prop("user").eq(user.getId())).list()) {
            for (MessageUid msgUid: Select.from(MessageUid.class).where(Condition.prop("uid").eq(uid.getId())).list()) {
                messages.add(msgUid.msg);
            }
        }
        return messages.size();
    }
}
