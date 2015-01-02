package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserId extends SugarRecord<UserId> {

    public User user;
    public UserId parent;
    public String uname;

    public UserId(String uname, UserId parent) {
        this.uname = uname;
        this.parent = parent;
    }

    @SuppressWarnings("unused")
    public UserId() {
        // Sugar ORM requires an empty constructor.
    }

    public static UserId read(JsonReader reader) throws IOException {
        reader.beginArray();

        UserId userId = null;
        while (reader.hasNext()) {
            String uid = reader.nextString();
            // Pull ID from database if it exists.
            UserId child = Select.from(UserId.class).where(
                    Condition.prop("uname").eq(uid),
                    userId == null ?
                        Condition.prop("parent").notLike("%"): // Sugar doesn't support IS NULL yet.
                        Condition.prop("parent").eq(userId.getId())
                ).first();

            if (child == null) {
                // ID doesn't exist - create it.
                child = new UserId(uid, userId);
                child.save();
            }
            userId = child;
        }
        reader.endArray();

        return userId;
    }

    protected static List<UserId> readUids(final JsonReader reader) throws IOException {
        reader.beginArray();

            // Get the uids given in the message.
            List<UserId> uids = new ArrayList<>();
            while (reader.hasNext()) {
                uids.add(read(reader));
            }
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
        reader.endArray();
        return uids;
    }


    private static void deleteUser(User toDelete, User changeTo) {
        for (UserId uid: Select.from(UserId.class).where(Condition.prop("user").eq(toDelete.getId())).list()) {
            uid.user = changeTo;
            uid.save();
        }
        toDelete.delete();
    }


    public void write(JsonWriter writer) throws IOException {
        writer.beginArray();
        // Write parents in array starting at the root parent.
        List<String> parents = new ArrayList<>();
        for(UserId user = this; user != null; user = user.parent) {
            parents.add(user.uname);
        }
        Collections.reverse(parents);
        for (String id: parents) {
            writer.value(id);
        }
        writer.endArray();
    }

}
