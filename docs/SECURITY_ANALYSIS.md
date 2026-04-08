# 安全分析文档

## 安全漏洞分析

### 1. 严重安全漏洞

#### 1.1 SQL注入漏洞 (高危)

**漏洞位置**: 
- `ReportService.runReport(String sql)`
- `ReportService.generateReport(Long reportId, String params)`

**漏洞代码**:
```java
// 直接执行用户输入的SQL - 严重安全漏洞
public List<Map<String, Object>> runReport(String sql) {
    return reportDao.executeSql(sql);
}

// 直接拼接SQL参数 - SQL注入风险
if (params != null && !params.isEmpty()) {
    sql = sql + " WHERE " + params;
}
```

**攻击示例**:
```sql
-- 恶意SQL注入
SELECT * FROM users; DROP TABLE reports; --
SELECT * FROM reports WHERE 1=1; UNION SELECT username, password, null FROM users; --
```

**风险等级**: 🔴 高危

**影响范围**:
- 数据库数据泄露
- 数据库被删除或篡改
- 系统完全被控制

#### 1.2 参数拼接漏洞 (高危)

**漏洞位置**: `ReportService.generateReport()`

**漏洞代码**:
```java
// 没有预处理，直接拼接参数（SQL注入风险）
if (params != null && !params.isEmpty()) {
    sql = sql + " WHERE " + params;
}
```

**攻击示例**:
```
params = "1=1; DROP TABLE users; --"
```

**风险等级**: 🔴 高危

#### 1.3 缺乏输入验证 (中危)

**漏洞位置**: 多个接口

**问题描述**:
- 没有对SQL内容进行安全检查
- 缺乏参数长度限制
- 没有特殊字符过滤

**风险等级**: 🟡 中危

### 2. 认证和授权问题

#### 2.1 JWT Token安全性

**当前实现分析**:
```java
// JwtTokenProvider.java
public String generateToken(User user) {
    // Token生成逻辑
}
```

**潜在问题**:
- Token过期时间可能过长
- 没有Token黑名单机制
- 缺乏Token刷新安全控制

**风险等级**: 🟡 中危

#### 2.2 权限控制不完善

**问题分析**:
- 部分接口缺乏权限验证
- 数据权限控制不够细粒度
- 缺乏操作频率限制

**风险等级**: 🟡 中危

### 3. 数据安全问题

#### 3.1 敏感数据存储

**密码存储**:
```java
// UserInitializer.java
// 需要确认密码是否正确加密
```

**问题**:
- 密码存储方式需要验证
- 缺乏密码复杂度要求
- 没有密码过期机制

#### 3.2 数据传输安全

**问题**:
- HTTP传输可能未加密
- 缺乏HTTPS强制使用
- 敏感数据明文传输

**风险等级**: 🟡 中危

### 4. 业务逻辑安全

#### 4.1 权限绕过风险

**潜在问题**:
- MAKER可能访问其他用户的报表
- CHECKER可能审批自己的报表
- 角色验证不够严格

#### 4.2 状态篡改风险

**问题**:
- 报表状态可能被恶意修改
- 缺乏状态变更的审计验证
- 并发操作可能导致状态不一致

## 安全加固建议

### 1. SQL注入防护

#### 1.1 使用预编译语句

**推荐实现**:
```java
@Repository
public class SafeReportDao {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public List<Map<String, Object>> executeReportSql(Long reportId, Map<String, Object> params) {
        // 从数据库获取预定义的SQL
        String sql = getReportSqlById(reportId);
        
        // 使用预编译语句
        return jdbcTemplate.queryForList(sql, params);
    }
    
    private String getReportSqlById(Long reportId) {
        // 从配置表获取SQL，确保SQL是预定义的
        return reportRepository.findSqlById(reportId);
    }
}
```

#### 1.2 SQL白名单验证

**实现方案**:
```java
@Service
public class SqlValidationService {
    
    private static final Set<String> ALLOWED_KEYWORDS = Set.of(
        "SELECT", "FROM", "WHERE", "AND", "OR", "ORDER", "BY", 
        "GROUP", "HAVING", "LIMIT", "OFFSET"
    );
    
    public void validateSql(String sql) {
        String upperSql = sql.toUpperCase();
        
        // 检查危险关键字
        String[] dangerousKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE"};
        for (String keyword : dangerousKeywords) {
            if (upperSql.contains(keyword)) {
                throw new SecurityException("不允许的SQL关键字: " + keyword);
            }
        }
        
        // 检查SQL注入模式
        if (upperSql.contains("--") || upperSql.contains("/*") || upperSql.contains("*/")) {
            throw new SecurityException("检测到SQL注入尝试");
        }
    }
}
```

#### 1.3 参数化查询

**安全实现**:
```java
public Map<String, Object> generateReportSafely(Long reportId, Map<String, Object> params) {
    Report report = reportDao.findById(reportId);
    if (report == null) {
        throw new RuntimeException("报表不存在");
    }
    
    // 验证SQL安全性
    sqlValidationService.validateSql(report.getSql());
    
    // 使用参数化查询
    List<Map<String, Object>> data = jdbcTemplate.queryForList(report.getSql(), params);
    
    return Map.of(
        "reportName", report.getName(),
        "data", data,
        "count", data.size()
    );
}
```

### 2. 认证安全加固

#### 2.1 JWT Token安全

**改进实现**:
```java
@Component
public class SecureJwtTokenProvider {
    
    @Value("${jwt.secret:mySecretKey}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:3600}") // 1小时
    private int jwtExpiration;
    
    public String generateToken(User user) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpiration * 1000);
        
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
}
```

#### 2.2 Token黑名单机制

**实现方案**:
```java
@Service
public class TokenBlacklistService {
    
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();
    
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
    
    @Scheduled(fixedRate = 3600000) // 每小时清理
    public void cleanupExpiredTokens() {
        blacklistedTokens.removeIf(token -> {
            try {
                Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
                return claims.getExpiration().before(new Date());
            } catch (Exception e) {
                return true; // 无效token直接移除
            }
        });
    }
}
```

### 3. 权限控制加固

#### 3.1 方法级权限控制

**实现方案**:
```java
@Component
@Aspect
public class PermissionAspect {
    
    @Before("@annotation(requiresRole)")
    public void checkPermission(JoinPoint joinPoint, RequiresRole requiresRole) {
        User currentUser = getCurrentUser();
        
        if (!Arrays.asList(requiresRole.value()).contains(currentUser.getRole())) {
            throw new AccessDeniedException("权限不足");
        }
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String[] value();
}
```

#### 3.2 数据权限控制

**实现方案**:
```java
@Service
public class DataPermissionService {
    
    public boolean canAccessReport(User user, ReportRun reportRun) {
        if ("MAKER".equals(user.getRole())) {
            // MAKER只能访问自己的报表
            return user.getUsername().equals(reportRun.getMakerUsername());
        } else if ("CHECKER".equals(user.getRole())) {
            // CHECKER可以访问所有报表
            return true;
        }
        return false;
    }
    
    public boolean canApproveReport(User user, ReportRun reportRun) {
        if (!"CHECKER".equals(user.getRole())) {
            return false;
        }
        
        // CHECKER不能审批自己的报表
        return !user.getUsername().equals(reportRun.getMakerUsername());
    }
}
```

### 4. 输入验证加固

#### 4.1 参数验证

**实现方案**:
```java
@Component
public class InputValidationService {
    
    private static final int MAX_SQL_LENGTH = 1000;
    private static final Pattern SQL_INJECTION_PATTERN = 
        Pattern.compile("('|(\\-\\-)|(;)|(\\||\\|)|(\\*|\\*))");
    
    public void validateReportInput(Report report) {
        // 验证报表名称
        if (report.getName() == null || report.getName().trim().isEmpty()) {
            throw new ValidationException("报表名称不能为空");
        }
        
        if (report.getName().length() > 100) {
            throw new ValidationException("报表名称长度不能超过100字符");
        }
        
        // 验证SQL
        validateSql(report.getSql());
    }
    
    public void validateSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new ValidationException("SQL不能为空");
        }
        
        if (sql.length() > MAX_SQL_LENGTH) {
            throw new ValidationException("SQL长度不能超过" + MAX_SQL_LENGTH + "字符");
        }
        
        // 检查SQL注入模式
        if (SQL_INJECTION_PATTERN.matcher(sql).find()) {
            throw new ValidationException("检测到潜在的SQL注入");
        }
        
        // 检查危险关键字
        String upperSql = sql.toUpperCase();
        String[] dangerousKeywords = {"DROP", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "EXEC", "EXECUTE"};
        for (String keyword : dangerousKeywords) {
            if (upperSql.contains(keyword)) {
                throw new ValidationException("不允许的SQL关键字: " + keyword);
            }
        }
    }
}
```

#### 4.2 请求频率限制

**实现方案**:
```java
@Component
public class RateLimitService {
    
    private final Map<String, Map<String, Long>> requestCounts = new ConcurrentHashMap<>();
    
    @EventListener
    public void handleRequest(HttpServletRequest request) {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();
        
        Map<String, Long> endpointCounts = requestCounts.computeIfAbsent(clientIp, k -> new ConcurrentHashMap<>());
        
        Long count = endpointCounts.getOrDefault(endpoint, 0L);
        if (count > getMaxRequestsPerMinute(endpoint)) {
            throw new RateLimitExceededException("请求频率超限");
        }
        
        endpointCounts.put(endpoint, count + 1);
        
        // 定时清理计数器
        scheduleCleanup(clientIp, endpoint);
    }
    
    private int getMaxRequestsPerMinute(String endpoint) {
        if (endpoint.contains("/reports/run")) {
            return 10; // 执行报表限制更严格
        }
        return 100; // 其他接口
    }
}
```

### 5. 数据安全加固

#### 5.1 密码安全

**实现方案**:
```java
@Component
public class PasswordService {
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public String encodePassword(String rawPassword) {
        // 密码复杂度验证
        if (!isPasswordComplex(rawPassword)) {
            throw new ValidationException("密码复杂度不足");
        }
        
        return passwordEncoder.encode(rawPassword);
    }
    
    private boolean isPasswordComplex(String password) {
        // 至少8位，包含大小写字母、数字和特殊字符
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&
               password.matches(".*[a-z].*") &&
               password.matches(".*\\d.*") &&
               password.matches(".*[!@#$%^&*].*");
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

#### 5.2 敏感数据加密

**实现方案**:
```java
@Component
public class DataEncryptionService {
    
    private final AESUtil aesUtil;
    
    public String encryptSensitiveData(String data) {
        return aesUtil.encrypt(data);
    }
    
    public String decryptSensitiveData(String encryptedData) {
        return aesUtil.decrypt(encryptedData);
    }
    
    public void encryptReportParameters(ReportRun reportRun) {
        if (reportRun.getParametersJson() != null) {
            String encrypted = encryptSensitiveData(reportRun.getParametersJson());
            reportRun.setParametersJson(encrypted);
        }
    }
}
```

### 6. 审计和监控

#### 6.1 安全审计

**实现方案**:
```java
@Component
public class SecurityAuditService {
    
    @EventListener
    public void handleSecurityEvent(SecurityEvent event) {
        SecurityAuditLog auditLog = SecurityAuditLog.builder()
                .eventType(event.getType())
                .username(event.getUsername())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .resource(event.getResource())
                .action(event.getAction())
                .timestamp(LocalDateTime.now())
                .success(event.isSuccess())
                .errorMessage(event.getErrorMessage())
                .build();
        
        securityAuditLogRepository.save(auditLog);
        
        // 实时告警
        if (!event.isSuccess()) {
            sendSecurityAlert(event);
        }
    }
    
    private void sendSecurityAlert(SecurityEvent event) {
        // 发送安全告警邮件或短信
        if (isHighRiskEvent(event)) {
            notificationService.sendSecurityAlert(event);
        }
    }
}
```

#### 6.2 异常监控

**实现方案**:
```java
@ControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(SqlInjectionException.class)
    public ResponseEntity<String> handleSqlInjection(SqlInjectionException e) {
        // 记录安全事件
        securityAuditService.recordSecurityEvent(
            SecurityEvent.builder()
                .type("SQL_INJECTION_ATTEMPT")
                .username(getCurrentUser().getUsername())
                .ipAddress(getClientIp())
                .errorMessage(e.getMessage())
                .build()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("请求被拒绝");
    }
}
```

## 安全配置建议

### 1. 应用安全配置

```yaml
# application.yml
security:
  jwt:
    secret: ${JWT_SECRET:your-256-bit-secret}
    expiration: 3600 # 1小时
    refresh-expiration: 604800 # 7天
  
  rate-limit:
    enabled: true
    requests-per-minute: 100
    report-requests-per-minute: 10
  
  password:
    min-length: 8
    require-complexity: true
    expiry-days: 90
  
  audit:
    enabled: true
    log-all-requests: true
    retention-days: 365
```

### 2. 网络安全配置

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/test").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint(unauthorizedHandler())
            .and()
            .headers().frameOptions().deny()
            .and()
            .headers().contentTypeOptions().and()
            .headers().httpStrictTransportSecurity();
        
        return http.build();
    }
}
```

## 安全测试建议

### 1. 安全测试用例

```java
@SpringBootTest
@AutoConfigureTestDatabase
public class SecurityTests {
    
    @Test
    public void testSqlInjectionPrevention() {
        // 测试SQL注入防护
        String maliciousSql = "SELECT * FROM users; DROP TABLE reports; --";
        
        assertThrows(SecurityException.class, () -> {
            reportService.runReport(maliciousSql);
        });
    }
    
    @Test
    public void testUnauthorizedAccess() {
        // 测试未授权访问
        User maker = createMakerUser();
        ReportRun otherUserReport = createOtherUserReport();
        
        assertThrows(AccessDeniedException.class, () -> {
            reportRunService.submitRun(otherUserReport.getId());
        });
    }
    
    @Test
    public void testRateLimit() {
        // 测试频率限制
        for (int i = 0; i < 15; i++) {
            reportController.runReport(Map.of("sql", "SELECT 1"));
        }
        
        assertThrows(RateLimitExceededException.class, () -> {
            reportController.runReport(Map.of("sql", "SELECT 1"));
        });
    }
}
```

### 2. 渗透测试建议

1. **SQL注入测试**: 使用SQLMap等工具
2. **权限绕过测试**: 尝试访问未授权资源
3. **会话管理测试**: Token劫持和重放攻击
4. **输入验证测试**: 各种恶意输入测试
5. **频率限制测试**: 暴力破解和DoS攻击

## 总结

### 当前安全状况
- 🔴 存在严重SQL注入漏洞
- 🟡 认证机制需要加强
- 🟡 权限控制不够完善
- 🟡 缺乏安全审计和监控

### 优先修复建议
1. **立即修复SQL注入漏洞**
2. **加强输入验证和参数化查询**
3. **完善权限控制和数据访问限制**
4. **实施安全审计和监控**

### 长期安全规划
1. **定期安全评估和渗透测试**
2. **安全编码规范和培训**
3. **自动化安全扫描集成**
4. **安全事件响应机制建立**
