# 云上纪念 - 部署指南

## 目录

- [一、阿里云服务器准备](#一阿里云服务器准备)
- [二、后端部署](#二后端部署)
- [三、管理后台前端部署](#三管理后台前端部署)
- [四、Nginx 反向代理配置](#四nginx-反向代理配置)
- [五、微信小程序部署](#五微信小程序部署)
- [六、HTTPS 与域名配置](#六https-与域名配置)
- [七、常见问题](#七常见问题)

---

## 一、阿里云服务器准备

### 1.1 购买 ECS 实例

| 配置项 | 推荐值 |
|--------|--------|
| 实例规格 | 2核4G（ecs.c7.large）起步 |
| 操作系统 | CentOS 7.9 / Ubuntu 22.04 |
| 系统盘 | 40GB SSD |
| 带宽 | 5Mbps 固定带宽或按量付费 |

### 1.2 安全组配置

在阿里云控制台 -> ECS -> 安全组中放行以下端口：

| 端口 | 用途 |
|------|------|
| 22 | SSH 登录 |
| 80 | HTTP（Nginx） |
| 443 | HTTPS（Nginx） |
| 18080 | 后端 API（可选，调试用，上线后建议关闭） |
| 3306 | MySQL（**仅内网访问，不要对公网开放**） |
| 6379 | Redis（**仅内网访问，不要对公网开放**） |

### 1.3 安装基础环境

SSH 登录服务器后执行：

```bash
# 更新系统
sudo yum update -y    # CentOS
# sudo apt update && sudo apt upgrade -y  # Ubuntu

# 安装 JDK 17
sudo yum install -y java-17-openjdk java-17-openjdk-devel
# 验证
java -version

# 安装 Maven
cd /opt
sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
sudo tar -xzf apache-maven-3.9.9-bin.tar.gz
sudo ln -s /opt/apache-maven-3.9.9 /opt/maven

# 配置环境变量
cat >> ~/.bashrc << 'EOF'
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
EOF
source ~/.bashrc
mvn -version

# 安装 Node.js 18+（用于前端构建）
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs
# 验证
node -v && npm -v

# 安装 Nginx
sudo yum install -y epel-release
sudo yum install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```

### 1.4 安装 MySQL

```bash
# 添加 MySQL 8 仓库
sudo rpm -Uvh https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm
sudo yum install -y mysql-community-server

# 启动 MySQL
sudo systemctl enable mysqld
sudo systemctl start mysqld

# 获取临时密码
sudo grep 'temporary password' /var/log/mysqld.log

# 修改 root 密码和安全配置
sudo mysql_secure_installation
```

或直接使用 **阿里云 RDS MySQL**（推荐生产环境），创建实例时选择：
- 版本：MySQL 8.0
- 规格：1核2G 基础版起步
- 存储：20GB

### 1.5 安装 Redis

```bash
sudo yum install -y redis
sudo systemctl enable redis
sudo systemctl start redis

# 配置密码（生产环境必须）
sudo vi /etc/redis.conf
# 修改：requirepass 你的Redis密码
sudo systemctl restart redis
```

或使用 **阿里云 Redis** 云服务。

---

## 二、后端部署

### 2.1 配置数据库

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库和用户
CREATE DATABASE memorial DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'admin'@'%' IDENTIFIED BY '你的密码';
GRANT ALL PRIVILEGES ON memorial.* TO 'admin'@'%';
FLUSH PRIVILEGES;

# 导入 SQL
USE memorial;
SOURCE /path/to/sql/ry_20250522.sql;
SOURCE /path/to/sql/memorial_ddl.sql;
SOURCE /path/to/sql/memorial_sample_data.sql;
SOURCE /path/to/sql/quartz.sql;
SOURCE /path/to/sql/memorial_menu_dict.sql;
```

### 2.2 修改后端配置

编辑 `ruoyi-admin/src/main/resources/application-druid.yml`：

```yaml
spring:
    datasource:
        druid:
            master:
                url: jdbc:mysql://127.0.0.1:3306/memorial?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
                username: admin
                password: 你的数据库密码
```

> 如果使用阿里云 RDS，将 `127.0.0.1` 替换为 RDS 内网地址。

编辑 `ruoyi-admin/src/main/resources/application.yml`，修改 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1        # 或阿里云 Redis 内网地址
      port: 6379
      database: 11
      password: 你的Redis密码   # 取消注释并填写密码
```

修改文件上传路径（Linux 环境）：

```yaml
ruoyi:
  profile: /home/ruoyi/uploadPath
```

修改 JWT 密钥（**生产环境务必更换**）：

```yaml
token:
  secret: 你的随机密钥-至少32位字符
  expireTime: 120            # 生产环境建议延长过期时间（分钟）
```

### 2.3 构建后端 JAR

在项目根目录执行：

```bash
mvn clean package -DskipTests
```

构建产物位于：`ruoyi-admin/target/ruoyi-admin.jar`

### 2.4 上传并启动后端

```bash
# 在服务器上创建目录
sudo mkdir -p /home/ruoyi/uploadPath
sudo mkdir -p /home/ruoyi/logs

# 将 jar 包和启动脚本上传到服务器
scp ruoyi-admin/target/ruoyi-admin.jar root@你的服务器IP:/home/ruoyi/
scp ry.sh root@你的服务器IP:/home/ruoyi/

# SSH 登录服务器
cd /home/ruoyi
chmod +x ry.sh

# 启动
./ry.sh start

# 查看日志
tail -f logs/ruoyi-admin.jar.log

# 验证
curl http://localhost:18080/
```

### 2.5 （可选）配置为 Systemd 服务

创建 `/etc/systemd/system/ruoyi.service`：

```ini
[Unit]
Description=RuoYi Memorial Backend
After=network.target mysql.service redis.service

[Service]
Type=forking
User=root
WorkingDirectory=/home/ruoyi
ExecStart=/home/ruoyi/ry.sh start
ExecStop=/home/ruoyi/ry.sh stop
ExecReload=/home/ruoyi/ry.sh restart
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable ruoyi
sudo systemctl start ruoyi
```

---

## 三、管理后台前端部署

### 3.1 修改生产环境配置

编辑 `ruoyi-ui/.env.production`：

```properties
VUE_APP_TITLE = 云上纪念管理系统
ENV = 'production'
VUE_APP_BASE_API = '/prod-api'
```

### 3.2 构建前端

```bash
cd ruoyi-ui
npm install
npm run build:prod
```

构建产物位于 `ruoyi-ui/dist/` 目录。

### 3.3 上传到服务器

```bash
# 上传 dist 目录到服务器
scp -r dist/* root@你的服务器IP:/usr/share/nginx/html/admin/
```

---

## 四、Nginx 反向代理配置

### 4.1 创建 Nginx 配置

创建 `/etc/nginx/conf.d/memorial.conf`：

```nginx
server {
    listen 80;
    server_name 你的域名.com;    # 替换为你的域名或服务器IP

    # 管理后台前端
    location / {
        root /usr/share/nginx/html/admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /prod-api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://127.0.0.1:18080/;
    }

    # 文件上传/下载代理
    location /profile/ {
        alias /home/ruoyi/uploadPath/;
    }

    # WebSocket 支持（如果需要）
    location /ws/ {
        proxy_pass http://127.0.0.1:18080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # Gzip 压缩
    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json;
    gzip_vary on;
    gzip_disable "MSIE [1-6]\.";

    client_max_body_size 20m;
}
```

### 4.2 检查并重启 Nginx

```bash
sudo nginx -t
sudo systemctl restart nginx
```

此时访问 `http://你的域名` 即可打开管理后台。

---

## 五、微信小程序部署

### 5.1 注册微信小程序

1. 访问 [微信公众平台](https://mp.weixin.qq.com/) 注册小程序账号
2. 完成主体信息认证（个人或企业）
3. 在「开发管理」->「开发设置」中获取 **AppID**

### 5.2 配置小程序 AppID

编辑 `memorial-app/src/manifest.json`，填入 AppID：

```json
{
  "mp-weixin": {
    "appid": "你的小程序AppID",
    "setting": {
      "urlCheck": true
    }
  }
}
```

### 5.3 配置生产环境 API 地址

编辑 `memorial-app/.env.production`：

```properties
VITE_API_BASE_URL=https://你的域名.com/prod-api
```

> **注意**：微信小程序要求所有接口必须为 HTTPS，且域名需在微信公众平台配置白名单。

### 5.4 配置服务器域名白名单

在微信公众平台 -> 开发管理 -> 开发设置 -> 服务器域名中配置：

| 域名类型 | 填写内容 |
|----------|---------|
| request 合法域名 | `https://你的域名.com` |
| uploadFile 合法域名 | `https://你的域名.com` |
| downloadFile 合法域名 | `https://你的域名.com` |

### 5.5 构建小程序代码

```bash
cd memorial-app
npm install

# 构建微信小程序
npm run build:mp-weixin
```

构建产物位于 `memorial-app/dist/build/mp-weixin/` 目录。

### 5.6 使用微信开发者工具上传

1. 下载 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具，选择「导入项目」
3. 项目目录选择 `memorial-app/dist/build/mp-weixin/`
4. 填入 AppID，点击确定
5. 在开发者工具中预览和调试
6. 确认无误后，点击右上角「上传」按钮
7. 填写版本号和项目备注

### 5.7 提交审核与发布

1. 登录微信公众平台
2. 进入「版本管理」
3. 在「开发版本」中找到刚上传的版本
4. 点击「提交审核」，填写审核信息
5. 审核通过后（通常 1-7 天），点击「发布」即可上线

### 5.8 小程序业务域名配置（如需 WebView）

如果小程序内嵌 H5 页面，还需在「开发设置」->「业务域名」中配置：
- 下载校验文件放到网站根目录
- 添加域名 `你的域名.com`

---

## 六、HTTPS 与域名配置

### 6.1 域名备案

阿里云服务器使用的域名必须完成 ICP 备案：
1. 登录 [阿里云 ICP 备案系统](https://beian.aliyun.com/)
2. 按指引提交备案材料
3. 备案通常需要 7-20 个工作日

### 6.2 域名解析

在阿里云域名控制台添加 A 记录：

| 记录类型 | 主机记录 | 记录值 |
|---------|---------|--------|
| A | @ | 你的ECS公网IP |
| A | www | 你的ECS公网IP |
| A | api | 你的ECS公网IP |

### 6.3 申请 SSL 证书

1. 登录 [阿里云 SSL 证书服务](https://yundun.console.aliyun.com/?p=cas)
2. 选择「免费证书」-> 创建证书
3. 填写域名，选择 DNS 验证
4. 按提示添加 TXT 解析记录完成验证
5. 下载 Nginx 格式证书

### 6.4 配置 Nginx HTTPS

将证书文件上传到服务器：

```bash
sudo mkdir -p /etc/nginx/ssl
scp 你的域名.pem root@服务器IP:/etc/nginx/ssl/
scp 你的域名.key root@服务器IP:/etc/nginx/ssl/
```

修改 Nginx 配置，在 `memorial.conf` 中添加：

```nginx
# HTTP 跳转 HTTPS
server {
    listen 80;
    server_name 你的域名.com;
    return 301 https://$host$request_uri;
}

# HTTPS 主配置
server {
    listen 443 ssl;
    server_name 你的域名.com;

    ssl_certificate /etc/nginx/ssl/你的域名.pem;
    ssl_certificate_key /etc/nginx/ssl/你的域名.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # 管理后台前端
    location / {
        root /usr/share/nginx/html/admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /prod-api/ {
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header REMOTE-HOST $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_pass http://127.0.0.1:18080/;
    }

    # 文件访问代理
    location /profile/ {
        alias /home/ruoyi/uploadPath/;
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json;
    gzip_vary on;

    client_max_body_size 20m;
}
```

```bash
sudo nginx -t
sudo systemctl restart nginx
```

---

## 七、常见问题

### Q1: 后端启动后无法连接数据库？

- 检查 MySQL 是否启动：`systemctl status mysqld`
- 检查数据库地址、端口、用户名、密码是否正确
- 如果使用 RDS，确保 ECS 和 RDS 在同一 VPC 下
- 检查安全组是否放行了 3306 端口（仅内网）

### Q2: 前端页面空白 / 接口 404？

- 确认后端已启动：`curl http://localhost:18080/`
- 检查 Nginx 配置中 `proxy_pass` 地址是否正确
- 检查 `VUE_APP_BASE_API` 是否和 Nginx location 匹配
- 查看 Nginx 错误日志：`tail -f /var/log/nginx/error.log`

### Q3: 小程序请求失败？

- 确认已配置 HTTPS（微信强制要求）
- 在微信公众平台配置了服务器域名白名单
- 小程序开发阶段可在开发者工具中勾选「不校验合法域名」进行调试
- 检查 `.env.production` 中的 API 地址是否正确

### Q4: 文件上传失败？

- 检查 `application.yml` 中 `ruoyi.profile` 路径是否存在且有写权限
- 检查 Nginx 的 `client_max_body_size` 配置
- 检查 Spring Boot 的 `max-file-size` 和 `max-request-size` 配置

### Q5: 如何查看后端日志？

```bash
# 实时查看
tail -f /home/ruoyi/logs/ruoyi-admin.jar.log

# 或使用 ry.sh
cd /home/ruoyi && ./ry.sh status
```

### Q6: 如何更新部署？

```bash
# 后端更新
cd 项目根目录
mvn clean package -DskipTests
scp ruoyi-admin/target/ruoyi-admin.jar root@服务器IP:/home/ruoyi/
ssh root@服务器IP "cd /home/ruoyi && ./ry.sh restart"

# 前端更新
cd ruoyi-ui
npm run build:prod
scp -r dist/* root@服务器IP:/usr/share/nginx/html/admin/

# 小程序更新
cd memorial-app
npm run build:mp-weixin
# 用微信开发者工具重新导入 dist/build/mp-weixin/ 并上传
```

---

## 部署架构总览

```
                    ┌─────────────────┐
                    │   阿里云 ECS     │
                    │                 │
  用户浏览器 ──────►│  Nginx :80/443  │
                    │   ├── /         │──► 管理后台静态文件
                    │   └── /prod-api │──► Spring Boot :18080
                    │                 │         │
  微信小程序 ──────►│  (HTTPS)        │         ├── MySQL :3306
                    │                 │         └── Redis :6379
                    └─────────────────┘
                           │
                    ┌──────┴──────┐
                    │ /home/ruoyi │
                    │   uploadPath│ ← 文件存储
                    └─────────────┘
```
