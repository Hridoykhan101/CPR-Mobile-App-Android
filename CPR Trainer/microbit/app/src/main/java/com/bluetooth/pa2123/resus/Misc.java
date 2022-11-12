package com.bluetooth.pa2123.resus;

public class Misc {
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static String hexToAscii(String hexStr) {
        StringBuilder output = new StringBuilder("");

        for (int i = 0; i < hexStr.length(); i += 2) {
            String str = hexStr.substring(i, i + 2);
            output.append((char) Integer.parseInt(str, 16));
        }

        return output.toString();
    }

    public static String hexToMac(String hex) {
        //After every second char, we will append an ':' to make the mac address valid, and make sure that all chars are capitalized
        String HEX = hex.toUpperCase();
        StringBuilder mac = new StringBuilder();
        for(int i=0; i<HEX.length(); i++) {
            mac.append(HEX.charAt(i));

            //If even
            if (i % 2 == 1 && i != HEX.length()-1) {
                mac.append(':');
            }
        }

        return mac.toString();
    }
}
