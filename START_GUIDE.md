# 启动指南

## 前置要求

### 1. 环境检查

确保已安装以下软件：

- ✅ **Java 17** - 后端运行环境
- ✅ **MongoDB** - 数据库服务
- ✅ **Node.js 16+** - 前端运行环境
- ✅ **Maven** (可选) - 如果使用命令行启动后端

### 2. MongoDB 启动

**Windows:**
```bash
# 方式1: 使用服务
net start MongoDB

# 方式2: 使用命令行
mongod --dbpath "C:\data\db"
```

**Linux/Mac:**
```bash
sudo systemctl start mongod
# 或
mongod --dbpath /data/db
```

**验证MongoDB:**
```bash
mongosh
# 或
mongo
```

## 启动方式

### 方式1: 使用批处理脚本（推荐 - Windows）

#### 启动所有服务
双击运行 `start-all.bat`，会自动打开两个窗口分别运行后端和前端。

#### 分别启动
- **后端**: 双击 `start-backend.bat`
- **前端**: 双击 `start-frontend.bat`

### 方式2: 使用IDE启动后端

#### IntelliJ IDEA
1. 打开项目
2. 找到 `src/main/java/com/murasame/smarthrm/SmartHrmApplication.java`
3. 右键 → Run 'SmartHrmApplication'
4. 等待启动完成（控制台显示 "Started SmartHrmApplication"）

#### Eclipse / VS Code
1. 打开项目
2. 找到主类 `SmartHrmApplication.java`
3. 运行主类

### 方式3: 命令行启动

#### 启动后端

**使用Maven:**
```bash
cd "G:\数据库课设\smartHRM"
mvn spring-boot:run
```

**使用JAR包:**
```bash
cd "G:\数据库课设\smartHRM"
java -jar target/smartHRM-V0.6-251220.jar
```

#### 启动前端

```bash
cd frontend

# 首次运行需要安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 访问地址

启动成功后，访问以下地址：

- **前端界面**: http://localhost:3000
- **后端API**: http://localhost:8080
- **MongoDB**: mongodb://localhost:27017/smartHRM

## 常见问题

### 1. 后端启动失败

**问题**: `Connection refused` 或 MongoDB 连接错误

**解决**:
1. 检查MongoDB是否运行: `mongosh` 或 `mongo`
2. 检查配置文件 `application-dev.yml` 中的MongoDB连接地址
3. 确保MongoDB服务在 `localhost:27017` 运行

### 2. 前端启动失败

**问题**: `npm install` 失败或端口被占用

**解决**:
1. 检查Node.js版本: `node --version` (需要16+)
2. 清除缓存: `npm cache clean --force`
3. 删除 `node_modules` 重新安装
4. 检查3000端口是否被占用

### 3. 接口调用失败

**问题**: 前端无法访问后端API

**解决**:
1. 确保后端已启动（访问 http://localhost:8080 测试）
2. 检查 `vite.config.js` 中的代理配置
3. 检查浏览器控制台的错误信息
4. 确保后端CORS配置正确（已在SecurityConfiguration中配置）

### 4. Maven未找到

**问题**: `mvn: command not found`

**解决**:
1. 安装Maven并添加到PATH
2. 或使用IDE直接运行主类
3. 或使用已编译的JAR包运行

## 开发模式

### 后端热重载

如果使用IDE运行，修改代码后会自动重新编译。

如果使用Maven命令行，需要手动重启。

### 前端热重载

Vite会自动检测文件变化并热重载，无需手动刷新浏览器。

## 生产部署

### 构建前端

```bash
cd frontend
npm run build
```

构建产物在 `frontend/dist` 目录。

### 构建后端

```bash
mvn clean package
```

JAR包在 `target/` 目录。

## 停止服务

- **后端**: 在运行窗口按 `Ctrl+C`
- **前端**: 在运行窗口按 `Ctrl+C`
- **MongoDB**: 根据启动方式停止服务

## 下一步

1. ✅ 确保MongoDB运行
2. ✅ 启动后端服务
3. ✅ 启动前端服务
4. ✅ 访问 http://localhost:3000 开始使用

如有问题，请查看项目README或API_INTEGRATION.md文档。


