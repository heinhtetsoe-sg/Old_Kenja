/*
 * $Id: 5792eec134e89f8da0b8d3cbfaab0c34311a6957 $
 *
 * 作成日: 2015/07/10
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.pdf;

import java.io.File;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import alp.co.jp.ARegion;
import alp.co.jp.ARepeat;
import alp.co.jp.Product;
import alp.co.jp.dom.SeriToPdf;
import alp.co.jp.element.AElement;
import alp.co.jp.element.Elements;
import alp.co.jp.element.FieldElement;
import alp.co.jp.text.FieldText;
import alp.co.jp.util.XmlPdfPath;


public class AlpPdf implements IPdf {

    private static Log log = LogFactory.getLog(AlpPdf.class);
    
    public interface IOutputPdf {
        public void outputPdf(IPdf ipdf, HttpServletRequest request) throws Exception;
    }

    public static Boolean print(final String basePath, final HttpServletRequest request, final OutputStream outputStream, final Map iPdfParameterMap, final Map fieldTranslateMap, final IOutputPdf iOutputPdfInstance) throws Exception {

        AlpPdf alppdf = null;
        Boolean rtn = Boolean.FALSE;
        try {
            //outputDebug(request);
            
            alppdf = new AlpPdf(basePath, outputStream);
            if (null != iPdfParameterMap) {
                for (final Iterator it = iPdfParameterMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry entry = (Map.Entry) it.next();
                    alppdf.setParameter((String) entry.getKey(), entry.getValue());
                }
            }

            alppdf.setFieldTranslateMap(fieldTranslateMap);
            iOutputPdfInstance.outputPdf(alppdf, request);
            
        } catch (final Exception ex) {
            log.error("exception!", ex);
            rtn = Boolean.TRUE;
        } catch (final Throwable ex) {
            log.error("error!", ex);
            rtn = Boolean.TRUE;
        } finally {
            if (null != alppdf) {
                alppdf.close();
            }
        }
        return rtn;
    }

    private final String PARAMETER_DEBUG = "DEBUG";
    private final String defaultPath = "mod_xml";

    final String _basePath;
    final OutputStream _out;
    SeriToPdf _seritopdf;
    boolean _isInited;
    AElement _aele;
    String[] array = new String[1000];
    boolean _isOutput;
    Map _parammap = new HashMap();

    List _groupFields; 
    int _recordIdxTmp;
    Map _recordValueTmp;
    List _fieldValueTmp;
    Map _attributeErrorField = Collections.EMPTY_MAP;
    Map _dataErrorField = Collections.EMPTY_MAP;
    Map<String, List<Integer>> _sameNameFieldInfo = Collections.EMPTY_MAP;
    boolean formSet = false;
    String _formname;
    
    private Map _fieldTranslateMap;
    private boolean _isFirstDebug;

    public AlpPdf(final String basePath, final OutputStream out) {
        log.fatal("$Revision: 56595 $ $Date: 2017-10-22 23:25:19 +0900 (日, 22 10 2017) $"); // CVSキーワードの取り扱いに注意
        _basePath = basePath;
        _seritopdf = new SeriToPdf(_basePath + defaultPath);
        _out = out;
        _isOutput = false;
        _groupFields = new ArrayList();
        _recordValueTmp = new HashMap();
        _fieldValueTmp = new ArrayList();
        _parammap.put("AlpPdf", "1");
    }
    
    public void setFieldTranslateMap(final Map fieldTranslateMap) {
        _fieldTranslateMap = fieldTranslateMap;
    }
    
    private String getTyohyoFieldName(final String srcFieldName) {
        if (null == _fieldTranslateMap) {
            return srcFieldName;
        }
        return StringUtils.defaultString((String) _fieldTranslateMap.get(srcFieldName), srcFieldName);
    }
    
    public void setParameter(final String paramname, final Object o) {
        _parammap.put(paramname, o);
    }
    
    public Object getParameter(final String paramname) {
        return _parammap.get(paramname);
    }
    
    public boolean isDebug() {
        return null != _parammap.get(PARAMETER_DEBUG);
    }

    public String getPdfName(final String formname) {
        if (null != formname) {
            if (formname.endsWith(".frm") || formname.endsWith(".xml")) {
                return formname.substring(0, formname.length() - 4);
            }
        }
        return formname;
    }

    public void addRecordField(final String[] fields) {
        if (null != fields) {
            for (int i = 0; i < fields.length; i++) {
                _groupFields.add(fields[i]);
            }
            log.info(" record field = " + _groupFields);
        }
    }

    public int VrSetForm(final String formname, final int dummy) {
        makePage();
        if (null == _formname || !_formname.equals(formname)) {
            log.info("setForm : " + formname);
            _formname = formname;
            final String pdfName = getPdfName(_formname);
            boolean initNormal = _seritopdf.Init(pdfName);
            if (!initNormal) {
                
            }
            final String xmlPath = _seritopdf.xmlPdfpath.getXmlPath();
            log.info(" xmlPath = " + xmlPath);
            final File xmlUseFile = searchXmlNoPath(new File(xmlPath));
            if (xmlUseFile.exists() && !xmlUseFile.getAbsolutePath().equals(xmlPath)) {
                _seritopdf.Init(getPdfName(xmlUseFile.getName()));
            } else if (!xmlUseFile.exists()) {
                final String altPath = _basePath + "src_xml";
                log.info(" xmlPath [" + xmlPath + "] not exists. change directory to [" + altPath + "].");
                _seritopdf.xmlPdfpath = new XmlPdfPath(altPath);
                _seritopdf.Init(pdfName);
            }
            if (!_isInited) {
                _seritopdf.PDF_From_Template(_out);
            }
            _isInited = true;
            _seritopdf.Change_Template();
            _aele = _seritopdf.getElements().getElement();
            _isFirstDebug = true;
            _attributeErrorField = new HashMap();
            if (isDebug()) {
                debugOutputXml(_seritopdf);
            }
            _sameNameFieldInfo = new TreeMap<String, List<Integer>>();
            final Map<String, List<List<Integer>>> work = new TreeMap();
            setSameNameFieldInfo(this, _seritopdf.getElements().getElement(), "", work);
            if (isDebug()) {
                log.info(" workFieldInfo size = " + work.size());
            }
            for (final Map.Entry<String, List<List<Integer>>> e : work.entrySet()) {
                if (e.getValue().size() == 1) {
                    // 正常
                    if (isDebug()) {
                        log.info(" OK field " + e.getKey() + " (" + e.getValue().get(0).size() + ") -> " + getFromToString(e.getValue().get(0)));
                    }
                    _sameNameFieldInfo.put(e.getKey(), e.getValue().get(0));
                } else {
                    // フォームの設定値がおかしいかも
                    log.info(" ERROR field " + e.getKey() + " -> " + e.getValue());
                }
            }
            if (isDebug()) {
                log.info(" remain workFieldInfo size = " + work.size() + " / nameFIeldInfo = " + _sameNameFieldInfo.size());
            }
            _dataErrorField = new TreeMap();
        }
        return 0;
    }

    private static String getFromToString(final List<Integer> src) {
        if (null == src) {
            return "null";
        }
        final StringBuffer stb = new StringBuffer("[");
        String comma = "";
        boolean addLast = false;
        Integer start = null;
        Integer current = null;
        for (final Integer i : src) {
            addLast = false;
            if (null == current) {
                start = i;
                current = i;
                continue;
            }
            if (current + 1 == i) {
                current = i;
                continue;
            }
            final String s = start == current ? start.toString() : start.toString() + "〜" + current.toString();
            stb.append(comma).append(s);
            comma = ", ";
            addLast = true;
            start = i;
            current = i;
        }
        if (!addLast) {
            final String s = start == current ? start.toString() : start.toString() + "〜" + current.toString();
            stb.append(comma).append(s);
            comma = ", ";
            addLast = true;
        }
        return stb.append("]").toString();
    }

    private static void debugOutputXml(final SeriToPdf seritopdf) {
        final Product product = seritopdf.getProduct();
        for (final ARegion region: product.getRegion()) {
            log.info(" region name = " + region.getName() + " (" + region.getX() + ", " + region.getY() + ", " + region.getWidth() + ", " + region.getHeight() + ")");
            for (final ARepeat r : region.getRepeat_list().getRepeat_()) {
                log.info("   | repeat id = " + r.getID() + " (" + (r.isVertical() ? "vertical" : "horizontal") + ", parent = " + r.getParent() + ", repeatX = " + r.getRepeatX() + ", repeatY = " + r.getRepeatY() + ")");
            }
            for (final FieldText ft : region.getField_list().getText_()) {
                log.info("   | fieldtext path = " + ft.getPath() + ", text = " + ft.getText() + ", (" + ft.getX() + ", " + ft.getY() + ", " + ft.getWidth() + ", " + ft.getHeight() + (null != ft.getRepeatID() ? ", repeat = " + ft.getRepeatID() : "") + ")");
            }
        }
//        final TreeSet set = new TreeSet();
//        debugElement(seritopdf.getElements().getElement(), "", set);
//        for (final Object o : set) {
//            log.info(" field = " + o);
//        }
    }

    private static File searchXmlNoPath(final File xmlPathFile) {
        final String name = xmlPathFile.getName();
        final String fileHead = name.substring(0, name.length() - 4);
        final Pattern pat = Pattern.compile(fileHead + "(|_[0-9]+).xml");
        log.debug(" search pattern = " + pat);
        final TreeMap numNamelengthMap = new TreeMap();
        final File[] files = xmlPathFile.getParentFile().listFiles();
        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            final Matcher m = pat.matcher(file.getName());
            if (m.matches()) {
                final String strnum = m.group(1);
                String bd;
                int length;
                if ("".equals(strnum)) {
                    bd = "0";
                    length = 0;
                } else {
                    bd = strnum.substring(1);
                    length = bd.length();
                }
                getNumNameMap(numNamelengthMap, new BigDecimal(bd)).put(new Integer(length), strnum);
            }
        }
        final File rtn;
        if (numNamelengthMap.isEmpty()) {
            rtn = xmlPathFile;
        } else {
            final TreeMap value = (TreeMap) numNamelengthMap.get(numNamelengthMap.lastKey());
            final String maxStrnum = (String) (value.get(value.lastKey()));
            log.debug(" name map = " + numNamelengthMap);
            rtn = new File(xmlPathFile.getParentFile().getAbsolutePath() + "/" + fileHead + maxStrnum + ".xml");
            log.info(" maxStrnum = " + maxStrnum + ", search result file = " + rtn);
        }
        return rtn;
    }
    
    private static TreeMap getNumNameMap(TreeMap numNamelengthMap, final BigDecimal key) {
        if (!numNamelengthMap.containsKey(key)) {
            numNamelengthMap.put(key, new TreeMap());
        }
        return (TreeMap) numNamelengthMap.get(key);
    }

    public int VrsOut(final String field, final String data) {
        return VrsOutn(field, -1, data);
    }
    
    public int setRecordString(final String field, int gyo, final String data) {
        if (null == data || gyo <= 0) {
            return -1;
        }
        _recordIdxTmp = gyo - 1;
        _recordValueTmp.put(field, data);
        return 0;
    }

    public int VrsOutn(final String field, int gyo, final String data) {
        if (null == data || null == field) {
            return -1;
        }
        _fieldValueTmp.add(new SetData(field, gyo, -1, data));
        return 0;
    }

    public int VrsOutn(final String field, int gyo, int retsu, final String data) {
        if (null == data || null == field) {
            return -1;
        }
        _fieldValueTmp.add(new SetData(field, gyo, retsu, data));
        return 0;
    }
    
    private void clearArray(final String[] array) {
        Arrays.fill(array, null);
    }
    
    private void makePage() {
        if (null == _aele) {
            if (_isInited) {
                log.info("_aele null.");
            }
        } else {
            for (final Iterator it = _fieldValueTmp.iterator(); it.hasNext();) {
                final SetData sd = (SetData) it.next();
                final String field = getTyohyoFieldName(sd._field);
                if (null == field) {
                    continue;
                }
                //
                final int n;
//                if (0 == sd._gyo) {
//                    n = FieldElement.SetFieldText(_aele, field, sd._data);
//                } else {
//                    clearArray(array);
//                    array[sd._gyo] = sd._data;
//                    n = FieldElement.SetFieldText(_aele, field, array, sd._gyo - 1, sd._gyo - 1);
//                }
                // うまくでないので実装しなおし
                n = jissouSetFieldText(sd, field);

                if (0 < n) {
                    _isOutput = true;
                    //log.info(" out :" + sd._field + ", " + sd._gyo + " = " + sd._data);
                } else {
                    //log.fatal("field not found:" + field + ", " + sd._gyo);
                }
            }
            clearArray(array);
            _fieldValueTmp.clear();
        }
        if (_isOutput || _isInited && "1".equals(_parammap.get("PRINT_PAGE_WITH_NO_DATA"))) {
            _seritopdf.Make_Page();
            log.fatal("makePage.");
            clearFieldData(_aele);
            _isOutput = false;
        }
    }
    
    private int jissouSetFieldText(final SetData sd, final String field) {
        int rtn = -1;
        AElement e = null;
        if (-1 == sd._gyo && !field.contains("/")) {
            e = fromField(field, _aele);
        } else if (0 < sd._gyo && !field.contains("/")) {
            e = fromFieldNo(field, sd._gyo, _aele);
        } else if (field.contains("/")) {
            final String[] tokens = field.split("/");
            if (-1 == sd._retsu) {
                e = fromField(tokens[1], fromFieldNo(tokens[0], sd._gyo, _aele));
            } else {
                e = fromFieldNo(tokens[0] + "/" + tokens[1], sd._retsu, fromFieldNo(tokens[0], sd._gyo, _aele));
            }
        }
        if (null == e) {
            //log.info(" sd " + field + ", " + sd._gyo + ", " + sd._retsu + " = " + sd._data + " -> " + e);
        } else {
            e.setValue(sd._data);
            rtn = 1;
        }
        return rtn;
    }

    public static AElement fromField(final String field, final AElement root) {
        AElement e = null;
        if (null != root) {
            for (AElement ce : root.getElement_()) {
                if (ce.getName().equals(field)) {
                    e = ce;
                    break;
                }
            }
        }
        return e;
    }

    public AElement fromFieldNo(final String field, final int no, final AElement root) {
        final List<Integer> indexList = _sameNameFieldInfo.get(field);
        if (null == indexList || indexList.size() < no) {
            if (null == _dataErrorField.get(field) || isDebug()) {
                log.fatal("field not found : " + field + " [" + no + "] <> (" + (null == indexList ? "null" : String.valueOf(indexList.size())) + ") " + getFromToString(indexList));
                _dataErrorField.put(field, no);
            }
            return null;
        }
        final int aeleIndex = indexList.get(no - 1);
        if (root.getElement_().size() - 1 < aeleIndex) {
            log.fatal("field index over : " + "[" + aeleIndex + "] <> " + root.getName() + ".len = " + root.getElement_().size());
            return null;
        }
        return root.getElement_().get(aeleIndex);
    }

//    private static void debugElement(final AElement e, final String head, final Set collection) {
//        for (AElement ce : e.getElement_()) {
//            collection.add(head + ce.getName());
//            debugElement(ce, head + ce.getName() + "/", collection);
//        }
//    }
    
    private static void setSameNameFieldInfo(final AlpPdf alpPdf, final AElement e, final String rootPath, final Map<String, List<List<Integer>>> rtn) {
        final Map<String, List<Integer>> sameNameIndexes = new HashMap<String, List<Integer>>();
        for (int i = 0; i < e.getElement_().size(); i++) {
            final AElement ce = (AElement) e.getElement_().get(i);
            getMappedList(sameNameIndexes, ce.getName()).add(i);
        }
//        if (alpPdf.isDebug()) {
//            log.info(" " + rootPath + ": add same name Indexes = " + sameNameIndexes);
//        }
        for (final Iterator<Map.Entry<String, List<Integer>>> it = sameNameIndexes.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<String, List<Integer>> entry = it.next();
            if (entry.getValue().size() == 1) {
                it.remove();
                continue;
            }
            final List<List<Integer>> differentIndexesSetList = getMappedList(rtn, append(rootPath, "/") + entry.getKey());
            boolean findSameIndexesSet = false;
            for (final List differentIndexesSet : differentIndexesSetList) {
                if (differentIndexesSet.equals(entry.getValue())) {
                    findSameIndexesSet = true;
                    break;
                }
            }
            if (!findSameIndexesSet) {
                differentIndexesSetList.add(entry.getValue());
            }
        }
        for (int i = 0; i < e.getElement_().size(); i++) {
            AElement ce = (AElement) e.getElement_().get(i);
            setSameNameFieldInfo(alpPdf, ce, append(rootPath, "/") + ce.getName(), rtn);
        }
    }
    
    private static String append(final String s, final String a) {
        return "".equals(s) ? s : s + a;
    }

    public int VrEndRecord() {
        String slash = "";
        StringBuffer groupField = null;
        StringBuffer groupValue = null;
//        log.info(" _recordValueTmp = " + _recordValueTmp);
        for (final Iterator it = _groupFields.iterator(); it.hasNext();) {
            final String field = (String) it.next();
            if (null == groupField) {
                groupField = new StringBuffer();
            }
            if (null == groupValue) {
                groupValue = new StringBuffer();
            }
            final String value = (String) _recordValueTmp.get(field);
            final String fieldname = getTyohyoFieldName(field);
            groupField.append(slash).append(fieldname);
            groupValue.append(slash).append(StringUtils.defaultString(value));
            slash = "/";
        }
        if (null != groupValue) {
            final Elements ele = new Elements(_aele);
            if (!_isOutput) {
                for (final Iterator it = _groupFields.iterator(); it.hasNext();) {
                    final String field = (String) it.next();
                    ele.clearElement(getTyohyoFieldName(field));
                }
            }
            ele.addElement(groupField.toString(), groupValue.toString());
            _isOutput = true;
        }
//        int outCount = 0;
        for (final Iterator it = _recordValueTmp.entrySet().iterator(); it.hasNext();) {
            final Map.Entry fv = (Map.Entry) it.next();
            final String field = (String) fv.getKey();
            if (_groupFields.contains(field)) {
                continue;
            }
            final String fieldname = getTyohyoFieldName(field);
            clearArray(array);
            array[_recordIdxTmp] = (String) fv.getValue();
            final int n = FieldElement.SetFieldText(_aele, fieldname, array, _recordIdxTmp, _recordIdxTmp);
            if (0 < n) {
                _isOutput = true;
//                log.info(" record out :" + fieldname + ", " + _recordIdxTmp + " = " + fv.getValue());
//                outCount += 1;
            } else {
                log.fatal("record field not found:" + fieldname);
            }
        }
        _recordValueTmp.clear();
//        log.info("VrEndRecord() (outCount = " + outCount + ")");
        return 0;
    }
    
    public int VrAttributen(String field, final int gyo, final String attr) {
        if (null == field) {
            return -1;
        }
        field = getTyohyoFieldName(field);
        final String fieldpath = field.contains("[") && field.contains("]") ? field : field + "[0]";
        FieldText fldtxt = _seritopdf.getFieldText(null, fieldpath);
        if (null == fldtxt) {
            if (null == _attributeErrorField.get(field + "-" + gyo)) {
                _attributeErrorField.put(field + "-" + gyo, "1");
                log.warn("そんなフィールドないですn field = " + fieldpath);
            }
        } else {
            //notImplemented("setAttributen(\"" + field + "\", " + gyo + ", \"" + attr + "\")");
            fldtxt.setBkcolor(gyo, "ピンク");
        }
        return 0;
    }
    public int VrAttribute(String field, final String attr) {
        if (null == field) {
            return -1;
        }
        field = getTyohyoFieldName(field);
        final String fieldpath = field;
        FieldText fldtxt = _seritopdf.getFieldText(null, fieldpath);
        if (null == fldtxt) {
            if (null == _attributeErrorField.get(field)) {
                _attributeErrorField.put(field, "1");
                log.warn("そんなフィールドないです field = " + fieldpath);
            }
        } else {
            //notImplemented("setAttribute(\"" + field + "\", \"" + attr + "\")");
            fldtxt.setBkcolor(0, "ピンク");
        }
        return 0;
    }
    public int VrImageOut(String field, String filePath) {
        notImplemented("VrImageOut(" + field + ", " + ")");
        return 0;
    }
    public int VrEndPage() {
        makePage();
        return 0;
    }
    private void notImplemented(final String message) {
        log.warn("not implemented: " + message);
    }

    public int close(final boolean hasData) {
        if (hasData == false) {
            VrSetForm("MES001.xml", -1);
            VrsOut("note", "対象データはありません。");
            makePage();
        }
        return close();
    }

    public int close() {
        if (_isInited && "1".equals(_parammap.get("PRINT_PAGE_WITH_NO_DATA"))) {
            
        } else {
            makePage();
        }
        _seritopdf.Close();
        log.fatal("close.");
        return 0;
    }
    
    public void setDebugField(final String field) {
//        _seritopdf.setDebugField(field);
    }
    
    public void setDebugText(final String text) {
//        _seritopdf.setDebugText(text);
    }
    
    public int getFieldCount(final String fieldname) {
        if (!_isInited) {
            log.warn("not initialized.");
            return -1;
        }
        final Object o = _sameNameFieldInfo.get(fieldname);
        if (null == o) {
            return -1;
        }
        return ((Collection) o).size();
    }
    
    private static void clearFieldData(final AElement ele) {
        // 子供の兄弟レベルを検索
        for (final Iterator it = ele.getElement_().iterator(); it.hasNext();) {
            final AElement e = (AElement) it.next();
            e.setValue(null);
            clearFieldData(e);
        }
    }
    
    private static <A,B> List<B> getMappedList(Map<A, List<B>> map, final A key) {
        if (null == map.get(key)) {
            map.put(key, new ArrayList<B>());
        }
        return map.get(key);
    }

    private static <E> Collection<Pair<Integer, E>> zipWithIndex(final List<E> list) {
        final Collection<Pair<Integer, E>> rtn = new ArrayList<Pair<Integer, E>>();
        for (int i = 0; i < list.size(); i++) {
            rtn.add(new Pair<Integer, E>(i, list.get(i)));
        }
        return rtn;
    }

    private static class Pair<A, B> {
        final A _a;
        final B _b;
        Pair(final A a, final B b) {
            _a = a;
            _b = b;
        }
        A fst() {
            return _a;
        }
        B snd() {
            return _b;
        }
    }

    private static class SetData {
        String _field;
        int _gyo;
        int _retsu;
        String _data;
        public SetData(String field, int gyo, int retsu, String data) {
            _field = field;
            _gyo = gyo;
            _retsu = retsu;
            _data = data;
        }
    }
}
