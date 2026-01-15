# 随机数选择器 (Random Number Selection)

一款用于随机抽取数字的 Android 应用，支持多种抽取模式和自定义设置。

## 功能特性

- **随机抽取模式**：支持从指定范围内的数字中随机抽取
- **概率抽取模式**：基于概率分布进行数字抽取
- **提取模式**：从指定数字池中提取数字
- **自定义范围**：支持用户自定义数字范围
- **动画效果**：抽取过程中的动画展示
- **自动更新**：支持应用自动检查更新
- **深色模式**：支持深色/浅色主题切换
- **数据持久化**：使用 DataStore 保存用户设置

## 技术栈

- **编程语言**：Kotlin
- **UI 框架**：Jetpack Compose
- **状态管理**：ViewModel + State
- **数据持久化**：DataStore
- **网络请求**：Kotlin Coroutines
- **JSON 解析**：KotlinX Serialization
- **构建系统**：Gradle (Kotlin DSL)

## 项目结构

```
app/
├── src/main/java/com/timome/sjxh/
│   ├── data/           # 数据层
│   │   └── repository/ # 数据仓库
│   ├── ui/             # UI 层
│   │   ├── screen/     # 各个页面
│   │   ├── theme/      # 主题配置
│   │   └── model/      # UI 模型
│   └── viewmodel/      # ViewModel 层
```

## 环境要求

- Android Studio Hedgehog (2023.1.1) 或更高版本
- Gradle 8.13
- Android Gradle Plugin 8.7.3
- 最低支持 Android API 级别 23
- 目标 Android API 级别 36

## 构建与运行

1. 克隆项目：
   ```bash
   git clone https://github.com/Timome-Sudo/Random-Number-Selection.git
   ```

2. 在 Android Studio 中打开项目

3. 同步项目依赖：
   ```bash
   ./gradlew sync
   ```

4. 在模拟器或真机上运行应用

## 版本更新

应用支持从 GitHub Releases 自动检查更新，版本号通过 Git 提交记录动态生成。

## 主要页面

- **首页**：应用的主要功能页面
- **设置页**：自定义抽取参数和应用设置
- **关于页**：应用信息、版本号和检查更新
- **提取页**：特殊提取模式页面

## 许可证

本项目根据 MIT 许可证发布。

## 贡献

欢迎提交 Issue 来帮助改进此项目。
