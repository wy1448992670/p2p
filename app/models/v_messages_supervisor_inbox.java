package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_supervisor_inbox extends Model {

	public String title;
    public Date time;
    public String sender_name;
    public Date reply_time;
    public String status;
	
    @Override
	public String toString() {
		return "v_messages_supervisor_inbox [title=" + title + ", time=" + time + ", sender_name="
				+ sender_name + ", status=" + status + ", id=" + id + "]\n";
	}
}