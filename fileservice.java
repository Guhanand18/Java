package com.bnpparibas.service;

import com.bnpparibas.dao.EmployeeDAO;
import com.bnpparibas.dao.ImportStatsDAO;
import com.bnpparibas.model.Employee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class FileService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ImportStatsDAO statsDAO = new ImportStatsDAO();

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd");

    /* ================= ENTRY ================= */
    public ImportResult processFile(File file, String user) {

        String name = file.getName().toLowerCase();

        if (name.endsWith(".csv")) {
            return process(file, user, "CSV");
        } else if (name.endsWith(".txt")) {
            return process(file, user, "TXT");
        }

        return new ImportResult(0, 0);
    }

    /* ================= MAIN PROCESS ================= */
    private ImportResult process(File file, String user, String type) {

        int valid = 0, invalid = 0;
        List<String> badLines = new ArrayList<>();
        List<Employee> savedEmployees = new ArrayList<>();
        Set<String> duplicateCheck = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty() || line.toLowerCase().startsWith("first")) continue;

                Employee emp = type.equals("CSV")
                        ? parseCsv(line)
                        : parseTxt(line);

                if (emp == null || isDuplicate(emp, duplicateCheck) || !isValid(emp)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                // assign values
                emp.setEmpId(generateEmpId());
                emp.setCreatedBy(user);
                emp.setStatus("NEW");
                emp.setFilename(file.getName());

                String result = employeeDAO.createEmployee(emp);

                if (result != null) {
                    valid++;
                    savedEmployees.add(emp);
                } else {
                    invalid++;
                    badLines.add(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // record stats
        Long importId = statsDAO.recordImport(user, valid, invalid);

        if (importId != null) {
            for (Employee e : savedEmployees) {
                e.setImportId(importId);
                employeeDAO.updateImportInfo(e);
            }
        }

        return new ImportResult(valid, invalid, badLines);
    }

    /* ================= VALIDATION ================= */
    private boolean isValid(Employee e) {

        if (e == null) return false;

        if (isBlank(e.getFirstName()) || isBlank(e.getLastName())) return false;
        if (isBlank(e.getDepartment()) || isBlank(e.getPosition())) return false;

        if (isBlank(e.getEmail()) || !e.getEmail().matches("^.+@.+\\..+$")) return false;
        if (isBlank(e.getPhone()) || !e.getPhone().matches("[6-9]\\d{9}")) return false;

        if (e.getDob() == null || e.getHireDate() == null) return false;
        if (e.getHireDate().before(e.getDob())) return false;

        if (!isAdult(e.getDob(), e.getHireDate())) return false;

        if (employeeDAO.existsByDobAndPhone(e.getDob(), e.getPhone())) return false;

        return true;
    }

    /* ================= HELPERS ================= */
    private boolean isAdult(Date dob, Date hire) {
        long diff = hire.getTime() - dob.getTime();
        double years = diff / (365.2425 * 24 * 60 * 60 * 1000);
        return years >= 20;
    }

    private boolean isDuplicate(Employee e, Set<String> set) {
        String key = DATE_FMT.format(e.getDob()) + "|" + e.getPhone();
        if (set.contains(key)) return true;
        set.add(key);
        return false;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Date parseDate(String s) {
        try {
            return DATE_FMT.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    /* ================= PARSERS ================= */
    private Employee parseCsv(String line) {

        String[] t = line.split(",");
        if (t.length != 9) return null;

        Employee e = new Employee();
        e.setFirstName(t[0].trim());
        e.setLastName(t[1].trim());
        e.setDepartment(t[2].trim());
        e.setPosition(t[3].trim());
        e.setEmail(t[4].trim());
        e.setPhone(t[5].trim());
        e.setAddress(t[6].trim());
        e.setDob(parseDate(t[7].trim()));
        e.setHireDate(parseDate(t[8].trim()));

        return e;
    }

    private Employee parseTxt(String line) {

        String[] t = line.split("\\s+");
        if (t.length < 9) return null;

        Employee e = new Employee();
        e.setFirstName(t[0]);
        e.setLastName(t[1]);
        e.setDepartment(t[2]);
        e.setPosition(t[3]);
        e.setEmail(t[4]);
        e.setPhone(t[5]);
        e.setAddress(t[6]);
        e.setDob(parseDate(t[7]));
        e.setHireDate(parseDate(t[8]));

        return e;
    }

    /* ================= EMP ID ================= */
    private String generateEmpId() {
        return String.valueOf(System.currentTimeMillis()).substring(7);
    }
}