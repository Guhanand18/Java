package com.bnpparibas.service;

import com.bnpparibas.dao.EmployeeDAO;
import com.bnpparibas.dao.ImportStatsDAO;
import com.bnpparibas.model.Employee;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class FileService {

    private EmployeeDAO employeeDAO = new EmployeeDAO();
    private ImportStatsDAO statsDAO = new ImportStatsDAO();

    // ================= ENTRY =================
    public ImportResult processFile(File file, String user) {

        String name = file.getName().toLowerCase();

        if (name.endsWith(".csv")) {
            return importCsv(file, user);
        } else if (name.endsWith(".txt")) {
            return importTxt(file, user);
        }

        System.out.println("Unsupported file type");
        return new ImportResult(0, 0);
    }

    // ================= CSV =================
    private ImportResult importCsv(File file, String user) {

        int valid = 0, invalid = 0;
        List<String> badLines = new ArrayList<>();
        List<Employee> saved = new ArrayList<>();
        Set<String> duplicateCheck = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty() || line.toLowerCase().startsWith("first"))
                    continue;

                Employee emp = parseCsv(line);

                if (!isValid(emp, duplicateCheck)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                prepareEmployee(emp, user, file.getName());

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
        Set<String> duplicateCheck = new HashSet<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;

            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                Employee emp = parseTxt(line);

                if (!isValid(emp, duplicateCheck)) {
                    invalid++;
                    badLines.add(line);
                    continue;
                }

                prepareEmployee(emp, user, file.getName());

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
    private Employee parseCsv(String line) {

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

    private Employee parseTxt(String line) {

        String[] parts = line.trim().split("\\s+");

        if (parts.length < 9) return null;

        int emailIndex = -1;

        for (int i = 0; i < parts.length; i++) {
            if (parts[i].contains("@")) {
                emailIndex = i;
                break;
            }
        }

        if (emailIndex == -1) return null;

        Employee e = new Employee();

        e.setFirstName(parts[0]);
        e.setLastName(parts[1]);
        e.setDepartment(parts[2]);

        StringBuilder pos = new StringBuilder();
        for (int i = 3; i < emailIndex; i++) {
            pos.append(parts[i]).append(" ");
        }

        e.setPosition(pos.toString().trim());
        e.setEmail(parts[emailIndex]);
        e.setPhone(parts[emailIndex + 1]);

        e.setDob(parseDate(parts[parts.length - 2]));
        e.setHireDate(parseDate(parts[parts.length - 1]));

        return e;
    }

    // ================= VALIDATION =================
    private boolean isValid(Employee e, Set<String> dupSet) {

        if (e == null) return false;

        if (isEmpty(e.getFirstName()) || isEmpty(e.getLastName())) return false;
        if (isEmpty(e.getDepartment()) || isEmpty(e.getPosition())) return false;

        if (!e.getEmail().matches(".+@.+\\..+")) return false;
        if (!e.getPhone().matches("[6-9]\\d{9}")) return false;

        if (e.getDob() == null || e.getHireDate() == null) return false;

        if (!isAdult(e.getDob(), e.getHireDate())) return false;

        if (employeeDAO.existsByDobAndPhone(e.getDob(), e.getPhone())) return false;

        String key = e.getDob() + "|" + e.getPhone();
        if (dupSet.contains(key)) return false;

        dupSet.add(key);
        return true;
    }

    // ================= HELPERS =================
    private void prepareEmployee(Employee e, String user, String fileName) {
        e.setEmpId(generateEmpId());
        e.setCreatedby(user);
        e.setStatus("NEW");
        e.setFilename(fileName);
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Date parseDate(String txt) {
        try {
            return java.sql.Date.valueOf(LocalDate.parse(txt.trim()));
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAdult(Date dob, Date hireDate) {

        LocalDate d1 = new java.sql.Date(dob.getTime()).toLocalDate();
        LocalDate d2 = new java.sql.Date(hireDate.getTime()).toLocalDate();

        return Period.between(d1, d2).getYears() >= 20;
    }

    private static int counter = 1;

    private String generateEmpId() {
        return String.format("%03d", counter++);
    }
}
