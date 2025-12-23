# SmartHRM 前端项目

智慧企业员工技能管理与项目匹配系统 - Vue3 前端

## 技术栈

- **Vue 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **Element Plus** - Vue 3 组件库
- **Vue Router** - 官方路由管理器
- **Axios** - HTTP 客户端

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 接口封装
│   │   ├── request.js   # Axios 配置
│   │   ├── employee.js   # 员工相关接口
│   │   ├── department.js # 部门相关接口
│   │   ├── skill.js      # 技能相关接口
│   │   ├── training.js   # 培训相关接口
│   │   ├── project.js    # 项目相关接口
│   │   └── skillMatch.js # 技能匹配接口
│   ├── views/            # 页面组件
│   │   ├── Employees.vue      # 员工管理
│   │   ├── Departments.vue    # 部门管理
│   │   ├── Skills.vue         # 技能管理
│   │   ├── Trainings.vue      # 培训管理
│   │   ├── ProjectMatch.vue   # 项目匹配
│   │   └── SkillMatch.vue     # 技能匹配
│   ├── layouts/          # 布局组件
│   │   └── Layout.vue    # 主布局
│   ├── router/           # 路由配置
│   │   └── index.js
│   ├── App.vue          # 根组件
│   └── main.js          # 入口文件
├── index.html           # HTML 模板
├── vite.config.js      # Vite 配置
└── package.json        # 项目配置
```

## 安装依赖

```bash
cd frontend
npm install
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 构建生产版本

```bash
npm run build
```

构建产物在 `dist` 目录

## 功能模块

### 1. 员工管理

- 员工列表查询（支持姓名搜索、分页）
- 新增/编辑员工信息
- 员工技能管理
- 员工项目关联
- 员工培训记录

### 2. 部门管理

- 部门列表查询（支持搜索、分页）
- 新增/编辑部门信息
- 部门员工管理
- 部门负责人设置

### 3. 技能管理

- 技能列表查询（支持搜索、分页）
- 新增/编辑技能信息
- 技能分类管理

### 4. 培训管理

- 培训课程列表（支持搜索、分页）
- 新增/编辑培训课程
- 培训与技能关联
- 培训学员管理

### 5. 项目匹配

- 项目列表查询
- 新增/编辑项目
- 项目技能需求设置
- 项目成员管理
- 项目任务管理
- 按项目名称或员工 ID 匹配项目

### 6. 技能匹配

- 根据技能需求匹配员工
- 支持多技能组合匹配
- 快速选择技能功能

## API 配置

前端通过 Vite 代理访问后端 API，配置在 `vite.config.js`：

```javascript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

## 注意事项

1. **后端 API 适配**：部分后端接口返回的是 Thymeleaf 渲染的 HTML，需要后端提供对应的 REST API JSON 接口。

2. **跨域问题**：开发环境通过 Vite 代理解决，生产环境需要后端配置 CORS。

3. **数据格式**：
   - 员工技能格式：`skillId:proficiency`（如：`1:4,2:3`）
   - 项目技能需求格式：`skillId:minProficiency`（如：`1:3,2:5`）

## 开发建议

1. 后端需要添加 REST API 接口返回 JSON 数据
2. 统一错误处理和消息提示
3. 优化加载性能和用户体验
4. 添加数据验证和表单校验

## 浏览器支持

- Chrome（推荐）
- Firefox
- Safari
- Edge

