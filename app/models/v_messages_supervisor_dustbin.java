package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_supervisor_dustbin extends Model {

	public String title;
    public Date time;
    public String sender_name;
    public Date reply_time;
    public Date delete_time;
    public String reply_status;
    
    @Override
	public String toString() {
		return "v_messages_supervisor_dustbin [title=" + title + ", time=" + time
				+ ", sender_name=" + sender_name + ", reply_time=" + reply_time + ", delete_time="
				+ delete_time + ", reply_status=" + reply_status + ", id=" + id + "]\n";
	}

}