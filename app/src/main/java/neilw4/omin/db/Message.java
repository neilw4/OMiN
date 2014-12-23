package neilw4.omin.db;

import com.orm.SugarRecord;

import java.sql.Blob;
import java.sql.Timestamp;

public class Message extends SugarRecord<Message> {
    public class MessageUid extends SugarRecord<Message> {
        public UserId id;
        public Message msg;
    }

    public User from;
    public Blob signature;
    public String body;
    public Timestamp sent;
    public int distributionCount;
    public boolean read;
}
