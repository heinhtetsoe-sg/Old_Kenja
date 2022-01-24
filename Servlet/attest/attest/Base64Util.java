package attest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

/**
 * 文字列をBase64にエンコード、デコードを行う。
 * @author yamarou
 */
public class Base64Util {
  public static void main(String[] args) throws Exception {
      String s = "あかさたなはまやらわ";
      System.out.println("元の文字列 " + s);

      String enc = Base64Util.encodeBase64(s);
      System.out.println("エンコード後 " + enc);

      String dec = Base64Util.decodeBase64(enc);
      System.out.println("デコード後 " + dec);
      
      String pass = Base64Util.encryptPassword(s);
      System.out.println("LDAPパスワード形式に変換後 " + pass);
  }

  /**
   * 引数strをBase64エンコーディングする。
   * @param     str 文字データ
   * @return    符号化文字列
   */
  public static String encodeBase64(String str)
    throws IOException, UnsupportedEncodingException, MessagingException {
    return encodeBase64(str.getBytes());
  }

  /**
   * 引数strをBase64エンコーディングする。
   * @param     data バイト型文字データ
   * @return    符号化文字列
   */
  public static String encodeBase64(byte[] data)
    throws IOException, UnsupportedEncodingException, MessagingException {
    ByteArrayOutputStream bao = new ByteArrayOutputStream();
    OutputStream out = MimeUtility.encode(bao, "base64");
    System.out.println(data);
    out.write(data);
    out.close();
    return bao.toString("iso-8859-1");
  }

  /**
   * 引数strをBase64デコーディングする。
   * @param     data 文字データ
   * @return    復号化文字列
   */
  public static String decodeBase64(String str)
    throws IOException, UnsupportedEncodingException, MessagingException {
    return decodeBase64(str.getBytes());
  }

  /**
   * 引数strをBase64デコーディングする。
   * @param     data バイト型文字データ
   * @return    復号化文字列
   */
  private static String decodeBase64(byte[] data)
    throws IOException, UnsupportedEncodingException, MessagingException {
    InputStream in = MimeUtility.decode(
      new ByteArrayInputStream(data), "base64");
    byte[] buf = new byte[1024];
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int len;
    while ((len = in.read(buf)) != -1) {
      out.write(buf, 0, len);
    }
    return new String(out.toByteArray());
  }

  /**
   * 引数strをLDAPのパスワード形式に変換する。
   * @param     str 文字データ
   * @return    暗号化文字列
   */
  public static String encryptPassword(String str)
    throws NoSuchAlgorithmException,
           IOException, UnsupportedEncodingException, MessagingException{
    // SHAハッシュ化
    MessageDigest md = MessageDigest.getInstance("SHA");
    byte[] data = str.getBytes();
    md.update(data);
    byte[] hashVal = md.digest();
    return "{SHA}" + encodeBase64(hashVal);
  }
}
