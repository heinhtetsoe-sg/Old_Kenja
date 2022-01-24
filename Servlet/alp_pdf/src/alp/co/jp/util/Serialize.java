package alp.co.jp.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class Serialize {

    // objをシリアライズしてファイルpathへ書込む
    public static boolean Output(Object obj, String path) {
	System.out.println("Serialize.Output path = " + path);
	boolean R = false;
	// シリアライズ出力
	ObjectOutputStream objOutStream;
	try {
	    objOutStream = new ObjectOutputStream(new FileOutputStream(path));
	    objOutStream.writeObject(obj);
	    objOutStream.close();
	    R = true;
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	return R;
    }

    // objへシリアライズファイルpathを読込む
    // objは呼出し側でnewのこと。
    public static Object Input(String path) {
	System.out.println("Serialize.Input path = " + path);
	// シリアライズ読込
	ObjectInputStream objInStream;
	Object obj = null;
	try {
	    objInStream = new ObjectInputStream(new FileInputStream(path));
	    obj = objInStream.readObject();
	    objInStream.close();
	} catch (FileNotFoundException e1) {
	    e1.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	}
	return obj;
    }
}
