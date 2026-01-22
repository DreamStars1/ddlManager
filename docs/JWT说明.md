# JWT 认证模块文档
## 一、模块概述
该模块基于 Spring Security 实现 JWT（JSON Web Token）认证机制，用于替代传统的会话认证，实现无状态的身份验证，适用于前后端分离、分布式系统等场景。核心包含 Token 生成、解析、验证以及请求拦截过滤等功能。

## 二、核心类说明
### 1. JwtTokenUtil（Token 工具类）
**路径**：`config/JwtTokenUtil.java`
**作用**：提供 JWT Token 的生成、解析、有效性验证等核心方法。

| 方法名 | 功能描述 | 参数说明 | 返回值 |
|--------|----------|----------|--------|
| `generateToken(UserDetails userDetails)` | 生成 JWT Token | `userDetails`：Spring Security 用户详情对象，包含用户名、权限等信息 | 生成的 JWT 字符串 |
| `getUsernameFromToken(String token)` | 从 Token 中解析用户名 | `token`：JWT 字符串 | 用户名（String） |
| `validateToken(String token, UserDetails userDetails)` | 验证 Token 有效性 | `token`：JWT 字符串；`userDetails`：用户详情对象 | 布尔值（true：有效，false：无效） |
| `isTokenExpired(String token)` | （私有）检查 Token 是否过期 | `token`：JWT 字符串 | 布尔值（true：已过期，false：未过期） |

**核心配置项**（需在 `application.yml` 中配置）：
```yaml
jwt:
  secret: your-secret-key # JWT 签名密钥（建议使用随机长字符串，避免泄露）
  expiration: 7200000     # Token 过期时间（单位：毫秒，示例为2小时）
```

### 2. JwtAuthenticationFilter（JWT 认证过滤器）
**路径**：`config/JwtAuthenticationFilter.java`
**作用**：继承 `OncePerRequestFilter`，拦截所有请求，提取并验证 JWT Token，完成用户认证上下文的填充。

**核心逻辑**：
1. 从请求头 `Authorization` 中提取 Token（格式：`Bearer {token}`）；
2. 解析 Token 中的用户名，若用户未被认证则加载用户详情；
3. 验证 Token 有效性，通过后将认证信息存入 `SecurityContextHolder`；
4. 放行请求，继续执行过滤器链。

**关键注解与注入**：
- `@Component`：使过滤器被 Spring 扫描并托管；
- 构造函数通过 `@Autowired` 注入 `JwtTokenUtil` 和 `UserDetailsService` 依赖。

## 三、集成配置（SecurityConfig）
**路径**：`config/SecurityConfig.java`
JWT 模块需与 Spring Security 核心配置集成，关键配置如下：

```java
// 禁用传统表单登录、HTTP Basic 认证
.formLogin().disable()
.httpBasic().disable()
// 配置会话为无状态（JWT 无需会话）
.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
// 将 JWT 过滤器添加到用户名密码过滤器之前
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
```

**核心配置说明**：
- `SessionCreationPolicy.STATELESS`：禁用 HttpSession，完全基于 Token 认证；
- 过滤器执行顺序：JWT 过滤器优先于默认的用户名密码过滤器，确保所有请求先经过 Token 验证。

## 四、使用流程
### 1. 生成 Token
用户登录成功后，通过 `JwtTokenUtil` 生成 Token 并返回给前端：
```java
@Autowired
private JwtTokenUtil jwtTokenUtil;

@Autowired
private UserDetailsService userDetailsService;

// 登录接口示例
public String login(String username, String password) {
    // 1. 验证用户名密码（省略）
    // 2. 加载用户详情
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    // 3. 生成 Token
    String token = jwtTokenUtil.generateToken(userDetails);
    // 4. 返回 Token 给前端
    return token;
}
```

### 2. 前端携带 Token
前端请求时，在 `Authorization` 请求头中携带 Token：
```http
GET /api/resource HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxMDIz...
```

### 3. 后端验证 Token
`JwtAuthenticationFilter` 自动拦截请求，验证 Token 有效性：
- 若 Token 无效（过期、签名错误、用户名不匹配），则认证失败，请求被拒绝；
- 若 Token 有效，自动完成用户认证，后续接口可通过 `SecurityContextHolder` 获取当前用户信息。

## 五、注意事项
1. **密钥安全**：`jwt.secret` 需使用高强度随机字符串，避免硬编码，生产环境建议通过环境变量注入；
2. **过期时间**：根据业务场景合理设置 `jwt.expiration`，过短影响用户体验，过长增加安全风险；
3. **权限控制**：结合 Spring Security 方法级权限注解（如 `@PreAuthorize("hasRole('ADMIN')")`），实现基于 Token 的细粒度权限控制。
