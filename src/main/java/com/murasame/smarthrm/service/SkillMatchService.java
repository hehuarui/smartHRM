package com.murasame.smarthrm.service;

import com.murasame.smarthrm.dto.SkillMatchDTO;
import com.murasame.smarthrm.entity.Employee;

import java.util.List;

public interface SkillMatchService {
	// 按「技能:最小熟练度」字符串列表匹配员工
	List<Employee> matchBySkills(List<SkillMatchDTO> reqs);
}
