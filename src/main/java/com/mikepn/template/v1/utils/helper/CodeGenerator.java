package com.mikepn.template.v1.utils.helper;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Component
public class CodeGenerator {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public static String generateEmployeeCode() {
        String datePart = dateFormat.format(new Date());
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "EMP-" + datePart + "-" + randomPart;
    }

    public static String generateDeductionCode() {
        String datePart = dateFormat.format(new Date());
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "DED-" + datePart + "-" + randomPart;
    }
}
