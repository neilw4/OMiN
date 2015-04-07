package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;


import com.orm.MySugarTransactionHelper;
import com.orm.SugarRecord;
import com.orm.SugarTransactionHelper;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import neilw4.omin.controller.MessageController;
import neilw4.omin.datastructure.BloomFilter;

import static neilw4.omin.Logger.*;

public final class Messages extends SugarRecord<Messages> {

    public static final String TAG = Messages.class.getSimpleName();
    public static final int MAX_MESSAGES = 15;

    @SuppressWarnings("unused")
    public Messages() {
        // Sugar ORM requires an empty constructor.
    }
    public static int read(final JsonReader reader) throws IOException {
        reader.setLenient(false);
        reader.beginArray();
        int count = MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Integer>() {
            @Override
            public Integer manipulateInTransaction() throws IOException {
                int count = 0;
                while(reader.hasNext()) {
                    Message.read(reader);
                    count += 1;
                }
                return count;
            }
        });
        reader.endArray();
        return count;
    }

    public static int write(final JsonWriter writer, final List<Message> messages) throws IOException {
        writer.setLenient(false);
        writer.beginArray();
        int count = MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Integer>() {
            @Override
            public Integer manipulateInTransaction() throws IOException {
                int count = 0;
                for (Message msg: messages) {
                    List<MessageUid> uids = Select.from(MessageUid.class).where(Condition.prop("msg").eq(msg.getId())).list();
                    boolean canSend = true;
                    for (MessageUid uid: uids) {
                        if (uid.signature == null) {
                            warn(TAG, "can't send message " + msg + "because no signature for user");
                            canSend = false;
                            break;
                        }
                    }

                    if (canSend) {
                        msg.write(writer);
                        count += 1;
                    }
                }
                return count;
            }
        });
        writer.endArray();
        return count;
    }

    public static void evict() {
        try {
            MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
                @Override
                public Void manipulateInTransaction() {
                    Select<Message> buffer = Select.from(Message.class).orderBy("last_sent");
                    int evicted = 0;
                    while (buffer.count() > MAX_MESSAGES) {
                        Message evict = buffer.first();
                        info(Message.TAG, "Evicted message " + evict);
                        evict.delete();
                        evicted += 1;
                    }
                    if (evicted > 0) {
                        MessageController.onMessagesChanged();
                    }
                    return null;
                }
            });
        } catch (IOException e) {
            error(Message.TAG, "Failure running eviction strategy: " + e.getMessage());
        }
    }

    public String filter;

    public static BloomFilter<Message> getFilter() {
        Messages messages = Select.from(Messages.class).first();
        if (messages == null) {
            return new BloomFilter<>(256, 2);
        } else {
            StringReader stringReader = new StringReader(messages.filter);
            JsonReader jsonReader = new JsonReader(stringReader);
            try {
                return BloomFilter.read(jsonReader);
            } catch (IOException e) {
                error(TAG, "error parsing " + messages.filter, e);
                messages.delete();
                return new BloomFilter<>(256, 2);
            }
        }
    }

    public static void setFilter(final BloomFilter<Message> filter) {
        SugarTransactionHelper.doInTansaction(new SugarTransactionHelper.Callback() {
            @Override
            public void manipulateInTransaction() {
                Messages messages = Select.from(Messages.class).first();
                if (messages == null) {
                    messages = new Messages();
                }
                messages.filter = filter.toString();
                messages.save();
            }
        });
    }
}
