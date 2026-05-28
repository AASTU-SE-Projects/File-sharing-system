# 📚 COMPLETE PROJECT DOCUMENTATION - Campus File Sharing System

---

## TABLE OF CONTENTS
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Database Schema](#database-schema)
5. [Core Components](#core-components)
6. [User Authentication Flow](#user-authentication-flow)
7. [File Management System](#file-management-system)
8. [RMI Architecture](#rmi-architecture)
9. [Threading Model](#threading-model)
10. [GUI Screens](#gui-screens)
11. [Configuration](#configuration)
12. [Complete User Journeys](#complete-user-journeys)

---

## PROJECT OVERVIEW

### What is This Project?
A **distributed file-sharing system** for campus students to:
- Register and authenticate securely
- Upload and download files
- Share files with other students
- Search for files
- Manage their profile
- Access shared files

### Key Features
✅ **Multi-User Support** - Multiple students can use simultaneously  
✅ **Secure Authentication** - Passwords hashed with BCrypt  
✅ **File Sharing** - Share files using unique tokens  
✅ **Search Capability** - Find files by name  
✅ **GUI Interface** - Modern JavaFX interface  
✅ **Database Persistence** - MySQL for permanent storage  
✅ **Network Enabled** - RMI for distributed access  
✅ **Thread-Safe** - Background operations don't freeze UI  

### Educational Value
Demonstrates:
1. **GUI Development** - JavaFX with multiple screens
2. **Database Programming** - JDBC with DAO pattern
3. **Distributed Systems** - Java RMI networking
4. **Concurrency** - Threads and thread safety
5. **Security** - Password hashing and access control
6. **Software Architecture** - Layered design pattern

---

## SYSTEM ARCHITECTURE

### High-Level Overview

```
┌──────────────────────────────────────────────────────────┐
│         PRESENTATION LAYER (GUI - JavaFX)               │
│  LoginUI  DashboardUI  RegisterUI  ProfileUI            │
└──────────────────────────┬───────────────────────────────┘
                           │
          ┌────────────────┴────────────────┐
          │                                 │
    ┌─────▼──────────┐          ┌──────────▼──────┐
    │  LOCAL MODE    │          │  NETWORK MODE   │
    │  (Direct DB)   │          │   (Via RMI)     │
    └─────┬──────────┘          └──────────┬──────┘
          │                               │
┌─────────▼───────────────────────────────▼────────────────┐
│         BUSINESS LOGIC LAYER                             │
│  RMI Services:          Local Services:                  │
│  ├─ UserServiceImpl      ├─ UserDAO                      │
│  ├─ FileServiceImpl      └─ FileDAO                      │
└──────────────────┬─────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│            DATA ACCESS LAYER (DAO)                       │
│  UserDAO    FileDAO    DBConnection                      │
└──────────────────┬──────────────────────────────────────┘
                   │
┌──────────────────▼──────────────────────────────────────┐
│          PERSISTENCE LAYER                               │
│  MySQL Database          File Storage                    │
│  ├─ users table          ├─ uploads/ folder            │
│  ├─ files table          └─ Network shares             │
│  └─ share_tokens table                                 │
└────────────────────────────────────────────────────────┘
```

### Data Flow

**When RMI Server is Running:**
```
Client → RMI Stub → Network → RMI Server → DAO → Database
```

**When RMI Server is NOT Running:**
```
Client → DAO → Database (Direct connection)
```

---

## TECHNOLOGY STACK

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Language** | Java 11+ | Core programming |
| **GUI Framework** | JavaFX | User interface |
| **Database** | MySQL 5.7+ | Data persistence |
| **Networking** | Java RMI | Remote method calls |
| **Concurrency** | Java Threads | Background operations |
| **Security** | BCrypt | Password hashing |
| **JDBC Driver** | MySQL Connector/J | Database connectivity |

### Libraries Used
```
- javafx.* (GUI components)
- java.rmi.* (Remote services)
- java.sql.* (Database access)
- java.nio.* (File operations)
- org.mindrot.jbcrypt.* (Password hashing)
```

---

## DATABASE SCHEMA

### 📊 Database Name: `campus_share`

### Table 1: Users
```sql
CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Fields:**
- `id` → Unique user identifier
- `username` → Display name (e.g., "student_123")
- `email` → Email address (e.g., "student@campus.edu")
- `password` → BCrypt-hashed password (60 characters)
- `created_at` → When account was created

**Example Row:**
```
id=1, username=john_student, email=john@campus.edu, 
password=$2a$12$..., created_at=2025-01-15 10:30:00
```

### Table 2: Files
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

**Fields:**
- `id` → File identifier
- `filename` → Original name (e.g., "notes.pdf")
- `stored_filename` → UUID-based name (e.g., "550e8400_notes.pdf")
- `filepath` → Full path to file
- `filesize` → Size in bytes
- `uploaded_by` → User ID of uploader (FK to users)
- `upload_date` → When uploaded

**Example Row:**
```
id=5, filename=notes.pdf, stored_filename=550e8400_notes.pdf,
filepath=uploads/550e8400_notes.pdf, filesize=125000,
uploaded_by=1, upload_date=2025-01-15 11:00:00
```

### Table 3: Share Tokens
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

**Fields:**
- `id` → Token identifier
- `file_id` → Which file is shared
- `user_id` → Who shared it
- `token` → Unique share token (UUID)
- `created_at` → When sharing happened

---

## CORE COMPONENTS

### Model Classes (Data Objects)

#### 1. User.java
```java
public class User implements Serializable {
    private int id;
    private String username;
    private String email;
    private transient String password;  // Not sent over network
}
```
**Used For:** Passing user data between components

#### 2. FileInfo.java
```java
public class FileInfo implements Serializable {
    private int id;
    private String filename;           // Original (notes.pdf)
    private String storedFilename;     // UUID-based (550e8400_notes.pdf)
    private String filepath;           // Full path
    private long filesize;
    private int uploadedBy;
    private Timestamp uploadDate;
}
```
**Used For:** File metadata in lists and RMI transfers

#### 3. FileDownload.java
```java
public class FileDownload implements Serializable {
    private String filename;
    private byte[] data;  // File binary content
}
```
**Used For:** Downloading files over RMI

### DAO Classes (Database Access)

#### DBConnection.java
```java
public class DBConnection {
    public static Connection connect() {
        // Creates connection to MySQL database
        // Used by: UserDAO, FileDAO
    }
}
```

#### UserDAO.java
```java
public class UserDAO {
    // Register new user
    public boolean registerUser(String username, String email, String password)
    
    // Authenticate user
    public User login(String email, String password)
    
    // Look up user
    public User getUserByEmail(String email)
    public User getUserById(int userId)
    
    // Check email exists
    public boolean emailExists(String email)
}
```

#### FileDAO.java
```java
public class FileDAO {
    // Upload operations
    public boolean saveFile(String filename, String storedFilename, 
                           String filepath, long filesize, int userId)
    
    // Download operations
    public FileInfo getFileById(int fileId)
    
    // List operations
    public List<FileInfo> getFilesByUser(int userId)
    
    // Delete operations
    public boolean deleteFile(int fileId)
    
    // Sharing operations
    public String generateShareToken(int fileId, int userId)
    public boolean revokeShareToken(int fileId, int userId)
    public List<FileInfo> getSharedFilesByUser(int userId)
}
```

### Utility Classes

#### PasswordUtil.java
```java
public class PasswordUtil {
    // Hash password with BCrypt (WORK_FACTOR=12)
    public static String hashPassword(String rawPassword)
    
    // Verify password against hash
    public static boolean verifyPassword(String rawPassword, String storedPassword)
    
    // Check if stored password is BCrypt format
    public static boolean isBcryptHash(String value)
}
```

**Security Features:**
- BCrypt with work factor 12 (expensive, slow computation)
- Constant-time comparison (prevents timing attacks)
- Auto-upgrade for legacy plain text passwords

#### AppConfig.java
```java
public class AppConfig {
    // Get storage path (local or network)
    public static Path getStorageRoot()
    
    // Check if network mode
    public static boolean isNetworkMode()
    
    // Get database connection details
    public static String getDbUrl()
    public static String getDbUser()
    public static String getDbPassword()
    
    // Get RMI connection details
    public static String getRmiHost()
    public static int getRmiPort()
}
```

**Configuration Priority:**
1. Java system properties (-D flags)
2. Environment variables
3. Default values in code

---

## USER AUTHENTICATION FLOW

### Registration Process

```
User enters:
  ├─ Username: "john_student"
  ├─ Email: "john@campus.edu"
  └─ Password: "mypassword123"
       │
       ▼
RegisterUI validates:
  ├─ Username not empty
  ├─ Email valid format
  ├─ Password length >= 8
  └─ Passwords match
       │
       ▼
UserDAO.registerUser():
  ├─ Check email not in database
  ├─ Hash password: 
  │  "mypassword123" → "$2a$12$R9h/cIPz0gi.URNNGUDK2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2"
  ├─ INSERT into users table:
  │  (username, email, password_hash)
  └─ Return success
       │
       ▼
LoginUI shows: "✅ Registration successful!"
```

### Login Process

```
User enters:
  ├─ Email: "john@campus.edu"
  └─ Password: "mypassword123"
       │
       ▼
LoginUI calls login():
       │
       ├─ Is RMI available? 
       │  YES → Use RMIClient.userService.loginUser()
       │  NO → Use UserDAO.login()
       │
       ▼
UserDAO.login():
  ├─ SELECT * FROM users WHERE email = ?
  ├─ Get stored hash: "$2a$12$R9h/cIPz0gi.URNNGUDK2OPST9/PgBkqquzi.Ss7KIUgO2t0jKMm2"
  ├─ BCrypt.checkpw("mypassword123", stored_hash)
  │  → Returns: TRUE/FALSE (constant-time comparison)
  ├─ Return User object if match
  └─ Return null if no match
       │
       ├─ User found?
       │  YES → DashboardUI opens
       │  NO → Show "Invalid credentials"
```

### Password Hash Upgrade Feature

```
If old database has plain text passwords:

Login process:
  ├─ Detect: password doesn't start with "$2"
  ├─ Plain text password match? YES
  ├─ Generate new BCrypt hash
  ├─ UPDATE users SET password = new_hash
  └─ Auto-upgraded! ✅
```

---

## FILE MANAGEMENT SYSTEM

### UUID-Based Filename Strategy

**Problem:** Two users upload "notes.pdf"
```
User A: uploads notes.pdf  → Stored as: notes.pdf
User B: uploads notes.pdf  → OVERWRITES User A's file! ❌
```

**Solution:** UUID Prefix
```
User A: uploads notes.pdf  → Stored as: 550e8400_notes.pdf
User B: uploads notes.pdf  → Stored as: a1b2c3d4_notes.pdf ✅

String uuid = UUID.randomUUID().toString().substring(0, 8);
String storedName = uuid + "_" + originalFilename;
```

### Upload Flow

```
User clicks "📤 Upload File"
       │
       ▼
FileChooser dialog appears
       │
User selects: "report.pdf" (350 KB)
       │
       ▼
DashboardUI.uploadFile():
  ├─ Validate filename
  ├─ Generate UUID: "f7g8h9i0"
  ├─ Create stored name: "f7g8h9i0_report.pdf"
  ├─ Read file bytes (350 KB)
  │
  ├─ Is RMI available?
  │  YES → fileService.uploadFile(name, bytes, userId)
  │  NO → Save locally to uploads/ folder
  │
  ├─ Write to storage: uploads/f7g8h9i0_report.pdf
  ├─ Save metadata to database:
  │  INSERT files(filename, stored_filename, filepath, filesize, uploaded_by)
  │  VALUES('report.pdf', 'f7g8h9i0_report.pdf', 'uploads/f7g8h9i0_report.pdf', 350000, 1)
  │
  └─ Return FileInfo object
       │
       ▼
UI shows: "✅ Upload successful"
       │
       ▼
refreshFileList(): Reload table from database
```

### Download Flow

```
User selects file from table: "report.pdf" (FileInfo object)
       │
       ▼
User clicks "⬇️ Download"
       │
       ▼
DashboardUI.downloadSelectedFile():
  ├─ Get FileInfo from table
  │ → id=10, filename=report.pdf, filepath=uploads/f7g8h9i0_report.pdf
  │
  ├─ Is RMI available?
  │  YES → fileService.downloadFile(fileId=10, userId=1)
  │  NO → Read directly from storage
  │
  ├─ Check user is authorized (security check)
  ├─ Read file bytes from storage
  ├─ Return FileDownload(filename, bytes)
  │
  ├─ Create downloads/ folder
  ├─ Write bytes to: downloads/report.pdf
  │
  └─ Return FileDownload object
       │
       ▼
UI shows: "✅ Download complete"
       │
User can open: downloads/report.pdf
```

### File Sharing Flow

```
User selects file: "report.pdf"
       │
       ▼
User clicks "🔗 Share"
       │
       ▼
FileServiceImpl.generateShareToken():
  ├─ Generate token: UUID = "abc123def456"
  ├─ INSERT share_tokens(file_id=10, user_id=1, token='abc123def456')
  └─ Return token string
       │
       ▼
PublicShareUI shows:
  "Share this token: abc123def456"
       │
User sends token to: student2@campus.edu
       │
Student 2:
  ├─ Enters share token: "abc123def456"
  ├─ System looks up: file_id=10 (report.pdf)
  ├─ Permission check: token exists
  ├─ Download file via: downloadFileByShareToken("abc123def456")
  │
  └─ ✅ Access granted!
```

---

## RMI ARCHITECTURE

### What is RMI?

**RMI** = Remote Method Invocation
- Call Java methods on remote machines
- Automatic serialization and network transport
- Feels like local method calls

### RMI System Components

```
┌─────────────────────────────────────┐
│   RMI REGISTRY (Port 1099)          │
│  ┌─────────────────────────────────┐│
│  │ Service: "UserService"          ││
│  │    → UserServiceImpl object      ││
│  │                                 ││
│  │ Service: "FileService"          ││
│  │    → FileServiceImpl object      ││
│  └─────────────────────────────────┘│
└─────────────────────────────────────┘
```

### RMI Server Components

#### RMIServer.java
```java
public class RMIServer {
    public static void main(String[] args) {
        // Step 1: Create RMI Registry on port 1099
        LocateRegistry.createRegistry(1099);
        
        // Step 2: Get registry reference
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        
        // Step 3: Create service implementations
        UserServiceRemote userService = new UserServiceImpl();
        FileServiceRemote fileService = new FileServiceImpl();
        
        // Step 4: Bind to registry
        registry.rebind("UserService", userService);
        registry.rebind("FileService", fileService);
        
        // Now clients can find and use these services!
    }
}
```

#### UserServiceImpl.java
```java
public class UserServiceImpl extends UnicastRemoteObject 
                            implements UserServiceRemote {
    private UserDAO userDAO = new UserDAO();
    
    public boolean registerUser(String username, String email, 
                               String password) throws RemoteException {
        // Actual implementation runs here on server
        return userDAO.registerUser(username, email, password);
    }
    
    public User loginUser(String email, String password) 
                         throws RemoteException {
        // Actual implementation runs here on server
        return userDAO.login(email, password);
    }
}
```

#### FileServiceImpl.java
```java
public class FileServiceImpl extends UnicastRemoteObject 
                           implements FileServiceRemote {
    private FileDAO fileDAO = new FileDAO();
    
    public FileInfo uploadFile(String filename, byte[] data, 
                              int userId) throws RemoteException {
        // File bytes transmitted over network
        // Actual write happens on server
        return fileDAO.saveFile(filename, storedName, path, 
                               data.length, userId);
    }
    
    public FileDownload downloadFile(int fileId, int userId) 
                                    throws RemoteException {
        // File bytes read on server
        // Transmitted over network to client
        byte[] data = readFileBytes(filepath);
        return new FileDownload(filename, data);
    }
}
```

### RMI Client Components

#### RMIClient.java
```java
public class RMIClient {
    private static FileServiceRemote fileService;
    private static UserServiceRemote userService;
    private static boolean isConnected = false;
    
    public static void initialize() throws RemoteException, NotBoundException {
        // Step 1: Locate registry
        Registry registry = LocateRegistry.getRegistry("localhost", 1099);
        
        // Step 2: Look up services
        fileService = (FileServiceRemote) registry.lookup("FileService");
        userService = (UserServiceRemote) registry.lookup("UserService");
        
        // Step 3: Cache for later use
        isConnected = true;
    }
    
    public static FileServiceRemote getFileService() {
        return fileService;  // Returns stub/proxy object
    }
    
    public static UserServiceRemote getUserService() {
        return userService;  // Returns stub/proxy object
    }
}
```

### Remote Method Call Flow

```
Client code:
  fileService.uploadFile("notes.pdf", bytes, userId)
       │
       ▼
  RMI Client Stub (proxy in client JVM):
  ├─ Serialize arguments: ("notes.pdf", byte[], 1)
  ├─ Create network packet
  └─ Send over TCP/IP
       │
       ▼ (Network Transport)
       │
       ▼
  RMI Server Skeleton (proxy in server JVM):
  ├─ Receive packet
  ├─ Deserialize arguments
  └─ Call actual method
       │
       ▼
  FileServiceImpl.uploadFile() [RUNS ON SERVER]:
  ├─ Validate input
  ├─ Write file to server storage
  ├─ Save metadata to server database
  └─ Create FileInfo result
       │
       ▼
  Serialize result: FileInfo object
       │
       ├─ Serialize object to bytes
       ├─ Create network packet
       └─ Send back to client
       │
       ▼ (Network Transport)
       │
       ▼
  RMI Client receives packet:
  ├─ Deserialize FileInfo
  └─ Return to client code
       │
       ▼
Client code has FileInfo object ✅
```

### RMI Interfaces

#### UserServiceRemote
```java
public interface UserServiceRemote extends Remote {
    boolean registerUser(String username, String email, 
                       String password) throws RemoteException;
    
    User loginUser(String email, String password) 
         throws RemoteException;
    
    User getUserByEmail(String email) throws RemoteException;
    User getUserById(int userId) throws RemoteException;
    boolean emailExists(String email) throws RemoteException;
    String ping() throws RemoteException;  // Test connection
}
```

#### FileServiceRemote
```java
public interface FileServiceRemote extends Remote {
    List<FileInfo> getFilesByUser(int userId) throws RemoteException;
    FileInfo uploadFile(String filename, byte[] data, int userId) 
             throws RemoteException;
    FileDownload downloadFile(int fileId, int userId) 
                 throws RemoteException;
    boolean deleteFile(int fileId, int userId) throws RemoteException;
    String generateShareToken(int fileId, int userId) 
           throws RemoteException;
    List<FileInfo> getSharedFilesByUser(int userId) 
                   throws RemoteException;
}
```

---

## THREADING MODEL

### Main Threads

```
┌─────────────────────────────────────────┐
│  MAIN UI THREAD (JavaFX Application)    │
│                                         │
│  Handles:                               │
│  ├─ Button clicks                       │
│  ├─ Text input                          │
│  ├─ Window management                   │
│  └─ UI updates                          │
│                                         │
│  IMPORTANT: Never block this thread!    │
└─────────────────────────────────────────┘
```

### Background Threads

#### Upload Thread
```java
// Main UI Thread:
uploadButton.setOnAction(e -> {
    // Create background task
    Task<String> uploadTask = new Task<>() {
        protected String call() throws Exception {
            // This runs on BACKGROUND thread
            // Won't freeze UI
            return executeUpload(selectedFile);
        }
    };
    
    // Set UI callback for when done
    uploadTask.setOnSucceeded(event -> {
        // This callback runs on UI thread
        statusLabel.setText("✅ Upload successful");
        refreshFileList();
    });
    
    // Start background thread
    new Thread(uploadTask).start();
});
```

#### Download Thread
```java
// Similar pattern:
Task<FileDownload> downloadTask = new Task<>() {
    protected FileDownload call() throws Exception {
        // Background: Download file (network I/O)
        return fileService.downloadFile(fileId, userId);
    }
};

downloadTask.setOnSucceeded(event -> {
    // UI Thread: Update UI
    FileDownload result = downloadTask.getValue();
    writeToFile(result.getData());
    statusLabel.setText("✅ Download complete");
});

new Thread(downloadTask).start();
```

#### Server Bootstrap Thread
```java
// ServerMode.java
Thread serverBootstrap = new Thread(() -> {
    // Background: Start RMI server
    RMIServer.main(new String[0]);
}, "rmi-server-bootstrap");

serverBootstrap.setDaemon(true);  // Don't block JVM shutdown
serverBootstrap.start();

// Meanwhile, main thread continues:
LoginUI.main(args);  // Show login window
```

#### Multi-User Test Threads
```java
// MultiUserTest.java
Platform.startup(() -> {
    // Window 1
    new LoginUI().start(new Stage());
    
    // Window 2 (delayed)
    new Thread(() -> {
        Thread.sleep(2000);
        Platform.runLater(() -> {
            new LoginUI().start(new Stage());
        });
    }).start();
});
```

### Thread Safety Rules

**❌ WRONG:**
```java
// This causes ConcurrentModificationException
new Thread(() -> {
    tableView.getItems().add(fileInfo);  // Not on UI thread!
}).start();
```

**✅ CORRECT:**
```java
// Use Platform.runLater() to switch to UI thread
new Thread(() -> {
    Platform.runLater(() -> {
        tableView.getItems().add(fileInfo);  // Safe!
    });
}).start();
```

---

## GUI SCREENS

### 1. Login Screen (LoginUI.java)

```
╔═══════════════════════════════════╗
║   Campus File Sharing System      ║
║        [Network Mode 🌐]          ║
│                                   │
│  Email: [________________]        │
│  Password: [________________]     │
│                                   │
│  [Login]  [Create New Account]    │
│                                   │
│  Status: [________________]       │
╚═══════════════════════════════════╝
```

**Features:**
- Email input field
- Password input field
- Login button → Calls loginUser()
- Register button → Opens RegisterUI
- Mode indicator (Server/Client)
- Status messages (colored: red=error, green=success)

**Flow:**
1. User enters credentials
2. Validates input (not empty)
3. Calls UserDAO.login() or RMIClient.userService.loginUser()
4. If success → DashboardUI opens
5. If failure → Shows "Invalid credentials"

### 2. Dashboard Screen (DashboardUI.java)

```
╔════════════════════════════════════════════════════════╗
║  Welcome, john_student!                                ║
║  Storage: uploads [Local Mode 💾]                      ║
│                                                        │
│  🔍 Search: [________________]  View: All Files       │
│                                                        │
│  [Upload] [Download] [Delete] [Share] [Shared Only]  │
│  [All Files] [Revoke] [Profile] [Logout]             │
│                                                        │
│  ┌─────────────────┬──────────┬──────────────┐        │
│  │ File Name       │ Size     │ Upload Date  │        │
│  ├─────────────────┼──────────┼──────────────┤        │
│  │ notes.pdf       │ 125 KB   │ 2025-01-15   │        │
│  │ report.docx     │ 350 KB   │ 2025-01-14   │        │
│  │ photo.jpg       │ 2.5 MB   │ 2025-01-10   │        │
│  └─────────────────┴──────────┴──────────────┘        │
│                                                        │
│  Upload Status: [________________]                     │
╚════════════════════════════════════════════════════════╝
```

**Features:**
- Welcome message (personalized)
- Storage path display
- Search bar (real-time filtering)
- File table (Name, Size, Date columns)
- Action buttons
- View mode switching
- Status label

**Buttons:**
- 📤 Upload → FileChooser → Background upload
- ⬇️ Download → Save to downloads/ folder
- 🗑️ Delete → Confirm then delete
- 🔗 Share → Generate and show token
- 📁 Shared Only → Filter shared files
- 📂 All Files → Show all files
- 🚫 Revoke Share → Revoke token
- 👤 Profile → Open ProfileUI
- Logout → Close and return to LoginUI

### 3. Register Screen (RegisterUI.java)

```
╔═══════════════════════════════════╗
║     Create New Account            │
│                                   │
│  Username: [________________]     │
│  Email: [________________]        │
│  Password: [________________]     │
│  Confirm: [________________]      │
│                                   │
│  [Register]  [Back to Login]     │
│                                   │
│  Status: [________________]       │
╚═══════════════════════════════════╝
```

**Features:**
- Username input (must be unique)
- Email input (must be valid and unique)
- Password input (minimum 8 chars)
- Confirm password input
- Validation on submit
- Status messages

**Validation:**
- Username not empty
- Email format valid (@, no spaces)
- Email not already in database
- Password >= 8 characters
- Passwords match
- Password hashed with BCrypt before storage

### 4. Profile Screen (ProfileUI.java)

```
╔═══════════════════════════════════╗
║     User Profile                  │
│                                   │
│  Username: john_student           │
│  Email: john@campus.edu           │
│  Member Since: 2025-01-15         │
│                                   │
│  [Change Password]  [Logout]      │
│                                   │
╚═══════════════════════════════════╝
```

**Features:**
- Display username
- Display email
- Display account creation date
- Change password option
- Logout button

### 5. Share Screen (PublicShareUI.java)

```
╔═══════════════════════════════════╗
║  File Sharing                     │
│                                   │
│  File: report.pdf                 │
│                                   │
│  Share Token:                     │
│  abc123def456xyz789               │
│                                   │
│  [Copy Token]  [Revoke]  [Close]  │
│                                   │
╚═══════════════════════════════════╝
```

**Features:**
- Shows file being shared
- Displays unique share token
- Copy button (for easy sharing)
- Revoke button (remove access)

---

## CONFIGURATION

### Configuration Sources (Priority Order)

1. **Java System Properties** (Highest Priority)
   ```bash
   java -Dcampusshare.rmiHost=192.168.1.10 -cp bin LoginUI
   ```

2. **Environment Variables**
   ```bash
   set CAMPUS_SHARE_RMI_HOST=192.168.1.10
   java -cp bin LoginUI
   ```

3. **Default Values in Code** (Lowest Priority)
   ```java
   private static final String DEFAULT_RMI_HOST = "localhost";
   ```

### Configuration Variables

| Variable | Type | Default | Purpose |
|----------|------|---------|---------|
| campusshare.rmiHost / CAMPUS_SHARE_RMI_HOST | String | localhost | RMI server IP/hostname |
| campusshare.rmiPort / CAMPUS_SHARE_RMI_PORT | Int | 1099 | RMI registry port |
| campusshare.dbUrl / CAMPUS_SHARE_DB_URL | String | jdbc:mysql://localhost:3306/campus_share | Database URL |
| campusshare.dbUser / CAMPUS_SHARE_DB_USER | String | root | Database username |
| campusshare.dbPassword / CAMPUS_SHARE_DB_PASSWORD | String | YUTI | Database password |
| campusshare.storageRoot / CAMPUS_SHARE_STORAGE_ROOT | String | uploads | Local storage folder |
| campusshare.networkServer / CAMPUS_SHARE_SERVER | String | (empty) | Network server for sharing |
| campusshare.networkShare / CAMPUS_SHARE_NETWORK_SHARE | String | uploads | Network share name |

### Configuration Examples

**Example 1: Local Mode (Default)**
```bash
# No configuration needed
java -cp bin LoginUI

# Result:
# - Connects to localhost:3306 MySQL
# - Uses local uploads/ folder
# - No network sharing
```

**Example 2: Network Mode**
```bash
# Set environment variable
set CAMPUS_SHARE_SERVER=192.168.1.10

java -cp bin LoginUI

# Result:
# - Connects to localhost:3306 MySQL
# - Uses \\192.168.1.10\uploads network path
# - All files accessible from network
```

**Example 3: Remote RMI Server**
```bash
# Use Java properties
java -Dcampusshare.rmiHost=192.168.1.10 \
     -Dcampusshare.rmiPort=1099 \
     -cp bin LoginUI

# Result:
# - Connects to 192.168.1.10 RMI server
# - All operations go through RMI
```

---

## COMPLETE USER JOURNEYS

### Journey 1: Register → Login → Upload → Download

```
STEP 1: START
└─ User opens app
   java -cp bin LoginUI

STEP 2: REGISTER
└─ Clicks "Create New Account"
   └─ RegisterUI.java opens
      ├─ Username: "john_student"
      ├─ Email: "john@campus.edu"
      ├─ Password: "secure123456"
      └─ Clicks "Register"
         └─ RegisterUI.registerUser()
            ├─ Hash password: "secure123456" → "$2a$12$..."
            ├─ INSERT into users table
            └─ ✅ "Registration successful"

STEP 3: LOGIN
└─ LoginUI.java shows again
   ├─ Email: "john@campus.edu"
   ├─ Password: "secure123456"
   └─ Clicks "Login"
      └─ LoginUI.loginUser()
         ├─ SELECT FROM users WHERE email = "john@campus.edu"
         ├─ BCrypt.checkpw() → MATCH
         └─ ✅ DashboardUI opens

STEP 4: UPLOAD
└─ DashboardUI.java shows
   └─ Clicks "📤 Upload File"
      ├─ FileChooser → User selects "report.pdf"
      ├─ Background thread starts
      │  ├─ Generate UUID: "f7g8h9i0"
      │  ├─ Read file bytes (350 KB)
      │  ├─ Save to uploads/f7g8h9i0_report.pdf
      │  ├─ INSERT files metadata into database
      │  └─ UI updates: "✅ Upload successful"
      └─ Table refreshes, shows "report.pdf"

STEP 5: DOWNLOAD
└─ User selects "report.pdf" from table
   └─ Clicks "⬇️ Download"
      ├─ Background thread starts
      │  ├─ Get FileInfo from database
      │  ├─ Read file from uploads/f7g8h9i0_report.pdf
      │  ├─ Save to downloads/report.pdf
      │  └─ UI updates: "✅ Download complete"
      └─ User opens downloads/report.pdf

RESULT: ✅ Complete file lifecycle demonstrated
```

### Journey 2: Multi-User File Sharing

```
PC-A (Student 1)                PC-B (Student 2)
─────────────────              ──────────────────

STEP 1: START SERVERS
Student 1:
└─ Terminal 1: run-rmi-server.bat
   ✅ RMI Server running (localhost:1099)

STEP 2: LOGIN - STUDENT 1
└─ Terminal 2: run-login.bat
   ├─ Login: john@campus.edu / password
   └─ ✅ DashboardUI opens (Student 1)

STEP 3: UPLOAD - STUDENT 1
└─ DashboardUI
   └─ Upload: "group_project.pdf"
      ├─ Stored as: 550e8400_group_project.pdf
      └─ Visible in Table

STEP 4: SHARE - STUDENT 1
└─ Select "group_project.pdf"
   └─ Click "🔗 Share"
      ├─ Generate token: "abc123def456"
      ├─ PublicShareUI shows token
      └─ Student 1 sends token to Student 2

STEP 5: LOGIN - STUDENT 2
Student 2 on PC-B:
└─ Terminal: run-login.bat
   ├─ Connects to RMI (localhost:1099)
   ├─ Login: jane@campus.edu / password
   └─ ✅ DashboardUI opens (Student 2)

STEP 6: VIEW - STUDENT 2
└─ DashboardUI shows only Student 2's files
   ├─ Does NOT see "group_project.pdf"
   ├─ (Different user, different file list)
   └─ But can use share token!

STEP 7: DOWNLOAD SHARED - STUDENT 2
└─ Student 2 enters token: "abc123def456"
   ├─ System looks up file from database
   ├─ Finds: "group_project.pdf" (uploaded by Student 1)
   ├─ Permission check: token valid ✅
   ├─ Download: group_project.pdf
   └─ Saved to: downloads/group_project.pdf

RESULT: ✅ Secure file sharing between users
```

### Journey 3: RMI Server Failure Handling

```
NORMAL STATE:
└─ RMI Server running
   └─ Clients use remote services
      ├─ LoginUI.loginUser() → RMI call
      └─ DashboardUI.uploadFile() → RMI call

RMI SERVER CRASHES:
└─ Connection lost!
   ├─ Network error
   ├─ Process killed
   └─ Whatever reason

CLIENT BEHAVIOR:
└─ RMIClient.initialize() throws exception
   ├─ Caught by LoginUI
   ├─ userService = null
   └─ Fall back to local mode!
      ├─ loginUser() uses UserDAO directly
      ├─ uploadFile() saves locally
      └─ Works fine! ✅

LOGIN:
└─ LoginUI.loginUser()
   ├─ Is RMI available? NO
   └─ Use: userDAO.login(email, password)
      ├─ Direct database call
      └─ ✅ User authenticates

UPLOAD:
└─ DashboardUI.executeUpload()
   ├─ Is RMI available? NO
   └─ Use: fileDAO.saveFile()
      ├─ Save to local uploads/ folder
      └─ ✅ File uploaded locally

RESULT: ✅ Graceful degradation
        System stays functional
        Just without network features
```

---

## SUMMARY

This is a **complete, production-quality educational project** demonstrating:

✅ **Multi-tier architecture** (GUI → Logic → DAO → DB)
✅ **Distributed systems** (RMI for networking)
✅ **Database design** (3 normalized tables)
✅ **Security** (BCrypt hashing, prepared statements)
✅ **Concurrency** (Threads, Task-based async)
✅ **GUI development** (JavaFX, multiple screens)
✅ **Error handling** (Graceful fallbacks)
✅ **Code organization** (Package structure, separation of concerns)

The system works in two modes:
1. **Local Mode** - Direct database access
2. **Network Mode** - Via RMI services

Perfect for learning enterprise Java development!
