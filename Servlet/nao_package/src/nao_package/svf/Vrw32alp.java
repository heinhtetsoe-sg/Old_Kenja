package nao_package.svf;

import java.io.*;

import nao_package.Alp_Property;

//import org.jfree.util.Log;

import jp.co.fit.vfreport.*;

/*
 * Vrw32alp.java
 * Vrw32alp.java
 * 翼システム SVF for JavaEditionのＡＬＰ仕様
 * '02.07.10
 * '03.06.12:旧漢字の「高」などに対応
 */
public class Vrw32alp extends Vrw32 {
    public static final String revision = "$Revision: 62230 $ $Date: 2018-09-11 09:17:39 +0900 (火, 11 9 2018) $"; // SVNキーワードの取り扱いに注意

    private String form_name;
    private boolean _isSetPathBefore = false;
    private String _setPath;
    public boolean _debug;
    public Vrw32alp(){
        super();
    }

    public int VrInit() {
        int sts = super.VrInit();
        if (sts != 0) {
            System.out.println("===> VrInit():" + sts);
        }
        return sts;
    }

    public int VrSetSpoolFileStream(OutputStream ostream) {
        int sts = super.VrSetSpoolFileStream(ostream);
        if (sts != 0) {
            System.out.println("===> VrSetSpoolFileStream():" + sts);
        }
        return sts;
    }

    public int VrSetForm(String s, int n) {
        final String fom;
        if (null != s && s.startsWith("/")) {
            fom = s;
        } else {
            fom = getPath(s);
        }
        int sts = super.VrSetForm(fom, n);
        if (sts != 0 || _debug) {
            System.out.println("===> VrSetForm(" + fom + "):" + sts);
        }
        return sts;
    }

    public String getPath(final String s) {
        String form = s;
        String formXml = form;
        int length = s.length() - 4;
        if (length > 0) {	//拡張子をfrm→xml
            form_name = s.substring(0, length);
            //super.VrSetSpoolFileName2(pdf);
            String ext = s.substring(length);
            if (ext.equalsIgnoreCase(".frm")){
                formXml = s.substring(0, length) + ".xml";
            }
        }

        if (!_isSetPathBefore) {
            try {
                Alp_Property alp_prop = new Alp_Property("/etc/SVF_FORM.properties");
                _setPath = alp_prop.getprop("SVF_FORM_PATH");
            } catch (Exception e) {
                System.out.println("SVF_FORM.propertiesが正しく設定されていません。" + e.getMessage());
                e.printStackTrace();
            }

            if (_setPath == null) {
                System.out.println("SVF_FORM.properties:SVF_FORM_PATH == null");
                _setPath = System.getProperty("SVF_FORM");
            }
            if (_setPath == null) {
                System.out.println("SVF_FORMパス未定義");
                _setPath = "";
            }

            System.out.println("SVF_FORM : " + _setPath);
            _isSetPathBefore = true;
        }
        String path = _setPath;
        String fom = path + form;
        if (new File(path + formXml).exists()) {
            fom = path + formXml;
        } else {
            form = path + form;
        }
        return fom;
    }

    public int VrSetQuery(String s, String vrq, int n) {
        int sts = super.VrSetQuery(s, vrq, n);
        if (sts != 0) {
            System.out.println("===> VrSetQuery(" + s + ", " + vrq + "):" + sts);
        }
        return sts;
    }

    public int VrReport() {
        int sts = super.VrReport();
        if (sts != 0) {
            System.out.println("===> VrReport():" + sts);
        }
        return sts;
    }

    public int VrEndRecord(){
        int sts = super.VrEndRecord();
        if (sts != 0 || _debug) {
            System.out.println("===> VrEndRecord():" + sts);
        }
        return sts;
    }

    public int VrPrint(){
        int sts = super.VrPrint();
        if (sts != 0) {
            System.out.println("===> VrPrint():" + sts);
        }
        return sts;
    }

    public int VrQuit(){
        int sts = super.VrQuit();
        if (sts != 0) {
            System.out.println("===> VrQuit():" + sts);
        }
        return sts;
    }

    public int VrsOut(String field, String data) {
        if (null == field) {
            return 0;
        }
//        if (data != null) {
//            data = data.replace('\uE2D3', '\u9AD9');  // 高（こう）
//            data = data.replace('\uE1CC', '\uFA11');  // 崎（さき）
//            data = data.replace('\uE200', '\u6801');  // 柳（りゅう）
//            data = data.replace('\uE189', '\u4f39');  // 但（たん）
//        }
//        else
//        	data = "";
        if (data == null) {
            data = "";
        }
        if ((form_name.equals(new String("MES001")))&&(field.equals(new String("note")))) {
            data = "note";
        }

        int sts = super.VrsOut(field, data);
        if (_debug) {
            System.out.println("===> VrsOut(" + field + ", " + data + "):" + sts);
        }
        return sts;
    }

    public int VrsOutn(String field, int gyo, String data) {
        if (null == field) {
            return 0;
        }
//        if (data != null) {
//            data = data.replace('\uE2D3', '\u9AD9');  // 高（こう）
//            data = data.replace('\uE1CC', '\uFA11');  // 崎（さき）
//            data = data.replace('\uE200', '\u6801');  // 柳（りゅう）
//            data = data.replace('\uE189', '\u4f39');  // 但（たん）
//        }
//        else
//        	data = "";
        if (data == null) {
            data = "";
        }
        int sts = super.VrsOutn(field, gyo, data);
        if (_debug) {
            System.out.println("===> VrsOutn(" + field + ", " + data + "):" + sts);
        }
        return sts;
    }
}
