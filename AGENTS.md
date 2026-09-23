# AGENTS.md

本文件适用于仓库根目录及全部子目录，供在本项目中工作的自动化代理使用。

## 项目概览

AgileBoot Back End 是一个基于 Java 8、Spring Boot 2.7 和 Maven 的多模块后端项目，根包名为 `com.agileboot`。主要技术包括 Spring Security/JWT、MyBatis-Plus、MySQL、Redis、H2、JUnit 和 Mockito。

仓库模块职责如下：

- `agileboot-admin`：管理后台 Web 入口、Controller、安全与登录等管理端适配逻辑；启动类为 `AgileBootAdminApplication`。
- `agileboot-api`：面向外部客户端的 API 入口，目前依赖领域层和基础设施层。
- `agileboot-domain`：核心业务模块。业务按领域组织，并进一步划分为 `command`、`query`、`dto`、`model` 和 `db`。
- `agileboot-infrastructure`：框架配置及外部能力集成，包括 Web、MyBatis、数据源、缓存、国际化等；不要在这里堆放业务规则。
- `agileboot-common`：跨模块共享的基础类型、枚举、异常、注解和通用工具；只有真正通用的代码才应放入此模块。
- `sql`：数据库初始化或升级脚本。
- `docker`：容器相关配置。

模块依赖大体遵循：入口模块（`admin`/`api`）→ `domain` → `infrastructure` → `common`。修改时保持现有依赖方向，避免让底层模块反向依赖入口模块。

## 开发原则

- 先阅读同一业务包中的现有实现，再按既有结构扩展；优先保持局部一致性，不引入另一套分层或命名体系。
- Controller 负责协议适配、参数接收和响应组装，核心业务规则放在 `agileboot-domain` 的应用服务或领域模型中。
- 写操作参数使用 `*Command`，查询条件使用 `*Query`，返回模型使用 `*DTO`；数据库对象沿用 `Sys*Entity`、`Sys*Mapper`、`Sys*Service`/`Impl` 的现有约定。
- 使用项目已有的 `ResponseDTO`、`PageDTO`、`ApiException` 和集中式 `ErrorCode`，不要为单个接口创建新的响应包装或随意抛出裸 `RuntimeException`。
- 枚举值优先使用现有 `BasicEnum` 体系，避免散落的魔法数字和字符串。
- 数据访问优先遵循现有 MyBatis-Plus Service/Mapper 模式。新增或修改 XML 映射时，同步检查 Java Mapper、实体字段及查询对象。
- 保持 Java 8 兼容；不要使用 records、文本块、`var`、新版集合工厂扩展等高版本语言特性。
- 使用 Lombok 时沿用相邻代码做法，不要为了小改动大范围替换已有 getter/setter 或对象模型。
- 不提交真实口令、Token、私钥或生产连接信息。修改 `application-*.yml` 时使用安全占位值，并保留 `application-prod.yml` 不入库的约定。
- 不修改或提交 `target/`、日志、IDE 元数据等生成文件。

## 代码风格

- Java 格式以仓库根目录的 `GoogleStyle.xml` 和相邻源文件为准：4 空格缩进，不使用 Tab，类名使用 PascalCase，方法与字段使用 camelCase，常量使用 UPPER_SNAKE_CASE。
- 包名全小写，并按业务能力组织；不要创建含大写字母的新包。现有测试包 `integrationTest` 属历史命名，新增生产包不要仿照。
- 导入应明确且有序，避免通配符导入；删除未使用的导入和死代码。
- 方法保持聚焦。复杂校验和状态变化应以有业务含义的方法表达，而不是在 Controller 中堆叠条件分支。
- 注释解释原因、约束或非显然行为，不复述代码。公共接口或复杂领域行为可补充简洁 Javadoc。
- 不做与任务无关的全文件格式化、重命名或依赖升级，确保差异最小且可审查。

## 构建与运行

优先使用仓库自带 Maven Wrapper。在 Windows PowerShell 中：

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -pl agileboot-admin -am spring-boot:run
```

在 Unix 类环境中使用对应的 `./mvnw`。开发模式默认读取 `basic,dev` profile，需要本地 MySQL 与 Redis，连接配置位于 `agileboot-admin/src/main/resources/application-dev.yml`。

无需外部 MySQL/Redis 的本地启动可使用 `basic,test` 配置及内嵌组件。不要仅为本地运行而提交 profile 或连接信息变更；优先通过 IDE、命令行参数或未跟踪的本地配置覆盖。

根 POM 的 Surefire 默认配置会跳过部分模块测试，而 `agileboot-admin` 显式启用测试。因此，验证代码时不要把一次未执行任何测试的成功构建当作测试通过。

## 测试要求

- 测试放在对应模块的 `src/test/java` 下，包路径尽量与被测类一致，类名使用 `*Test`。
- 纯业务规则和工具类优先写快速单元测试；数据库、Spring 容器或多组件协作行为才使用集成测试。
- 项目同时存在 JUnit 4 和 JUnit 5 测试。扩展既有测试时沿用该文件当前版本；新测试优先使用 JUnit 5，且不要在同一个测试类中混用两套生命周期或断言风格。
- 领域集成测试使用 `agileboot-domain/src/test/resources/application.yml` 中的 `basic,test` profile 和 H2 初始化脚本。测试必须可重复执行，不依赖执行顺序或开发者机器上的持久数据。
- 涉及时间、网络、Redis、文件路径或静态状态的测试应隔离外部副作用；不要新增必须访问公网才能通过的测试。

常用验证命令：

```powershell
# 与 CI 接近的全仓验证
.\mvnw.cmd -B verify -Dmaven.test.failure.ignore=false -Dgpg.skip

# 明确启用测试，验证一个模块及其依赖
.\mvnw.cmd -pl agileboot-domain -am -DskipTests=false test

# 定向执行测试；-am 场景下允许无匹配测试的依赖模块继续构建
.\mvnw.cmd -pl agileboot-domain -am -DskipTests=false -Dsurefire.failIfNoSpecifiedTests=false -Dtest=UserModelTest test
```

根据改动范围选择验证：

- `common` 或 `infrastructure` 改动通常需要运行其自身测试及受影响的上层模块测试。
- 领域规则改动至少运行相应 model/application service 测试；涉及持久化时运行对应集成测试。
- Controller、安全配置、序列化或应用配置改动至少编译并测试 `agileboot-admin`，必要时启动应用做接口冒烟验证。
- POM、共享配置或跨模块接口改动运行全仓 `verify`。

如果因本地缺少 MySQL、Redis、网络或平台能力而无法运行某项验证，在交付说明中明确列出未运行的命令和原因，不要声称测试已通过。

## 数据库与配置变更

- 表结构或种子数据变化必须同步更新适当的 SQL 脚本；若测试依赖该结构，也同步更新 `agileboot-infrastructure/src/main/resources/h2sql/` 下的 H2 schema/data。
- MySQL 专用 SQL 与 H2 测试 SQL 语法可能不同，应分别验证，不要假定可直接复用。
- 配置项应有清晰前缀，并在现有配置类中集中绑定；新增环境配置时检查 `application.yml`、`application-dev.yml`、`application-test.yml` 和 `application-basic.yml` 的职责边界。
- 更改接口字段、枚举值或权限标识时，检查前端契约、数据库数据、缓存键和 OpenAPI 注解是否需要同步。

## 提交与交付

- 提交信息简短、具体，说明实际变更；仓库历史以中文描述为主，但没有强制 Conventional Commits 规范。
- 提交前检查 `git diff` 和 `git status`，不要覆盖或夹带用户已有的无关改动。
- 交付时说明：改了什么、为什么、运行了哪些验证及结果、仍有哪些限制或后续操作。
- 不要自行提交、推送、创建发布或修改 CI 密钥，除非用户明确要求。
