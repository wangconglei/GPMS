package cn.edu.ecit.cl.wang.sys.pojo;

import java.util.HashMap;
import java.util.Map;

import cn.edu.ecit.cl.wang.sys.po.BaseTree;
import cn.edu.ecit.cl.wang.sys.po.Org;

public class OrgTree extends BaseTree{

	private static final long serialVersionUID = -5042931257302124801L;
	
	public OrgTree(){
		
	}
	public OrgTree(Org org){
		Map<String, Object> map= new HashMap<String,Object>();
		map.put("porgId",org.getPorgId());
		map.put("orgType",org.getOrgType());
		map.put("isUse",org.getIsUse());
		map.put("address",org.getAddress());
		map.put("orgDesc",org.getOrgDesc());
		setAttributes(map);
		setId(org.getOrgId().toString());
		setText(org.getOrgNm());
		setParentId(String.valueOf(org.getPorgId()));
	}

}
