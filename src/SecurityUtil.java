import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.xml.bind.DatatypeConverter;


public class SecurityUtil {

	
	/**
	 * Create a hash with a hex representation from a given base string and salt string
	 * @param base
	 * @param salt
	 * @return
	 */
	public static String sha256HexString(String base, String salt) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.reset();
	        digest.update(salt.getBytes());
	        
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	
	/**
	 * Create a hash with a hex representation from a given base char array and salt string
	 * @param base
	 * @param salt
	 * @return
	 */
	public static String sha256HexString(char[] base, String salt) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.reset();
	        digest.update(salt.getBytes());
	        
	        byte[] bytes = Charset.forName("UTF-8").encode(CharBuffer.wrap(base)).array();
	        
	        byte[] hash = digest.digest(bytes);
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	
	public static byte[] sha256Byte(String base, String salt) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        digest.reset();
	        digest.update(salt.getBytes());
	        
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));


	        return hash;
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	
	
   /**
    * From a base 64 representation, returns the corresponding byte[] 
    * @param data String The base64 representation
    * @return byte[]
    * @throws IOException
    */
	public static byte[] base64ToByte(String data) {
		return DatatypeConverter.parseBase64Binary(data);
	}

	/**
	 * From a byte[] return the corresponding base 64 representation
	 * @param data
	 * @return
	 */
	public static String byteToBase64(byte[] data) {
		return DatatypeConverter.printBase64Binary(data);
	}
	
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	
	/**
	 * 
	 * @param data
	 * @return
	 */
	public static String byteToHexString(byte[] data) {
		// convert the byte to hex format method 2
		StringBuffer hexString = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			String hex = Integer.toHexString(0xff & data[i]);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
	
	/**
	 * Generate a new random salt 
	 * @return the salt represented as a hex string
	 */
	public static String generateSalt() {
		SecureRandom random = null;
		try {
			random = SecureRandom.getInstance("SHA1PRNG");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// Salt generation 64 bits long
		byte[] bSalt = new byte[8];
		random.nextBytes(bSalt);
		// Digest computation
		return byteToHexString(bSalt);
	}
}
