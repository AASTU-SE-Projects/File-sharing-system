# 👨‍💻 Group Members

<p align="center">

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=1E90FF&center=true&vCenter=true&width=500&lines=Dawit+Lulie" />


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=FF8C00&center=true&vCenter=true&width=500&lines=Hayat+Zeynu" />


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=8A2BE2&center=true&vCenter=true&width=500&lines=Haset+Tesfaye" />


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=32CD32&center=true&vCenter=true&width=500&lines=Emran+Seid" />


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=FF1493&center=true&vCenter=true&width=500&lines=Hayat+Musema" />


<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=28&pause=0&color=00CED1&center=true&vCenter=true&width=500&lines=Hayat+Khayradin" />

</p>


#  File Sharing System

A **distributed file-sharing application** built with Java, JavaFX, MySQL, and RMI. Perfect for a campus environment where students can securely upload, download, and share files with each other.

---

## ⭐ Key Features

✅ **User Authentication** - Secure registration and login with BCrypt password hashing  
✅ **File Management** - Upload, download, delete files with UUID-based naming  
✅ **File Sharing** - Share files with other users using unique tokens  
✅ **Real-time Search** - Find files by name instantly  
✅ **Multi-User Support** - Multiple students can use simultaneously  
✅ **User Profiles** - View and manage account information  
✅ **Distributed Architecture** - RMI-based client-server with local fallback  
✅ **Thread-Safe GUI** - Background operations don't freeze the interface  
✅ **Responsive UI** - Modern JavaFX interface with multiple screens  

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────┐
│         GUI LAYER (JavaFX)                  │
│  LoginUI  DashboardUI  RegisterUI  Profile  │
└──────────────┬──────────────────────────────┘
               │
    ┌──────────┴──────────┐
    │                     │
┌───▼─────────┐   ┌──────▼──────┐
│ LOCAL MODE  │   │NETWORK MODE  │
│ (Direct DB) │   │ (via RMI)    │
└───┬─────────┘   └──────┬───────┘
    │                    │
┌───▼────────────────────▼────────┐
│   BUSINESS LOGIC (DAO/Services) │
│  UserDAO  FileDAO  RMI Services │
└───┬─────────────────────────────┘
    │
┌───▼──────────────────────────────┐
│   DATA PERSISTENCE               │
│  MySQL Database  File Storage    │
└──────────────────────────────────┘
```

---

## 🎯 Supported Scenarios

| Scenario | Description | Status |
|----------|-------------|--------|
| **Single PC, Single User** | One student, local database | ✅ Supported |
| **Single PC, Multi-User** | Multiple students on same PC with RMI | ✅ Supported |
| **Multi-PC (LAN)** | Multiple students on different PCs via RMI | ✅ Supported |
| **No Network** | Graceful fallback to local-only mode | ✅ Supported |

---

## 📋 Prerequisites

### Required
- **Java 11 or higher** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **MySQL 5.7 or higher** - [Download](https://dev.mysql.com/downloads/mysql/)
- **JavaFX SDK** - Included in project (lib/ folder)
- **MySQL JDBC Driver** - Included in project (lib/ folder)

### Optional
- **IDE**: IntelliJ IDEA, Eclipse, or VS Code
- **Git** - For version control

### System Requirements
- **RAM**: 2GB minimum
- **Disk Space**: 500MB
- **Network**: For multi-PC mode

---

## 🚀 Quick Start

### 1️⃣ Database Setup

```sql
-- Create database
CREATE DATABASE campus_share;

-- Use database
USE campus_share;

-- Create tables (app will auto-create on first run)
-- Or manually run the SQL from documentation
```

**Note:** The application will automatically create tables if they don't exist.

### 2️⃣ Update Configuration

Edit `src/util/AppConfig.java`:

```java
// Database credentials
private static final String DEFAULT_DB_USER = "root";      // Your MySQL user
private static final String DEFAULT_DB_PASSWORD = "****";   // Your MySQL password
```

Or set environment variables:
```bash
set CAMPUS_SHARE_DB_USER=root
set CAMPUS_SHARE_DB_PASSWORD=YUTI
```

### 3️⃣ Compile the Project

```bash
cd C:\Users\hayat\Desktop\AP Project

javac -d bin -cp "bin;lib/*" ^
  src/*.java ^
  src/dao/*.java ^
  src/model/*.java ^
  src/util/*.java ^
  src/rmi/*.java
```

### 4️⃣ Run the Application

**Option A: Single User (Local Mode)**
```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml -Dprism.order=sw -cp bin LoginUI
```

**Option B: Multi-User (with RMI Server)**

Terminal 1 - Start RMI Server:
```bash
java -cp "bin;lib/*" rmi.RMIServer
```

Terminal 2 - Start Client 1:
```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml -Dprism.order=sw -cp bin LoginUI
```

Terminal 3 - Start Client 2:
```bash
java --module-path lib --add-modules javafx.controls,javafx.fxml -Dprism.order=sw -cp bin LoginUI
```

**Option C: Using Batch Scripts (Windows)**
```bash
# Terminal 1
run-rmi-server.bat

# Terminal 2
run-login.bat

# Terminal 3
run-login.bat
```

---

## 📁 Project Structure

```
C:\Users\hayat\Desktop\AP Project\
│
├── src/                              # Source code
│   ├── *.java                        # Main UI classes
│   │   ├── LoginUI.java              # Login screen
│   │   ├── DashboardUI.java          # Main dashboard
│   │   ├── RegisterUI.java           # Registration screen
│   │   ├── ProfileUI.java            # User profile
│   │   └── PublicShareUI.java        # Sharing interface
│   │
│   ├── dao/                          # Data Access Objects
│   │   ├── UserDAO.java              # User operations
│   │   ├── FileDAO.java              # File operations
│   │   └── DBConnection.java         # JDBC connection
│   │
│   ├── model/                        # Data Models
│   │   ├── User.java                 # User entity
│   │   ├── FileInfo.java             # File metadata
│   │   └── FileDownload.java         # Download transfer object
│   │
│   ├── rmi/                          # Remote Services
│   │   ├── RMIServer.java            # RMI registry
│   │   ├── RMIClient.java            # RMI client
│   │   ├── UserServiceRemote.java    # Remote user interface
│   │   ├── UserServiceImpl.java       # User service implementation
│   │   ├── FileServiceRemote.java    # Remote file interface
│   │   └── FileServiceImpl.java       # File service implementation
│   │
│   └── util/                         # Utilities
│       ├── AppConfig.java            # Configuration
│       ├── PasswordUtil.java         # Password hashing
│       ├── FileValidationUtil.java   # File validation
│       ├── LaunchMode.java           # Launch mode enum
│       ├── ServerMode.java           # Server entry point
│       └── ClientMode.java           # Client entry point
│
├── bin/                              # Compiled .class files
├── lib/                              # External libraries
│   ├── javafx-*.jar                  # JavaFX libraries
│   └── mysql-connector-java-*.jar    # MySQL JDBC driver
│
├── uploads/                          # Local file storage (created automatically)
├── downloads/                        # Downloaded files location
│
├── run-login.bat                     # Quick start: Login
├── run-rmi-server.bat                # Quick start: RMI Server
│
├── README.md                         # This file
└── PROJECT_DETAILS_COMPLETE.md       # Complete documentation
```

---

## 💾 Database Schema

### Users Table
```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Files Table
```sql
CREATE TABLE files (
  id INT PRIMARY KEY AUTO_INCREMENT,
  filename VARCHAR(255) NOT NULL,
  stored_filename VARCHAR(255),
  filepath VARCHAR(500) NOT NULL,
  filesize BIGINT NOT NULL,
  uploaded_by INT NOT NULL,
  upload_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE CASCADE
);
```

### Share Tokens Table
```sql
CREATE TABLE share_tokens (
  id INT PRIMARY KEY AUTO_INCREMENT,
  file_id INT NOT NULL,
  user_id INT NOT NULL,
  token VARCHAR(100) UNIQUE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (file_id) REFERENCES files(id) ON DELETE CASCADE,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

## 🔐 Security Features

✅ **Password Hashing** - BCrypt with work factor 12 (resistant to brute force)  
✅ **Prepared Statements** - SQL injection protection  
✅ **Email Uniqueness** - Prevents duplicate accounts  
✅ **File Validation** - Sanitizes filenames and validates uploads  
✅ **Share Tokens** - Unique tokens for secure sharing  
✅ **Access Control** - Users can only access their own files  
✅ **Password Auto-Upgrade** - Legacy plain text passwords automatically upgraded to BCrypt  

---

## 🎮 Usage Guide

### Registration
1. Click "Create New Account"
2. Enter username, email, password
3. Click "Register"
4. ✅ Account created

### Login
1. Enter email and password
2. Click "Login"
3. ✅ Dashboard opens

### Upload File
1. Click "📤 Upload File" button
2. Select file from your computer
3. Wait for upload to complete
4. ✅ File appears in table

### Download File
1. Select file from table
2. Click "⬇️ Download" button
3. File saved to `downloads/` folder
4. ✅ Open your file

### Share File
1. Select file from table
2. Click "🔗 Share" button
3. Copy the share token
4. Send token to another user
5. Other user enters token to access

### View Profile
1. Click "👤 Profile" button
2. View your account information
3. Option to change password

### Logout
1. Click "Logout" button
2. ✅ Return to login screen

---

## 🔧 Configuration Options

### Local Mode (Default)
```bash
# No configuration needed
java -cp bin LoginUI
```
- Files stored in: `uploads/` folder
- Only one PC can access files
- No network required

### Network Mode (Optional)
```bash
# Set environment variable
set CAMPUS_SHARE_SERVER=192.168.1.10

java -cp bin LoginUI
```
- Files stored on network share: `\\SERVER-PC\uploads\`
- All PCs can access same files
- Requires RMI server running on SERVER-PC

### Custom RMI Host
```bash
set CAMPUS_SHARE_RMI_HOST=192.168.1.10
set CAMPUS_SHARE_RMI_PORT=1099

java -cp bin LoginUI
```

### Using Java Properties
```bash
java -Dcampusshare.rmiHost=192.168.1.10 ^
     -Dcampusshare.dbUser=root ^
     -Dcampusshare.dbPassword=YUTI ^
     -cp bin LoginUI
```

---

## 🌐 RMI Architecture

The application uses **Java RMI** for network communication:

```
Client PC                           Server PC
┌─────────────────┐                ┌──────────────────┐
│  LoginUI        │                │  RMIServer       │
│  DashboardUI    │                │  ┌────────────┐  │
│  RMIClient      │───Network───►  │  │UserService │  │
└─────────────────┘                │  │FileService │  │
                                   │  └────────────┘  │
                                   │       │          │
                                   │       ▼          │
                                   │  MySQL DB        │
                                   └──────────────────┘
```

### Start RMI Server
```bash
java -cp "bin;lib/*" rmi.RMIServer
```

### How RMI Works
1. Server binds services to RMI Registry
2. Client looks up services in registry
3. Client calls remote methods like local methods
4. RMI handles serialization and network transport
5. Results returned to client

### Fallback Mode
If RMI server is unavailable:
- Client automatically switches to local DAO
- App continues working without network features
- Uses direct database connections

---

## 🧵 Threading & Performance

The application uses **background threads** for non-blocking operations:

### File Operations
- Upload runs on background thread → UI stays responsive
- Download runs on background thread → No freezing
- Database queries use connection pooling

### UI Updates
- All UI changes happen on JavaFX thread
- Safe using `Platform.runLater()`
- No race conditions or deadlocks

### RMI Communication
- Remote calls handled asynchronously
- Server processes multiple client requests
- Scales to handle multiple concurrent users

---

## 📊 Technical Specifications

| Aspect | Specification |
|--------|---------------|
| **Architecture** | 3-tier (GUI → Logic → DB) |
| **Pattern** | MVC with DAO pattern |
| **GUI Framework** | JavaFX |
| **Database** | MySQL (JDBC) |
| **Networking** | Java RMI |
| **Threading** | JavaFX Tasks + Threads |
| **Security** | BCrypt + Prepared Statements |
| **Max File Size** | 20MB (configurable) |
| **Concurrent Users** | Unlimited |
| **Storage Mode** | Local or Network |

---

## ⚠️ Troubleshooting

### "Cannot connect to database"
- ✅ Check MySQL is running
- ✅ Verify username and password in AppConfig.java
- ✅ Check database `campus_share` exists

### "RMI Server not found"
- ✅ Start RMI server first: `run-rmi-server.bat`
- ✅ Check port 1099 is not blocked
- ✅ Verify RMI host configuration

### "Login window won't appear"
- ✅ Check JavaFX libs are in lib/ folder
- ✅ Recompile: `javac -d bin -cp "bin;lib/*" src/**/*.java`
- ✅ Check Java version >= 11

### "File upload/download fails"
- ✅ Select a file first
- ✅ Check uploads/ folder exists and is writable
- ✅ Verify file size < 20MB

### "Permission denied when accessing network share"
- ✅ Check network share permissions
- ✅ Verify share name matches configuration
- ✅ Test network connectivity with ping

---

## 📚 Documentation

For detailed information, see:

- **PROJECT_DETAILS_COMPLETE.md** - Comprehensive technical documentation
  - Architecture diagrams
  - Database schema details
  - Complete API reference
  - User flow diagrams
  - Threading models
  - RMI details

- **HOW_TO_RUN.md** - Quick start guide
  - 3 ways to run the application
  - Testing scenarios
  - Multi-user setup

---

## 🎓 Learning Outcomes

This project demonstrates:

1. **GUI Development**
   - JavaFX framework
   - Event handling
   - Multi-screen applications
   - Thread-safe UI updates

2. **Database Programming**
   - JDBC connectivity
   - SQL queries with PreparedStatements
   - DAO pattern
   - Transactions

3. **Distributed Systems**
   - Java RMI
   - Client-server architecture
   - Remote method invocation
   - Service registration

4. **Concurrency**
   - Thread management
   - Background tasks
   - Thread safety
   - JavaFX Platform

5. **Security**
   - Password hashing (BCrypt)
   - SQL injection prevention
   - Access control
   - Secure file storage

6. **Software Architecture**
   - 3-tier architecture
   - Model-View-Controller pattern
   - Separation of concerns
   - Design patterns (DAO, Singleton, Factory)

---

## 🚀 Deployment

### Single Machine
```bash
1. Ensure MySQL is running
2. Run: run-rmi-server.bat
3. Run: run-login.bat (multiple times for multiple users)
```

### Multiple Machines (LAN)

**Server Machine:**
```bash
1. MySQL running with database
2. Java compiled
3. Run RMI Server: java -cp "bin;lib/*" rmi.RMIServer
```

**Client Machines:**
```bash
1. Set environment: set CAMPUS_SHARE_RMI_HOST=SERVER_IP
2. Run: java -cp "bin;lib/*" LoginUI
3. Connect to server automatically
```

---

## 📝 Sample Test Data

### Test User 1
```
Username: student1
Email: student1@campus.edu
Password: password123
```

### Test User 2
```
Username: student2
Email: student2@campus.edu
Password: password123
```

To create test data:
1. Register users through GUI
2. Or insert directly into MySQL:
```sql
INSERT INTO users (username, email, password) VALUES 
('student1', 'student1@campus.edu', '$2a$12$...');
```

---

## 📞 Support & Issues

### Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| Can't compile | Check Java 11+, javac in PATH |
| Can't run | Check mysql-connector-java in lib/ |
| GUI not showing | Check JavaFX libs, try -Dprism.order=sw |
| RMI connection fails | Check RMI server running, port 1099 open |
| File operations fail | Check uploads/ folder writable |

### Getting Help
1. Check PROJECT_DETAILS_COMPLETE.md
2. Check console output for error messages
3. Verify all prerequisites installed
4. Check configuration in AppConfig.java

---

## 📄 License

This is an educational project created for learning purposes.

---

## 👨‍💻 Author

Created as a demonstration of enterprise Java development concepts.

**Project Type:** Educational  
**Target Audience:** Computer Science Students  
**Skill Level:** Intermediate to Advanced  

---

## 📊 Project Statistics

| Metric | Value |
|--------|-------|
| **Total Java Files** | 18+ |
| **Lines of Code** | 3000+ |
| **GUI Screens** | 5 |
| **Database Tables** | 3 |
| **RMI Services** | 2 |
| **Features** | 10+ |

---

## 🎉 Getting Started

1. **Read this README** - You are here! ✓
2. **Set up database** - Create MySQL database
3. **Update config** - Edit AppConfig.java
4. **Compile** - Run javac command
5. **Run** - Use batch script or java command
6. **Explore** - Try all features!
7. **Read full docs** - Open PROJECT_DETAILS_COMPLETE.md

---

## ✨ Features Showcase

### Before Using
```
❌ No file storage
❌ No user accounts
❌ No sharing capability
❌ Single machine only
```

### After Using
```
✅ Secure file storage
✅ Multi-user accounts
✅ File sharing with tokens
✅ Network-enabled (multi-machine)
✅ Professional GUI
✅ Real-time search
✅ Background operations
✅ Thread-safe architecture
```

---

**Happy coding! 🚀**

For questions or issues, refer to PROJECT_DETAILS_COMPLETE.md or check the code comments.
