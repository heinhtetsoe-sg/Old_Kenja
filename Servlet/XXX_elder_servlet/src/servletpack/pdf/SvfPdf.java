
package servletpack.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.SvfField;

public class SvfPdf implements IPdf {
    private static Log log = LogFactory.getLog(SvfPdf.class);

    public static String setFormArgN = "setFormArgN";
    private final Vrw32alp _svf;
    private final Map _parammap;

    private String currentFormname = null;
    private boolean _svfFieldInitError = false;
    private Map _svfFieldMap = new HashMap();
    private Map _formNoFieldSetMap = new TreeMap();

    private final String PROPERTY_FILE = "IPdf.properties";
    private Properties _properties;

    public static SvfPdf init(final OutputStream outputStream) {

        Vrw32alp svf = new Vrw32alp();          //PDF生成クラスのインスタンス生成(ＡＬＰ仕様）
        
        //  svf設定
        svf.VrInit();                           //クラスの初期化
        svf.VrSetSpoolFileStream(outputStream);          //PDFファイル名の設定

        SvfPdf svfPdf = new SvfPdf(svf);
        return svfPdf;
    }
    public SvfPdf(final Vrw32alp svf, final Map parammap) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        _svf = svf;
        _parammap = parammap;
        
        _properties = new Properties();
        InputStream st = null;
        try {
            File file = new File(PROPERTY_FILE);
            st = new FileInputStream(file);
            _properties.load(st);
        } catch (Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != st) {
                try {
                    st.close();
                } catch (Exception e) {
                    // だまってcloseする...
                }
            }
        }
    }
    public SvfPdf(final Vrw32alp svf) {
        this(svf, new HashMap());
    }
    public final Vrw32alp getVrw32alp() {
        return _svf;
    }
    public void setParameter(final String paramname, final Object o) {
        _parammap.put(paramname, o);
    }
    public Object getParameter(final String paramname) {
        return _parammap.get(paramname);
    }
    public int getParamInt(final String name, final int defaultValue) {
        if ((_parammap.get(name) instanceof String) && NumberUtils.isDigits((String) _parammap.get(name))) {
            return Integer.parseInt((String) _parammap.get(name));
        }
        return defaultValue;
    }
    public int VrSetForm(final String formname, final int n) {
        currentFormname = formname;
        int rtn = getVrw32alp().VrSetForm(currentFormname, n);
        try {
            _svfFieldInitError = false;
            _svfFieldMap = SvfField.getSvfFormFieldInfoMapGroupByName(getVrw32alp());
        } catch (Throwable t) {
            log.info("SvfField init error", t);
            _svfFieldInitError = true;
        }
        return rtn;
    }
    public int VrsOut(final String field, final String data) {
        if (null == field || null == data) {
            return 0;
        }
        return getVrw32alp().VrsOut(field, data);
    }
    public void addRecordField(final String[] fields) {
    }
    public int setRecordString(final String field, final int gyo, final String data) {
        return VrsOut(field, data);
    }
    public boolean checkField(final String field) {
        boolean exist = false;
        if (!_svfFieldInitError) {
            SvfField svfField = (SvfField) _svfFieldMap.get(field);
            if (null != svfField) {
                exist = true;
                if ("1".equals(_properties.get("outputDebug"))) {
                    log.info(" checkField " + svfField.toString());
                }
            } else {
                if (!getMappedSet(_formNoFieldSetMap, currentFormname).contains(field)) {
                    log.warn(" !!! form " + currentFormname + " has no field [" + field + "] !!!");
                    getMappedSet(_formNoFieldSetMap, currentFormname).add(field);
                }
            }
        }
        return exist;
    }
    
    public int VrsOutn(final String field, final int gyo, final String data) {
        if (null == field || null == data) {
            return 0;
        }
        checkField(field);
        return getVrw32alp().VrsOutn(field, gyo, data);
    }
    public int VrAttributen(final String field, final int gyo, final String attr) {
        checkField(field);
        return getVrw32alp().VrAttributen(field, gyo, attr);
    }
    public int VrAttribute(final String field, final String attr) {
        checkField(field);
        return getVrw32alp().VrAttribute(field, attr);
    }
    public int VrEndRecord() {
        return getVrw32alp().VrEndRecord();
    }
    public int VrEndPage() {
        return getVrw32alp().VrEndPage();
    }
    public int VrImageOut(final String field, final String filePath) {
        checkField(field);
        return VrsOut(field, filePath);
    }
    public int close(final boolean hasData) {
        if (!hasData) {
            VrSetForm("MES001.frm", 0);
            VrsOut("note", "note");
            VrEndPage();
        }
        return close();
    }
    public int close() {
        return getVrw32alp().VrQuit();
    }
    
    private static Set getMappedSet(final Map map, final Object key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeSet());
        }
        return (Set) map.get(key1);
    }
}
