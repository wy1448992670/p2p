package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_system extends Model {

	public Long user_id;
    public String title;
    public Date time;
    public String content;
    public String read_status;
    
	@Override
	public String toString() {
		return "v_messages_system [user_id=" + user_id + ", title=" + title
				+ ", time=" + time + ", content=" + content + ", read_status="
				+ read_status + ", id=" + id + "]\n";
	}
}