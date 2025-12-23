# API 集成说明

## 后端接口适配

由于后端部分控制器使用 `@Controller` 返回 Thymeleaf 视图，前端需要后端提供对应的 REST API JSON 接口。

### 需要添加的 REST API 接口

#### 1. 员工管理接口

需要在 `EmployeeController` 中添加以下 REST API：

```java
@RestController
@RequestMapping("/api/employees")
public class EmployeeRestController {
    
    // 获取员工列表（JSON）
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Page<EmployeeDTO>> listEmployees(
        @RequestParam(required = false) String empName,
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        Page<Employee> page = employeeService.listEmployeesWithPage(empName, pageNum, pageSize);
        // 转换为DTO并返回
        return ResponseEntity.ok(page);
    }
    
    // 获取所有员工
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.listAllEmployees());
    }
    
    // 新增员工（JSON）
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeDTO dto) {
        // 处理逻辑
        return ResponseEntity.ok("成功");
    }
    
    // 更新员工（JSON）
    @PostMapping("/mod")
    @ResponseBody
    public ResponseEntity<?> updateEmployee(@RequestBody EmployeeDTO dto) {
        // 处理逻辑
        return ResponseEntity.ok("成功");
    }
    
    // 删除员工（JSON）
    @PostMapping("/del")
    @ResponseBody
    public ResponseEntity<?> deleteEmployee(@RequestParam Integer id) {
        // 处理逻辑
        return ResponseEntity.ok("成功");
    }
}
```

#### 2. 部门管理接口

需要在 `DepartmentController` 中添加 REST API：

```java
@RestController
@RequestMapping("/api/departments")
public class DepartmentRestController {
    
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Page<DepartmentDTO>> listDepartments(
        @RequestParam(required = false) String searchKey,
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize
    ) {
        // 返回分页数据
    }
    
    @GetMapping("/all")
    @ResponseBody
    public ResponseEntity<List<Department>> getAllDepartments() {
        // 返回所有部门
    }
    
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveDepartment(@RequestBody Department dept) {
        // 保存逻辑
    }
    
    @PostMapping("/delete")
    @ResponseBody
    public ResponseEntity<?> deleteDepartment(@RequestParam Integer id) {
        // 删除逻辑
    }
}
```

### 已存在的 REST API 接口

以下接口已经存在，可以直接使用：

1. **技能管理** (`/skill/*`) - 已使用 `@RestController`
2. **培训管理** (`/training/*`) - 部分接口有 `@ResponseBody`
3. **项目匹配** (`/projectmatch/*`) - 部分接口有 `@ResponseBody`
4. **技能匹配** (`/skillmatch/*`) - 部分接口有 `@ResponseBody`

### CORS 配置

如果前后端分离部署，需要在后端添加 CORS 配置：

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### 临时解决方案

如果暂时无法修改后端，可以：

1. 使用现有的 `@ResponseBody` 接口
2. 在前端使用 `fetch` 直接调用后端接口
3. 解析返回的 HTML（不推荐）

### 数据格式说明

#### 员工技能格式
```
"1:4,2:3"  // 技能1（4级），技能2（3级）
```

#### 项目技能需求格式
```
"1:3,2:5"  // 技能1（最低3级），技能2（最低5级）
```

#### 日期格式
```
"2025-01-15T10:30:00"  // ISO 8601 格式
```

### 测试接口

可以使用 Postman 或 curl 测试接口：

```bash
# 获取技能列表
curl http://localhost:8080/skill/list?page=0&size=10

# 搜索技能
curl http://localhost:8080/skill/search?name=Java

# 新增技能
curl -X POST http://localhost:8080/skill/add \
  -H "Content-Type: application/json" \
  -d '{"skillName":"Vue.js","skillKind":"前端开发"}'
```


