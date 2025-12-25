package com.murasame.smarthrm.controller;

import com.murasame.smarthrm.dao.*;
import com.murasame.smarthrm.dto.EmployeeDTO;
import com.murasame.smarthrm.entity.*;
import com.murasame.smarthrm.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 员工管理REST控制器
 * 处理员工相关的所有RESTful API请求
 * 所有请求路径统一前缀：/employees
 */
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private static final Logger log = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;
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

    /**
     * 统一响应结构
     */
    private static class ApiResponse {
        private boolean success;
        private String message;
        private Object data;
        private Map<String, String> errors;

        public ApiResponse(boolean success, String message, Object data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public ApiResponse(boolean success, String message, Object data, Map<String, String> errors) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.errors = errors;
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Object getData() { return data; }
        public Map<String, String> getErrors() { return errors; }
    }

    /**
     * 员工列表查询（分页/搜索）
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse> listEmployees(
            @RequestParam(required = false) String empName,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            Page<Employee> empPage = employeeService.listEmployeesWithPage(empName, pageNum, pageSize);
            List<Employee> employees = empPage.getContent();
            List<Department> departments = departmentDao.findAll();

            // 处理部门信息
            for (Employee emp : employees) {
                if (emp.getDepId() == null) {
                    emp.setDeptName("未分配");
                    emp.setDeptType("unassigned");
                } else {
                    boolean found = false;
                    for (Department dept : departments) {
                        if (Objects.equals(dept.getId(), emp.getDepId())) {
                            emp.setDeptName(dept.getDepName());
                            emp.setDeptType("normal");
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        emp.setDeptName("部门已删除");
                        emp.setDeptType("deleted");
                    }
                }

                // ========== 新增：处理员工任务列表（前端表格显示） ==========
                if (emp.getTasks() == null) {
                    // 从任务表查询该员工负责的任务，补充到员工实体
                    List<Task> empTasks = taskDao.findByManagerId(emp.get_id());
                    List<Map<String, Integer>> taskList = empTasks.stream()
                            .map(task -> Map.of("taskId", task.get_id()))
                            .collect(Collectors.toList());
                    emp.setTasks(taskList);
                }
            }

            // 封装分页数据
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("employees", employees);
            pageData.put("pageNum", empPage.getNumber() + 1);
            pageData.put("pageSize", empPage.getSize());
            pageData.put("totalPages", empPage.getTotalPages());
            pageData.put("totalElements", empPage.getTotalElements());
            pageData.put("empName", empName);

            return ResponseEntity.ok(new ApiResponse(true, "查询成功", pageData));
        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "查询失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取员工详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getEmployeeDetail(@PathVariable Integer id) {
        try {
            Employee employee = employeeService.findEmployeeById(id);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "员工不存在", null));
            }

            // 查询关联数据
            List<Department> departments = departmentDao.findAll();
            List<Project> allProjects = projectDao.findAll();
            List<Task> allTasks = taskDao.findAll();
            List<Training> allTrainings = trainingDao.findAll();
            List<Skill> allSkills = skillDao.findAll();

            // 处理部门信息
            if (employee.getDepId() != null) {
                departments.stream()
                        .filter(dept -> Objects.equals(dept.getId(), employee.getDepId()))
                        .findFirst()
                        .ifPresent(dept -> {
                            employee.setDeptName(dept.getDepName());
                            employee.setDeptType("normal");
                        });
            } else {
                employee.setDeptName("未分配");
                employee.setDeptType("unassigned");
            }

            // 处理关联数据
            List<Integer> existingProjectIds = employee.getProjects() != null ?
                    employee.getProjects().stream()
                            .map(projMap -> projMap.get("projId"))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList()) : new ArrayList<>();

            // ========== 已有逻辑：查询员工已负责的任务ID ==========
            List<Integer> existingTaskIds = taskDao.findByManagerId(id).stream()
                    .map(Task::get_id)
                    .collect(Collectors.toList());

            List<Integer> existingTrainingIds = trainingDao.findByMemberEmpId(id).stream()
                    .map(Training::get_id)
                    .collect(Collectors.toList());

            String existingSkillsStr = employee.getSkillList() != null ?
                    employee.getSkillList().stream()
                            .filter(skillMap -> skillMap.get("skillId") != null && skillMap.get("proficiency") != null)
                            .map(skillMap -> skillMap.get("skillId") + ":" + skillMap.get("proficiency"))
                            .collect(Collectors.joining(",")) : "";

            // 封装返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("employee", employee);
            data.put("departments", departments);
            data.put("allProjects", allProjects);
            data.put("allTasks", allTasks);  // 前端下拉的所有任务
            data.put("allTrainings", allTrainings);
            data.put("allSkills", allSkills);
            data.put("existingProjectIds", existingProjectIds);
            data.put("existingTaskIds", existingTaskIds);  // 员工已负责的任务ID
            data.put("existingTrainingIds", existingTrainingIds);
            data.put("existingSkillsStr", existingSkillsStr);

            return ResponseEntity.ok(new ApiResponse(true, "查询成功", data));
        } catch (Exception e) {
            log.error("查询员工详情失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "查询失败: " + e.getMessage(), null));
        }
    }

    /**
     * 获取表单所需的选项数据
     */
    @GetMapping("/form-options")
    public ResponseEntity<ApiResponse> getFormOptions() {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("departments", departmentDao.findAll());
            options.put("allProjects", projectDao.findAll());
            options.put("allTasks", taskDao.findAll());  // 已有逻辑：返回所有任务
            options.put("allTrainings", trainingDao.findAll());
            options.put("allSkills", skillDao.findAll());

            return ResponseEntity.ok(new ApiResponse(true, "获取选项成功", options));
        } catch (Exception e) {
            log.error("获取表单选项失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "获取失败: " + e.getMessage(), null));
        }
    }

    /**
     * 新增员工
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addEmployee(@Valid @RequestBody EmployeeDTO dto, BindingResult br) {
        // 表单验证
        if (br.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            br.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "表单验证失败", null, errors));
        }

        try {
            Employee employee = new Employee();
            employee.setEmpName(dto.getName());

            // 处理部门ID
            String depIdStr = dto.getDepartment();
            employee.setDepId(StringUtils.hasText(depIdStr) ? Integer.parseInt(depIdStr.trim()) : null);

            // 处理加入时间
            LocalDateTime joinTime = dto.getJoinDate() != null ? dto.getJoinDate() : LocalDateTime.now();
            employee.setJoinDate(joinTime);

            // ========== 新增：初始化任务列表（和项目/培训保持一致） ==========
            employee.setSkillList(new ArrayList<>());
            employee.setProjects(new ArrayList<>());
            employee.setTrainingList(new ArrayList<>());
            employee.setTasks(new ArrayList<>());  // 初始化任务列表，避免空指针

            // ========== 新增：校验任务ID合法性 ==========
            if (dto.getNewManagerTaskIds() != null) {
                for (Integer taskId : dto.getNewManagerTaskIds()) {
                    if (taskId == null || taskId <= 0) {
                        return ResponseEntity.badRequest()
                                .body(new ApiResponse(false, "任务ID必须为正整数", null));
                    }
                    // 校验任务是否存在
                    Task task = taskDao.findById(taskId);
                    if (task == null) {
                        return ResponseEntity.badRequest()
                                .body(new ApiResponse(false, "任务ID:" + taskId + " 不存在", null));
                    }
                }
            }

            employeeService.saveEmployee(employee, dto);
            return ResponseEntity.ok(new ApiResponse(true, "员工「" + dto.getName() + "」新增成功", employee.get_id()));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "部门ID必须为数字", null));
        } catch (Exception e) {
            log.error("新增员工失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "新增失败: " + e.getMessage(), null));
        }
    }

    /**
     * 更新员工
     */
    @PostMapping("/mod")
    public ResponseEntity<ApiResponse> modEmployee(
            @Valid @RequestBody EmployeeDTO dto,
            BindingResult br) {
        // 表单验证
        if (br.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            br.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "表单验证失败", null, errors));
        }

        try {
            Employee oldEmployee = employeeService.findEmployeeById(dto.getId());
            if (oldEmployee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "员工不存在", null));
            }

            Employee employee = new Employee();
            employee.set_id(dto.getId());
            employee.setEmpName(dto.getName());

            // 处理部门ID
            if (dto.getDepartment() != null && !dto.getDepartment().trim().isEmpty()) {
                employee.setDepId(Integer.parseInt(dto.getDepartment().trim()));
            } else {
                employee.setDepId(null);
            }

            // 处理加入时间
            employee.setJoinDate(dto.getJoinDate() != null ? dto.getJoinDate() : oldEmployee.getJoinDate());

            // ========== 新增：初始化任务列表（和项目保持一致） ==========
            employee.setSkillList(new ArrayList<>());
            employee.setProjects(new ArrayList<>());
            employee.setTrainingList(new ArrayList<>());  // 补充初始化培训列表
            employee.setTasks(new ArrayList<>());  // 初始化任务列表，避免空指针

            // ========== 新增：校验任务ID合法性 ==========
            if (dto.getNewManagerTaskIds() != null) {
                for (Integer taskId : dto.getNewManagerTaskIds()) {
                    if (taskId == null || taskId <= 0) {
                        return ResponseEntity.badRequest()
                                .body(new ApiResponse(false, "任务ID必须为正整数", null));
                    }
                    // 校验任务是否存在
                    Task task = taskDao.findById(taskId);
                    if (task == null) {
                        return ResponseEntity.badRequest()
                                .body(new ApiResponse(false, "任务ID:" + taskId + " 不存在", null));
                    }
                }
            }

            employeeService.updateEmployee(employee, dto);
            return ResponseEntity.ok(new ApiResponse(true, "员工ID:" + dto.getId() + " 更新成功", null));
        } catch (Exception e) {
            log.error("更新员工失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }

    /**
     * 删除员工
     */
    @PostMapping("/del")
    public ResponseEntity<ApiResponse> deleteEmployee(
            @RequestParam Integer id,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(required = false) String empName) {
        try {
            employeeService.deleteEmployee(id);  // service已处理任务负责人置空
            return ResponseEntity.ok(new ApiResponse(
                    true,
                    "员工ID:" + id + " 删除成功（含关联数据同步）",
                    Map.of("pageNum", pageNum, "empName", empName)
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, e.getMessage(), null));
        }
    }
}