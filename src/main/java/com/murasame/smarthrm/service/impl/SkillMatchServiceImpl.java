package com.murasame.smarthrm.service.impl;

import com.murasame.smarthrm.dao.EmployeeDao;
import com.murasame.smarthrm.dto.SkillMatchDTO;
import com.murasame.smarthrm.entity.Employee;
import com.murasame.smarthrm.service.SkillMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillMatchServiceImpl implements SkillMatchService {
	private final EmployeeDao employeeDAO;

	@Override
	public List<Employee> matchBySkills(List<SkillMatchDTO> reqs){
		return employeeDAO.findBySkillsRequired(reqs);
	}
}
