package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import static junit.framework.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import neilw4.omin.datastructure.BloomFilter;

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
                            Condition.prop("parent").notLike("%") : // Sugar doesn't support IS NULL yet.
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

    public static boolean valid(String uname) {
        return Pattern.matches("[a-z]+", uname);
    }

    protected static List<UserId> readUids(final JsonReader reader) throws IOException {
        reader.beginArray();

        // Get the uids given in the message.
        List<UserId> uids = new ArrayList<>();
        while (reader.hasNext()) {
            uids.add(read(reader));
        }
        reader.endArray();
        User.consolidateUserIds(uids);
        return uids;
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

    @Override
    public void save() {
        assertNotNull(uname);
        assertTrue(valid(uname));
        super.save();
    }


    public static BloomFilter<UserId> interestedUserIds() {
        BloomFilter<UserId> filter = new BloomFilter<UserId>(20, 2);
        for (User user: User.interestedUsers()) {
            for (UserId id: Select.from(UserId.class).where(Condition.prop("user").eq(user.getId())).list()) {
                filter.put(id);
            }
        }
        return filter;
    }


    @Override
    public String toString() {
        if (parent != null) {
            return parent.toString() + "/" + uname;
        } else {
            return uname;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

}
