package com.partners.weather.encrypt;
import java.io.UnsupportedEncodingException;

public class RC4Crypt {

	private final String mainKey="ajvGGILjwtZ30feLFmLn";
	private byte[] box;
	public String plaintext;
	public byte[] bytePlaintext;
	public byte[] result;
	public byte[] subKey;
	public int length;

	public RC4Crypt() {
		makeBox();
	}
	public RC4Crypt(String text) {
		plaintext = text;
		makeBox();
		getBytePlaintext();
		try {
			length = bytePlaintext.length;
			getSubKey();
			encrypt();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	public RC4Crypt(byte[] textBs) {
		result = textBs;
		makeBox();
		try {
			length = result.length;
			getSubKey();
			decrypt();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	private void makeBox() {
		box = new byte[256];
		for (int i = 0; i < 256; i++) {
			if (i < 127) {
				box[i] = (byte) i;
			} else {
				box[i] = (byte) (i - 256);
			}
		}
	}

	private void getBytePlaintext() {
		bytePlaintext = plaintext.getBytes();
	}

	public String recoverToString() {
		return new String(bytePlaintext);
	}
	private void getSubKey() throws Exception {
		int j = 0, hold = 0;
		byte temp = 0;
		byte[] key = mainKey.getBytes();
		for (int i = 0; i < 256; i++) {
			j = (j + (box[i] > 0 ? box[i] : (box[i] + 256)) + (key[i
					% key.length] > 0 ? key[i % key.length] : (key[i
					% key.length] + 256))) % 256;
			temp = box[i];
			box[i] = box[j];
			box[j] = temp;
		}
		int i = j = 0;
		subKey = new byte[length];
		for (int k = 0; k < length; k++) {
			i = ++i % 256;
			j = (j + (box[i] > 0 ? box[i] : (box[i] + 256))) % 256;
			temp = box[i];
			box[i] = box[j];
			box[j] = temp;
			subKey[k] = box[((box[i] > 0 ? box[i] : (box[i] + 256)) + (box[j] > 0 ? box[j]
					: (box[j] + 256))) % 256];
		}
	}
	private void encrypt() throws Exception {
		result = new byte[length];
		for (int i = 0; i < length; i++) {
			result[i] = (byte) (bytePlaintext[i] ^ subKey[i]);
		}
	}
	private void decrypt() throws Exception {
		bytePlaintext = new byte[length];
		for (int i = 0; i < length; i++) {
			bytePlaintext[i] = (byte) (result[i] ^ subKey[i]);
		}
	}
	public static void main(String[] args) throws Exception 
	{
		byte[] result = new RC4Crypt("12345678901234567890123456789012345678901234567890").result;
		try {
	
			String a=Base64.encode(result);
			System.out.println(	a);
			System.out.println(	new String(new RC4Crypt(Base64.decode(a)).bytePlaintext, "UTF-8"));
		
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}