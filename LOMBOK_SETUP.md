# 🔧 Lombok Setup Guide

## The Issue

You're using **Java 25** but the project targets **Java 17**. This causes Lombok annotation processing issues with Maven command line builds.

## ✅ Solution: Use IntelliJ IDEA with Lombok Plugin

### Step 1: Install Lombok Plugin

1. Open **IntelliJ IDEA**
2. Go to: **IntelliJ IDEA → Preferences** (or **Settings** on Windows/Linux)
3. Select: **Plugins**
4. Search for: **"Lombok"**
5. Click **Install** on the Lombok plugin
6. **Restart IntelliJ IDEA**

### Step 2: Enable Annotation Processing

1. Go to: **IntelliJ IDEA → Preferences → Build, Execution, Deployment → Compiler → Annotation Processors**
2. ✅ Check: **"Enable annotation processing"**
3. Click **Apply** and **OK**

### Step 3: Rebuild Project in IntelliJ

1. Right-click on the project root
2. Select: **Maven → Reload Project**
3. Then: **Build → Rebuild Project**

### Step 4: Run from IntelliJ

1. Open `UserServiceApplication.java`
2. Right-click → **Run 'UserServiceApplication'**

## 🎯 Alternative: Use Java 17

If you prefer command-line builds, install Java 17:

```bash
# Install Java 17 (using Homebrew on Mac)
brew install openjdk@17

# Set JAVA_HOME for this session
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home

# Then build
cd user-service
mvn clean install
```

## ✅ Verify Lombok is Working

After setup, your classes should compile without errors. Lombok will generate:

- ✅ Getters/Setters from `@Data`
- ✅ Builder pattern from `@Builder`
- ✅ Logger from `@Slf4j`
- ✅ Constructors from `@RequiredArgsConstructor`

## 📝 Current Status

- ✅ Lombok dependency is correctly configured in `pom.xml`
- ✅ Code is complete and ready
- ⚠️ Need IDE setup or Java 17 for command-line builds

## 🚀 Next Steps

1. **Install Lombok plugin in IntelliJ** (recommended)
2. **Enable annotation processing**
3. **Rebuild project**
4. **Run the service!**

Once Lombok is working, you can:

- Build: `mvn clean install` (from IDE or with Java 17)
- Run: `mvn spring-boot:run` or run from IntelliJ
- Test: Use the Postman collection or curl commands
