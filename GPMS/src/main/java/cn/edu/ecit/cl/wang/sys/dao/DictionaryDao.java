package cn.edu.ecit.cl.wang.sys.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.mapper.BaseMapper;

import cn.edu.ecit.cl.wang.sys.po.Dictionary;

public interface DictionaryDao extends BaseMapper<Dictionary>{
	public String getDicValueByEntiry(@Param("dictionary")Dictionary dictionary);

	public List<Dictionary> getDictionaryByKey(@Param("dictionary")Dictionary dictionary);
}
