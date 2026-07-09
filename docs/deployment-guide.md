# 云上纪念 - 部署指南

> 单机部署：后端 jar、MySQL、Redis、Nginx、两个前端静态产物全部跑在同一台阿里云 ECS 上。
> Nginx 作为唯一公网入口，承担两个职责：**① 同源反代解决 CORS；② 反代 OSS 内网 endpoint，文件查看/下载走内网、免公网流量费。**

---

## 目录

- [一、架构总览](#一架构总览)
- [二、服务器准备](#二服务器准备)
- [三、后端部署](#三后端部署)
- [四、前端构建与上传](#四前端构建与上传)
- [五、Nginx 配置（核心）](#五nginx-配置核心)
- [六、OSS 内网链路详解](#六oss-内网链路详解)
- [七、HTTPS 与域名](#七https-与域名)
- [八、微信小程序部署](#八微信小程序部署)
- [九、已知限制与注意事项](#九已知限制与注意事项)
- [十、常见问题](#十常见问题)
- [十一、更新部署流程](#十一更新部署流程)

---

## 一、架构总览

```
                         ┌──────────── 阿里云 ECS（单机）────────────┐
                         │                                            │
   用户浏览器 / 小程序 ──HTTPS──►  Nginx :443                            │
                         │            │                                 │
                         │            ├─ /          ► H5 静态产物        │
                         │            ├─ /api/      ► Spring Boot :18080 │
                         │            └─ /oss/      ► OSS 内网 endpoint  │
                         │            │                                 │
   管理后台浏览器 ──HTTP──►  Nginx :8080                                  │
                         │            ├─ /          ► admin 静态产物     │
                         │            └─ /prod-api/ ► Spring Boot :18080 │
                         │                                              │
                         │   Spring Boot :18080                          │
                         │      ├─ MySQL :3306  (同机, 127.0.0.1)         │
                         │      ├─ Redis :6379  (同机, 127.0.0.1)         │
                         │      └─ OSS      (internal endpoint, 走内网)  │
                         └──────────────────────────────────────────────┘
```

### 文件链路（上传 / 查看都走内网，免 OSS 公网流量费）

| 操作 | 链路 | 是否走公网 |
|------|------|-----------|
| **上传** | 前端 → nginx `/api/` → 后端 → OSS SDK（`oss-...-internal` 内网 endpoint）→ OSS | 否（后端到 OSS 走 ECS 内网）|
| **查看/下载** | 前端 → `https://你的域名/oss/xxx` → nginx `/oss/` → OSS 内网 endpoint → OSS | 否（nginx 到 OSS 走 ECS 内网）|

- 上传：后端用 `memorial.oss.endpoint`（**内网 endpoint**）建 OSSClient，文件从 ECS 走内网写入 OSS。
- 查看：后端返回给前端的文件 URL 用 `memorial.oss.url-prefix`（**指向 nginx 的 `https://你的域名/oss`**），前端访问该 URL 时由 nginx 反代到 OSS 内网 endpoint。
- CORS：前端页面、`/api`、`/prod-api`、`/oss` 全部同一个域名（同源），浏览器不会有跨域问题。

> 关键设计：`endpoint`（上传通道）与 `url-prefix`（对外 URL）是两个独立配置项。`endpoint` 保持内网省流量，`url-prefix` 指向 nginx 让外网可访问。

---

## 二、服务器准备

### 2.1 安全组放行端口

| 端口 | 用途 | 是否对公网开放 |
|------|------|---------------|
| 22 | SSH | 是（建议限源 IP）|
| 80 | HTTP（跳转 HTTPS）| 是 |
| 443 | HTTPS（H5 + OSS 反代入口）| 是 |
| 8080 | 管理后台 | 是（或限源 IP）|
| 18080 | 后端 Spring Boot | **否**，仅 nginx 内网反代 |
| 3306 | MySQL | **否**，仅本机 |
| 6379 | Redis | **否**，仅本机 |

### 2.2 安装基础环境

```bash
# JDK 17
sudo yum install -y java-17-openjdk java-17-openjdk-devel      # CentOS
# sudo apt update && sudo apt install -y openjdk-17-jdk         # Ubuntu
java -version

# Maven
cd /opt
sudo wget https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.tar.gz
sudo tar -xzf apache-maven-3.9.9-bin.tar.gz
sudo ln -s /opt/apache-maven-3.9.9 /opt/maven
cat >> ~/.bashrc << 'EOF'
export MAVEN_HOME=/opt/maven
export PATH=$MAVEN_HOME/bin:$PATH
EOF
source ~/.bashrc && mvn -version

# Node.js 18+（构建前端用，构建完可不常驻）
curl -fsSL https://rpm.nodesource.com/setup_18.x | sudo bash -
sudo yum install -y nodejs
node -v && npm -v

# Nginx
sudo yum install -y epel-release && sudo yum install -y nginx
sudo systemctl enable --now nginx
```

### 2.3 安装 MySQL 8

```bash
sudo rpm -Uvh https://dev.mysql.com/get/mysql80-community-release-el7-7.noarch.rpm
sudo yum install -y mysql-community-server
sudo systemctl enable --now mysqld
sudo grep 'temporary password' /var/log/mysqld.log        # 取临时密码
sudo mysql_secure_installation                            # 改密码
```

### 2.4 安装 Redis

```bash
sudo yum install -y redis
sudo vi /etc/redis.conf       # 设置 requirepass 你的Redis密码（生产必填）
sudo systemctl enable --now redis
```

> 全部组件与后端同机，后端连接用 `127.0.0.1`，不走公网。

---

## 三、后端部署

### 3.1 初始化数据库

```bash
mysql -u root -p
```

```sql
CREATE DATABASE memorial DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'admin'@'%' IDENTIFIED BY '你的数据库密码';
GRANT ALL PRIVILEGES ON memorial.* TO 'admin'@'%';
FLUSH PRIVILEGES;

USE memorial;
SOURCE /path/to/sql/ry_20250522.sql;
SOURCE /path/to/sql/quartz.sql;
SOURCE /path/to/sql/memorial_ddl.sql;
SOURCE /path/to/sql/memorial_menu_dict.sql;
SOURCE /path/to/sql/memorial_sample_data.sql;
SOURCE /path/to/sql/memorial_alter_v1_cemetery_monument.sql;
SOURCE /path/to/sql/memorial_alter_v2_qrcode_url.sql;
```

### 3.2 修改后端配置

#### 3.2.1 数据源 — `ruoyi-admin/src/main/resources/application-druid.yml`

```yaml
spring:
    datasource:
        druid:
            master:
                url: jdbc:mysql://127.0.0.1:3306/memorial?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8
                username: admin
                password: 你的数据库密码
```

#### 3.2.2 Redis、OSS、二维码 — `ruoyi-admin/src/main/resources/application.yml`

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1          # 同机部署用 127.0.0.1，避免走公网回环
      port: 6379
      database: 11
      password: 你的Redis密码   # 生产必填

memorial:
  qrcode:
    h5-base-url: https://你的域名.com   # 二维码扫码跳转的 H5 地址，生产必改
  oss:
    endpoint: https://oss-cn-beijing-internal.aliyuncs.com   # 内网 endpoint，后端上传用，保持不变
    bucket: memorials
    access-key-id: ${OSS_ACCESS_KEY_ID}          # 用环境变量注入，勿写死、勿提交
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}  # 用环境变量注入
    url-prefix: https://你的域名.com/oss          # ★ 改为 nginx 域名（前端查看用），不要用内网域名

token:
  secret: 你的随机密钥-至少32位字符                # 生产务必更换
  expireTime: 120
```

> **三个必改项**：
> 1. `memorial.oss.url-prefix` → `https://你的域名.com/oss`（原值是 OSS 内网域名，浏览器访问不到）
> 2. `memorial.oss.access-key-id/secret` → 用环境变量填回（你已清空）
> 3. `memorial.qrcode.h5-base-url` → H5 正式域名

#### 3.2.3 注入 OSS 密钥（环境变量）

```bash
# 写到 jar 启动用户的环境，或直接在启动脚本里 export
export OSS_ACCESS_KEY_ID=你的AK
export OSS_ACCESS_KEY_SECRET=你的SK
```

若用 systemd 托管（见 3.5），在 unit 文件的 `[Service]` 段加 `Environment=`。

> 安全：阿里云 RAM 用**仅 OSS 读写**的子账号 AK，不要用主账号 AK；密钥泄露立即在 RAM 控制台禁用重置。

### 3.3 构建后端 JAR

项目根目录：

```bash
mvn -pl ruoyi-admin -am clean package -DskipTests
# 产物：ruoyi-admin/target/ruoyi-admin.jar
```

### 3.4 上传并启动

```bash
sudo mkdir -p /home/ruoyi/logs
scp ruoyi-admin/target/ruoyi-admin.jar root@服务器IP:/home/ruoyi/
scp ry.sh root@服务器IP:/home/ruoyi/

ssh root@服务器IP
cd /home/ruoyi && chmod +x ry.sh
export OSS_ACCESS_KEY_ID=你的AK
export OSS_ACCESS_KEY_SECRET=你的SK
./ry.sh start
tail -f logs/ruoyi-admin.jar.log
curl http://localhost:18080/      # 验证（仅本机可达）
```

> `ry.sh` 以当前目录为 `APP_HOME`，**必须 `cd` 到 jar 所在目录再执行**。

### 3.5（推荐）Systemd 托管

`/etc/systemd/system/ruoyi.service`：

```ini
[Unit]
Description=RuoYi Memorial Backend
After=network.target mysqld.service redis.service

[Service]
Type=forking
User=root
WorkingDirectory=/home/ruoyi
Environment=OSS_ACCESS_KEY_ID=你的AK
Environment=OSS_ACCESS_KEY_SECRET=你的SK
ExecStart=/home/ruoyi/ry.sh start
ExecStop=/home/ruoyi/ry.sh stop
ExecReload=/home/ruoyi/ry.sh restart
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now ruoyi
```

---

## 四、前端构建与上传

### 4.1 H5（memorial-app）

```bash
cd memorial-app
npm install
npm run build:h5
# 产物：memorial-app/dist/build/h5/
```

生产环境配置 `memorial-app/.env.production`（**仅小程序用**，H5 走同源不读此值）：

```properties
VITE_API_BASE_URL=https://你的域名.com
```

> H5 的 `request.ts` 用条件编译：H5 端 `BASE_URL` 恒为空，所有请求走同源 `/api/...`，由 nginx 反代后端，天然无 CORS。

上传到服务器：

```bash
sudo mkdir -p /usr/share/nginx/html/h5
scp -r memorial-app/dist/build/h5/* root@服务器IP:/usr/share/nginx/html/h5/
```

### 4.2 管理后台（ruoyi-ui）

```bash
cd ruoyi-ui
npm install
npm run build:prod
# 产物：ruoyi-ui/dist/
```

`.env.production` 默认即可（`VUE_APP_BASE_API = '/prod-api'`）。

上传到服务器：

```bash
sudo mkdir -p /usr/share/nginx/html/admin
scp -r ruoyi-ui/dist/* root@服务器IP:/usr/share/nginx/html/admin/
```

---

## 五、Nginx 配置（核心）

### 5.1 路由规则总览

| server | location | 目标 | proxy_pass 尾斜杠 | 说明 |
|--------|----------|------|------------------|------|
| H5 `:80/443` | `/` | H5 静态产物 | — | hash 路由 |
| H5 `:80/443` | `/api/` | `127.0.0.1:18080` | **不带**（保留 `/api` 前缀）| 后端 `@RequestMapping("/api/...")` |
| H5 `:80/443` | `/oss/` | OSS 内网 endpoint | **带**（去掉 `/oss` 前缀）| 文件查看/下载，走内网 |
| admin `:8080` | `/` | admin 静态产物 | — | history 路由，需 `try_files` |
| admin `:8080` | `/prod-api/` | `127.0.0.1:18080` | **带**（去掉 `/prod-api` 前缀）| 后端 `/system/...`、`/memorial/...` |
| admin `:8080` | `/oss/` | OSS 内网 endpoint | **带** | 与 H5 一致，保持 server 自洽 |

> **proxy_pass 尾斜杠极易踩坑**：
> - `/api/` → `proxy_pass http://127.0.0.1:18080;`（**不带 /**）：`/api/family/upload` 原样转发给后端。若误带 `/`，`/api` 前缀会被去掉，后端 404。
> - `/prod-api/` → `proxy_pass http://127.0.0.1:18080/;`（**带 /**）：`/prod-api/system/user` → 后端 `/system/user`。
> - `/oss/` → `proxy_pass https://memorials.oss-...internal.aliyuncs.com/;`（**带 /**）：`/oss/memorial/x.jpg` → OSS `/memorial/x.jpg`。

### 5.2 完整配置（按场景选）

> 暂无域名用 **5.2.1 IP 版**（HTTP）；申请到域名后用 **5.2.2 域名版**（HTTPS）。两套之间切换只改 nginx 配置 + `url-prefix`/`h5-base-url` + 旧数据 SQL，业务代码不动。

#### 5.2.1 无域名（IP）版 —— 暂无域名时用这套

以服务器 IP `8.140.249.192` 为例（换成你的实际公网 IP）。纯 HTTP，无需 SSL 证书。

> ⚠️ `url-prefix`/`h5-base-url` 必须用**公网 IP**，不能用 `127.0.0.1`：前端在浏览器/手机上，访问 `127.0.0.1` 会指向用户自己的设备，图片打不开、扫码跳转失败。只有 nginx `proxy_pass` 到后端那一步用 `127.0.0.1:18080` 是对的（nginx 与后端同机）。

`/etc/nginx/conf.d/memorial.conf`：

```nginx
# =========================================================
# H5 前端（HTTP 80，浏览器主入口）
# =========================================================
server {
    listen 80;
    server_name _;                    # 匹配 IP 访问
    client_max_body_size 100m;        # 上传上限，需 ≥ 后端 multipart 限制

    # ① H5 静态产物（hash 路由）
    location / {
        root /usr/share/nginx/html/h5;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # ② H5 / 小程序 后端 API（保留 /api 前缀，proxy_pass 不带尾斜杠）
    location /api/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_set_header Host              $http_host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    }

    # ③ OSS 文件查看/下载（nginx 走内网 endpoint，免公网流量费）
    location /oss/ {
        proxy_pass https://memorials.oss-cn-beijing-internal.aliyuncs.com/;
        proxy_set_header Host              memorials.oss-cn-beijing-internal.aliyuncs.com;  # 必须，OSS 按 Host 路由 bucket
        proxy_ssl_server_name on;          # SNI，否则 OSS 可能拒绝握手
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json image/svg+xml;
    gzip_vary on;
}

# =========================================================
# 管理后台（HTTP 8080）
# =========================================================
server {
    listen 8080;
    server_name _;
    client_max_body_size 100m;

    # ① admin 静态产物（history 路由，必须 try_files）
    location / {
        root /usr/share/nginx/html/admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # ② 管理后台后端 API（去掉 /prod-api 前缀，proxy_pass 带尾斜杠）
    location /prod-api/ {
        proxy_pass http://127.0.0.1:18080/;
        proxy_set_header Host              $http_host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    }

    # ③ OSS 文件查看（与 H5 一致）
    location /oss/ {
        proxy_pass https://memorials.oss-cn-beijing-internal.aliyuncs.com/;
        proxy_set_header Host              memorials.oss-cn-beijing-internal.aliyuncs.com;
        proxy_ssl_server_name on;
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json image/svg+xml;
    gzip_vary on;
}
```

配套后端配置（`application.yml`，与 3.2.2 一致，这里给出 IP 版取值）：

```yaml
memorial:
  qrcode:
    h5-base-url: http://8.140.249.192        # 公网 IP，HTTP
  oss:
    endpoint: https://oss-cn-beijing-internal.aliyuncs.com   # 不变，上传走内网
    url-prefix: http://8.140.249.192/oss       # 指向 nginx 的 IP 入口，HTTP
```

- 安全组放行 `80`、`8080`；`18080` 不对公网。
- 旧数据迁移 SQL 把前缀替换为 `http://8.140.249.192/oss`（模板见 6.3）。
- 小程序无法用此方案（强制 HTTPS + 备案域名），仅能在开发者工具勾「不校验合法域名」内测。
- 限制：全程 HTTP，浏览器标"不安全"、微信内打开 H5 受限、不能用微信 JS-SDK。切域名步骤见 7.3。

#### 5.2.2 有域名版（HTTPS） —— 申请到域名后用

`/etc/nginx/conf.d/memorial.conf`：

```nginx
# =========================================================
# H5 前端（HTTPS 443，小程序/浏览器主入口）
# =========================================================
server {
    listen 443 ssl;
    server_name 你的域名.com;

    ssl_certificate     /etc/nginx/ssl/你的域名.pem;
    ssl_certificate_key /etc/nginx/ssl/你的域名.key;
    ssl_protocols       TLSv1.2 TLSv1.3;
    ssl_ciphers         HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    client_max_body_size 100m;     # 上传文件上限，需 ≥ 后端 multipart 限制

    # ① H5 静态产物（hash 路由，try_files 兜底无副作用）
    location / {
        root /usr/share/nginx/html/h5;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # ② H5 / 小程序 后端 API（保留 /api 前缀，proxy_pass 不带尾斜杠）
    location /api/ {
        proxy_pass http://127.0.0.1:18080;
        proxy_set_header Host              $http_host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # ③ OSS 文件查看/下载（nginx 走内网 endpoint，免公网流量费）
    location /oss/ {
        proxy_pass https://memorials.oss-cn-beijing-internal.aliyuncs.com/;
        proxy_set_header Host              memorials.oss-cn-beijing-internal.aliyuncs.com;  # 必须，OSS 按 Host 路由 bucket
        proxy_ssl_server_name on;          # SNI，否则 OSS 可能拒绝握手
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json image/svg+xml;
    gzip_vary on;
}

# HTTP 80 跳转 HTTPS
server {
    listen 80;
    server_name 你的域名.com;
    return 301 https://$host$request_uri;
}

# =========================================================
# 管理后台（HTTP 8080；如需 HTTPS 可参照上面 443 写法）
# =========================================================
server {
    listen 8080;
    server_name _;

    client_max_body_size 100m;

    # ① admin 静态产物（history 路由，必须 try_files）
    location / {
        root /usr/share/nginx/html/admin;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # ② 管理后台后端 API（去掉 /prod-api 前缀，proxy_pass 带尾斜杠）
    location /prod-api/ {
        proxy_pass http://127.0.0.1:18080/;
        proxy_set_header Host              $http_host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # ③ OSS 文件查看（与 H5 一致）
    location /oss/ {
        proxy_pass https://memorials.oss-cn-beijing-internal.aliyuncs.com/;
        proxy_set_header Host              memorials.oss-cn-beijing-internal.aliyuncs.com;
        proxy_ssl_server_name on;
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain application/javascript application/x-javascript text/css application/xml text/javascript application/json image/svg+xml;
    gzip_vary on;
}
```

### 5.3 检查并生效

```bash
sudo nginx -t
sudo systemctl reload nginx
```

### 5.4 验证

```bash
# H5 页面
curl -I https://你的域名.com/
# H5 API（应返回 JSON，不是 404）
curl https://你的域名.com/api/family/list
# OSS 反代（任取一个已上传文件的 objectKey，应返回图片字节 / 200）
curl -I https://你的域名.com/oss/memorial/<userId>/album/<uuid>.jpg
# 管理后台
curl -I http://你的域名.com:8080/
```

> `/oss/` 反代要求 nginx 所在 ECS 与 OSS 同地域（这里是北京），internal endpoint 仅 ECS 内网可达。本地开发机访问 OSS 请用公网 endpoint，或直接用 OSS 绑定域名。

---

## 六、OSS 内网链路详解

### 6.1 上传链路（后端 → OSS，走内网）

```
前端 POST /api/family/upload
  → nginx /api/ → 后端 :18080
  → OssService.upload() 用 memorial.oss.endpoint（internal）建 OSSClient
  → putObject 到 bucket=memorials（ECS 内网写入，不走公网）
  → 返回 url = memorial.oss.url-prefix + "/" + objectKey
     = https://你的域名.com/oss/memorial/<userId>/album/<uuid>.jpg
```

- `endpoint` = `https://oss-cn-beijing-internal.aliyuncs.com`：**保持内网**，上传不走公网。
- 上传接口（均返回完整 OSS URL，前端直接存库 / 直接 `<image :src>`）：
  - `POST /api/family/upload`（封面）、`/api/family/album/upload`（相册）、`/api/family/video/upload`（视频）
  - `POST /memorial/deceased/album/upload`（管理后台相册）
  - 二维码 PNG 由 `QrCodeUtil` 后端生成后 `uploadBytes` 上传

### 6.2 查看/下载链路（nginx → OSS，走内网）

```
前端 <image :src="https://你的域名.com/oss/memorial/.../x.jpg">
  → nginx /oss/ → proxy_pass（去 /oss 前缀）→ https://memorials.oss-...-internal.aliyuncs.com/memorial/.../x.jpg
  → ECS 内网回源 OSS，返回图片给前端
```

- `url-prefix` = `https://你的域名.com/oss`：**指向 nginx**，前端访问的是你的域名，由 nginx 在内网回源 OSS。
- 前端（H5 / 小程序 / 管理后台）拿到 URL 后直接用，**不拼任何前缀**（已是完整 https 地址）。

### 6.3 旧数据迁移（重要）

历史数据库里可能存了旧的 OSS 内网域名 URL（`https://memorials.oss-cn-beijing-internal.aliyuncs.com/...`），公网打不开。切到 nginx 反代后，需把旧 URL 批量替换成新前缀：

```sql
-- 通用模板：对所有存储了 OSS URL 的表/字段执行（以实际表名为准）
UPDATE 表名
SET 字段 = REPLACE(字段,
                   'https://memorials.oss-cn-beijing-internal.aliyuncs.com',
                   'https://你的域名.com/oss')
WHERE 字段 LIKE 'https://memorials.oss-cn-beijing-internal.aliyuncs.com%';
```

典型需迁移的字段：`coverImage`、`cemeteryPhoto`、`imageUrl`、`videoUrl`、`coverUrl`、`qrcodeUrl`、`avatar`（涉及 deceased、相册、视频、二维码、用户等表，按实际表结构逐表执行）。

> 若库里本就没有旧数据（首次部署），可跳过本步。

### 6.4 CORS 说明

切到 nginx 反代后，前端页面、API、图片全部同源（`https://你的域名.com`），**浏览器侧不存在 CORS**。OSS 桶的 CORS 配置无需开放给前端域名（因为前端不直连 OSS）。

---

## 七、HTTPS 与域名

### 7.1 域名解析

阿里云域名控制台添加 A 记录：

| 记录类型 | 主机记录 | 记录值 |
|---------|---------|--------|
| A | @ | ECS 公网 IP |
| A | www | ECS 公网 IP |

### 7.2 备案与 SSL 证书

- 阿里云服务器使用的域名须完成 [ICP 备案](https://beian.aliyun.com/)。
- [SSL 证书服务](https://yundun.console.aliyun.com/?p=cas) → 免费证书 → DNS 验证 → 下载 Nginx 格式。

```bash
sudo mkdir -p /etc/nginx/ssl
scp 你的域名.pem root@服务器IP:/etc/nginx/ssl/
scp 你的域名.key root@服务器IP:/etc/nginx/ssl/
sudo nginx -t && sudo systemctl reload nginx
```

> 小程序强制 HTTPS，H5 域名必须上 443。管理后台 8080 可保持 HTTP，或用同证书改 443。

### 7.3 无域名临时方案（IP:端口）

域名/备案未就绪时，H5 + 管理后台可先用服务器公网 IP 跑通（纯 HTTP）。**完整配置（nginx + 后端 yml + 安全组 + 限制）见 5.2.1**，本节只补充从 IP 切到域名时要做的步骤。

> 微信小程序无法用 IP 方案（强制 HTTPS + 备案域名），仅能在开发者工具勾「不校验合法域名」内测。

**后续切域名**（架构不变，业务代码不动）：

1. **nginx**：用 5.2.2 的域名版配置替换 5.2.1 的 IP 版（加 443 + SSL 证书，80 跳转 443）。
2. **`application.yml`**：`h5-base-url` 改 `https://你的域名.com`，`url-prefix` 改 `https://你的域名.com/oss`。
3. **`memorial-app/.env.production`**：`VITE_API_BASE_URL=https://你的域名.com`（小程序用）。
4. **旧数据 SQL**：把库里的 `http://8.140.249.192/oss` 再替换成 `https://你的域名.com/oss`（模板见 6.3）。
5. **安全组**：放行 `443`（`80` 保留做跳转）。
6. **微信公众平台**：配置服务器域名白名单（见 8.3）。

---

## 八、微信小程序部署

### 8.1 配置 AppID

`memorial-app/src/manifest.json` 填入 `mp-weixin.appid`（当前为空，发布前必填）。

### 8.2 生产 API 地址

`memorial-app/.env.production`：

```properties
VITE_API_BASE_URL=https://你的域名.com
```

> 小程序端 `request.ts` 用此值作 BASE_URL（H5 端被条件编译短路，不读此值）。`/api` 前缀在代码里拼接。

### 8.3 服务器域名白名单

微信公众平台 → 开发管理 → 开发设置 → 服务器域名：

| 域名类型 | 填写 |
|----------|------|
| request 合法域名 | `https://你的域名.com` |
| uploadFile 合法域名 | `https://你的域名.com` |
| downloadFile 合法域名 | `https://你的域名.com` |

> 小程序里的图片展示走 `https://你的域名.com/oss/...`，已包含在 request / downloadFile 域名内。

### 8.4 构建与上传

```bash
cd memorial-app
npm run build:mp-weixin
# 产物：memorial-app/dist/build/mp-weixin/
```

用微信开发者工具导入 `dist/build/mp-weixin/`，填 AppID，预览 → 上传 → 公众平台提交审核。

---

## 九、已知限制与注意事项

1. **RuoYi 原生本地文件功能已失效**：`ruoyi.profile` 被改成占位字符串 `'使用阿里云OSS存储:com.ruoyi.memorial.config.OssProperties'`（仅说明用，框架不解析）。导致：
   - `/common/upload`、`/common/download`、`/profile/**` 静态资源映射、管理后台个人头像上传（`/system/user/profile/avatar`）均不可用。
   - **纪念馆业务不受影响**（业务上传已全部走 `OssService` + 自定义接口）。
   - 管理后台通用 `ImageUpload` 组件默认 `action=/common/upload` 若被用到会失败；纪念馆表单已改用 `/memorial/deceased/album/upload`。
   - 若需恢复头像/通用上传，应将其接入 `OssService`，而非改回本地路径。

2. **大文件上传限制**：后端 `application.yml` 当前 `max-file-size: 10MB`、`max-request-size: 20MB`，nginx `client_max_body_size 100m`。若上传大视频，需同步调大后端这两项与 nginx 限制。

3. **OSS 地域一致**：nginx `/oss/` 反代内网 endpoint，要求 ECS 与 OSS 同地域（北京）。跨地域只能用公网 endpoint（会产生流量费）。

4. **AK/SK 安全**：切勿写死在 yml 或提交 git，用环境变量注入；RAM 用最小权限子账号。

---

## 十、常见问题

**Q1 后端启动后接口 404 / 前端白屏？**
- 确认后端在跑：`curl http://localhost:18080/`（本机）。
- 检查 nginx `proxy_pass` 尾斜杠：`/api/` 不带 `/`、`/prod-api/` 带 `/`（见 5.1）。
- 看日志：`tail -f /var/log/nginx/error.log`、`tail -f /home/ruoyi/logs/ruoyi-admin.jar.log`。

**Q2 图片显示不出来（403 / 无法访问）？**
- 确认 `memorial.oss.url-prefix` 已改为 `https://你的域名.com/oss`（不是内网域名）。
- 确认 nginx `/oss/` 的 `proxy_set_header Host` 设为 OSS 内网域名（OSS 按 Host 路由 bucket）。
- 确认 `proxy_ssl_server_name on`。
- 旧数据未迁移：执行 6.3 的 SQL 替换。

**Q3 上传报错？**
- 检查 `OSS_ACCESS_KEY_ID/SECRET` 环境变量是否注入到后端进程。
- 文件过大：调大后端 `max-file-size` / `max-request-size` 和 nginx `client_max_body_size`。
- AK 权限：RAM 子账号需有 `oss:PutObject` 权限。

**Q4 小程序请求失败？**
- 必须 HTTPS，且域名在白名单（request / uploadFile / downloadFile）。
- 开发阶段可在开发者工具勾选「不校验合法域名」。

**Q5 数据库 / Redis 连不上？**
- 同机部署用 `127.0.0.1`，不要填公网 IP 走回环。
- 确认 `systemctl status mysqld redis`。

**Q6 如何查看后端日志？**
```bash
tail -f /home/ruoyi/logs/ruoyi-admin.jar.log
cd /home/ruoyi && ./ry.sh status
```

---

## 十一、更新部署流程

```bash
# —— 后端 ——
mvn -pl ruoyi-admin -am clean package -DskipTests
scp ruoyi-admin/target/ruoyi-admin.jar root@服务器IP:/home/ruoyi/
ssh root@服务器IP "cd /home/ruoyi && ./ry.sh restart"
# 或 systemd: ssh root@服务器IP "sudo systemctl restart ruoyi"

# —— H5 ——
cd memorial-app && npm run build:h5
scp -r dist/build/h5/* root@服务器IP:/usr/share/nginx/html/h5/

# —— 管理后台 ——
cd ../ruoyi-ui && npm run build:prod
scp -r dist/* root@服务器IP:/usr/share/nginx/html/admin/

# —— 小程序 ——
cd ../memorial-app && npm run build:mp-weixin
# 微信开发者工具重新导入 dist/build/mp-weixin/ 上传
```

### 开发调试（可选，非生产）

开发阶段用 `./dev.sh -d` 一键起三个 dev server（后端 18080、H5 5173、管理后台 8008），各自 dev server 自带 proxy 已解决 CORS，**无需 nginx**。仅在需要统一公网端口或联调时才前置 nginx 反代到 dev server（参考生产配置把 `proxy_pass` 目标改成 5173 / 8008）。正式上线用本文档的构建产物 + nginx 方案。
