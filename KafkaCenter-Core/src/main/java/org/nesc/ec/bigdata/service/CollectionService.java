package org.nesc.ec.bigdata.service;

import java.util.List;
import java.util.Map;

import org.nesc.ec.bigdata.model.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.nesc.ec.bigdata.mapper.CollectionMapper;

@Service
public class CollectionService {
	
	@Autowired
	CollectionMapper collectionMapper;
	
	public List<Collections> list(Long userId, String type){
		return collectionMapper.listByUser(userId, type);
		
	}
	
	public Collections getInfo(Long userId,String type,String name,String clusterId) {
		return collectionMapper.getInfo(userId, type, name, clusterId);
	}
	
	public boolean isDelete(Long id) {
		return collectionMapper.deleteById(id) > 0;
	}
	
	public boolean insert(Collections collections) {
		return collectionMapper.insert(collections) > 0;
	}

	public List<Collections> getTotal(Long userId,String type){
		return collectionMapper.listByUser(userId, type);
	}
	
	public boolean deleteByMap(Map<String,Object> map) {
		return collectionMapper.deleteByMap(map) > 0;
	}
	
	boolean selectByMap(Map<String, Object> map) {
		return collectionMapper.selectByMap(map).size() > 0;
	}
}
