package com.hteck.playtube.common;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

public class RC4 {
	private static final int SENCRYPT_LENGTH = 256;
	private static final int KEY_MIN_LENGTH = 5;
	private byte[] keyValue;
	private byte[] sbox;

	public RC4(String keyValue)  throws Exception{
		setKeyValue(keyValue);
	}

	public RC4() {
	}

	public byte[] decrypt(final byte[] msg) {
		return encrypt(msg);
	}

	public byte[] encrypt(final byte[] msg) {
		sbox = initSBox(keyValue);
		byte[] code = new byte[msg.length];
		int i = 0;
		int j = 0;
		for (int n = 0; n < msg.length; n++) {
			i = (i + 1) & 255;
			j = (j + sbox[i]) & 255;
			swap(i, j, sbox);
			int rand = sbox[(sbox[i] + sbox[j]) & 255];
			code[n] = (byte) (rand ^ (int) msg[n]);
		}
		return code;
	}

	private byte[] initSBox(byte[] key) {
		byte[] sbox = new byte[SENCRYPT_LENGTH];
		int j = 0;

		for (int i = 0; i < SENCRYPT_LENGTH; i++) {
			sbox[i] = (byte)i;
		}

		for (int i = 0; i < SENCRYPT_LENGTH; i++) {
			j = (j + sbox[i] + key[i % key.length]) & 255;
			swap(i, j, sbox);
		}
		return sbox;
	}

	private void swap(int i, int j, byte[] sbox) {
		byte temp = sbox[i];
		sbox[i] = sbox[j];
		sbox[j] = temp;
	}

	public void setKeyValue(String keyValue) throws Exception {
		if (!(keyValue.length() >= KEY_MIN_LENGTH && keyValue.length() < SENCRYPT_LENGTH)) {
			throw new Exception("Key length has to be between "
					+ KEY_MIN_LENGTH + " and " + (SENCRYPT_LENGTH - 1));
		}

		this.keyValue = keyValue.getBytes("UTF-16LE");
	}
}
