package cn.edu.ecit.cl.wang.sys.service.impl;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;

import cn.edu.ecit.cl.wang.sys.common.utils.MD5;
import cn.edu.ecit.cl.wang.sys.common.utils.SpringSecurityUtils;
import cn.edu.ecit.cl.wang.sys.dao.SysParamDao;
import cn.edu.ecit.cl.wang.sys.dao.UserDao;
import cn.edu.ecit.cl.wang.sys.dao.UtilsDao;
import cn.edu.ecit.cl.wang.sys.po.SysParam;
import cn.edu.ecit.cl.wang.sys.po.User;
import cn.edu.ecit.cl.wang.sys.service.IUserService;

@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements IUserService {
 
	@Autowired
	SysParamDao sysParamDao;

	@Autowired
	UserDao userDao;
	
	@Autowired
	UtilsDao utilsDao;
	
	@Override
	public Page<User> selectPage(User user, int pageNum, int pageSize) {
		if(user==null){
			user=new User();
		}
		if(user.getOrgId()==null){
			user.setOrgId(SpringSecurityUtils.getCurrentUser().getOrgId());
		}
		EntityWrapper<User> ew=new EntityWrapper<User>(user);
		Page<User> page=new Page<User>(pageNum,pageSize);
		page.setRecords(userDao.selectPage(page, ew));
		return page;
	}
	
	@Override
	public boolean insert(User user) {
		user.setUserId(utilsDao.selectKey("seq_base_user"));
		user.setCreateAt(new Timestamp(System.currentTimeMillis()));
		user.setSignAt(user.getCreateAt());
		user.setCreator(SpringSecurityUtils.getCurrentUser().getUserId());
		user.setIsDel("0");
		user.setPasswd(MD5.encode(user.getPasswd()));
		user.setCanApply("1");
		return super.insert(user);
	}
	
	@Override
	public boolean updateById(User entity) {
		if(StringUtils.isEmpty(entity.getPasswd())){
			entity.setPasswd(null);
		}else{
			entity.setPasswd(MD5.encode(entity.getPasswd()));
		}
		entity.setModAt(new Timestamp(System.currentTimeMillis()));
		entity.setModer(SpringSecurityUtils.getCurrentUser().getUserId());
		userDao.updateModCount(entity.getUserId());
		return super.updateById(entity);
	}
	
	/**
	 * 根据用户登陆名得到id
	 */
	@Override
	public Long getIdByLoginNm(String loginNm) {
		return userDao.getIdByLoginNm(loginNm);
	}

	/**
	 * 密码输入错误次数加一，如次数达到系统设置值，则锁定用户
	 */
	@Override
	public void updatePassErr(Long userId){
		User user = selectById(userId);
		if (user != null) {
			String userPasswderr = sysParamDao.getParamValueByEntiry(new SysParam("system", "userPasswderr"));
			if ((userPasswderr != null) && (Integer.valueOf(userPasswderr).intValue() != 0)) {
				userDao.updatePassErr(userId);
				if (user.getPasswdErr() + 1 >= Integer.valueOf(userPasswderr).intValue()) {
					userDao.updateLockUser(userId);
				}
			}
		}
	}

	/**
	 * 根据用户名查询用户是否被锁定
	 */
	@Override
	public boolean isUserLocked(Long userId) {
		String flag=userDao.isUserLocked(userId);
		return "1".equals(flag);
	}
	
	@Override
	public User getUnLockUserById(Long id) {
		return userDao.getUnLockUserById(id);
	}

	@Override
	public boolean lockUserBatchIds(List<Long> ids) {
		boolean result=true;
		for(Long id:ids){
			if(userDao.updateLockUser(id)<1){
				result=false;
			}
		}
		return result;
	}
	@Override
	public boolean unLockUserBatchIds(List<Long> ids) {
		boolean result=true;
		for(Long id:ids){
			if(userDao.updateUnLockUser(id)<1){
				result=false;
			}
		}
		return result;
	}

	@Override
	public Page<User> getUsersByRoleId(User user,Long roleId, int pageNum, int pageSize) {
		if(user==null){
			user=new User();
		}
		if(user.getOrgId()==null || user.getOrgId()==0){
			user.setOrgId(SpringSecurityUtils.getCurrentUser().getOrgId());
		}
		EntityWrapper<User> ew=new EntityWrapper<User>(user);
		Page<User> page=new Page<User>(pageNum,pageSize);
		page.setRecords(userDao.getUsersByRoleId(page,ew ,roleId));
		return page;
	}

	@Override
	public Page<User> getUsersExRoleId(User user, Long roleId, int pageNum, int pageSize) {
		if(user==null){
			user=new User();
		}
		if(user.getOrgId()==null || user.getOrgId()==0){
			user.setOrgId(SpringSecurityUtils.getCurrentUser().getOrgId());
		}
		EntityWrapper<User> ew=new EntityWrapper<User>(user);
		Page<User> page=new Page<User>(pageNum,pageSize);
		page.setRecords(userDao.getUsersExRoleId(page, ew, roleId));
		return page;
	}

	@Override
	public List<User> getMyStuList() {
		return userDao.getMyStuList(SpringSecurityUtils.getCurrentUser().getUserId());
	}

	@Override
	public User getMyMentor() {
		return userDao.getMyMentor(SpringSecurityUtils.getCurrentUser().getUserId());
	}

	@Override
	public int changePasswd(String oldPasswd, String newPasswd) {
		String passwd=SpringSecurityUtils.getCurrentUser().getPasswd();
		if(passwd.equals(MD5.encode(oldPasswd.trim()))){
			User u=new User();
			u.setUserId(SpringSecurityUtils.getCurrentUser().getUserId());
			u.setPasswd(MD5.encode(newPasswd.trim()));
			if(userDao.updateById(u)>0){
				return 1;
			}else{
				return 2;
			}
		}else{
			return 3;
		}
	}

	@Override
	public boolean upMyInfo(User user) {
		user.setUserId(SpringSecurityUtils.getCurrentUser().getUserId());
		return userDao.updateById(user)>0;
	}

}