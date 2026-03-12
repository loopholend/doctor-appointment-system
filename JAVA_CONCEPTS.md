# Java Concepts in MediCare+ — An Educational Guide

> This document explains **why** every major Java and Java EE decision was made in this project.
> Each section shows the real code from this project and contrasts it with the alternative approach.

---

## Table of Contents
1. [PreparedStatement vs Statement](#1-preparedstatement-vs-statement)
2. [JDBC — Connecting to a Database](#2-jdbc--connecting-to-a-database)
3. [Static Initializer Block](#3-static-initializer-block)
4. [HttpServlet — The Core of Java EE Web](#4-httpservlet--the-core-of-java-ee-web)
5. [doGet vs doPost](#5-doget-vs-dopost)
6. [HttpSession — Remembering the User](#6-httpsession--remembering-the-user)
7. [sendRedirect vs forward](#7-sendredirect-vs-forward)
8. [Transactions — setAutoCommit, commit, rollback](#8-transactions--setautocommit-commit-rollback)
9. [Exception Handling — try-catch-finally](#9-exception-handling--try-catch-finally)
10. [Password Hashing — Why Not Store Plain Text?](#10-password-hashing--why-not-store-plain-text)
11. [Packages — Organising Classes](#11-packages--organising-classes)
12. [Static Utility Classes](#12-static-utility-classes)
13. [StringBuilder — Efficient String Building](#13-stringbuilder--efficient-string-building)
14. [DatabaseMetaData — Inspecting the DB Schema](#14-databasemetadata--inspecting-the-db-schema)
15. [File Upload — MultipartConfig](#15-file-upload--multipartconfig)
16. [web.xml — Deployment Descriptor](#16-webxml--deployment-descriptor)
17. [volatile + synchronized — Thread Safety](#17-volatile--synchronized--thread-safety)
18. [Properties File — Externalising Configuration](#18-properties-file--externalising-configuration)
19. [Constant-Time Comparison — Security Detail](#19-constant-time-comparison--security-detail)
20. [Generated Keys — Getting the Auto-Increment ID Back](#20-generated-keys--getting-the-auto-increment-id-back)

---

## 1. PreparedStatement vs Statement

### The Problem with `Statement`

When you use a plain `Statement`, you build the SQL query by concatenating strings directly:

```java
// DANGEROUS — DO NOT DO THIS
String sql = "SELECT * FROM patients WHERE username = '" + username + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

If the user types `' OR '1'='1` as their username, the query becomes:
```sql
SELECT * FROM patients WHERE username = '' OR '1'='1'
```
This returns **every row** in the table. This attack is called **SQL Injection** — one of the most common ways databases get hacked.

### Why We Use `PreparedStatement`

```java
// SAFE — from PatientLoginServlet.java
String sql = "SELECT * FROM patients WHERE username = ?";
PreparedStatement pstmt = conn.prepareStatement(sql);
pstmt.setString(1, username);   // The ? is replaced SAFELY
ResultSet rs = pstmt.executeQuery();
```

**How it works:**
- The `?` is a **parameter placeholder**. The SQL structure is sent to MySQL first, compiled/planned once.
- `setString(1, username)` then sends the data value separately. MySQL treats it as pure data — never as SQL code.
- Even if the user types `' OR '1'='1`, it is treated literally as a string, not as SQL.

**Bonus performance benefit:** If you run the same query many times (e.g., login is called repeatedly), the DB has already compiled the plan — it just swaps in new values. `Statement` recompiles every time.

---

## 2. JDBC — Connecting to a Database

JDBC (Java Database Connectivity) is the standard Java API for talking to any relational database. It has three steps:

```java
// From DBConnection.java

// Step 1: Load the MySQL driver class into memory
Class.forName("com.mysql.cj.jdbc.Driver");

// Step 2: Ask DriverManager for a connection using the URL + credentials
Connection conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/medical_appointment_system?useSSL=false",
    "root",
    "root"
);

// Step 3: Use the connection to run SQL
PreparedStatement pstmt = conn.prepareStatement("SELECT ...");
```

**Why `Class.forName()`?**
This line dynamically loads the MySQL JDBC driver. Without it, Java does not know which database driver to use. Newer JDBC versions (4.0+) can auto-detect the driver if the JAR is on the classpath, but explicitly loading it is safer and clearer.

**Why wrap it in a utility class?**
Every servlet needs a connection. If we wrote the connection code in every servlet, and later the DB password changed, we would need to update 40+ files. `DBConnection.getConnection()` is a single place to change — this is the **DRY principle** (Don't Repeat Yourself).

---

## 3. Static Initializer Block

```java
// From DBConnection.java
public class DBConnection {

    private static final Properties props = new Properties();

    static {  // <-- This runs ONCE when the class is first loaded by JVM
        try (InputStream is = DBConnection.class.getClassLoader()
                                   .getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                // Fallback hardcoded values
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/...");
            }
        } catch (IOException e) {
            System.err.println("ERROR loading db.properties: " + e.getMessage());
        }
    }
```

**Why use a `static` block?**
- `static` means it belongs to the *class*, not to any particular object instance.
- The `static { ... }` block runs **automatically, exactly once**, the first time `DBConnection` is used anywhere in the application.
- This is the right place to do one-time setup like reading a config file — you don't want to re-read the file on every single database call.

**Analogy:** Think of it like a constructor, but for the class itself rather than for an object.

---

## 4. HttpServlet — The Core of Java EE Web

Every page in this application is a **Servlet** — a Java class that handles HTTP requests.

```java
// From PatientLoginServlet.java
@WebServlet(urlPatterns = {"/patient/login"})
public class PatientLoginServlet extends HttpServlet {
    ...
}
```

**What is `extends HttpServlet`?**
`HttpServlet` is a class provided by the Java EE library (`javax.servlet`). It already knows how to receive HTTP requests. By extending it, our class **inherits** all that networking infrastructure. We only need to write what is specific to our use case.

**What is `@WebServlet`?**
This annotation tells GlassFish: *"When a browser requests the URL `/patient/login`, call this class."* No extra configuration needed (when annotations are active).

**The Servlet Lifecycle:**
1. GlassFish starts → loads the servlet class (calls `init()`)
2. A request arrives → GlassFish calls `service()` → which calls `doGet()` or `doPost()`
3. Server shuts down → calls `destroy()`

We never call these methods ourselves. GlassFish manages the lifecycle.

---

## 5. doGet vs doPost

```java
// From PatientLoginServlet.java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    response.sendRedirect("login.html");  // Show the form
}

@Override
public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String username = request.getParameter("t1");  // Process form submission
    String password = request.getParameter("t2");
    ...
}
```

| HTTP Method | When Used | Visible in URL? | Bookmarkable? |
|-------------|-----------|-----------------|---------------|
| `GET`       | Fetching a page, reading data | Yes (query string) | Yes |
| `POST`      | Submitting a form, changing data | No (request body) | No |

**Why does the login form use POST?**
- The username and password would appear in the browser URL bar if GET was used.
- POST data goes in the HTTP request body — not in the URL — so it does not appear in browser history, server logs, or referrer headers.
- Rule of thumb: **GET = read data, POST = write/change data.**

**Why implement `doGet` to redirect for a login servlet?**
If someone types `/patient/login` in the browser address bar, the browser sends a GET request. Without a `doGet()`, GlassFish returns a `405 Method Not Allowed` error. Redirecting to the HTML form is the correct, user-friendly response.

---

## 6. HttpSession — Remembering the User

HTTP is **stateless** — every request is independent. The server forgets you after each response. Sessions solve this.

```java
// From DoctorRegistrationServlet.java — Creating a session after login
HttpSession session = request.getSession(true);  // true = create if none exists
session.setAttribute("doctorId", doctorId);
session.setAttribute("doctorUsername", username);
session.setAttribute("doctorName", name);
```

```java
// From NavHelper.java — Checking a session on a protected page
public static String requireAdminSession(HttpServletRequest request,
                                         HttpServletResponse response)
        throws IOException {
    HttpSession session = request.getSession(false);  // false = don't create
    if (session == null || session.getAttribute("adminName") == null) {
        response.sendRedirect("/Doctor-Appointment-System/admin/login");
        return null;  // Caller must stop processing
    }
    return (String) session.getAttribute("adminName");
}
```

**How it works under the hood:**
1. First login → server creates a session object, generates a random **session ID** (e.g., `A3F9B2...`).
2. Server sends the session ID to the browser as a **cookie** called `JSESSIONID`.
3. On every subsequent request, the browser automatically sends the cookie back.
4. Server looks up the session object using that ID → knows who you are.

**`getSession(true)` vs `getSession(false)`:**
- `true` → create a new session if one doesn't exist (use on login).
- `false` → return `null` if no session exists (use on protected pages — you want to detect unauthenticated users, not accidentally create sessions for them).

---

## 7. sendRedirect vs forward

Both move the user to another page, but they work very differently.

### `sendRedirect` — Browser does a NEW request

```java
// From PatientLoginServlet.java
response.sendRedirect("dashboard");
```

- Server sends HTTP `302 Found` back to the browser.
- Browser makes a **brand new GET request** to the new URL.
- The URL in the address bar **changes**.
- Use after form submission (POST) to prevent double-submit on browser refresh.

### `forward` — Server handles it internally

```java
// Hypothetical example
RequestDispatcher rd = request.getRequestDispatcher("dashboard.jsp");
rd.forward(request, response);
```

- Server processes the new resource internally.
- Browser never knows — the URL in the address bar **stays the same**.
- Request attributes set before the forward are available at the destination.
- Use when you want to pass data (via `request.setAttribute()`) to another resource.

**The POST-Redirect-GET pattern:**
In this project, every form submission (POST) is followed by `sendRedirect` on success. Why? If the user refreshes the page after a redirect, the browser re-issues a GET request (which is safe). If we had forwarded instead, a refresh would resubmit the POST form — potentially inserting duplicate records into the database.

---

## 8. Transactions — setAutoCommit, commit, rollback

When adding a doctor, we must insert into **two tables** — `doctor_accounts` and `doctor_profiles`. What if the first insert succeeds but the second one fails (e.g., duplicate license number)? Without transactions, we'd have a "half-doctor" in the database.

```java
// From AdminAddDoctorServlet.java
conn.setAutoCommit(false);  // Tell MySQL: hold all changes, don't save yet

try {
    // Insert into table 1
    try (PreparedStatement acctPs = conn.prepareStatement(
            "INSERT INTO doctor_accounts (username, email_address, password_hash) VALUES (?,?,?)")) {
        acctPs.setString(1, username);
        acctPs.setString(2, email);
        acctPs.setString(3, PasswordUtil.hashPassword(password));
        acctPs.executeUpdate();
    }

    // Insert into table 2
    ps = conn.prepareStatement("INSERT INTO doctor_profiles (...) VALUES (...)");
    // ... set parameters ...
    ps.executeUpdate();

    conn.commit();  // Both succeeded — save permanently
    response.sendRedirect("doctors?added=1");

} catch (SQLIntegrityConstraintViolationException e) {
    conn.rollback();  // Undo ALL changes — neither insert is saved
    renderForm(out, adminName, "Username or license already exists.");
} catch (Exception e) {
    conn.rollback();  // Undo ALL changes
    e.printStackTrace();
} finally {
    conn.setAutoCommit(true);  // Always restore default behaviour
    conn.close();
}
```

**Key concepts:**
- `setAutoCommit(false)` — by default MySQL commits every statement immediately. This turns that off.
- `commit()` — save all pending changes permanently.
- `rollback()` — undo all pending changes as if they never happened.
- This guarantees **atomicity** — either both inserts happen, or neither does. No half-states.

**In the real world:** Bank transfers use this. When you transfer money, the debit from account A and the credit to account B must both succeed or both fail.

---

## 9. Exception Handling — try-catch-finally

```java
// From PatientLoginServlet.java
Connection conn = null;
PreparedStatement pstmt = null;
ResultSet rs = null;

try {
    conn = DBConnection.getConnection();
    pstmt = conn.prepareStatement("SELECT * FROM patients WHERE username = ?");
    pstmt.setString(1, username);
    rs = pstmt.executeQuery();
    // ... process results ...

} catch (SQLIntegrityConstraintViolationException e) {
    // Specific: duplicate key — tell user the username is taken
    renderForm(out, adminName, "Username already exists.");

} catch (SQLException e) {
    // Broader: any other SQL error
    e.printStackTrace();

} catch (Exception e) {
    // Broadest: anything else (ClassNotFoundException etc)
    out.println("<p>Error: " + e.getMessage() + "</p>");

} finally {
    // This block ALWAYS runs — even if an exception was thrown
    try {
        if (rs != null)    rs.close();
        if (pstmt != null) pstmt.close();
        if (conn != null)  conn.close();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}
```

**Why catch specific exceptions first?**
Java checks `catch` blocks top to bottom, using the **first match**. `SQLIntegrityConstraintViolationException` *is-a* `SQLException` (it extends it). If you put `catch (SQLException e)` first, the specific case is never reached. Always go **specific → broad**.

**Why `finally`?**
Even if an exception occurs halfway through, the database connection **must** be closed. If you don't close connections, you eventually run out (connection pool exhausted) and the whole application stops working. `finally` always runs — even when there's an exception, even when there's a `return` statement — making it the only safe place to put cleanup code.

**try-with-resources (modern alternative):**
```java
// Used in DBConnection.java for short-lived resources
try (Statement stmt = conn.createStatement()) {
    stmt.executeUpdate("RENAME TABLE ...");
}  // stmt.close() called automatically here — no finally needed
```
The `try-with-resources` syntax automatically closes any `AutoCloseable` (like `Statement`, `ResultSet`) at the end of the block. Cleaner, but requires the variable to be declared inside the `try(...)`.

---

## 10. Password Hashing — Why Not Store Plain Text?

```java
// From PasswordUtil.java
public static String hashPassword(String plainPassword) {
    SecureRandom random = new SecureRandom();
    byte[] salt = new byte[16];
    random.nextBytes(salt);  // Random 16 bytes

    PBEKeySpec spec = new PBEKeySpec(
        plainPassword.toCharArray(), // password as char[] not String (security)
        salt,
        65536,  // iterations: hash computed 65,536 times (slow = harder to crack)
        256     // output length in bits
    );
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    byte[] hash = factory.generateSecret(spec).getEncoded();

    // Store as "base64(salt):base64(hash)"
    return Base64.getEncoder().encodeToString(salt)
           + ":" + Base64.getEncoder().encodeToString(hash);
}
```

**Why not store `password = "mypassword123"` directly?**
If your database is ever leaked (hacked, accidental backup exposure), everyone's password is exposed immediately. Medical data + real passwords = very serious legal and ethical problem.

**Why not use MD5 or SHA-256 alone?**
These are fast hash functions. A modern GPU can compute billions of SHA-256 hashes per second, allowing attackers to brute-force passwords quickly using precomputed tables (rainbow tables).

**What makes PBKDF2 better?**
1. **Salt** — a random value added to the password before hashing. Two users with the same password get completely different hashes. Rainbow tables are useless.
2. **Iterations (65,536)** — the hash is computed 65 thousand times in a loop. This makes each guess slow (~100ms). You can still log in quickly, but an attacker trying millions of passwords is slowed drastically.
3. **HMAC-SHA256** — a cryptographically strong function designed for this purpose.

**Why `char[]` instead of `String` for the password?**
In Java, `String` objects are **immutable and interned** — once created, they can stay in memory for a long time before garbage collection. A memory dump could expose the password. A `char[]` array can be manually zeroed out (`Arrays.fill(arr, '0')`) immediately after use. This is why `PBEKeySpec` accepts `char[]`.

---

## 11. Packages — Organising Classes

```java
package admin;   // AdminAddDoctorServlet.java lives here
package doctor;  // DoctorLoginServlet.java lives here
package patient; // PatientLoginServlet.java lives here
package common;  // DBConnection.java, PasswordUtil.java, NavHelper.java
```

**Why use packages?**
- **Namespace**: Two classes can have the same name if they are in different packages (e.g., Java's own `java.util.Date` and `java.sql.Date`).
- **Organisation**: In a project with 42 Java files, packages let you quickly know what a class does just from its location.
- **Access control**: The `protected` and default (package-private) access modifiers control which packages can see which classes.

**The `common` package:**
Classes used by every role (admin, doctor, patient) — `DBConnection`, `PasswordUtil`, `NavHelper` — are placed in `common` to signal they are shared utilities, not role-specific logic.

---

## 12. Static Utility Classes

```java
// From NavHelper.java
public class NavHelper {

    // Static method — no object needed to call it
    public static String requireAdminSession(HttpServletRequest req,
                                              HttpServletResponse res) { ... }

    public static void writeAdminNavbar(PrintWriter out, String title) { ... }

    public static void writeAdminSidebar(PrintWriter out, String name,
                                          String activeItem) { ... }

    public static String esc(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }
}
```

**Why `static` methods?**
`NavHelper` holds no state of its own — it just contains reusable functions. Making methods `static` means:
- Callers write `NavHelper.writeAdminNavbar(out, "Dashboard")` — no need to do `new NavHelper()` first.
- There is no object to create and garbage-collect — more efficient.
- The intent is clear: this is a collection of helper functions, not a stateful object.

**Why the `esc()` method?**
When we write user-supplied data into HTML (e.g., a doctor's name into a page), we must **escape** HTML special characters. If a user's name was `<script>alert('hacked')</script>`, writing it directly into HTML would run JavaScript in the visitor's browser. This is called **XSS (Cross-Site Scripting)**. `esc()` converts `<` to `&lt;` so the browser renders it as text, not as code.

---

## 13. StringBuilder — Efficient String Building

```java
// From DBConnection.java
StringBuilder alterSql = new StringBuilder()
    .append("ALTER TABLE ")
    .append(tableName)
    .append(" MODIFY COLUMN password_hash VARCHAR(")
    .append(PASSWORD_COLUMN_LENGTH)
    .append(") ")
    .append(nullable ? "NULL" : "NOT NULL");

if (defaultValue != null) {
    alterSql.append(" DEFAULT '").append(defaultValue).append("'");
}

stmt.executeUpdate(alterSql.toString());
```

**Why not just use `+` to concatenate strings?**
```java
// Naive approach
String sql = "ALTER TABLE " + tableName + " MODIFY COLUMN password_hash VARCHAR("
             + PASSWORD_COLUMN_LENGTH + ") " + ...;
```

In Java, `String` is **immutable** — every `+` operation creates a **new String object** and discards the old one. Concatenating 10 pieces creates ~10 temporary objects. For a small number of strings this is fine, but in a loop or for many pieces it wastes memory and CPU.

`StringBuilder` has an internal resizable char array. `.append()` just adds to the end — no new objects created until you call `.toString()`. Think of `String` as a sticky note you rewrite from scratch each time, versus `StringBuilder` as a whiteboard you keep adding to.

---

## 14. DatabaseMetaData — Inspecting the DB Schema

```java
// From DBConnection.java
DatabaseMetaData metaData = conn.getMetaData();

// Check if a table exists
private static boolean tableExists(DatabaseMetaData metaData, String tableName)
        throws SQLException {
    try (ResultSet rs = metaData.getTables(null, null, tableName,
                                            new String[]{"TABLE"})) {
        return rs.next();  // true if at least one row returned
    }
}

// Check if a column exists
private static boolean columnExists(DatabaseMetaData metaData,
                                     String tableName, String columnName)
        throws SQLException {
    try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
        return rs.next();
    }
}
```

**What is `DatabaseMetaData`?**
It is the JDBC API for asking the database about **its own structure** — what tables exist, what columns they have, what their types are, what constraints are defined. We used this to build an **automatic migration system** — on first startup, the app checks if old column names (e.g., `name`, `CN`) exist and renames them to the new descriptive names (`full_name`, `contact_number`). No manual SQL scripts needed.

---

## 15. File Upload — MultipartConfig

Normally, HTTP form data is encoded as `key=value` pairs (URL-encoded). But files are binary data — they need a different encoding called **multipart/form-data**.

```java
// From AdminAddDoctorServlet.java
@WebServlet("/admin/add-doctor")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,   // 2 MB: buffer in memory up to this size
    maxFileSize       = 1024 * 1024 * 10,   // 10 MB: max size per single file
    maxRequestSize    = 1024 * 1024 * 50    // 50 MB: max total request size
)
public class AdminAddDoctorServlet extends HttpServlet {

    protected void doPost(...) {
        Part filePart = request.getPart("profileImage");  // Get the file part
        if (filePart != null && filePart.getSize() > 0) {
            InputStream imageStream = filePart.getInputStream();
            // ...save binary data to DB as BLOB
        }
    }
}
```

**Why is the annotation not enough?**
Our `web.xml` has `metadata-complete="true"` — this tells GlassFish *"ignore all annotations, trust only web.xml"*. So `@MultipartConfig` is silently ignored at runtime. The fix was to also add a `<multipart-config>` element inside the servlet declaration in `web.xml`. Lesson: annotations and XML descriptors serve the same purpose; when XML says "I'm complete", annotations are skipped.

**Why store images as BLOB in the database?**
Simpler deployment — no file system path to manage, images are backed up with the database, and access control is automatic (you only get the image if your SQL query succeeds). Trade-off: DB size grows larger and querying all rows for non-image data loads unnecessary bytes. For small-scale apps this is perfectly fine.

---

## 16. web.xml — Deployment Descriptor

`web.xml` is the central configuration file for the web application. It tells the server:
- Which class handles which URL
- What the app is called
- Session timeout settings
- Welcome files

```xml
<!-- From web/WEB-INF/web.xml -->
<servlet>
    <servlet-name>AdminAddDoctorServlet</servlet-name>
    <servlet-class>admin.AdminAddDoctorServlet</servlet-class>
    <multipart-config>
        <max-file-size>10485760</max-file-size>       <!-- 10 MB -->
        <max-request-size>52428800</max-request-size> <!-- 50 MB -->
        <file-size-threshold>2097152</file-size-threshold> <!-- 2 MB -->
    </multipart-config>
</servlet>
<servlet-mapping>
    <servlet-name>AdminAddDoctorServlet</servlet-name>
    <url-pattern>/admin/add-doctor</url-pattern>
</servlet-mapping>
```

**Why does element order matter?**
XML Schemas define a strict order for child elements. GlassFish uses an XSD (XML Schema Definition) to validate `web.xml` on every deployment. The `<multipart-config>` children must appear in this exact order: `<location>` → `<max-file-size>` → `<max-request-size>` → `<file-size-threshold>`. Getting the order wrong causes a `SAXParseException` — the app refuses to deploy. This is why we got the *"Invalid content was found starting with element 'max-file-size'"* error in this project.

---

## 17. volatile + synchronized — Thread Safety

```java
// From DBConnection.java
private static volatile boolean schemaValidated = false;

private static void ensureSchemaUpToDate(Connection conn) throws SQLException {
    if (schemaValidated) return;          // Fast path: skip if already done

    synchronized (DBConnection.class) {   // Lock the class object
        if (schemaValidated) return;       // Re-check inside the lock (double-checked locking)

        migrateSchema(conn);
        schemaValidated = true;
    }
}
```

**The problem:** GlassFish handles many requests in parallel using multiple threads. If two requests arrive simultaneously and `schemaValidated` is `false`, both threads could start running the migration at the same time — causing errors like "table already renamed".

**`volatile`:** Without this keyword, each CPU core may cache the variable in its own register. One thread sets it to `true`, but another thread reads the cached `false` from its register. `volatile` forces every read/write to go directly to main memory — all threads see the same value.

**`synchronized`:** Only one thread at a time can enter a `synchronized` block. All others wait. This ensures the migration runs exactly once, even under heavy concurrent load.

**Double-checked locking pattern:** We check `schemaValidated` twice — once outside the lock (fast, no waiting) and once inside (safe). Why? `synchronized` is expensive — it involves locks and memory barriers. The outer check means 99.9% of requests skip locking entirely once migration is complete.

---

## 18. Properties File — Externalising Configuration

```properties
# src/java/db.properties
db.url=jdbc:mysql://localhost:3306/medical_appointment_system?useSSL=false
db.user=root
db.password=root
admin.password=Admin@1234
```

```java
// Reading it in Java
Properties props = new Properties();
InputStream is = DBConnection.class.getClassLoader()
                               .getResourceAsStream("db.properties");
props.load(is);
String url = props.getProperty("db.url");
```

**Why not hardcode `"root"` and `"root"` directly in the Java source?**
- **Deployment flexibility:** The same `.war` file can be deployed on a development machine, test server, and production server — each with different database passwords — without recompiling.
- **Security:** Database passwords must never be committed to source control (GitHub). By keeping credentials in `db.properties` and adding it to `.gitignore`, the credentials stay on the server, not in the public codebase.
- **Maintainability:** Change the password in one place — not scattered across 40 files.

**`getResourceAsStream` vs `new FileInputStream`:**
`getResourceAsStream` searches the **classpath** (where `.class` files live). This works correctly whether the app is run from an IDE, an Ant build, or a WAR file on a server. `new FileInputStream("db.properties")` searches the current working directory — which varies and is unreliable in a server environment.

---

## 19. Constant-Time Comparison — Security Detail

```java
// From PasswordUtil.java
// Constant-time comparison to prevent timing attacks
if (actualHash.length != expectedHash.length) return false;
int diff = 0;
for (int i = 0; i < actualHash.length; i++) {
    diff |= actualHash[i] ^ expectedHash[i];  // XOR each byte
}
return diff == 0;  // diff is 0 only if every byte matched
```

**Why not just use `Arrays.equals(actualHash, expectedHash)`?**
`Arrays.equals` short-circuits — it returns `false` as soon as it finds the first different byte. This means a correct password prefix takes slightly *longer* to reject than a completely wrong password. An attacker making thousands of login attempts can measure these tiny timing differences to gradually guess the password byte by byte. This is called a **timing attack**.

**Our solution:** XOR (`^`) every byte pair and accumulate the result in `diff`. If all bytes match, all XOR results are `0`, and `diff` stays `0`. If any byte differs, some XOR result is non-zero, and `diff` becomes non-zero. Crucially, this loop **always runs to completion** regardless of where the difference is — so every comparison takes the same amount of time. The attacker learns nothing from timing.

---

## 20. Generated Keys — Getting the Auto-Increment ID Back

```java
// From DoctorRegistrationServlet.java

// Tell JDBC: after this INSERT, give me back the auto-generated ID
PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
pstmt.executeUpdate();

// Fetch the generated key
ResultSet generatedKeys = pstmt.getGeneratedKeys();
if (generatedKeys.next()) {
    int doctorId = generatedKeys.getInt(1);  // Column index 1 = the new ID

    // Store in session for use on next page
    session.setAttribute("doctorId", doctorId);
}
```

**Why do we need this?**
The `doctor_id` column in `doctor_accounts` is `INT AUTO_INCREMENT PRIMARY KEY`. MySQL assigns the value automatically when the row is inserted. We do not know what number it will be before the INSERT. After the INSERT, we need that ID to link to the `doctor_profiles` table and to store in the session.

`Statement.RETURN_GENERATED_KEYS` tells the JDBC driver to capture the generated key and make it available via `getGeneratedKeys()`. Without this flag, `getGeneratedKeys()` returns an empty result set.

---

## Summary Table

| Concept | Where Used | Why |
|---------|-----------|-----|
| `PreparedStatement` | Every servlet that queries DB | Prevent SQL Injection |
| `JDBC / DriverManager` | `DBConnection.java` | Standard Java DB API |
| `static {}` block | `DBConnection.java` | One-time config file loading |
| `extends HttpServlet` | All 42 servlet classes | Inherit HTTP request handling |
| `doGet` / `doPost` | All servlets | Separate page-load from form-submit |
| `HttpSession` | Login, dashboard, protected pages | Maintain user login state across requests |
| `sendRedirect` | After form POST success | Prevent double-submit on refresh |
| Transactions | `AdminAddDoctorServlet` | Atomically insert into 2 tables |
| `try-catch-finally` | All servlets | Ensure DB connections are always closed |
| PBKDF2 hashing | `PasswordUtil.java` | Secure password storage with salt |
| Packages | All classes | Organise by role (admin/doctor/patient/common) |
| `static` methods | `NavHelper.java` | Shared utility functions without object state |
| `StringBuilder` | `DBConnection.java` | Efficient dynamic SQL building |
| `DatabaseMetaData` | `DBConnection.java` | Auto-detect and migrate old schema |
| `@MultipartConfig` | `AdminAddDoctorServlet` | Enable binary file upload (doctor images) |
| `web.xml` | `WEB-INF/web.xml` | Authoritative servlet-to-URL mapping |
| `volatile` + `synchronized` | `DBConnection.java` | Thread-safe one-time initialisation |
| Properties file | `db.properties` | Externalise credentials from source code |
| Constant-time compare | `PasswordUtil.java` | Prevent timing attacks on login |
| `RETURN_GENERATED_KEYS` | `DoctorRegistrationServlet` | Get auto-increment ID after INSERT |

---

*This project is a practical example of core Java SE and Java EE (Servlet/JDBC) concepts working together in a real-world multi-role web application.*
