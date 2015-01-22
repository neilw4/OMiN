package neilw4.omin.db;

import android.util.JsonReader;
import android.util.JsonWriter;


import com.orm.MySugarTransactionHelper;
import com.orm.SugarRecord;
import com.orm.SugarTransactionHelper;
import com.orm.query.Select;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.List;

import neilw4.omin.datastructure.BloomFilter;

import static neilw4.omin.Logger.*;

public final class Messages extends SugarRecord<Messages> {

    public static final int MAX_MESSAGES = 10;

    @SuppressWarnings("unused")
    public Messages() {
        // Sugar ORM requires an empty constructor.
    }
    public static void read(final JsonReader reader) throws IOException {
        try {
            reader.setLenient(false);
            reader.beginArray();
            MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
                @Override
                public Void manipulateInTransaction() throws IOException {
                    while(reader.hasNext()) {
                        Message.read(reader);
                    }
                    return null;
                }
            });
            reader.endArray();
        } finally {
            reader.close();
        }
    }

    public static void write(final JsonWriter writer, final List<Message> messages) throws IOException {
        try {
            writer.setLenient(false);
            writer.beginArray();
            MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
                @Override
                public Void manipulateInTransaction() throws IOException {
                    for (Message msg: messages) {
                        msg.write(writer);
                    }
                    return null;
                }
            });
            writer.endArray();
        } finally {
            writer.close();
        }
    }

    public static void evict() {
        try {
            MySugarTransactionHelper.doInTransaction(new MySugarTransactionHelper.Callback<Void>() {
                @Override
                public Void manipulateInTransaction() {
                    // TODO: check this evicts the oldest message, not the youngest.
                    Select<Message> buffer = Select.from(Message.class).orderBy("last_sent");
                    while (buffer.count() > MAX_MESSAGES) {
                        Message evict = buffer.first();
                        debug(Message.TAG, "Evicted message " + evict);
                        evict.delete();
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
            return BloomFilter.read(jsonReader);
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
