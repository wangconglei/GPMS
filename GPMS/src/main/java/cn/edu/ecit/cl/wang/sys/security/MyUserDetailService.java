package cn.edu.ecit.cl.wang.sys.security;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.edu.ecit.cl.wang.sys.common.utils.GlobalProperties;
import cn.edu.ecit.cl.wang.sys.common.utils.SystemUtils;
import cn.edu.ecit.cl.wang.sys.controller.UserController;
import cn.edu.ecit.cl.wang.sys.po.LoginRecord;
import cn.edu.ecit.cl.wang.sys.po.Role;
import cn.edu.ecit.cl.wang.sys.service.ILoginRecordService;
import cn.edu.ecit.cl.wang.sys.service.IOrgService;
import cn.edu.ecit.cl.wang.sys.service.IRoleService;
import cn.edu.ecit.cl.wang.sys.service.IUserService;

public class MyUserDetailService implements UserDetailsService {
	
	private static final Logger logger = LogManager.getLogger(UserController.class);
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private ILoginRecordService loginRecordService;
	
	@Autowired
	private IOrgService orgService;
	
	@Autowired
	private IRoleService roleService;
	
	@Autowired
	private GlobalProperties globalProperties;

	public UserDetails loadUserByUsername(String loginNm) throws UsernameNotFoundException, DataAccessException {
		
		//登陆名为空
		if (StringUtils.isEmpty(loginNm)) {
			logger.debug("loadUserByUsername->UsernameNotFoundException: " + loginNm + " isEmpty");
			throw new UsernameNotFoundException("用户 " + loginNm + " 不存在");
		}
		
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		//从数据库中找到该用户
		Long usrId=userService.getIdByLoginNm(loginNm);
		if (usrId == null || usrId==0) {
			logger.debug("loadUserByUsername->UsernameNotFoundException:" + loginNm + "= null");
			//保存其登陆记录
			loginRecordService.insert(new LoginRecord(loginNm, "0", SystemUtils.getIpAddress(request)));
			throw new UsernameNotFoundException("用户 " + loginNm + " 不存在");
		}
		boolean isUserLocked = userService.isUserLocked(usrId);
		if (isUserLocked) {
			logger.debug("loadUserByUsername->LockedException:" + loginNm + " isUserLocked");
			throw new LockedException("用户 " + loginNm + " 已被锁定!");
		}
		
		MyUserDetails userDetails = userService.getUserDetailsById(usrId);
		if (userDetails != null) {
			userDetails.setSubOrgList(orgService.getSubOrgIdList(userDetails.getOrgId()));
		}
		//向CurrentUser中注入其角色和菜单列表
		setGrantedAuthority(userDetails);
		//保存其登陆记录
		loginRecordService.insert(new LoginRecord(loginNm, "1", SystemUtils.getIpAddress(request)));
		return userDetails;
	}

	/**
	 * 从数据库读取权限信息，若没有权限，则赋予配置文件中的默认权限，并设置到MyUserDetails对象中
	 * 
	 * @param username
	 * @return
	 */
	public void setGrantedAuthority(MyUserDetails userDetails) {
		//获取用户角色列表
		List<Long> roleIds = userService.getRolesByUserid(userDetails.getUserId());
		//若用户角色列表为空，加入系统设置的默认角色
		if ((roleIds.isEmpty()) && (globalProperties.getDefaultRoleId() != null)) {
			Role role = roleService.selectById(globalProperties.getDefaultRoleId());
			if (role != null) {
				roleIds.add(role.getRoleId());
			}
		}
		if (!roleIds.isEmpty()) {
			userDetails.setRoleIds(roleIds);
		}
		//放入用户所有权限
		Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
		for(Long roleId:roleIds){
			grantedAuthorities.add(new SimpleGrantedAuthority(roleId.toString()));
		}
		userDetails.setGrantedAuthorities(grantedAuthorities);
	}
}
