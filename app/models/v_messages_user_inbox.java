package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_user_inbox extends Model {

	public String sender_name;
    public Long user_id;
    public String title;
    public Date time;
    public String content;
    public String read_status;
	
    @Override
	public String toString() {
		return "v_messages_user_inbox [id=" + id + ", read_status=" + read_status + ", sender_name=" + sender_name
				+ ", time=" + time + ", title=" + title + ", content=" + content + ", user_id=" + user_id 
				+ "] + \n";
	}

}