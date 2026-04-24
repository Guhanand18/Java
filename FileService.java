package com.bnpparibas.service;

import com.bnpparibas.dao.EmployeeDAO;
import com.bnpparibas.dao.ImportStatsDAO;
import com.bnpparibas.model.Employee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

public class FileService {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final ImportStatsDAO statsDAO = new ImportStatsDAO();

    private static final SimpleDateFormat DOB_FMT = new SimpleDateFormat("yyyy-MM-dd");

    // ENTRY
    public ImportResult processFile(File file, String currentUser) {

        String lower = file.getName().toLowerCase();

        if (lower.endsWith(".csv")) {
            return importCsv(file, currentUser);
        } else if (lower.endsWith(".txt")) {
            return importTxt(file, currentUser);
        }

        System.err.println("Unsupported file type");
        return new ImportResult(0, 0);
    }

    // ================= CSV =================
    private ImportResult importCsv(File file, String user) {

        int valid = 0, invalid = 0;
        List<String> badLines = new ArrayList<>();
        List<Employee> saved = new ArrayList<>();
        Set<String> dupSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty() || line.toLowerCase().startsWith("first"))
                    continue;

                Employee emp = parseCsvLine(line);

                if (emp == null) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                if (isDuplicateInImport(emp, dupSet) || !validateEmployee(emp)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                emp.setEmpId(generateEmpId());
                emp.setCreatedby(user);
                emp.setStatus("NEW");
                emp.setFilename(file.getName());

                if (employeeDAO.createEmployee(emp) != null) {
                    valid++;
                    saved.add(emp);
                } else {
                    invalid++;
                    badLines.add(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Long importId = statsDAO.recordImport(user, valid, invalid);

        for (Employee e : saved) {
            e.setImportId(importId);
            employeeDAO.updateImportInfo(e);
        }

        return new ImportResult(valid, invalid, badLines);
    }

    // ================= TXT =================
    private ImportResult importTxt(File file, String user) {

        int valid = 0, invalid = 0;
        List<String> badLines = new ArrayList<>();
        List<Employee> saved = new ArrayList<>();
        Set<String> dupSet = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty())
                    continue;

                Employee emp = parseSpaceSeparatedLine(line);

                if (emp == null) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                if (isDuplicateInImport(emp, dupSet) || !validateEmployee(emp)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                emp.setEmpId(generateEmpId());
                emp.setCreatedby(user);
                emp.setStatus("NEW");
                emp.setFilename(file.getName());

                if (employeeDAO.createEmployee(emp) != null) {
                    valid++;
                    saved.add(emp);
                } else {
                    invalid++;
                    badLines.add(line);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Long importId = statsDAO.recordImport(user, valid, invalid);

        for (Employee e : saved) {
            e.setImportId(importId);
            employeeDAO.updateImportInfo(e);
        }

        return new ImportResult(valid, invalid, badLines);
    }

    // ================= PARSERS =================
    private Employee parseCsvLine(String line) {

        String[] t = line.split(",");

        if (t.length < 9) return null;

        Employee e = new Employee();
        e.setFirstName(t[0].trim());
        e.setLastName(t[1].trim());
        e.setDepartment(t[2].trim());
        e.setPosition(t[3].trim());
        e.setEmail(t[4].trim());
        e.setPhone(t[5].trim());
        e.setAddress(t[6].trim());
        e.setDob(parseDate(t[7]));
        e.setHireDate(parseDate(t[8]));

        return e;
    }

    private Employee parseSpaceSeparatedLine(String line) {

        String[] raw = line.trim().split("\\s+");
        if (raw.length < 9) return null;

        int emailIdx = -1;
        for (int i = 0; i < raw.length; i++) {
            if (raw[i].matches(".+@.+\\..+")) {
                emailIdx = i;
                break;
            }
        }

        if (emailIdx == -1) return null;

        Employee e = new Employee();

        e.setFirstName(raw[0]);
        e.setLastName(raw[1]);
        e.setDepartment(raw[2]);

        StringBuilder pos = new StringBuilder();
        for (int i = 3; i < emailIdx; i++) {
            pos.append(raw[i]).append(" ");
        }
        e.setPosition(pos.toString().trim());

        e.setEmail(raw[emailIdx]);

        String phone = raw[emailIdx + 1];
        e.setPhone(phone);

        e.setDob(parseDate(raw[raw.length - 2]));
        e.setHireDate(parseDate(raw[raw.length - 1]));

        return e;
    }

    // ================= VALIDATION =================
    private boolean validateEmployee(Employee e) {

        if (e == null) return false;

        if (isBlank(e.getFirstName()) || !e.getFirstName().matches("[A-Za-z .'-]+")) return false;
        if (isBlank(e.getLastName()) || !e.getLastName().matches("[A-Za-z .'-]+")) return false;
        if (isBlank(e.getDepartment())) return false;
        if (isBlank(e.getPosition())) return false;

        if (isBlank(e.getEmail()) || !e.getEmail().matches(".+@.+\\..+")) return false;

        if (isBlank(e.getPhone()) || !e.getPhone().matches("[6-9]\\d{9}")) return false;

        if (e.getDob() == null || e.getHireDate() == null) return false;

        if (!isAtLeastYearsBetween(e.getDob(), e.getHireDate(), 20)) return false;

        if (employeeDAO.existsByDobAndPhone(e.getDob(), e.getPhone())) return false;

        return true;
    }

    // ================= HELPERS =================
    private boolean isDuplicateInImport(Employee e, Set<String> seen) {
        String key = DOB_FMT.format(e.getDob()) + "|" + e.getPhone();
        if (seen.contains(key)) return true;
        seen.add(key);
        return false;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Date parseDate(String txt) {
        try {
            return java.sql.Date.valueOf(LocalDate.parse(txt.trim()));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAtLeastYearsBetween(Date start, Date end, int years) {
        long diff = end.getTime() - start.getTime();
        return (diff / (1000L * 60 * 60 * 24 * 365)) >= years;
    }

    private String generateEmpId() {
        return String.format("%03d", new Random().nextInt(999));
    }
}