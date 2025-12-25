package com.murasame.smarthrm.service.impl;
//林1224
import com.murasame.smarthrm.dao.*;
import com.murasame.smarthrm.dto.EmployeeDTO;
import com.murasame.smarthrm.entity.*;
import com.murasame.smarthrm.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeDao employeeDao;
    @Autowired
    private DepartmentDao departmentDao;
    @Autowired
    private ProjectDao projectDao;
    @Autowired
    private TaskDao taskDao;
    @Autowired
    private TrainingDao trainingDao;
    @Autowired
    private SkillDao skillDao;

    // ========== 原有方法不变 ==========
    @Override
    public List<Employee> findAllEmployees() {
        return employeeDao.findAll();
    }

    @Override
    public Employee findEmployeeById(Integer id) {
        return employeeDao.findById(id);
    }

    @Override
    public List<Employee> listAllEmployees() {
        return employeeDao.findAll();
    }

    // ========== 核心修改：新增员工时绑定任务关联 ==========
    @Override
    public void saveEmployee(Employee employee, EmployeeDTO dto) {
        Integer newEmpId = generateEmpId();
        employee.set_id(newEmpId);
        log.info("开始新增员工：生成自增ID = {}，员工姓名 = {}", newEmpId, employee.getEmpName());

        updateEmployeeSkills(employee, dto);
        bindDepartment(employee);
        bindProjects(employee.get_id(), dto, employee);
        // 新增：绑定任务关联
        bindTasks(employee.get_id(), dto, employee);
        bindTrainings(employee.get_id(), dto, employee);

        employeeDao.update(employee);
        log.info("员工ID: {} 新增成功（含所有关联关系）", newEmpId);
    }

    // ========== 核心修改：编辑员工时处理任务关联变更 ==========
    @Override
    public void updateEmployee(Employee newEmployee, EmployeeDTO dto) {
        Integer empId = newEmployee.get_id();
        Employee oldEmployee = employeeDao.findById(empId);
        if (oldEmployee == null) {
            throw new RuntimeException("员工ID:" + empId + " 不存在");
        }

        updateEmployeeSkills(newEmployee, dto);
        handleDepartmentChange(oldEmployee, newEmployee);
        handleProjectChange(empId, dto, newEmployee);
        // 新增：处理任务关联变更
        handleTaskChange(empId, dto, newEmployee);
        handleTrainingChange(empId, dto, newEmployee);

        employeeDao.update(newEmployee);
    }

    // ========== 核心修改：删除员工时调用任务关联清理 ==========
    @Override
    public void deleteEmployee(Integer empId) {
        Employee employee = employeeDao.findById(empId);
        if (employee == null) {
            throw new RuntimeException("员工ID:" + empId + " 不存在");
        }

        handleDeptDelete(empId, employee.getDepId());
        handleProjectDelete(empId);
        // 新增：调用任务关联清理
        handleTaskDelete(empId);
        handleTrainingDelete(empId);

        employeeDao.deleteById(empId);
    }

    @Override
    public List<Employee> findEmployeesByName(String empName) {
        if (StringUtils.hasText(empName)) {
            return employeeDao.findByEmpNameLikeIgnoreCase(empName);
        } else {
            return employeeDao.findAll();
        }
    }

    @Override
    public Page<Employee> listEmployeesWithPage(String empName, int pageNum, int pageSize) {
        return employeeDao.findByEmpNameLikeWithPage(empName, pageNum, pageSize);
    }

    // ========== 原有私有方法不变（技能/部门/项目/培训） ==========
    private void updateEmployeeSkills(Employee newEmployee, EmployeeDTO dto) {
        List<Map<String, Integer>> updatedSkillList = new ArrayList<>();
        String skillsStr = dto.getSkills();

        if (skillsStr != null && !skillsStr.trim().isEmpty()) {
            String[] skillArray = skillsStr.split(",");
            for (String skillItem : skillArray) {
                String[] skillParts = skillItem.split(":");
                if (skillParts.length != 2
                        || !skillParts[0].matches("\\d+")
                        || !skillParts[1].matches("\\d+")) {
                    throw new RuntimeException("技能格式错误：" + skillItem + "，请按「技能ID:熟练度」格式输入（例：1:4），无需技能可留空");
                }

                Integer skillId = Integer.parseInt(skillParts[0]);
                Integer proficiency = Integer.parseInt(skillParts[1]);

                Skill existSkill = skillDao.findById(skillId);
                if (existSkill == null) {
                    throw new RuntimeException("技能ID:" + skillId + " 不存在，请选择系统中已有的技能，无需技能可留空");
                }

                if (proficiency < 1 || proficiency > 5) {
                    throw new RuntimeException("技能「" + existSkill.getSkillName() + "」（ID:" + skillId + "）的熟练度需在1-5之间，无需技能可留空");
                }

                Map<String, Integer> skillMap = new HashMap<>();
                skillMap.put("skillId", skillId);
                skillMap.put("proficiency", proficiency);
                updatedSkillList.add(skillMap);
            }

            List<Map<String, Integer>> distinctSkillList = updatedSkillList.stream()
                    .collect(Collectors.toMap(
                            skill -> skill.get("skillId"),
                            skill -> skill,
                            (oldVal, newVal) -> oldVal
                    ))
                    .values()
                    .stream()
                    .collect(Collectors.toList());

            newEmployee.setSkillList(distinctSkillList);
        } else {
            newEmployee.setSkillList(new ArrayList<>());
        }
    }

    private void handleDepartmentChange(Employee oldEmp, Employee newEmp) {
        Integer oldDepId = oldEmp.getDepId();
        Integer newDepId = newEmp.getDepId();

        if (!equals(oldDepId, newDepId)) {
            if (oldDepId != null) {
                Department oldDept = departmentDao.findById(oldDepId);
                if (oldDept != null && oldDept.getEmpList() != null) {
                    oldDept.getEmpList().removeIf(empMap -> oldEmp.get_id().equals(empMap.get("empId")));
                    if (oldEmp.get_id().equals(oldDept.getManagerId())) {
                        oldDept.setManagerId(null);
                    }
                    departmentDao.update(oldDept);
                }
            }

            if (newDepId != null) {
                Department newDept = departmentDao.findById(newDepId);
                if (newDept == null) {
                    throw new RuntimeException("新部门ID:" + newDepId + " 不存在");
                }
                if (newDept.getEmpList() == null) {
                    newDept.setEmpList(new ArrayList<>());
                }
                boolean exists = newDept.getEmpList().stream()
                        .anyMatch(empMap -> newEmp.get_id().equals(empMap.get("empId")));
                if (!exists) {
                    newDept.getEmpList().add(Map.of("empId", newEmp.get_id()));
                    departmentDao.update(newDept);
                }
            }
        }
    }

    private void handleProjectChange(Integer empId, EmployeeDTO dto, Employee newEmployee) {
        List<Integer> oldProjectIds = getOldProjectIds(empId);
        List<Integer> newProjectIds = dto.getNewProjectIds() == null ? new ArrayList<>() : dto.getNewProjectIds();

        for (Integer projId : oldProjectIds) {
            if (!newProjectIds.contains(projId)) {
                Project project = getValidProject(projId);
                if (project.getMembers() == null) {
                    continue;
                }
                boolean removed = project.getMembers().removeIf(member -> empId.equals(member.getEmpId()));
                if (removed) {
                    projectDao.update(project);
                }
            }
        }

        for (Integer projId : newProjectIds) {
            if (!oldProjectIds.contains(projId)) {
                Project project = getValidProject(projId);
                if (project.getMembers() == null) {
                    project.setMembers(new ArrayList<>());
                }
                boolean alreadyInProject = project.getMembers().stream()
                        .anyMatch(member -> empId.equals(member.getEmpId()));
                if (!alreadyInProject) {
                    Project.Member member = new Project.Member();
                    member.setEmpId(empId);
                    project.getMembers().add(member);
                    projectDao.update(project);
                }
            }
        }

        List<Map<String, Integer>> employeeProjects = newProjectIds.stream()
                .map(projId -> Map.of("projId", projId))
                .collect(Collectors.toList());
        newEmployee.setProjects(employeeProjects);
    }

    private void handleTrainingChange(Integer empId, EmployeeDTO dto, Employee newEmployee) {
        List<Integer> oldTrainingIds = getOldTrainingIds(empId);
        List<Integer> newTrainingIds = dto.getNewTrainingIds() == null ? new ArrayList<>() : dto.getNewTrainingIds();

        log.info("员工ID: {} - 旧培训ID列表: {}", empId, oldTrainingIds);
        log.info("员工ID: {} - 新培训ID列表: {}", empId, newTrainingIds);

        for (Integer trainId : oldTrainingIds) {
            if (!newTrainingIds.contains(trainId)) {
                Training training = getValidTraining(trainId);
                log.info("培训ID: {} - 从数据库读取的memberList: {}", trainId, training.getMembers());

                if (training.getMembers() == null) {
                    training.setMembers(new ArrayList<>());
                    log.info("培训ID: {} - 数据库memberList为null，初始化空列表", trainId);
                    continue;
                }

                boolean removed = training.getMembers().removeIf(memberId -> empId.equals(memberId));
                if (removed) {
                    trainingDao.update(training);
                    log.info("培训ID: {} - 成功从memberList删除员工ID: {}", trainId, empId);
                } else {
                    log.info("培训ID: {} - memberList中无员工ID: {}，无需删除", trainId, empId);
                }
            }
        }

        for (Integer trainId : newTrainingIds) {
            if (!oldTrainingIds.contains(trainId)) {
                Training training = getValidTraining(trainId);
                if (training.getMembers() == null) {
                    training.setMembers(new ArrayList<>());
                    log.info("培训ID: {} - 数据库memberList为null，初始化空列表", trainId);
                }

                boolean alreadyInTraining = training.getMembers().stream()
                        .anyMatch(memberId -> empId.equals(memberId));
                if (!alreadyInTraining) {
                    training.getMembers().add(empId);
                    trainingDao.update(training);
                    log.info("培训ID: {} - 成功向memberList添加员工ID: {}", trainId, empId);
                } else {
                    log.info("培训ID: {} - 员工ID: {} 已在memberList中，无需重复添加", trainId, empId);
                }
            }
        }

        List<Map<String, Integer>> employeeTrainings = newTrainingIds.stream()
                .map(trainId -> Map.of("trainId", trainId))
                .collect(Collectors.toList());
        newEmployee.setTrainingList(employeeTrainings);
        log.info("员工ID: {} - 最终关联的培训列表: {}", empId, employeeTrainings);
    }

    private void handleDeptDelete(Integer empId, Integer depId) {
        if (depId != null) {
            Department department = departmentDao.findById(depId);
            if (department != null) {
                if (department.getEmpList() != null) {
                    department.getEmpList().removeIf(empMap -> empId.equals(empMap.get("empId")));
                }
                if (empId.equals(department.getManagerId())) {
                    department.setManagerId(null);
                }
                departmentDao.update(department);
            }
        }
    }

    private void handleProjectDelete(Integer empId) {
        log.info("开始处理员工ID: {} 的项目关联删除", empId);

        List<Project> projects = projectDao.findByMemberEmpId(empId);
        if (projects.isEmpty()) {
            log.info("员工ID: {} 未关联任何项目，无需删除项目成员", empId);
            return;
        }

        for (Project project : projects) {
            Integer projId = project.getId();
            if (project.getMembers() == null) {
                project.setMembers(new ArrayList<>());
                log.info("项目ID: {} - members为null，初始化空列表", projId);
                continue;
            }

            boolean removed = project.getMembers().removeIf(member -> empId.equals(member.getEmpId()));
            if (removed) {
                projectDao.update(project);
                log.info("项目ID: {} - 成功从members中移除员工ID: {}", projId, empId);
            } else {
                log.info("项目ID: {} - members中无员工ID: {}，无需删除", projId, empId);
            }
        }

        log.info("员工ID: {} 的项目关联删除处理完成", empId);
    }

    // ========== 原有方法：任务删除清理（已存在，补充调用） ==========
    private void handleTaskDelete(Integer empId) {
        log.info("开始处理员工ID: {} 的任务关联删除", empId);
        List<Task> tasks = taskDao.findByManagerId(empId);
        if (tasks.isEmpty()) {
            log.info("员工ID: {} 未负责任何任务，无需清理任务负责人", empId);
            return;
        }
        for (Task task : tasks) {
            task.setManagerId(null);
            taskDao.update(task);
            log.info("任务ID: {}（{}）- 成功置空负责人（员工ID: {}）", task.get_id(), task.getTaskName(), empId);
        }
        log.info("员工ID: {} 的任务关联删除处理完成", empId);
    }

    private void handleTrainingDelete(Integer empId) {
        log.info("开始处理员工ID: {} 的培训关联删除", empId);

        List<Training> trainings = trainingDao.findByMemberEmpId(empId);
        if (trainings.isEmpty()) {
            log.info("员工ID: {} 未关联任何培训，无需删除培训成员", empId);
            return;
        }

        for (Training training : trainings) {
            Integer trainId = training.get_id();
            String trainName = training.getTrainName();

            if (training.getMembers() == null) {
                training.setMembers(new ArrayList<>());
                log.info("培训ID: {}（{}）- memberList为null，初始化空列表", trainId, trainName);
                continue;
            }

            boolean removed = training.getMembers().removeIf(memberId -> empId.equals(memberId));
            if (removed) {
                trainingDao.update(training);
                log.info("培训ID: {}（{}）- 成功从memberList移除员工ID: {}", trainId, trainName, empId);
            } else {
                log.info("培训ID: {}（{}）- memberList中无员工ID: {}，无需删除", trainId, trainName, empId);
            }
        }

        log.info("员工ID: {} 的培训关联删除处理完成", empId);
    }

    private Integer generateEmpId() {
        log.info("开始生成员工自增ID");
        List<Employee> allEmps = employeeDao.findAll();
        Integer maxId = allEmps.stream()
                .map(Employee::get_id)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(null);
        Integer newEmpId = (maxId == null) ? 1 : maxId + 1;
        log.info("现有最大员工ID：{}，生成新ID：{}", maxId, newEmpId);
        return newEmpId;
    }

    private void bindDepartment(Employee employee) {
        Integer depId = employee.getDepId();
        if (depId == null) {
            log.warn("员工ID: {} 未选择所属部门，跳过部门关联", employee.get_id());
            return;
        }
        Department dept = departmentDao.findById(depId);
        if (dept == null) {
            throw new RuntimeException("部门ID:" + depId + " 不存在，无法关联");
        }
        if (dept.getEmpList() == null) {
            dept.setEmpList(new ArrayList<>());
        }
        boolean exists = dept.getEmpList().stream()
                .anyMatch(empMap -> employee.get_id().equals(empMap.get("empId")));
        if (!exists) {
            dept.getEmpList().add(Map.of("empId", employee.get_id()));
            departmentDao.update(dept);
            log.info("员工ID: {} 已关联到部门ID: {}", employee.get_id(), depId);
        }
    }

    private void bindProjects(Integer empId, EmployeeDTO dto, Employee employee) {
        List<Integer> newProjectIds = dto.getNewProjectIds() == null ? new ArrayList<>() : dto.getNewProjectIds();
        if (newProjectIds.isEmpty()) {
            log.warn("员工ID: {} 未选择任何项目，跳过项目关联", empId);
            employee.setProjects(new ArrayList<>());
            return;
        }

        List<Map<String, Integer>> employeeProjects = new ArrayList<>();
        for (Integer projId : newProjectIds) {
            Project project = getValidProject(projId);
            if (project.getMembers() == null) {
                project.setMembers(new ArrayList<>());
            }
            boolean alreadyIn = project.getMembers().stream()
                    .anyMatch(member -> empId.equals(member.getEmpId()));
            if (!alreadyIn) {
                Project.Member member = new Project.Member();
                member.setEmpId(empId);
                project.getMembers().add(member);
                projectDao.update(project);
                log.info("员工ID: {} 已加入项目ID: {}", empId, projId);
            }
            employeeProjects.add(Map.of("projId", projId));
        }
        employee.setProjects(employeeProjects);
    }

    // ========== 新增：绑定新增员工的任务关联 ==========
    private void bindTasks(Integer empId, EmployeeDTO dto, Employee employee) {
        List<Integer> newTaskIds = dto.getNewManagerTaskIds() == null ? new ArrayList<>() : dto.getNewManagerTaskIds();
        if (newTaskIds.isEmpty()) {
            log.warn("员工ID: {} 未选择任何负责任务，跳过任务关联", empId);
            employee.setTasks(new ArrayList<>());
            return;
        }

        List<Map<String, Integer>> employeeTasks = new ArrayList<>();
        for (Integer taskId : newTaskIds) {
            // 校验任务存在性
            Task task = getValidTask(taskId);
            // 核心修改：如果任务已有负责人，先清空旧负责人的关联
            if (task.getManagerId() != null) {
                Integer oldManagerId = task.getManagerId();
                // 1. 找到旧负责人员工，移除该任务关联
                Employee oldManager = employeeDao.findById(oldManagerId);
                if (oldManager != null && oldManager.getTasks() != null) {
                    oldManager.getTasks().removeIf(taskMap -> taskId.equals(taskMap.get("taskId")));
                    employeeDao.update(oldManager);
                    log.info("任务ID: {}（{}）原有负责人（员工ID: {}），已移除其任务关联", taskId, task.getTaskName(), oldManagerId);
                }
                log.warn("任务ID: {}（{}）已有负责人（员工ID: {}），将替换为新员工ID: {}", taskId, task.getTaskName(), oldManagerId, empId);
            }
            // 设置任务负责人为当前员工
            task.setManagerId(empId);
            taskDao.update(task);
            log.info("员工ID: {} 已绑定为任务ID: {}（{}）的负责人", empId, taskId, task.getTaskName());
            // 封装员工的任务列表
            employeeTasks.add(Map.of("taskId", taskId));
        }
        employee.setTasks(employeeTasks);
    }

    private void bindTrainings(Integer empId, EmployeeDTO dto, Employee employee) {
        List<Integer> newTrainingIds = dto.getNewTrainingIds() == null ? new ArrayList<>() : dto.getNewTrainingIds();
        if (newTrainingIds.isEmpty()) {
            log.warn("员工ID: {} 未选择任何培训，跳过培训关联", empId);
            employee.setTrainingList(new ArrayList<>());
            return;
        }

        List<Map<String, Integer>> employeeTrainings = new ArrayList<>();
        for (Integer trainId : newTrainingIds) {
            Training training = getValidTraining(trainId);
            if (training.getMembers() == null) {
                training.setMembers(new ArrayList<>());
            }
            boolean alreadyIn = training.getMembers().stream()
                    .anyMatch(memberId -> empId.equals(memberId));
            if (!alreadyIn) {
                training.getMembers().add(empId);
                trainingDao.update(training);
                log.info("员工ID: {} 已加入培训ID: {}", empId, trainId);
            }
            employeeTrainings.add(Map.of("trainId", trainId));
        }
        employee.setTrainingList(employeeTrainings);
    }

    // ========== 新增：处理编辑员工的任务关联变更 ==========
    private void handleTaskChange(Integer empId, EmployeeDTO dto, Employee newEmployee) {
        // 1. 获取旧任务ID列表（员工原本负责的任务）
        List<Integer> oldTaskIds = getOldTaskIds(empId);
        // 2. 获取新任务ID列表（员工现在选择负责的任务）
        List<Integer> newTaskIds = dto.getNewManagerTaskIds() == null ? new ArrayList<>() : dto.getNewManagerTaskIds();

        log.info("员工ID: {} - 旧负责任务ID列表: {}", empId, oldTaskIds);
        log.info("员工ID: {} - 新负责任务ID列表: {}", empId, newTaskIds);

        // 3. 退出旧任务：置空旧任务的负责人ID（如果不在新列表里）
        for (Integer taskId : oldTaskIds) {
            if (!newTaskIds.contains(taskId)) {
                Task task = getValidTask(taskId);
                // 仅当任务负责人是当前员工时，才置空（防止误操作）
                if (empId.equals(task.getManagerId())) {
                    task.setManagerId(null);
                    taskDao.update(task);
                    log.info("任务ID: {}（{}）- 成功移除员工ID: {} 的负责人权限", taskId, task.getTaskName(), empId);
                } else {
                    log.info("任务ID: {}（{}）- 负责人已不是员工ID: {}，无需处理", taskId, task.getTaskName(), empId);
                }
            }
        }

        // 4. 加入新任务：设置新任务的负责人ID为当前员工（如果不在旧列表里）
        for (Integer taskId : newTaskIds) {
            if (!oldTaskIds.contains(taskId)) {
                Task task = getValidTask(taskId);
                // 核心修改：如果任务已有其他负责人，先清空旧负责人关联
                if (task.getManagerId() != null && !task.getManagerId().equals(empId)) {
                    Integer oldManagerId = task.getManagerId();
                    // 4.1 找到旧负责人员工，移除该任务关联
                    Employee oldManager = employeeDao.findById(oldManagerId);
                    if (oldManager != null && oldManager.getTasks() != null) {
                        oldManager.getTasks().removeIf(taskMap -> taskId.equals(taskMap.get("taskId")));
                        employeeDao.update(oldManager);
                        log.info("任务ID: {}（{}）原有负责人（员工ID: {}），已移除其任务关联", taskId, task.getTaskName(), oldManagerId);
                    }
                    log.warn("任务ID: {}（{}）已有负责人（员工ID: {}），将替换为新员工ID: {}", taskId, task.getTaskName(), oldManagerId, empId);
                }
                // 设置当前员工为任务负责人
                task.setManagerId(empId);
                taskDao.update(task);
                log.info("任务ID: {}（{}）- 成功设置员工ID: {} 为负责人", taskId, task.getTaskName(), empId);
            }
        }

        // 5. 同步更新员工的任务关联列表
        List<Map<String, Integer>> employeeTasks = newTaskIds.stream()
                .map(taskId -> Map.of("taskId", taskId))
                .collect(Collectors.toList());
        newEmployee.setTasks(employeeTasks);
        log.info("员工ID: {} - 最终负责的任务列表: {}", empId, employeeTasks);
    }

    // ========== 工具方法补充 ==========
    private List<Integer> getOldProjectIds(Integer empId) {
        return projectDao.findByMemberEmpId(empId).stream()
                .map(Project::getId)
                .collect(Collectors.toList());
    }

    private List<Integer> getOldTaskIds(Integer empId) {
        return taskDao.findByManagerId(empId).stream()
                .map(Task::get_id)
                .collect(Collectors.toList());
    }

    private List<Integer> getOldTrainingIds(Integer empId) {
        return trainingDao.findByMemberEmpId(empId).stream()
                .map(Training::get_id)
                .collect(Collectors.toList());
    }

    private boolean equals(Integer a, Integer b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    private Project getValidProject(Integer projId) {
        Project project = projectDao.findById(projId);
        if (project == null) {
            log.error("项目ID: {} 不存在", projId);
            throw new RuntimeException("项目ID:" + projId + " 不存在");
        }
        return project;
    }

    // ========== 新增：校验任务存在性 ==========
    private Task getValidTask(Integer taskId) {
        Task task = taskDao.findById(taskId);
        if (task == null) {
            log.error("任务ID: {} 不存在", taskId);
            throw new RuntimeException("任务ID:" + taskId + " 不存在");
        }
        return task;
    }

    private Training getValidTraining(Integer trainId) {
        Training training = trainingDao.findById(trainId);
        if (training == null) {
            log.error("培训ID: {} 不存在", trainId);
            throw new RuntimeException("培训ID:" + trainId + " 不存在");
        }
        return training;
    }
}