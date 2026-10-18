# E8 SCIM 开通

← [用户故事地图](../User-Story-Map.md)

## 背景

企业希望在 Okta / Microsoft Entra ID 中维护一次人员与组，再自动同步到
Explore IAM 和下游产品，而不是逐个系统手工建号、停用和删除。Explore IAM
作为身份中心：接收外部 IdP 的 SCIM 2.0 调用，再把用户推送到 Explore AI。

---

## US-18 外部 IdP 通过 SCIM 开通用户与组

**As a** IAM 管理员  
**I want** Okta / Entra ID 通过 `/scim/v2` 创建、更新、停用和删除 IAM 用户与组  
**So that** 人员变动只需在企业 IdP 中维护一次

### 验收标准

1. **Scenario** 授权的 SCIM Client 创建用户
   **GIVEN** 外部 IdP 持有 `scim-provisioner` 客户端的 `client_credentials` 令牌（`write:iam_scim`）  
   **WHEN** `POST /scim/v2/Users`（`application/scim+json`）  
   **THEN** 返回 201、`Location` 与弱 ETag `W/"<version>"`  
   **AND** 写入 `identity:CreateUser` 管理活动，Actor 为 `scim-provisioner`

2. **Scenario** 缺少 scope 被拒
   **GIVEN** 令牌不含 `write:iam_scim`  
   **WHEN** 调用任一 `/scim/v2` 端点  
   **THEN** 返回 403 SCIM Error（`application/scim+json`）

3. **Scenario** 并发修改冲突
   **GIVEN** 用户当前版本为 `W/"3"`  
   **WHEN** `PUT` 或 `PATCH` 携带 `If-Match: W/"2"`  
   **THEN** 返回 412，用户不变

4. **Scenario** 组成员同步
   **GIVEN** 组已存在  
   **WHEN** `PATCH /scim/v2/Groups/{id}` 增删 `members`  
   **THEN** 成员变更生效，并登记 `GroupMembershipChanged` 领域事件

### 状态

已实现

---

## US-19 IAM 推送用户到 Explore AI

**As a** IAM 管理员  
**I want** IAM 把（分配组内的）用户自动推送到 Explore AI  
**So that** 员工首次打开 Explore AI 前账号已就绪，离开分配组后立即停用

### 验收标准

1. **Scenario** 新用户被推送
   **GIVEN** Provisioning Target `explore-ai` 已启用，且用户属于分配组（或未配置分配组）  
   **WHEN** IAM 用户被创建或资料变更  
   **THEN** IAM 在事务提交后异步调用 Explore AI `/scim/v2/Users`（`write:ai_scim`）  
   **AND** `externalId` 为 IAM 用户 id，记录 Provisioned Account 与已同步版本

2. **Scenario** 重复事件幂等
   **GIVEN** Provisioned Account 已同步到当前版本  
   **WHEN** 同一事件被重新投递  
   **THEN** 不发起 SCIM 调用

3. **Scenario** 移出分配组
   **GIVEN** 用户已推送到 Explore AI  
   **WHEN** 用户被移出分配组或在 IAM 停用  
   **THEN** Explore AI 账号被设为 `active=false`，无法再登录

4. **Scenario** 目标暂时不可用
   **GIVEN** Explore AI 返回 5xx / 429 或网络错误  
   **WHEN** 重试耗尽  
   **THEN** 事件发布保持未完成，由定时重投或重启后重投补发

### 状态

已实现

---

## US-20 删除与停用即时生效

**As a** 合规审计员  
**I want** IdP 删除或停用用户后，IAM 不再签发令牌，下游数据同步删除  
**So that** 离职人员无法继续访问，个人数据按要求擦除

### 验收标准

1. **Scenario** 停用或删除后拒绝签发
   **GIVEN** 用户已被 SCIM 停用或删除  
   **WHEN** 使用既有会话或刷新令牌换取新令牌  
   **THEN** 既有 IAM 会话被结束，令牌端点返回 `invalid_grant`

2. **Scenario** 用户名复用不继承授权
   **GIVEN** 旧用户被改名或删除，新用户使用同一用户名  
   **WHEN** 旧用户的会话或刷新令牌被使用  
   **THEN** 按登录时绑定的 IAM 用户 id 解析用户，请求被拒绝

3. **Scenario** 删除传播到下游
   **GIVEN** 用户在 Explore AI 有 Provisioned Account（即使目标已暂停）  
   **WHEN** 用户在 IAM 被删除  
   **THEN** IAM 调用 Explore AI `DELETE /scim/v2/Users/{id}`，Explore AI 擦除该账号的数据

### 状态

已实现

---

## 手工验证

自动化测试之外，上线前用官方测试套件跑一遍 SCIM 端点：

- [Microsoft SCIM Validator](https://scimvalidator.microsoft.com/)：对 IAM `/scim/v2`（Users + Groups）使用 `scim-provisioner` 令牌。
- [Okta SCIM 2.0 测试](https://developer.okta.com/docs/guides/scim-provisioning-integration-prepare/main/)：Runscope / Okta CRUD 测试集。
- Explore AI `/scim/v2/Users` 只接受 IAM 的 `write:ai_scim` 令牌，可用同一套用例验证 Users 部分。

## 参考

- [RFC 7643 SCIM Core Schema](https://datatracker.ietf.org/doc/html/rfc7643)
- [RFC 7644 SCIM Protocol](https://datatracker.ietf.org/doc/html/rfc7644)
- [Microsoft Entra SCIM provisioning](https://learn.microsoft.com/en-us/entra/identity/app-provisioning/use-scim-to-provision-users-and-groups)
- [Spring Modulith event publication registry](https://docs.spring.io/spring-modulith/reference/events.html)
