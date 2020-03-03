package business;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import models.t_system_options;
import models.t_wechat_menus;
import play.Logger;
import play.db.jpa.JPA;
import services.WeChatMenuManageService;
import utils.ErrorInfo;
import utils.WeChatUtil;

import com.shove.gateway.weixin.gongzhong.vo.menu.Menu;
import com.shove.gateway.weixin.gongzhong.vo.menu.SubMenu;

import constants.Constants;

/**
 * 后台管理员对微信，参数设置，以及菜单设置实现类
 * @author fefrg
 *
 */
public class WeChatMenu {
	
	private long _id;
	public long id;
	
	private String _name;
	public String name;
	
	private short _type;
	public short type;
	
	private String _key;
	public String key;
	
	private String _url;
	public String url;
	
	private long _parent_id;
	public long parent_id;
	
	public List<WeChatMenu> childMenus;
	
	public long getId() {
		return this._id;
	}

	public void setId(long id) {
		/**
		 * 填充
		 */
		t_wechat_menus menu = t_wechat_menus.findById(id);
		if(null == menu) {
			this._id = -1;
			
			return;
		}
		
		this._id = id;
		this._name = menu.name;
		this._type = menu.type;
		this._key = menu.key;
		this._url = menu.url;
		this._parent_id = menu.parent_id;
	}

	public String getName() {
		return this._name;
	}

	public void setName(String name) {
		this._name = name;
	}

	public short getType() {
		return this._type;
	}

	public void setType(short type) {
		this._type = type;
	}

	public String getKey() {
		return this._key;
	}

	public void setKey(String key) {
		this._key = key;
	}

	public String getUrl() {
		return this._url;
	}

	public void setUrl(String url) {
		this._url = url;
	}

	public long getParent_id() {
		return this._parent_id;
	}

	public void setParent_id(long parent_id) {
		this._parent_id = parent_id;
	}

	/**
	 * 初始化微信菜单，从数据库中读出数据封装成t_weixin_menus对象，然后组装成封装好的List<Menu>对象再提交到微信服务器
	 */
	public static void createMenu(ErrorInfo error){
		error.clear();
		
		/**
		 * 从数据库是读取所有菜单数据
		 */
		List<Menu> list = getMenuList(error);
		
		
		/**
		 * 向微信服务器提交数据
		 */
		WeChatMenuManageService.createMenu(list);
	}
	
	/**
	 * 编辑微信菜单名称(等上线后，会更改链接地址，类型）
	 * 首先更新数据库中数据，
	 * 然后再从数据库中读取所有菜单数据，
	 * 最后创建菜单，覆盖掉所有公众号以前的菜单 
	 * @param name
	 * @param error
	 */
	public void editMenuName(String name, long id, ErrorInfo error) {
		error.clear();
		
		try {
			Query query = JPA.em().createNativeQuery("update t_wechat_menus set name = ? where id = ?",t_wechat_menus.class);
			query.setParameter(1, name);
			query.setParameter(2, id);
			
			query.executeUpdate();
		} catch(Exception e) {
			JPA.setRollbackOnly();
			
			Logger.error("更新微信菜单时 %s", e.getMessage());
			
			error.code = -1;
			error.msg = e.getMessage();
			return;
		}
		
		/**
		 * 从数据库是读取所有菜单数据
		 */
		List<Menu> list = getMenuList(error);
		
		/**
		 * 向微信服务器提交数据
		 */
		WeChatMenuManageService.createMenu(list);
		
	}
	
	/**
	 * 从数据库读取菜单数据封装成Menu对象
	 * @param error
	 * @return
	 */
	public static List<Menu> getMenuList(ErrorInfo error) {
		error.clear();
		
		//创建提交到微信服务器上的根菜单集合
		List<Menu> menusList = new ArrayList<Menu>();
		String sql = "select m.* from t_wechat_menus m where m.parent_id = -1";
		try {
			Query query = JPA.em().createNativeQuery(sql, t_wechat_menus.class);
			//得到数据库中父类菜单集合
			List<t_wechat_menus> parentMenuList = query.getResultList();
			
			if(null != parentMenuList) {
				
				
				for(t_wechat_menus menu : parentMenuList) {
					
					String sql2 = "select m.* from t_wechat_menus m where m.parent_id = ?";
					Query query2 = JPA.em().createNativeQuery(sql2, t_wechat_menus.class);
					query2.setParameter(1, menu.id);
					/**
					 * 得到数据库中子菜单集合
					 */
					List<t_wechat_menus> subMenuList = query2.getResultList();
					
					if(null != subMenuList) {
						/**
						 * 创建提交到微信服务器上面的根菜单对象,以及子菜单集合
						 */
						Menu weiXinParentMenuList = new Menu();
						weiXinParentMenuList.setName(menu.name);
						if(menu.type == 1) {
							weiXinParentMenuList.setType("view");
						} else {
							weiXinParentMenuList.setType("click");
						}
						
						List<SubMenu> weiXinSubMenuList = new ArrayList<SubMenu>();
						for(t_wechat_menus menu2 : subMenuList) {
							SubMenu subMenu = new SubMenu();
							subMenu.setName(menu2.name);
							if(menu2.type == 1) {
								subMenu.setType("view");
								//读取出url地址，将其中参数替换成配置文件中的值
								String urlRequest = menu2.url;
								urlRequest = urlRequest.replace("APPID", Constants.APPID);
								urlRequest = urlRequest.replace("REDIRECT_URI", WeChatUtil.urlEncodeUTF8(Constants.REDIRECT_URI));
								
								subMenu.setUrl(urlRequest);
							}
							else {
								subMenu.setType("click");
								subMenu.setKey(menu2.key);
							}
							
							weiXinSubMenuList.add(subMenu);
						}
						
						weiXinParentMenuList.setSub_button(weiXinSubMenuList);
						
						/**
						 * 最后添加到menusList中
						 */
					
						menusList.add(weiXinParentMenuList);
					}
				}
			}
		} catch(Exception e) {
			Logger.error("查询微信菜单时%s", e.getMessage());
			
			error.code = -1;
			
			return null;
		}
		return menusList;
		
	}
	
	/**
	 * 查询微信欢迎语
	 * @param key
	 * @return
	 */
	public static String getWeiXinWelcomeToLanguage(ErrorInfo error) {
		String value = null;
		
		try{
			Query query = JPA.em().createNativeQuery("select * from t_system_options where _key = ?",t_system_options.class);
			query.setParameter(1, "weixin_welcome_to_language");
			
			List<t_system_options> list = query.getResultList();
			
			if(null != list && list.size() > 0) {
				value = list.get(0)._value;
			}
		} catch(Exception e) {
			Logger.error("查询微信欢迎语时：%s", e.getMessage());
			error.code = -1;
			error.msg = "查询微信欢迎语时异常";
			
			return null;
		}
		
		error.code = 1;
		
		return value;
	}
	
	/**
	 * 编辑欢迎语
	 * @param value
	 * @param error
	 */
	public static void editWeiXinWelcomeToLanguage(String value, ErrorInfo error) {
		
		try{
			Query query = JPA.em().createNativeQuery("update t_system_options set _value = ? where _key = ?");
			query.setParameter(1, value);
			query.setParameter(2, "weixin_welcome_to_language");
			
			query.executeUpdate();
		} catch(Exception e) {
			Logger.error("编辑微信欢迎语时：%s", e.getMessage());
			error.code = -1;
			error.msg = "编辑微信欢迎语时异常";
			
			return;
		}
		
		error.code = 1;
	}
	/**
	 * 查询微信咨询语
	 * @return
	 */
	public static String getWeiXinConsultingLanguage(ErrorInfo error) {
		String value = null;
		
		try{
			Query query = JPA.em().createNativeQuery("select * from t_system_options where _key = ?",t_system_options.class);
			query.setParameter(1, "weixin_consulting_language");
			
			List<t_system_options> list = query.getResultList();
			
			if(null != list && list.size() > 0) {
				value = list.get(0)._value;
			}
		} catch(Exception e) {
			Logger.error("查询微信咨询语时：%s", e.getMessage());
			error.code = -1;
			error.msg = "查询微信咨询语时异常";
			
			return null;
		}
		
		error.code = 1;
		
		return value;
	}
	
	/**
	 * 编辑咨询语
	 * @param value
	 * @param error
	 */
	public static void editWeiXinConsultingLanguage(String value, ErrorInfo error) {
		
		try{
			Query query = JPA.em().createNativeQuery("update t_system_options set _value = ? where _key = ?");
			query.setParameter(1, value);
			query.setParameter(2, "weixin_consulting_language");
			
			query.executeUpdate();
		} catch(Exception e) {
			Logger.error("编辑微信咨询语时：%s", e.getMessage());
			error.code = -1;
			error.msg = "编辑微信咨询语异常";
			
			return;
		}
		
		error.code = 1;
		
	}
	
	/**
	 * 后台类别管理时，查询菜单
	 * @param error
	 * @return
	 */
	public static List<WeChatMenu> queryMenu(ErrorInfo error) {
		List<t_wechat_menus> parentMenus = new ArrayList<t_wechat_menus>();
		List<WeChatMenu> menus = new ArrayList<WeChatMenu>();
		List<WeChatMenu> childMenus = null;
		try {
			parentMenus = t_wechat_menus.find("parent_id = -1 order by id asc").fetch();
			
			if (parentMenus != null) {
				for(t_wechat_menus menu : parentMenus) {
					List<t_wechat_menus> list = t_wechat_menus.find("parent_id = ? order by id asc", menu.id).fetch();
					if (list != null) {
						WeChatMenu weChatMenu = new WeChatMenu();
						childMenus = new ArrayList<WeChatMenu>();
						weChatMenu.id = menu.id;
						weChatMenu.key = menu.key;
						weChatMenu.name = menu.name;
						weChatMenu.type = menu.type;
						weChatMenu.url = menu.url;
						weChatMenu.parent_id = menu.parent_id;
						for(t_wechat_menus childMenu : list) {
							WeChatMenu weChatMenu2 = new WeChatMenu();
							weChatMenu2.id = childMenu.id;
							weChatMenu2.key = childMenu.key;
							weChatMenu2.name = childMenu.name;
							weChatMenu2.type = childMenu.type;
							weChatMenu2.url = childMenu.url;
							weChatMenu2.parent_id = childMenu.parent_id;
							
							childMenus.add(weChatMenu2);
						}
						weChatMenu.childMenus = childMenus;
						menus.add(weChatMenu);
					}
				}
			}
		} catch (Exception e) {
			Logger.info("查询菜单时%s", e.getMessage());
			error.code = -1;
			error.msg = "查询菜单时异常";
			return null;
		}
		
		error.code = 1;
		
		return menus;
	}

	public List<WeChatMenu> getChildMenus() {
		return childMenus;
	}

	public void setChildMenus(List<WeChatMenu> childMenus) {
		this.childMenus = childMenus;
	}
	
	
}
