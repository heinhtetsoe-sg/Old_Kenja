package nao_package;

import java.io.*;
import java.util.Properties;

public class Alp_Property {
	static String propname=null;
	public Alp_Property(String propname){
		Alp_Property.propname = propname;
	}
	public final String getprop(String item) {
		return getprop(item, null);
	}
	public final String getprop(String item, String def) {
		if (Alp_Property.propname == null){
			System.out.println("Alp_Property.propname == null");
			return null;
		}
		InputStream inputStreams = null;
		inputStreams = this.getClass().getClassLoader().getResourceAsStream(Alp_Property.propname);
		final Properties prop = new Properties();
		try {
			// 入力ストリームから、propに読み込む
			prop.load(inputStreams);
		} catch (final IOException e) {
			System.out.println("getprop "+e.toString());
			return null;
		}
		return prop.getProperty(item, def);
	}
}
