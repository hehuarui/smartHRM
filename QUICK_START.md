# 快速启动指南

## 🚀 一键启动（Windows）

### 方法1: 使用批处理脚本（最简单）

1. **双击运行 `start-all.bat`**
   - 会自动启动后端和前端两个服务
   - 会打开两个命令行窗口

2. **等待启动完成**
   - 后端: 看到 "Started SmartHrmApplication" 表示启动成功
   - 前端: 看到 "Local: http://localhost:3000" 表示启动成功

3. **访问应用**
   - 打开浏览器访问: http://localhost:3000

### 方法2: 分别启动

**启动后端:**
- 双击 `start-backend.bat`
- 或使用IDE运行 `SmartHrmApplication.java`

**启动前端:**
- 双击 `start-frontend.bat`

## 📋 启动前检查清单

- [ ] MongoDB 服务已启动
  ```bash
  # 检查MongoDB是否运行
  mongosh
  # 或 Windows服务中查看 MongoDB 服务状态
  ```

- [ ] Java 17 已安装
  ```bash
  java -version
  ```

- [ ] Node.js 已安装（前端）
  ```bash
  node --version
  npm --version
  ```

## 🔧 手动启动步骤

### 1. 启动MongoDB

**Windows:**
```bash
# 方式1: 启动Windows服务
net start MongoDB

# 方式2: 命令行启动
mongod --dbpath "C:\data\db"
```

**验证:**
```bash
mongosh
# 应该能连接到MongoDB
```

### 2. 启动后端（Spring Boot）

**使用IDE (推荐):**
1. 打开 IntelliJ IDEA / Eclipse
2. 打开项目
3. 找到 `src/main/java/com/murasame/smarthrm/SmartHrmApplication.java`
4. 右键 → Run

**使用命令行:**
```bash
# 如果Maven在PATH中
mvn spring-boot:run

# 或使用已编译的JAR
java -jar target/smartHRM-V0.6-251220.jar
```

**启动成功标志:**
```
Started SmartHrmApplication in X.XXX seconds
```

### 3. 启动前端（Vue3 + Vite）

```bash
cd frontend

# 首次运行需要安装依赖（如果还没安装）
npm install

# 启动开发服务器
npm run dev
```

**启动成功标志:**
```
  VITE v5.x.x  ready in xxx ms

  ➜  Local:   http://localhost:3000/
  ➜  Network: use --host to expose
```

## 🌐 访问地址

- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **MongoDB**: mongodb://localhost:27017/smartHRM

## ⚠️ 常见问题

### 问题1: 后端启动失败 - MongoDB连接错误

**错误信息:**
```
Exception in thread "main" com.mongodb.MongoSocketException
```

**解决方法:**
1. 确保MongoDB服务已启动
2. 检查 `application-dev.yml` 中的MongoDB连接地址
3. 默认应该是 `mongodb://localhost:27017/smartHRM`

### 问题2: 前端无法访问后端API

**解决方法:**
1. 确保后端已启动（访问 http://localhost:8080 测试）
2. 检查浏览器控制台是否有CORS错误
3. 后端已配置允许所有请求（SecurityConfiguration）

### 问题3: 端口被占用

**后端8080端口被占用:**
```bash
# Windows查看端口占用
netstat -ano | findstr :8080

# 结束进程（替换PID）
taskkill /PID <进程ID> /F
```

**前端3000端口被占用:**
- 修改 `frontend/vite.config.js` 中的端口号
- 或结束占用3000端口的进程

### 问题4: Maven未找到

**解决方法:**
1. 安装Maven并添加到PATH
2. 或使用IDE直接运行主类（推荐）
3. 或使用已编译的JAR包

## 📝 下一步

1. ✅ 启动MongoDB
2. ✅ 启动后端服务
3. ✅ 启动前端服务
4. ✅ 访问 http://localhost:3000
5. ✅ 开始使用系统！

## 🎯 功能测试

启动成功后，可以测试以下功能：

1. **员工管理** - 添加、编辑、删除员工
2. **部门管理** - 管理部门信息
3. **技能管理** - 管理技能库
4. **培训管理** - 管理培训课程
5. **项目匹配** - 搜索和匹配项目
6. **技能匹配** - 根据技能需求匹配员工

## 💡 提示

- 后端和前端需要同时运行
- MongoDB必须运行才能使用系统
- 首次使用建议先添加一些测试数据
- 查看浏览器控制台可以了解API调用情况

如有问题，请查看 `START_GUIDE.md` 获取详细说明。


