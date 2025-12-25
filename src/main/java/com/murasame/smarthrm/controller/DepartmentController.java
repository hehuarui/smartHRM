package com.murasame.smarthrm.controller;
//lin1224
import com.murasame.smarthrm.dto.DepartmentDTO;
import com.murasame.smarthrm.entity.Department;
import com.murasame.smarthrm.entity.Employee;
import com.murasame.smarthrm.service.DepartmentService;
import com.murasame.smarthrm.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门管理REST控制器
 * 处理部门列表查询、新增/编辑/删除等请求，返回JSON格式数据
 * 所有请求路径统一前缀：/departments
 */
@RestController
@RequestMapping("/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * 部门列表查询（支持模糊搜索、分页）
     */
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> listDepartments(
            @RequestParam(required = false) String searchKey,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        Page<DepartmentDTO> deptPage = departmentService.listDepartments(searchKey, pageNum, pageSize);

        Map<String, Object> response = new HashMap<>();
        response.put("departments", deptPage.getContent());
        response.put("pageNum", deptPage.getNumber() + 1);
        response.put("pageSize", deptPage.getSize());
        response.put("total", deptPage.getTotalElements());
        response.put("totalPages", deptPage.getTotalPages());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 获取部门详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Integer id) {
        Department dept = departmentService.getDepartmentById(id);
        return new ResponseEntity<>(dept, HttpStatus.OK);
    }

    /**
     * 获取部门员工
     */
    @GetMapping("/{id}/employees")
    public ResponseEntity<List<Employee>> getDepartmentEmployees(@PathVariable Integer id) {
        List<Employee> employees = departmentService.getEmployeesByDeptId(id);
        return new ResponseEntity<>(employees, HttpStatus.OK);
    }

    /**
     * 保存部门（支持新增/编辑）
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveDepartment(@RequestBody Department dept) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (dept.getId() == null) {
                departmentService.saveDepartment(dept);
                response.put("success", true);
                response.put("message", "新增部门成功！");
            } else {
                departmentService.updateDepartment(dept);
                response.put("success", true);
                response.put("message", "更新部门成功！");
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 删除部门
     */
    @PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteDepartment(
            @RequestParam Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            departmentService.deleteDepartment(id);
            response.put("success", true);
            response.put("message", "部门删除成功！");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

//    /**
//     * 获取所有部门（用于下拉选项）
//     */
//    @GetMapping("/all")
//    public ResponseEntity<List<Department>> getAllDepartments() {
//        List<Department> departments = departmentService.listAllDepartments();
//        return new ResponseEntity<>(departments, HttpStatus.OK);
//    }
}