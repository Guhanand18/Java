package com.bnpparibas.service;

import com.bnpparibas.dao.EmployeeDAO;
import com.bnpparibas.dao.ImportStatsDAO;
import com.bnpparibas.model.Employee;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class FileService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ImportStatsDAO statsDAO = new ImportStatsDAO();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    // ================= MAIN ENTRY =================
    public ImportResult processFile(File file, String currentUser) {

        String name = file.getName().toLowerCase();

        if (name.endsWith(".csv")) {
            return importCsv(file, currentUser);
        } else if (name.endsWith(".txt")) {
            return importTxt(file, currentUser);
        } else {
            System.out.println("Unsupported file type");
            return new ImportResult(0, 0);
        }
    }

    // ================= CSV IMPORT =================
    private ImportResult importCsv(File file, String user) {

        int valid = 0;
        int invalid = 0;

        List<String> badLines = new ArrayList<>();
        List<Employee> savedEmployees = new ArrayList<>();

        Set<String> duplicateSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(file)))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.toLowerCase().startsWith("first"))
                    continue;

                Employee emp = parseCsv(line);

                if (emp == null) {
                    logInvalid("CSV", line, "Parsing failed");
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                // duplicate inside file
                if (isDuplicate(emp, duplicateSet)) {
                    logInvalid("CSV", line, "Duplicate in file");
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                // validation + DB duplicate
                if (!validate(emp)) {
                    logInvalid("CSV", line, "Validation failed");
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                // SAVE
                emp.setEmpId(generateEmpId());
                emp.setCreatedBy(user);
                emp.setStatus("NEW");
                emp.setFilename(file.getName());

                String res = employeeDAO.createEmployee(emp);

                if (res != null) {
                    valid++;
                    savedEmployees.add(emp);
                } else {
                    logInvalid("CSV", line, "DB insert failed");
                    invalid++;
                    badLines.add(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        statsDAO.recordImport(user, valid, invalid);

        return new ImportResult(valid, invalid, badLines);
    }

    // ================= TXT IMPORT =================
    private ImportResult importTxt(File file, String user) {

        int valid = 0;
        int invalid = 0;

        List<String> badLines = new ArrayList<>();
        Set<String> duplicateSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.isEmpty() || line.toLowerCase().startsWith("first"))
                    continue;

                Employee emp = parseTxt(line);

                if (emp == null) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                if (isDuplicate(emp, duplicateSet)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                if (!validate(emp)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                emp.setEmpId(generateEmpId());
                emp.setCreatedBy(user);
                emp.setStatus("NEW");
                emp.setFilename(file.getName());

                String res = employeeDAO.createEmployee(emp);

                if (res != null) valid++;
                else invalid++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        statsDAO.recordImport(user, valid, invalid);

        return new ImportResult(valid, invalid, badLines);
    }

    // ================= PARSERS =================
    private Employee parseCsv(String line) {

        try {
            String[] t = line.split(",");

            if (t.length != 8) return null;

            Employee e = new Employee();
            e.setFirstName(t[0].trim());
            e.setLastName(t[1].trim());
            e.setDepartment(t[2].trim());
            e.setPosition(t[3].trim());
            e.setEmail(t[4].trim());
            e.setPhone(t[5].trim());
            e.setAddress(t[6].trim());
            e.setDob(parseDate(t[7]));

            return e;

        } catch (Exception e) {
            return null;
        }
    }

    private Employee parseTxt(String line) {

        try {
            String[] t = line.split("\\s+");

            if (t.length < 8) return null;

            Employee e = new Employee();
            e.setFirstName(t[0]);
            e.setLastName(t[1]);
            e.setDepartment(t[2]);
            e.setPosition(t[3]);
            e.setEmail(t[4]);
            e.setPhone(t[5]);
            e.setAddress(t[6]);
            e.setDob(parseDate(t[7]));

            return e;

        } catch (Exception e) {
            return null;
        }
    }

    // ================= VALIDATION =================
    private boolean validate(Employee e) {

        if (isBlank(e.getFirstName())) return false;
        if (isBlank(e.getEmail()) || !e.getEmail().contains("@")) return false;

        // Indian phone rule
        if (!e.getPhone().matches("[6-9]\\d{9}")) return false;

        if (e.getDob() == null) return false;

        // DB duplicate
        if (employeeDAO.existsByDobAndPhone(e.getDob(), e.getPhone()))
            return false;

        return true;
    }

    // ================= DUPLICATE =================
    private boolean isDuplicate(Employee e, Set<String> set) {

        String key = e.getDob() + "|" + e.getPhone();

        if (set.contains(key)) return true;

        set.add(key);
        return false;
    }

    // ================= UTIL =================
    private Date parseDate(String txt) {
        try {
            return DATE_FMT.parse(txt);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String generateEmpId() {
        int id = new Random().nextInt(1000);
        return String.format("%03d", id);
    }

    private void logInvalid(String source, String line, String reason) {
        System.out.println("[INVALID][" + source + "] " + reason + " -> " + line);
    }
}
