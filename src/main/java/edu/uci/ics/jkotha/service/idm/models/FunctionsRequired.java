package edu.uci.ics.jkotha.service.idm.models;

import edu.uci.ics.jkotha.service.idm.logger.ServiceLogger;
import edu.uci.ics.jkotha.service.idm.security.Token;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionsRequired {
    public static boolean isValidEmail(String email) {
        //String blanck = " ";
        String[] components1 = email.split("@", 0);
        //System.out.println(components1[0].length());
        if (components1.length != 2 | components1[0].length() == 0)
            return false;
        String[] components2 = components1[1].split("\\.", 0);
        //System.out.println(components2.length);
        if (components2.length < 2) {
            return false;
        } else {
            if (components2[0].length() == 0 | components2[1].length() == 0)
                return false;
        }

        return true;
    }

//    public boolean isValidEmailAddress(String email) {
//        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
//        java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
//        java.util.regex.Matcher m = p.matcher(email);
//        return m.matches();
//    }

    public static boolean isValidPassowrd(char[] password) {
        boolean isLowerCase = false,
                isUppperCase = false,
                isNumber = false,
                isSpecialChar = false,
                result = false;
        for (char c : password) {
            if (Character.isUpperCase(c)) {
                isUppperCase = true;
            } else if (Character.isLowerCase(c)) {
                isLowerCase = true;
            } else if (Character.isDigit(c)) {
                isNumber = true;
            } else {
                if ((c > 32 & c < 48) | (c > 57 & c < 65) | (c > 90 & c < 97) | (c > 122 & c < 127))
                    isSpecialChar = true;
            }
            result = isLowerCase & isNumber & isSpecialChar & isUppperCase;
            if (result)
                break;
        }
        return result;

//        String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{7,16})";
//        Pattern p = Pattern.compile(pattern);
//        Matcher matcher = p.matcher(new String(password));
//        return matcher.matches();

    }

    public static boolean isPasswordSame(char[] password1, char[] password2) {
        String passwordA = new String(password1);
        String passwordB = new String(password2);
        if (passwordA.length() != passwordB.length())
            return false;
        for (int i = 0; i < password1.length; ++i) {
            if (passwordA.charAt(i) != passwordB.charAt(i))
                return false;
        }
        return true;
    }

    public static char[] toCharArray(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();
        for (byte b : byteArray) {
            sb.append(format(Integer.toHexString(Byte.toUnsignedInt(b))));
        }
        return sb.toString().toCharArray();
    }

    private static String format(String binS) {
        int length = 2 - binS.length();
        char[] padArray = new char[length];
        Arrays.fill(padArray, '0');
        String padString = new String(padArray);
        return padString + binS;
    }

    public static int getPlevel(String plevel) {
        plevel.toLowerCase();
        if (plevel.equals("root"))
            return 1;
        else if (plevel.equals("admin"))
            return 2;
        else if (plevel.equals("employee"))
            return 3;
        else if (plevel.equals("Service"))
            return 4;
        else if (plevel.equals("user"))
            return 5;
        return -1;
    }

    public static String getPlevel(int plevel) {
        switch (plevel) {
            case 1:
                return "ROOT";
            case 2:
                return "ADMIN";
            case 3:
                return "EMPLOYEE";
            case 4:
                return "SERVICE";
            case 5:
                return "USER";
        }
        return null;
    }

    public static String toStringforDB(byte[] input) {
        return new String(input);
    }

    public static String getHashedPass(byte[] hashedPassword) {
        StringBuffer buf = new StringBuffer();
        for (byte b : hashedPassword) {
            buf.append(format(Integer.toHexString(Byte.toUnsignedInt(b))));
        }
        return buf.toString();
    }

    public static UserModel[] toUserArray(ResultSet resultSet) {
        ArrayList<UserModel> finalResult = new ArrayList<>();

        try {
            while (resultSet.next()) {

                UserModel result1 = new UserModel(resultSet.getInt("id"),
                        resultSet.getString("email"),
                        FunctionsRequired.getPlevel(resultSet.getInt("plevel")));
                finalResult.add(result1);
                //ServiceLogger.LOGGER.info(result1.toString());
            }
        } catch (SQLException e) {
            ServiceLogger.LOGGER.info("SQL exception");
        }
        UserModel[] result = new UserModel[finalResult.size()];
        for (int i = 0; i < result.length; i++) {
            //ServiceLogger.LOGGER.info("user:"+i);
            result[i] = finalResult.get(i);
            //ServiceLogger.LOGGER.info(result[i].toString());
        }
        return result;
    }

    public static byte[] toByteArray(String input) {
        return Token.convert(input);
    }
}
