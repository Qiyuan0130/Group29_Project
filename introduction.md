# BUPT 国际学院助教招聘系统 - 开发规范与功能清单

## 1. 核心功能模块 (Core Functional Modules)

### 1.1 助教端 (Candidate / TA)
* [cite_start]**档案管理**：支持创建、修改个人申请人资料 [cite: 27]。
* [cite_start]**简历附件**：支持上传 PDF 或文本格式的 CV [cite: 27]。
* [cite_start]**职位检索**：支持查看当前所有可用的助教岗位（含模块助教、监考等）[cite: 24, 28]。
* [cite_start]**申请系统**：实现在线申请职位并实时追踪申请进度状态 [cite: 28, 29]。

### 1.2 模块负责人端 (Module Organiser - MO)
* [cite_start]**岗位管理**：支持 MO 发布招聘岗位信息 [cite: 31]。
* [cite_start]**人才选拔**：支持查看职位申请人列表并进行录取筛选 [cite: 32]。

### 1.3 管理员端 (Admin)
* [cite_start]**负荷监控**：全局查看所有助教的当前总工作量，确保资源分配合理 [cite: 33]。

### 1.4 AI 辅助功能 (AI-Powered Features)
* [cite_start]**智能匹配**：自动对比申请人技能与岗位要求的匹配度 [cite: 35]。
* [cite_start]**技能分析**：自动识别申请人缺失的关键技能 [cite: 36]。
* [cite_start]**工作量平衡**：辅助管理员进行科学的 TA 工作量平衡分配 [cite: 36]。

---

## 2. 技术约束与开发规范 (Technical Constraints)

### 2.1 技术栈要求 (Mandatory)
* [cite_start]**语言环境**：必须使用 **Java** 语言开发 [cite: 41]。
* [cite_start]**系统架构**：仅限 **Standalone Java Application** 或轻量级 **Java Servlet/JSP Web App** [cite: 41, 42]。
* [cite_start]**严禁框架**：禁止使用 Spring Boot、Hibernate 等复杂第三方框架，以聚焦基础原理 [cite: 45, 46]。

### 2.2 数据存储规范
* [cite_start]**严禁数据库**：禁止使用任何形式的数据库（如 MySQL、NoSQL 等）[cite: 44]。
* [cite_start]**文件驱动**：所有数据必须存储在简单的**文本文件**中，支持格式：`.txt`, `CSV`, `JSON`, `XML` [cite: 43, 44]。

### 2.3 开发流程与代码管理
* [cite_start]**版本控制**：必须使用 **GitHub** 进行协作 [cite: 65, 77]。
* [cite_start]**分支策略**：每位成员必须在独立的 **Visible Branch** 上提交代码，且必须有对应的 Commit 记录作为贡献证据 [cite: 65, 77]。
* [cite_start]**最终合并**：所有开发分支必须在考核前合并至 **Master/Main** 分支 [cite: 110]。

---

## 3. 关键交付物清单 (Deliverables)
* [cite_start]**源代码**：完整的 Java 源码 [cite: 122]。
* [cite_start]**测试程序**：相关的测试代码与用例 [cite: 123]。
* [cite_start]**技术文档**：包含 JavaDocs、用户手册及详细的 Readme 说明书 [cite: 124, 125, 126]。