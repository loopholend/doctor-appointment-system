package common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Centralized database connection utility.
 * Reads credentials from db.properties on the classpath (src/java/db.properties).
 * Automatically migrates old table/column names to new schema on first connection.
 */
public class DBConnection {

    private static final Properties props = new Properties();
    private static final int PASSWORD_COLUMN_LENGTH = 255;
    private static volatile boolean schemaValidated = false;

    static {
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                System.err.println("WARNING: db.properties not found on classpath. Using hardcoded fallback values.");
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/medical_appointment_system?useSSL=false");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "root");
                props.setProperty("admin.password", "Admin@1234");
            }
        } catch (IOException e) {
            System.err.println("ERROR loading db.properties: " + e.getMessage());
        }
    }

    /**
     * Returns a new JDBC connection using credentials from db.properties.
     * Ensures schema migration is performed on first connection.
     */
    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        Connection conn = DriverManager.getConnection(
            props.getProperty("db.url"),
            props.getProperty("db.user"),
            props.getProperty("db.password")
        );
        ensureSchemaUpToDate(conn);
        return conn;
    }

    /**
     * Returns the admin password from db.properties.
     */
    public static String getAdminPassword() {
        return props.getProperty("admin.password", "Admin@1234");
    }

    private static void ensureSchemaUpToDate(Connection conn) throws SQLException {
        if (schemaValidated) {
            return;
        }

        synchronized (DBConnection.class) {
            if (schemaValidated) {
                return;
            }

            System.out.println("DBConnection: Starting schema validation and migration...");
            migrateSchema(conn);
            ensurePasswordColumnCapacity(conn, "patients");
            ensurePasswordColumnCapacity(conn, "doctor_accounts");
            schemaValidated = true;
            System.out.println("DBConnection: Schema validation complete.");
        }
    }

    private static void migrateSchema(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Migrate table: patient -> patients
            if (tableExists(metaData, "patient") && !tableExists(metaData, "patients")) {
                System.out.println("  Renaming table: patient -> patients");
                stmt.executeUpdate("RENAME TABLE patient TO patients");
            }
            
            // Migrate table: doctors -> doctor_accounts
            if (tableExists(metaData, "doctors") && !tableExists(metaData, "doctor_accounts")) {
                System.out.println("  Renaming table: doctors -> doctor_accounts");
                stmt.executeUpdate("RENAME TABLE doctors TO doctor_accounts");
            }
            
            // Migrate table: doctor_info -> doctor_profiles
            if (tableExists(metaData, "doctor_info") && !tableExists(metaData, "doctor_profiles")) {
                System.out.println("  Renaming table: doctor_info -> doctor_profiles");
                stmt.executeUpdate("RENAME TABLE doctor_info TO doctor_profiles");
            }
            
            // Migrate table: medical_details -> patient_medical_records
            if (tableExists(metaData, "medical_details") && !tableExists(metaData, "patient_medical_records")) {
                System.out.println("  Renaming table: medical_details -> patient_medical_records");
                stmt.executeUpdate("RENAME TABLE medical_details TO patient_medical_records");
            }
            
            // Migrate columns in patients table
            if (tableExists(metaData, "patients")) {
                renameColumn(stmt, metaData, "patients", "name", "full_name", "VARCHAR(100)");
                renameColumn(stmt, metaData, "patients", "CN", "contact_number", "VARCHAR(15)");
                renameColumn(stmt, metaData, "patients", "email", "email_address", "VARCHAR(100)");
                renameColumn(stmt, metaData, "patients", "password", "password_hash", "VARCHAR(255)");
            }
            
            // Migrate columns in doctor_accounts table
            if (tableExists(metaData, "doctor_accounts")) {
                renameColumn(stmt, metaData, "doctor_accounts", "name", "full_name", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_accounts", "CN", "contact_number", "VARCHAR(15)");
                renameColumn(stmt, metaData, "doctor_accounts", "email", "email_address", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_accounts", "password", "password_hash", "VARCHAR(255)");
            }
            
            // Migrate columns in doctor_profiles table
            if (tableExists(metaData, "doctor_profiles")) {
                renameColumn(stmt, metaData, "doctor_profiles", "email", "email_address", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_profiles", "primary_speciality", "primary_specialty", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_profiles", "secondary_speciality", "secondary_specialty", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_profiles", "clinic_visit_time_slot", "clinic_visit_schedule", "VARCHAR(100)");
                renameColumn(stmt, metaData, "doctor_profiles", "about_doctor", "professional_bio", "TEXT");
                renameColumn(stmt, metaData, "doctor_profiles", "bio", "professional_bio", "TEXT");
                renameColumn(stmt, metaData, "doctor_profiles", "approved_by", "approved_by_admin", "VARCHAR(100)");
            }
            
            // Migrate columns in patient_medical_records table
            if (tableExists(metaData, "patient_medical_records")) {
                renameColumn(stmt, metaData, "patient_medical_records", "medical_id", "record_id", "INT");
                renameColumn(stmt, metaData, "patient_medical_records", "patient_username", "patient_account_username", "VARCHAR(50)");
                renameColumn(stmt, metaData, "patient_medical_records", "blood_group", "blood_type", "VARCHAR(10)");
                renameColumn(stmt, metaData, "patient_medical_records", "diabetes", "diabetes_status", "VARCHAR(20)");
                renameColumn(stmt, metaData, "patient_medical_records", "thyroid", "thyroid_status", "VARCHAR(20)");
                renameColumn(stmt, metaData, "patient_medical_records", "bp", "blood_pressure_status", "VARCHAR(20)");
                renameColumn(stmt, metaData, "patient_medical_records", "asthma", "has_asthma", "VARCHAR(10)");
                renameColumn(stmt, metaData, "patient_medical_records", "allergies", "has_allergies", "VARCHAR(10)");
                renameColumn(stmt, metaData, "patient_medical_records", "surgeries", "has_previous_surgeries", "VARCHAR(10)");
            }
            
            // Migrate columns in appointments table
            if (tableExists(metaData, "appointments")) {
                renameColumn(stmt, metaData, "appointments", "patient_username", "patient_account_username", "VARCHAR(50)");
                renameColumn(stmt, metaData, "appointments", "status", "appointment_status", "VARCHAR(20)");
            }
        }
    }

    private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private static void renameColumn(Statement stmt, DatabaseMetaData metaData, String tableName, 
                                     String oldColumnName, String newColumnName, String columnType) throws SQLException {
        if (columnExists(metaData, tableName, oldColumnName) && !columnExists(metaData, tableName, newColumnName)) {
            System.out.println("  Renaming column: " + tableName + "." + oldColumnName + " -> " + newColumnName);
            stmt.executeUpdate("ALTER TABLE " + tableName + " CHANGE COLUMN " + oldColumnName + " " + newColumnName + " " + columnType);
        }
    }

    private static void ensurePasswordColumnCapacity(Connection conn, String tableName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, "password_hash")) {
            if (!rs.next()) {
                return;
            }

            int columnSize = rs.getInt("COLUMN_SIZE");
            boolean nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
            String defaultValue = rs.getString("COLUMN_DEF");

            if (columnSize >= PASSWORD_COLUMN_LENGTH) {
                return;
            }

            StringBuilder alterSql = new StringBuilder()
                .append("ALTER TABLE ")
                .append(tableName)
                .append(" MODIFY COLUMN password_hash VARCHAR(")
                .append(PASSWORD_COLUMN_LENGTH)
                .append(") ")
                .append(nullable ? "NULL" : "NOT NULL");

            if (defaultValue != null) {
                alterSql.append(" DEFAULT '").append(defaultValue.replace("'", "''")).append("'");
            }

            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(alterSql.toString());
            }
        }
    }
}
