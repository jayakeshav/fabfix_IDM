
package edu.uci.ics.jkotha.service.idm;

import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;
import edu.uci.ics.jkotha.service.idm.security.Crypto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {
    public static void main(String[] args) {


        String email = "johndoe@uci811.edu";
        System.out.println(FunctionsRequired.isValidEmail(email));

//        char[] password = {'d','2','d','4','7','@','3','S'};
//        System.out.println(password);
//        byte[] salt=Crypto.genSalt();
//        byte[] hashedpassword = Crypto.hashPassword(password,salt);
//        String saltString =  FunctionsRequired.getHashedPass(salt);
//        byte[] saltRebuilt = FunctionsRequired.toByteArray(saltString);
//        byte[] rebuithasedPassword = Crypto.hashPassword(password,saltRebuilt);
//       try {
//           System.out.println(FunctionsRequired.getHashedPass(salt));
//           System.out.println(FunctionsRequired.getHashedPass(hashedpassword));
//           System.out.println(saltString);
//           System.out.println(FunctionsRequired.getHashedPass(saltRebuilt));
//           System.out.println(FunctionsRequired.getHashedPass(rebuithasedPassword));
//
//       }catch (Exception e){
//           e.printStackTrace();
//       }
    }

    private static boolean isValidEmail(String email){
        String[] components1 = email.split("@",0);
        //System.out.println(components1.length);
        if (components1.length!=2)
            return false;
        String[] components2 = components1[1].split("\\.",0);
        //System.out.println(components2.length);
        if (components2.length!=2)
            return false;
        return true;
    }

    private static boolean isValidPassowrd(char[] password){
        String pattern = "((?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%]).{7,16})";
        Pattern p = Pattern.compile(pattern);
        Matcher matcher = p.matcher(new String(password));
        return matcher.matches();

    }
}
