package models;

import java.util.Date;
import javax.persistence.Entity;
import play.db.jpa.Model;

@Entity
public class v_messages_user_outbox extends Model {

	public Long user_id;
    public String receiver_name;
    public String title;
    public Date time;
    public String content;    

    public v_messages_user_outbox() {
		super();
	}

    /**
     * 查询列表页用
     * @param id
     * @param receiver_name
     * @param title
     * @param time
     */
	public v_messages_user_outbox(long id, String receiver_name, String title, Date time, String content) {
		super();
		this.id = id;
		this.receiver_name = receiver_name;
		this.title = title;
		this.time = time;
		this.content = content;
	}

	@Override
	public String toString() {
		return "v_messages_user_outbox [id="+ id +", user_id=" + user_id + ", receiver_name=" + receiver_name
				+ ", title=" + title + ", content=" + content + ", time=" + time + "]\n";
	}
	

}