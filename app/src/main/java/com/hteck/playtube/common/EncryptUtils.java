package com.hteck.playtube.common;

import android.util.Base64;

import java.security.InvalidKeyException;

public class EncryptUtils {
    public static byte[] encrypt(byte[] key, byte[] data)
    {
        return encryptOutput(key, data);
    }

    public static byte[] decrypt(byte[] key, byte[] data)
    {
        return encryptOutput(key, data);
    }

    private static byte[] encryptInitalize(byte[] key)
    {
    	byte[] s = new byte[256];
    	for (int i = 0; i < 256; i++) {
    		s[i] = (byte)i;
    	}

        for (int i = 0, j = 0; i < 256; i++)
        {
            j = (j + key[i % key.length] + s[i]) & 255;

            swap(s, i, j);
        }

        return s;
    }

    private static byte[] encryptOutput(byte[] key, byte[] data)
    {
        byte[] s = encryptInitalize(key);

        int i = 0;
        int j = 0;

        byte[] result = new byte[data.length];
        for (int index = 0; index < data.length; ++index) {
        	i = (i + 1) & 255;
        	j = (j + s[i]) & 255;
        	swap(s, i, j);
        	result[index] = (byte)(data[index] ^ s[(s[i] + s[j]) & 255]);
        }
        return result;
    }

    private static void swap(byte[] s, int i, int j)
    {
        byte c = s[i];

        s[i] = s[j];
        s[j] = c;
    }

    private static final String PRIVATEKEY = "pt_android";

    public static String decrypt(String input)
    {
        try {
            try {
                RC4 rc4 = new RC4(PRIVATEKEY);
                byte[] result =  Base64.decode(input, Base64.DEFAULT);
                String result1 = new String(rc4.decrypt(result), "UTF-16LE");
                return result1;
            } catch (InvalidKeyException e) {
                System.err.println(e.getMessage());
            }
            return "";
        }
        catch(Exception e) {
            return "";
        }
    }
}
