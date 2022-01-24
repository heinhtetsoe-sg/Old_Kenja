// kanji=漢字
/*
 * $Id: 8d39f6a82d0bf11fbce289439cf3c34c069e0af9 $
 *
 * 作成日: 
 * 作成者: yamasiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */

package servletpack.KNJA;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jp.co.alp.kenja.common.dao.SQLUtils;
import nao_package.db.DB2UDB;
import nao_package.svf.Vrw32alp;
import servletpack.KNJZ.detail.KNJDefineCode;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJServletpacksvfANDdb2;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.KNJ_Get_Info;
import servletpack.KNJZ.detail.KNJ_SchoolinfoSql;
import servletpack.KNJZ.detail.KnjDbUtils;

/**
 * 学校教育システム 賢者 [学籍管理] 生徒指導要録 中学校用
 */

public class KNJA133F {
    private static final Log log = LogFactory.getLog(KNJA133F.class);
    
    private final int _3NENJI = 3;
    
    private boolean hasdata;
    
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {
        svf_out_ex(request, response, Collections.EMPTY_MAP);
    }
    
    public void svf_out_ex(
            final HttpServletRequest request,
            final HttpServletResponse response,
            final Map paramMap
    ) throws Exception {
        final KNJServletpacksvfANDdb2 sd = new KNJServletpacksvfANDdb2(); // 帳票におけるＳＶＦおよびＤＢ２の設定
        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        Param param = null;
        try {
            // print svf設定
            sd.setSvfInit(request, response, svf);
            
            // ＤＢ接続
            db2 = sd.setDb(request);
            if (sd.openDb(db2)) {
                log.error("db open error! ");
                return;
            }
            // パラメータの取得
            param = getParam(request, db2, paramMap);
            
            // 印刷処理
            printSvf(db2, svf, param);
        } catch (final Exception e) {
            log.error("exception!", e);
        } finally {
            if (null != param) {
                param.closeStatementQuietly();
            }
            
            // 終了処理
            sd.closeSvf(svf, hasdata);
            sd.closeDb(db2);
        }
    }
    
    private Param getParam(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {
        log.fatal("$Revision: 72737 $ $Date: 2020-03-05 17:11:29 +0900 (木, 05 3 2020) $"); // CVSキーワードの取り扱いに注意
        KNJServletUtils.debugParam(request, log);
        return new Param(request, db2, paramMap);
    }
    
    /**
     * 印刷処理 NO001 Modify
     */
    private void printSvf(final DB2UDB db2, final Vrw32alp svf, final Param param) {
        final Map staffMstMap = StaffMst.load(db2, param._year);
        
        final List<Student> studentList = getStudentList(db2, param, staffMstMap);
        
        for (final Iterator<Student> it = studentList.iterator(); it.hasNext();) {
            final Student student = it.next();
            log.debug(" schregno = " + student._schregno);
            
            student._personalInfo = PersonalInfo.load(db2, param, student, staffMstMap, null);
            student._attendList = Attend.load(db2, param, student);
            student._htrainRemarkDatList = HTrainRemarkDat.load(db2, param, student);
//            student._htrainremarkHdat = HtrainremarkHdat.load(db2, param, student._schregno);
            student._actRecordList = ActRecord.load(db2, param, student);
            student._classViewList = ClassView.load(db2, param, student);
            student._valueRecordList = ValueRecord.load(db2, param, student);
            student._printClassList = Student.getPrintClassList(student._classViewList, student._valueRecordList);

            print1(svf, param, student);
            it.remove();
        }
    }
    
    private List<Student> getStudentList(final DB2UDB db2, final Param param, final Map staffMstMap) {
        
        final List<String> schregnoList = param.getSchregnoList(db2);
        
        final List<Student> studentList = new ArrayList();
        for (final String schregno : schregnoList) {
            final Student student = new Student(schregno);
            student.loadGuard(db2, param);
            studentList.add(student);
        }
        Student.setSchregEntGrdHistComebackDat(db2, param, studentList, staffMstMap);
        return studentList;
    }

    private static <A, B> List<B> getMappedList(final Map<A, List<B>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList<B>());
        }
        return map.get(key1);
    }

    private static <A, B, C> Map<B, C> getMappedMap(final Map<A, Map<B, C>> map, final A key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new HashMap<B, C>());
        }
        return map.get(key1);
    }

    private static Calendar getCalendarOfDate(final String date) {
        final java.sql.Date sqlDate = java.sql.Date.valueOf(date);
        final Calendar cal = Calendar.getInstance();
        cal.setTime(sqlDate);
        return cal;
    }

    private static int getNendo(final Calendar cal) {
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        if (Calendar.JANUARY <= month && month <= Calendar.MARCH) {
            return year - 1;
        }
        return year;
    }
    
    private static String getString(final String field, final Map map) {
        try {
            if (null == field || null == map || !map.isEmpty() && !map.containsKey(field)) {
                // フィールド名が間違い
                throw new RuntimeException("指定されたフィールドのデータがありません。フィールド名：'" + field + "' :" + map);
            }
        } catch (final Exception e) {
            log.error("exception!", e);
        }
        if (null == field) {
            return null;
        }
        return (String) map.get(field);
    }
    
    private static abstract class Cond {
        
        public static Field field(final String field) {
            return new Field(field);
        }
        
        public abstract boolean isValid(final Map row);
        
        public abstract String toSql();
        
        private static class Field {
            final String _field;
            Field(final String field) {
                _field = field;
            }
            
            public Cond isNull() {
                return new IsNull(_field);
            }
            
            public Cond eq(final String v) {
                return new Cmp(_field, v, 0);
            }
            
            public Cond lessThan(final String v) {
                return new Cmp(_field, v, -1);
            }
            
            public Cond greaterThan(final String v) {
                return new Cmp(_field, v, 1);
            }
            
            public Cond lessEqual(final String v) {
                return lessThan(v).or(eq(v));
            }
            
            public Cond greaterEqual(final String v) {
                return greaterThan(v).or(eq(v));
            }
            
            public Cond between(final String v1, final String v2) {
                final Cond c = greaterEqual(v1).and(lessEqual(v2));
                return c;
            }
        }

        public Cond and(final Cond c2) {
            return new And(this, c2);
        }
        
        public Cond or(final Cond c2) {
            return new Or(this, c2);
        }

        private static class IsNull extends Cond {
            final String _field;
            public IsNull(final String field) {
                _field = field;
            }
            public boolean isValid(final Map row) {
                return null == getString(_field, row);
            }
            public String toSql() {
                return _field + " IS NULL ";
            }
        }

        private static class Cmp extends Cond {
            final String _field;
            final String _val;
            final int _c;
            Cmp(final String field, final String val, final int c) {
                _field = field;
                _val = val;
                _c = c;
            }
            public boolean isValid(final Map row) {
                final String v = getString(_field, row);
                if (null == v) {
                    return false;
                }
                final int cmp = v.compareTo(_val);
                boolean ret;
                if (_c > 0) {
                    ret = cmp > 0;
                } else if (_c < 0) {
                    ret = cmp < 0;
                } else {
                    ret = cmp == 0;
                }
                //log.debug("  " + (_c > 0 ? "GT" : _c < 0 ? "LT" : "EQ") + ": field = " + _field + ", v = " + v + ", _val = " + _val + ", cmp = " + cmp + ", ret = " + ret);
                return ret;
            }
            public String toSql() {
                if (_c > 0) {
                    return _field + " > '" + _val + "' ";
                } else if (_c < 0) {
                    return _field + " < '" + _val + "' ";
                } else {
                    return _field + " = '" + _val + "' ";
                }
            }
        }
        
        private static class True extends Cond {
            public boolean isValid(final Map row) {
                return true;
            }
            public String toSql() {
                return " (true) ";
            }
        }

        private static class And extends Cond {
            final Cond _c1;
            final Cond _c2;
            public And(final Cond c1, final Cond c2) {
                _c1 = c1;
                _c2 = c2;
            }
            public boolean isValid(final Map row) {
                return _c1.isValid(row) && _c2.isValid(row);
            }
            public String toSql() {
                return "(" + _c1.toSql() + ") AND (" + _c2.toSql() + ")";
            }
        }
        
        private static class Or extends Cond {
            final Cond _c1;
            final Cond _c2;
            public Or(final Cond c1, final Cond c2) {
                _c1 = c1;
                _c2 = c2;
            }
            public boolean isValid(final Map row) {
                return _c1.isValid(row) || _c2.isValid(row);
            }
            public String toSql() {
                return "(" + _c1.toSql() + ") OR (" + _c2.toSql() + ")";
            }
        }
    }

    private static List filter(final List mapList, final Cond cond) {
        final List rtn = new ArrayList();
        for (final Iterator it = mapList.iterator(); it.hasNext();) {
            final Map row = (Map) it.next();
            if (cond.isValid(row)) {
                rtn.add(row);
            }
        }
        return rtn;
    }

    private void print1(final Vrw32alp svf, final Param param, final Student student) {
        final String form = "KNJA133F.frm";
        svf.VrSetForm(form, 4);
        
        printSchool(svf, param, student);
        
        printStudent(svf, param, student);

        printShokenShukketsu(svf, param, student);

        printRecord(svf, param, student);
        hasdata = true;
    }

    private void printRecord(final Vrw32alp svf, final Param param, final Student student) {
        final int maxRecord = 30 * 2;
        int line1 = 0;
        
        final List<String> valueRecordClassCdList = new ArrayList(); // 評定の教科
        final Map<String, List<ValueRecord>> subclassValueMap = new HashMap();
        for (final ValueRecord valueRecord : student.getValueRecordList()) {
            getMappedList(subclassValueMap, valueRecord._classCd).add(valueRecord);
            if (!valueRecordClassCdList.contains(valueRecord._classCd)) {
            	valueRecordClassCdList.add(valueRecord._classCd);
            }
        }
        log.info(" value key = " + subclassValueMap.keySet());
        log.info(" valueRecordClassCdList = " + valueRecordClassCdList);
        
        final List<ClassView> printClassViewList = getPrintClassViewList(student._classViewList, valueRecordClassCdList, subclassValueMap);

        for (final ClassView classview : printClassViewList) {
            // log.debug(" = " + classview);
            
            for (final ViewSubclass viewSubclass : classview._viewSubclassList) {
                // log.debug("     = " + viewSubclass);
                final String keyCd = getKeyCd(classview, viewSubclass);
                final String showname = getShowname(classview, viewSubclass);
                final String name = classview.setClassname(showname, param);  // 教科名のセット
                //log.info(" classview name = [" + name + "]");
                int i = 0;  //教科名称カウント用変数を初期化
                final String field;
                final int inc;
//                log.debug(" kanten " + classview._classcd + "-" + viewSubclass._curriculumcd + "-" + viewSubclass._subclasscd + ", [" + viewSubclass._subclassname + "]");
//                if (param._isKaijyo) {
//                    field = "CLASS1";
//                    inc = 1;
//                } else {
                    final boolean useClass2 = classview.getViewNum() > 0 && 2 <= StringUtils.defaultString(showname).length() / classview.getViewNum();
                    if (useClass2) {
                        field = "CLASS2";
                        inc = 2;
                    } else {
                        field = "CLASS1";
                        inc = 1;
                    }
//                }
                
                    
                final int viewCount = viewSubclass._viewList.size() == 0 ? 1 : viewSubclass._viewList.size();
                for (int vi = 0; vi < viewCount; vi++) {
            		svf.VrsOut("CLASSCD1", keyCd);  //教科コード
            		if (i < name.length()) {
            			svf.VrsOut(field, name.substring(i));  //教科名称
            			i += inc;
            		}
                	if (vi < viewSubclass._viewList.size()) {
                		final View view = viewSubclass._viewList.get(vi);
                		// log.debug("         = " + view);
//                    if (param._isKaijyo) {
//                        if (vi == 0) {
//                            svfVrsOut(svf. field, showname);  //教科名称
//                            i = 999;
//                        }
//                    } else {
//                    }
//                    log.debug(" view cd = " + view._viewcd);
                		svf.VrsOut("VIEW1", view._viewname);  //観点名称
                		
//                    final String subclasscd = "1".equals(param._useCurriculumcd) ? classview._classcd + "-" + viewSubclass._curriculumcd + "-" + viewSubclass._subclasscd : viewSubclass._subclasscd;
//                    for (int gi = 0; gi < 3; gi++) {
//                        final String g = String.valueOf(gi + 1);
//                        if (param.isSlashView(subclasscd, g, vi + 1)) {
//                            svf.VrsOut("VIEW_SHASH" + g, "／");  //観点スラッシュ
//                        }
//                    }
                		for (final ViewStatus viewStatus : view._viewMap.values()) {
//                        if (!student.isPrintYear(viewStatus._year, param)) {
//                            continue;
//                        }
//                        if (null != viewStatus._year && !(student._personalInfo.getYearBegin() <= Integer.parseInt(viewStatus._year) && Integer.parseInt(viewStatus._year) <= student.getPersonalInfoYearEnd(student._personalInfo, param))) {
//                            continue;
//                        }
                			if (viewStatus._g == _3NENJI) {
                				svf.VrsOut("ASSESS1", viewStatus._status);  //観点
                			}
                		}
                	}
                    if (vi == viewSubclass._viewList.size() / 2) {
                        
                        final List<ValueRecord> valueRecordList = getMappedList(subclassValueMap, classview._classcd);
                        if (valueRecordList.size() > 0) {
                            
                        	for (final ValueRecord valueRecord : valueRecordList) {

                                if (valueRecord._g != _3NENJI) {
                                	continue;
                                }
                                
                                if (valueRecord._value != null) {
                                    final String value;
                                    if (param._d065Name1List.contains(valueRecord._subclassCd)) {
                                        value = (String) param._d001Abbv1Map.get(valueRecord._value);
                                    } else if ("1".equals(valueRecord._electDiv)) { // 選択科目は固定で読み替え 11 -> A, 22 -> B, 33 -> C
                                        if ("11".equals(valueRecord._value)) {
                                            value = "A";
                                        } else if ("22".equals(valueRecord._value)) {
                                            value = "B";
                                        } else if ("33".equals(valueRecord._value)) {
                                            value = "C";
                                        } else {
                                            value = valueRecord._value;
                                        }
                                    } else {
                                        value = valueRecord._value;
                                    }
//                                    if (param()._isKaijyo) {
//                                        svf.VrsOutn("ASSESS2_" + valueRecord._g, (i + 1), value);  //評定
//                                    } else {
                                        svf.VrsOut("ASSESS2", value); // 評定
//                                    }
                                }
                        	}
                        }
                    }
                    line1++;
                    svf.VrEndRecord();
                }
                if (null != name) {
                    if (i < name.length()) {
                        for (int j = i; j < name.length(); j += inc) {
                            line1++;
                            svf.VrsOut("CLASSCD1", keyCd);  //教科コード
                            svf.VrsOut(field, name.substring(j));  // 教科名称
                            svf.VrEndRecord();
                        }
                    } else {
                        if (!"1".equals(param._seitoSidoYorokuCyugakuKantenNoBlank)) {
                            final int isPrint = ((line1 % maxRecord != 0) ? 1 : -1);
                            if (-1 != isPrint) { // 行数がオーバーしない場合、レコードを印刷
                                line1++;
                                svf.VrsOut("CLASSCD1", keyCd);  // 教科コード
                                svf.VrEndRecord();
                            }
                        }
                    }
                }
            }
        }
        
        if (0 == line1 || 0 < line1 && line1 % maxRecord != 0) {
            for (int i = line1 % maxRecord; i < maxRecord; i++) {
                svf.VrEndRecord();
            }
        }
    }

	private List<ClassView> getPrintClassViewList(final List<ClassView> classViewList, final List<String> valueRecordClassCdList, final Map<String, List<ValueRecord>> subclassValueMap) {
		final List<ClassView> printClassViewList = new ArrayList();
		printClassViewList.addAll(classViewList);

		for (final String classCd : valueRecordClassCdList) {
        	boolean hasClassView = false; // 評定の教科が観点の教科にあるかチェック
        	for (final ClassView classview : classViewList) {
        		if (classview._classcd.equals(classCd)) {
        			hasClassView = true;
        			break;
        		}
        	}
        	log.info(" classCd = " + classCd + ", hasClassView = " + hasClassView);
        	if (!hasClassView) { // 評定の教科が観点の教科になければ追加
        		ClassView cv = null;
        		ViewSubclass viewSubclass = null;
                for (final ValueRecord valueRecord : getMappedList(subclassValueMap, classCd)) {
                    if (null == cv) {
                    	log.info(" add class " + valueRecord._classCd + " : " + valueRecord._className);
                    	cv = new ClassView(valueRecord._classCd, valueRecord._className, valueRecord._electDiv);
                    	printClassViewList.add(cv);
                    }
                    if (null == viewSubclass) {
                    	log.info(" add subclass " + valueRecord._curriculumCd + "-" + valueRecord._subclassCd + " : " + valueRecord._subclassName);
                    	viewSubclass = new ViewSubclass(valueRecord._curriculumCd, valueRecord._subclassCd, valueRecord._subclassName);
                    	cv._viewSubclassList.add(viewSubclass);
                    }
                }
        	}
        }
        return printClassViewList;
	}
    
    /**
     * 同一教科で異なる科目が２つ以上ある場合科目コードの順番を返す。そうでなければ(通常の処理）教科コードを返す。
     * @param student
     * @param classview
     * @return 科目名または教科名
     */
    private String getKeyCd(final ClassView classview, final ViewSubclass viewSubclass) {
        final TreeSet set = getSubclassCdSet(classview);
        if (set.size() == 1) {
            return classview._classcd;
        } else if (set.size() > 1) {
            final DecimalFormat df = new DecimalFormat("00");
            int order = 0;
            int n = 0;
            for (final Iterator it = set.iterator(); it.hasNext();) {
                final String subclassCd = (String) it.next();
                if (viewSubclass._subclasscd.equals(subclassCd)) {
                    order = n;
                    break;
                }
                n += 1;
            }
            return df.format(order);
        }
        return null;
    }

    /**
     * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
     * @param student
     * @param classview
     * @return 科目名または教科名
     */
    private String getShowname(final ClassView classview, final ViewSubclass viewSubclass) {
        final TreeSet set = getSubclassCdSet(classview);
        if (set.size() > 1 || "1".equals(classview._electdiv)) {
            return viewSubclass._subclassname;
        } else if (set.size() == 1) {
            return classview._classname;
        }
        return null;
    }

    /**
     * 同一教科の科目コードを得る
     * @param student
     * @param classview
     * @return
     */
    private TreeSet getSubclassCdSet(final ClassView classview) {
        final TreeSet set = new TreeSet();
        for (final Iterator it = classview._viewSubclassList.iterator(); it.hasNext();) {
            final ViewSubclass viewSubclass = (ViewSubclass) it.next();
            set.add(viewSubclass._subclasscd);
        }
        return set;
    }

    /**
     * 同一教科で異なる科目が２つ以上ある場合科目名を返す。そうでなければ(通常の処理）教科名を返す。
     * @param student
     * @param valueRecord
     * @return 科目名または教科名
     */
    private String getShowname(final Student student, final ValueRecord valueRecord) {
        final TreeSet set = getSubclassCdSet(student, valueRecord);
        if (set.size() > 1 || "1".equals(valueRecord._electDiv)) {
            return valueRecord._subclassName;
        } else if (set.size() == 1) {
            return valueRecord._className;
        }
        return null;
    }

    /**
     * 同一教科の科目コードを得る
     * @param student
     * @param classview
     * @return
     */
    private TreeSet getSubclassCdSet(final Student student, final ValueRecord valueRecord) {
        final TreeSet set = new TreeSet();
        for (final Iterator it = student.getValueRecordList().iterator(); it.hasNext();) {
            final ValueRecord valueRecord0 = (ValueRecord) it.next();
            if (valueRecord0._classCd.equals(valueRecord._classCd)) {
                set.add(valueRecord0._subclassCd);
            }
        }
        return set;
    }
    
    private void printSchool(final Vrw32alp svf, final Param param, final Student student) {
        final String schoolname = !StringUtils.isEmpty(param._certifSchoolName) ? param._certifSchoolName : param._schoolName1;
        svf.VrsOut("SCHOOLNAME" + (KNJ_EditEdit.getMS932ByteLength(schoolname) <= 40 ? "1" : "2"), schoolname); // 学校名
        
        svf.VrsOut("ZIPCODE1", param._schoolZipCd); // 郵便番号（学校） 郵便番号

        final String schoolAddr = StringUtils.defaultString(param._schoolAddr1) + StringUtils.defaultString(param._schoolAddr2);
        final int ketaSchoolAddr = Math.max(KNJ_EditEdit.getMS932ByteLength(schoolAddr), KNJ_EditEdit.getMS932ByteLength(schoolAddr));
        svf.VrsOut("ADDRESS_gakko1" + (ketaSchoolAddr <= 40 ? "" : ketaSchoolAddr <= 50 ? "_2" : "_3"), schoolAddr); // 住所
        
        for (final Gakuseki gakuseki : student._personalInfo._gakusekiList) {
            
            if (gakuseki._gradeCd == _3NENJI) {
                final String pname = Util.join(gakuseki._principal1._staffMst.getNameLine(gakuseki._year));
                final int ketastf2 = KNJ_EditEdit.getMS932ByteLength(pname);
                svf.VrsOut("STAFF_NAME1_" + (ketastf2 <= 40 ? "1" : ketastf2 <= 50 ? "2" : "3"), pname); // 校長氏名
                
                final String hrstaffname = Util.join(gakuseki._staff1._staffMst.getNameLine(gakuseki._year));
                svf.VrsOut("STAFF_NAME2_" + (KNJ_EditEdit.getMS932ByteLength(hrstaffname) <= 40 ? "1" : KNJ_EditEdit.getMS932ByteLength(hrstaffname) <= 50 ? "2" : "3"), hrstaffname); // 担任氏名
            }
        }
    }

    private void printStudent(final Vrw32alp svf, final Param param, final Student student) {
        svf.VrsOut("KANA1", student._personalInfo._studentKana); // ふりがな
        
        final int ketaName = KNJ_EditEdit.getMS932ByteLength(student._personalInfo._studentName1);
        svf.VrsOut("NAME1_" + (ketaName <= 20 ? "1" : ketaName <= 30 ? "2" : "3"), student._personalInfo._studentName1); // 生徒氏名

        if (student._personalInfo._addressList.size() > 0) {
            final Address address = student._personalInfo._addressList.get(student._personalInfo._addressList.size() - 1);
            final String addr = StringUtils.defaultString(address._address1) + StringUtils.defaultString(address._address2);
            svf.VrsOut("ADDRESS1_" + (KNJ_EditEdit.getMS932ByteLength(addr) <= 40 ? "1" : KNJ_EditEdit.getMS932ByteLength(addr) <= 50 ? "2" : "3"), addr); // 住所
        }
        
        svf.VrsOut("SEX", student._personalInfo._sex); // 性別
        svf.VrsOut("BIRTHDAY", student._personalInfo._birthdayFormatted); // 生年月日
        svf.VrsOut("TRANSFER_DATE_4", student._personalInfo._grdDateFormatted); // 卒業日
        

        svf.VrsOut("KANA2", student._personalInfo._guardKana); // ふりがな
        svf.VrsOut("NAME2_" + (KNJ_EditEdit.getMS932ByteLength(student._personalInfo._guardName) <= 20 ? "1" : KNJ_EditEdit.getMS932ByteLength(student._personalInfo._guardName) <= 30 ? "2" : "3"), student._personalInfo._guardName); // 生徒氏名

        if (student._personalInfo._guardianAddressList.size() > 0) {
            final Address address = student._personalInfo._guardianAddressList.get(student._personalInfo._guardianAddressList.size() - 1);

            svf.VrsOut("ZIPCODE2", address._zipCd); // 郵便番号（学校） 郵便番号
            final int ketaGrdAddr = Math.max(KNJ_EditEdit.getMS932ByteLength(address._address1), KNJ_EditEdit.getMS932ByteLength(address._address2));
            svf.VrsOut("GUARD_ADDRESS1_" + (ketaGrdAddr <= 40 ? "1" : ketaGrdAddr <= 50 ? "2" : "3"), address._address1); // 住所
            svf.VrsOut("GUARD_ADDRESS2_" + (ketaGrdAddr <= 40 ? "1" : ketaGrdAddr <= 50 ? "2" : "3"), address._address2); // 住所
        }
    }

    private void printShokenShukketsu(final Vrw32alp svf, final Param param, final Student student) {
        
        //行動の記録・特別活動の記録
        final StringBuffer specialActRemark = new StringBuffer();
        String nl = "";
        for (final HTrainRemarkDat remarkRecord : student._htrainRemarkDatList) {
            if (null != remarkRecord._specialActRemark) {
            	if (param._isKinJunior || param._isMeiji || param._isChiben) {
            		specialActRemark.delete(0, specialActRemark.length());
            	}
                specialActRemark.append(nl).append(remarkRecord._specialActRemark);
                nl = "\n";
            }
        }
        Util.printSvfRenban(svf, "SPECIALACTVIEW", Util.retDividString(specialActRemark.toString(), ShokenSize.getShokenSize(null, 17, 7), param));
        
        for (final ActRecord act : student._actRecordList) {

            if (NumberUtils.isDigits(act._code) && KNJA133J_0.getG(param, act._year, act._annual) == _3NENJI) {
                if ("1".equals(act._record)) {
                    if ("2".equals(act._div)){
                        svf.VrsOutn("SPECIALACT1", Integer.parseInt(act._code), "○"); //特別行動の記録
                    }
                    if ("1".equals(act._div)) {
                        svf.VrsOut("ACTION" + String.valueOf(Integer.parseInt(act._code)), "○"); //行動の記録
                    }
                }
            }
        }

        // 総合的な学習の時間の記録・総合所見
        for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
            
            if (remark._g == _3NENJI) {
                // 総合的な学習の時間の記録 学習活動
                if (null != remark._totalstudyact) {
                    //final ShokenSize size = ShokenSize.getShokenSize(param._HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J, 5, 8);
                    Util.printSvfRenban(svf, "TOTAL_ACT1", Util.retDividString(remark._totalstudyact, ShokenSize.getShokenSize(null, 5, 8), param));
                }
                // 総合的な学習の時間の記録 観点
                if (null != remark._viewremark) {
                    Util.printSvfRenban(svf, "TOTAL_VIEW1", Util.retDividString(remark._viewremark, ShokenSize.getShokenSize(null, 10, 8), param));
                }
                // 総合的な学習の時間の記録 評価
                if (null != remark._totalstudyval) {
                    //final ShokenSize size = ShokenSize.getShokenSize(param._HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J, 15, 8);
                    Util.printSvfRenban(svf, "TOTAL_VALUE1", Util.retDividString(remark._totalstudyval, ShokenSize.getShokenSize(null, 15, 8), param));
                }

                //final ShokenSize size = ShokenSize.getShokenSize(param._HTRAINREMARK_DAT_TOTALREMARK_SIZE_J, 44, 10);
                final ShokenSize size = ShokenSize.getShokenSize(null, 44, 10);
                Util.printSvfRenban(svf, "TOTALREMARK21_1", Util.retDividString(remark._totalRemark, size._mojisu * 2, param));
            }
        }

        for (final Attend attend : student._attendList) {
            if (attend._g == _3NENJI) {
                svf.VrsOut("LESSON",  attend._lesson);           //授業日数
                svf.VrsOut("SUSPEND", attend._suspendMourning);  //出停・忌引
                svf.VrsOut("PRESENT", attend._requirePresent);   //要出席
                svf.VrsOut("ABSENCE", attend._absent);           //欠席
                svf.VrsOut("ATTEND",  attend._present);          //出席
            }
        }

        for (final HTrainRemarkDat remark : student._htrainRemarkDatList) {
            if (remark._g == _3NENJI) {
                
                //final ShokenSize size = ShokenSize.getShokenSize(param._HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J, 35, 2);
                final ShokenSize size = ShokenSize.getShokenSize(null, 35, 2);
                
                final List<String> list = Util.retDividString(remark._attendrecRemark, size._mojisu * 2, size._gyo, param);
                for (int i = 0 ; i < list.size(); i++) {
                    final String n = i == 0 ? "" : String.valueOf(i + 1);
                    svf.VrsOut("REMARK" + n, list.get(i)); //出欠の記録備考
                }
            }
        }
    }
    
    private static class Util {

        private static String join(final List<String> list, final String comma) {
            if (null == list) {
                return null;
            }
            final StringBuffer stb = new StringBuffer();
            for (final String s : list) {
            	if (StringUtils.isEmpty(s)) {
            		continue;
            	}
            	if (stb.length() != 0) {
            		stb.append(comma);
            	}
                stb.append(s);
            }
            return stb.toString();
        }

        private static String join(final List<String> list) {
        	return join(list, "");
        }
        
        protected static void printSvfRenban(final Vrw32alp svf, final String field, final List<String> list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    svf.VrsOutn(field, i + 1, list.get(i));
                }
            }
        }

        protected static List<String> retDividString(final String targetsrc, final ShokenSize size, final Param param) {
        	return retDividString(targetsrc, size._mojisu * 2, size._gyo, param);
        }

        protected static List<String> retDividString(final String targetsrc, final int dividlen, final int dividnum, final Param param) {
            final List<String> lines = retDividString(targetsrc, dividlen, param);
            if (lines.size() > dividnum) {
                return lines.subList(0, dividnum);
            }
            return lines;
        }

        protected static List<String> retDividString(String targetsrc, final int dividlen, final Param param) {
            if (targetsrc == null) {
                return Collections.emptyList();
            }
            return KNJ_EditKinsoku.getTokenList(targetsrc, dividlen);
        }
    }

    /**
     * 生徒情報
     */
    private static class Student {
        final String _schregno;
        PersonalInfo _personalInfo;
        List<Attend> _attendList;
        List<HTrainRemarkDat> _htrainRemarkDatList;
        List _schregBaseMstList;
        List _guardianDatList;
        List _guardianHistDatList;
        List _schregBaseHistDatList;
//        HtrainremarkHdat _htrainremarkHdat;
        List<ActRecord> _actRecordList;
        List<ClassView> _classViewList;
        List<ValueRecord> _valueRecordList;
        List<String> _printClassList;
        List _schregEntGrdHistComebackDatList;
        final Map _yearLimitCache = new HashMap();
        public Student(final String schregno) {
            _schregno = schregno;
        }
        
        private void loadGuard(final DB2UDB db2, final Param param) {
            _schregBaseMstList     = KnjDbUtils.query(db2, "SELECT * FROM SCHREG_BASE_MST      WHERE SCHREGNO = '" + _schregno + "' ");
            _schregBaseHistDatList = KnjDbUtils.query(db2, "SELECT * FROM SCHREG_BASE_HIST_DAT WHERE SCHREGNO = '" + _schregno + "' ORDER BY ISSUEDATE ");
            _guardianDatList       = KnjDbUtils.query(db2, "SELECT * FROM GUARDIAN_DAT         WHERE SCHREGNO = '" + _schregno + "' ");
            _guardianHistDatList   = KnjDbUtils.query(db2, "SELECT * FROM GUARDIAN_HIST_DAT    WHERE SCHREGNO = '" + _schregno + "' ORDER BY ISSUEDATE ");
        }
        
//        protected boolean isPrintYear(final String regdYear, final Param param) {
//            final String year = "1".equals(param._seitoSidoYorokuCyugakuKirikaeNendoForRegdYear) ? regdYear : _personalInfo._curriculumYear;
//            if (null == _yearLimitCache.get(year)) {
//                Boolean rtn;
//                if (!NumberUtils.isDigits(year)) {
//                    rtn = Boolean.FALSE;
//                } else {
//                    rtn = new Boolean(Integer.parseInt(year) >= param._seitoSidoYorokuCyugakuKirikaeNendo);
//                }
//                log.info(" kirikaeRegd = " + param._seitoSidoYorokuCyugakuKirikaeNendoForRegdYear + ", check year = " + year + ", kirikaeNendo = " + param._seitoSidoYorokuCyugakuKirikaeNendo + ", print? = " + rtn);
//                _yearLimitCache.put(year, rtn);
//            }
//            return ((Boolean) _yearLimitCache.get(year)).booleanValue();
//        }

        public static TreeSet<String> gakusekiYearSet(final List<Gakuseki> gakusekiList) {
            final TreeSet<String> set = new TreeSet<String>();
            for (final Gakuseki g : gakusekiList) {
                set.add(g._year);
            }
            return set;
        }

        /**
         * 印刷する生徒情報
         */
        private List<PersonalInfo> getPrintSchregEntGrdHistList(final Param param) {
            final List rtn = new ArrayList();
            if (_schregEntGrdHistComebackDatList.size() == 0) {
                return Collections.singletonList(_personalInfo);
            }
//            // 復学が同一年度の場合、復学前、復学後を表示
//            // 復学が同一年度ではない場合、復学後のみ表示
//            final List personalInfoList = new ArrayList();
//            personalInfoList.addAll(_schregEntGrdHistComebackDatList);
//            personalInfoList.add(_personalInfo);
//            for (final Iterator it = personalInfoList.iterator(); it.hasNext();) {
//                final PersonalInfo personalInfo = (PersonalInfo) it.next();
//                final int begin = personalInfo.getYearBegin();
//                final int end = personalInfo.getYearEnd(param);
//                if (begin <= Integer.parseInt(param._year) && Integer.parseInt(param._year) <= end) {
//                    rtn.add(personalInfo);
//                }
//            }
            return rtn;
        }
        
        /**
         * 複数の生徒情報に年度がまたがる場合、成績等は新しい生徒情報のページのみに表示するため年度の上限を計算する
         * @param target 対象の生徒情報
         * @return 対象の生徒情報の年度の上限
         */
        private int getPersonalInfoYearEnd(final PersonalInfo target, final Param param) {
            final TreeSet yearSetAll = new TreeSet();
            final List personalInfoList = getPrintSchregEntGrdHistList(param);
            for (final ListIterator it = personalInfoList.listIterator(personalInfoList.size()); it.hasPrevious();) { // 新しい生徒情報順
                final PersonalInfo personalInfo = (PersonalInfo) it.previous();
                final int begin = personalInfo.getYearBegin();
                final int end = personalInfo.getYearEnd(param);
                final TreeSet yearSet = new TreeSet();
                for (int y = begin; y <= end; y++) {
                    final Integer year = new Integer(y);
                    if (yearSetAll.contains(year)) {
                        // 新しい生徒情報で表示されるものは含まない
                    } else {
                        yearSetAll.add(year);
                        yearSet.add(year);
                    }
                }
                if (target == personalInfo) {
                    if (yearSet.isEmpty()) {
                        return -1; // 対象の生徒情報は成績等は表示しない
                    }
                    return ((Integer) yearSet.last()).intValue();
                }
            }
            return -1; // 対象の生徒情報は成績等は表示しない
        }
        
        public int currentGradeCd(final Param param) {
            final int paramYear = Integer.parseInt(param._year);
            int diffyear = 100;
            int currentGrade = -1;
            for (final Iterator it = _personalInfo._gakusekiList.iterator(); it.hasNext();) {
                final Gakuseki gakuseki = (Gakuseki) it.next();
                if (!StringUtils.isNumeric(gakuseki._year) || -1 == gakuseki._gradeCd) {
                    continue;
                }
                final int dy = paramYear - Integer.parseInt(gakuseki._year);
                if (dy >= 0 && diffyear > dy) {
                    currentGrade = gakuseki._gradeCd;
                    diffyear = dy;
                }
            }
            return currentGrade;
        }
        
        public static List<String> getPrintClassList(final List<ClassView> classViewList, final List<ValueRecord> valueRecordList) {
            final Set<String> printClassSet = new TreeSet();
            for (final ClassView classView : classViewList) {
                for (final ViewSubclass viewSubclass : classView._viewSubclassList) {
                    if (!"1".equals(classView._electdiv)) {
                        printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                    } else {
                        for (final View view : viewSubclass._viewList) {
                            for (final ViewStatus viewStatus : view._viewMap.values()) {
                                if (null != viewStatus._status) {
                                    printClassSet.add(classView._classcd + ":" + viewSubclass._subclasscd);
                                }
                            }
                        }
                    }
                }
            }
            for (final ValueRecord valueRecord : valueRecordList) {
                if (!"1".equals(valueRecord._electDiv)) {
                    printClassSet.add(valueRecord._classCd + ":" + valueRecord._subclassCd);
                } else {
                    if (null != valueRecord._value) {
                        printClassSet.add(valueRecord._classCd + ":" + valueRecord._subclassCd);
                    }
                }
            }
            return new ArrayList<String>(printClassSet);
        }
        
        private List<ClassView> getPrintClassViewList() {
            final List<ClassView> classViewList = new ArrayList();
            for (final ClassView cv : _classViewList) {
                for (final ViewSubclass viewSubclass : cv._viewSubclassList) {
                    if (_printClassList.contains(cv._classcd + ":" + viewSubclass._subclasscd)) {
                        classViewList.add(cv);
                        break;
                    }
                }
            }

            final List<ClassView> rtn = new ArrayList();
            for (final ClassView cv : classViewList) {
                
                final ClassView classview = new ClassView(cv._classcd, cv._classname, cv._electdiv);
                rtn.add(classview);
                for (final ViewSubclass viewSubclass : cv._viewSubclassList) {
                    if (_printClassList.contains(cv._classcd + ":" + viewSubclass._subclasscd)) {
                        classview._viewSubclassList.add(viewSubclass);
                    }
                }
            }
            return rtn;
        }
        
        private List<ValueRecord> getValueRecordList() {
            final List<ValueRecord> rtn = new ArrayList();
            for (final ValueRecord vr : _valueRecordList) {
                if (_printClassList.contains(vr._classCd + ":" + vr._subclassCd)) {
                    rtn.add(vr);
                }
            }
            return rtn;
        }

        private static void setSchregEntGrdHistComebackDat(final DB2UDB db2, final Param param, final List studentList, final Map staffMstMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map schregComebackDateMap = new HashMap();
            try {
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    student._schregEntGrdHistComebackDatList = Collections.EMPTY_LIST;
                }
                if (!param._hasSchregEntGrdHistComebackDat) {
                    return;
                }
                final String sql = 
                        " SELECT T1.* "
                        + " FROM SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 "
                        + " WHERE T1.SCHREGNO = ? AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' "
                        + " ORDER BY COMEBACK_DATE ";
                // log.debug(" comeback sql = " + sql);
                ps = db2.prepareStatement(sql);
                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        getMappedList(schregComebackDateMap, student._schregno).add(rs.getString("COMEBACK_DATE"));
                    }
                    db2.commit();
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == schregComebackDateMap.get(student._schregno)) {
                    continue;
                }
                student._schregEntGrdHistComebackDatList = new ArrayList();
                final List comebackDateList = (List) schregComebackDateMap.get(student._schregno);
                log.debug(" schregno = " + student._schregno + ",  comebackdate = " + comebackDateList);
                for (final Iterator cit = comebackDateList.iterator(); cit.hasNext();) {
                    final String comebackDate = (String) cit.next();
                    PersonalInfo comebackPersonalInfo = PersonalInfo.load(db2, param, student, staffMstMap, comebackDate);
                    student._schregEntGrdHistComebackDatList.add(comebackPersonalInfo);
                }
            }
        }
    }

    private String getBirthday(final DB2UDB db2, final String birthday, final String birthdayFlg, final Param param) {
        final String birthdayStr;
        if (param._isSeireki || (!param._isSeireki && "1".equals(birthdayFlg))) {
            birthdayStr = KNJ_EditDate.h_format_S(birthday, "yyyy") + "年" + KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP_MD(birthday));
        } else {
            birthdayStr = KNJ_EditDate.setDateFormat2(KNJ_EditDate.h_format_JP(db2, birthday));
        }
        return birthdayStr;
    }
    
    /**
     * 生徒情報
     */
    private static class PersonalInfo {
        final String _studentName1;
        final String _studentName;
        final String _studentRealName;
        final String _studentNameHistFirst;
        final String _studentKana;
        final String _guardName;
        final String _guardNameHistFirst;
        final String _guardKana;
        final boolean _useRealName;
        final boolean _nameOutputFlg;
        final String _courseName;
        final String _majorName;
        final String _birthdayFlg;
        final String _birthday;
        final String _birthdayFormatted;
        final String _sex;
        final String _finishDate;
        final String _installationDiv;
        final String _jName;
        final String _finschoolTypeName;
        
        final String _entYear;
        final String _entDate;
        final String _entReason;
        final String _entSchool;
        final String _entAddr;
        final Integer _entDiv;
        final String _entDivName;
        final String _grdYear;
        final String _grdDate;
        final String _grdDateFormatted;
        final String _grdReason;
        final String _grdSchool;
        final String _grdAddr;
        final String _grdNo;
        final Integer _grdDiv;
        final String _grdDivName;
        final String _curriculumYear;
        final String _tengakuSakiZenjitu;
        final String _nyugakumaeSyussinJouhou;
        String _comebackDate;
        
        List<Address> _addressList;
        List<Address> _guardianAddressList;
        List<Gakuseki> _gakusekiList;
        List _transferInfoList;

        /**
         * コンストラクタ。
         */
        public PersonalInfo(
        		final DB2UDB db2,
                final Param param,
                final Map baseRow,
                final Student student,
                final Map entGrdHistRow
        ) {
            _entYear    = getString("ENT_YEAR", entGrdHistRow); 
            _entDate    = getString("ENT_DATE", entGrdHistRow);
            _entReason  = getString("ENT_REASON", entGrdHistRow);
            _entSchool  = getString("ENT_SCHOOL", entGrdHistRow);
            _entAddr    = getString("ENT_ADDR", entGrdHistRow);
            _entDiv     = StringUtils.isNumeric(getString("ENT_DIV", entGrdHistRow)) ? Integer.valueOf(getString("ENT_DIV", entGrdHistRow)) : null;
            _entDivName = getString("ENT_DIV_NAME", entGrdHistRow);
            _grdYear    = getString("GRD_YEAR", entGrdHistRow);
            _grdDate    = getString("GRD_DATE", entGrdHistRow);
            _grdReason  = getString("GRD_REASON", entGrdHistRow);
            _grdSchool  = getString("GRD_SCHOOL", entGrdHistRow);
            _grdAddr    = getString("GRD_ADDR", entGrdHistRow);
            _grdNo      = getString("GRD_NO", entGrdHistRow);
            _grdDiv     = StringUtils.isNumeric(getString("GRD_DIV", entGrdHistRow)) ? Integer.valueOf(getString("GRD_DIV", entGrdHistRow)) : null;
            _grdDivName = getString("GRD_DIV_NAME", entGrdHistRow);
            
            _curriculumYear = getString("CURRICULUM_YEAR", entGrdHistRow);
            _tengakuSakiZenjitu = getString("TENGAKU_SAKI_ZENJITU", entGrdHistRow);
            _nyugakumaeSyussinJouhou = getString("NYUGAKUMAE_SYUSSIN_JOUHOU", entGrdHistRow);
            
            if (null != _grdDate) {
            	_grdDateFormatted = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP(db2, _grdDate), param._year);
            } else {
            	_grdDateFormatted = KNJ_EditDate.setDateFormat(db2, null, param._year);
            }
            
            Cond issuedateInRegdStart = (Cond.field("ISSUEDATE").between(_entDate, StringUtils.defaultString(_grdDate, "9999-12-31")))
                                     .or(Cond.field("ISSUEDATE").lessEqual(_entDate).and(Cond.field("EXPIREDATE").isNull().or(Cond.field("EXPIREDATE").greaterEqual(_entDate))));
            if (null == _entDate) {
                // 対象は全て
                issuedateInRegdStart = new Cond.True();
            }
            log.info(" histDateFilter = " + issuedateInRegdStart.toSql());

            {
                final Map baseMstRow = KnjDbUtils.firstRow(student._schregBaseMstList);
                String name = StringUtils.defaultString(getString("NAME", baseMstRow));
                String nameKana = StringUtils.defaultString(getString("NAME_KANA", baseMstRow));
                
                String nameHistFirst = null;
                final List histList = filter(student._schregBaseHistDatList, Cond.field("NAME_FLG").eq("1").and(issuedateInRegdStart));

                if (histList.isEmpty()) {
                    log.debug(" schreg_base_hist_dat (name_flg) empty.");
                } else {
                    final Map histFirstRow = KnjDbUtils.firstRow(histList);
                    nameHistFirst = StringUtils.defaultString(getString("NAME", histFirstRow));
                    
                    final Map histLastRow = KnjDbUtils.lastRow(histList);
                    final String nameHistLast = StringUtils.defaultString(getString("NAME", histLastRow));
                    if (null != nameHistLast && !nameHistLast.equals(name)) {
                        // SCHREG_BASE_MST.NAMEは最新の値。卒業後に変更されているかもしれないのでその際は卒業時点の氏名を表示する。以下同様
                        name = nameHistLast;
                        nameKana = StringUtils.defaultString(getString("NAME_KANA", histLastRow));
                        log.debug(" set (name, nameKana) from hist = (" + name + ", " + nameKana + ")");
                    }
                }

                _useRealName = "1".equals(getString("USE_REAL_NAME", baseRow));
                _nameOutputFlg = "1".equals(getString("NAME_OUTPUT_FLG", baseRow));

                if (_useRealName) {
                    String realName = StringUtils.defaultString(getString("REAL_NAME", baseMstRow));
                    String realNameKana = StringUtils.defaultString(getString("REAL_NAME_KANA", baseMstRow));
                    String realNameHistFirst = null;
                    String nameWithRealNameHistFirst = null;

                    final List realNameHistList = filter(student._schregBaseHistDatList, Cond.field("REAL_NAME_FLG").eq("1").and(issuedateInRegdStart));
                    if (!realNameHistList.isEmpty()) {
                        final Map realNameHistFirstRow = KnjDbUtils.firstRow(realNameHistList);
                        realNameHistFirst = StringUtils.defaultString(getString("REAL_NAME", realNameHistFirstRow));
                        nameWithRealNameHistFirst = StringUtils.defaultString(getString("NAME", realNameHistFirstRow));
                        
                        final Map realNameHistLastRow = KnjDbUtils.lastRow(realNameHistList);
                        String realNameHistLast = StringUtils.defaultString(getString("REAL_NAME", realNameHistLastRow));
                        if (null != realNameHistLast && !realNameHistLast.equals(realName)) {
                            realName = realNameHistLast;
                            realNameKana = StringUtils.defaultString(getString("REAL_NAME_KANA", realNameHistLastRow));
                            name = StringUtils.defaultString(getString("NAME", realNameHistLastRow));
                            nameKana = StringUtils.defaultString(getString("NAME_KANA", realNameHistLastRow));
                            log.debug(" set (realName, realNameKana, name, nameKana) from hist = (" + realName + ", " + realNameKana + ", " + name + ", " + nameKana + ")");
                        }
                    }
                    _studentName          = name;
                    _studentRealName      = realName;
                    _studentKana          = getPrintName(_useRealName, _nameOutputFlg, realNameKana, nameKana);
                    _studentName1         = getPrintName(_useRealName, _nameOutputFlg, realName, name);
                    _studentNameHistFirst = getPrintName(_useRealName, _nameOutputFlg, realNameHistFirst, nameWithRealNameHistFirst);
                } else {
                    _studentName          = name;
                    _studentRealName      = null;
                    _studentKana          = getPrintName(_useRealName, _nameOutputFlg, null, nameKana);
                    _studentName1         = getPrintName(_useRealName, _nameOutputFlg, null, name);
                    _studentNameHistFirst = getPrintName(_useRealName, _nameOutputFlg, null, nameHistFirst);
                }
            }

            {
                final Map guardianDatRow = KnjDbUtils.firstRow(student._guardianDatList);
                String guardName = StringUtils.defaultString(getString("GUARD_NAME", guardianDatRow));
                String guardKana = StringUtils.defaultString(getString("GUARD_KANA", guardianDatRow));

                String guardNameHistFirst = null;
                final List guardHistList = filter(student._guardianHistDatList, Cond.field("GUARD_NAME_FLG").eq("1").and(issuedateInRegdStart));
                if (!guardHistList.isEmpty()) {
                    final Map firstRow = KnjDbUtils.firstRow(guardHistList);
                    guardNameHistFirst = StringUtils.defaultString(getString("GUARD_NAME", firstRow));
                    
                    final Map lastRow = KnjDbUtils.lastRow(guardHistList);
                    final String guardNameHistLast = StringUtils.defaultString(getString("GUARD_NAME", lastRow));
                    if (null != guardNameHistLast && !guardNameHistLast.equals(guardName)) {
                        guardName = guardNameHistLast;
                        guardKana = StringUtils.defaultString(getString("GUARD_KANA", lastRow));
                        log.debug(" set (guardName, guardKana) from hist = (" + guardName + ", " + guardKana + ")");
                    }
                }

                final boolean useGuardRealName = "1".equals(getString("USE_GUARD_REAL_NAME", baseRow));
                final boolean guardNameOutputFlg = "1".equals(getString("GUARD_NAME_OUTPUT_FLG", baseRow));
                if (useGuardRealName) {
                    String guardRealKana = StringUtils.defaultString(getString("GUARD_REAL_KANA", guardianDatRow));
                    String guardRealName = StringUtils.defaultString(getString("GUARD_REAL_NAME", guardianDatRow));
                    String guardRealNameHistFirst = null;
                    String guardNameWithGuardRealNameHistFirst = null;

                    final List guardRealNameHistList = filter(student._guardianHistDatList, Cond.field("GUARD_REAL_NAME_FLG").eq("1").and(issuedateInRegdStart));
                    if (!guardRealNameHistList.isEmpty()) {
                        final Map firstRow = KnjDbUtils.firstRow(guardRealNameHistList);
                        guardRealNameHistFirst = StringUtils.defaultString(getString("GUARD_REAL_NAME", firstRow));
                        guardNameWithGuardRealNameHistFirst = StringUtils.defaultString(getString("GUARD_NAME", firstRow));
                        
                        final Map lastRow = KnjDbUtils.lastRow(guardRealNameHistList);
                        final String guardRealNameHistLast = StringUtils.defaultString(getString("GUARD_REAL_NAME", lastRow));
                        if (null != guardRealNameHistLast && guardRealNameHistLast.equals(guardRealName)) {
                            guardRealKana = StringUtils.defaultString(getString("GUARD_REAL_KANA", lastRow));
                            guardRealName = guardRealNameHistLast;
                            guardKana = StringUtils.defaultString(getString("GUARD_KANA", lastRow));
                            guardName = StringUtils.defaultString(getString("GUARD_NAME", lastRow));
                            log.debug(" set (guardRealName, guardRealKana, guardName, guardKana) from hist = (" + guardRealName + ", " + guardRealKana + ", " + guardName + ", " + guardKana + ")");
                        }
                    }
                    _guardKana          = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealKana, guardKana);
                    _guardName          = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealName, guardName);
                    _guardNameHistFirst = getPrintName(useGuardRealName, guardNameOutputFlg, guardRealNameHistFirst, guardNameWithGuardRealNameHistFirst);
                } else {
                    _guardKana          = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardKana);
                    _guardName          = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardName);
                    _guardNameHistFirst = getPrintName(useGuardRealName, guardNameOutputFlg, null, guardNameHistFirst);
                }
            }
            
            _courseName = getString("COURSENAME", baseRow);
            _majorName = getString("MAJORNAME", baseRow);
            
            _birthdayFlg = getString("BIRTHDAY_FLG", baseRow);
            _birthday = getString("BIRTHDAY", baseRow);
            if (null != _birthday) {
            	_birthdayFormatted = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP(db2, _birthday), param._year) + "生";
            } else {
            	_birthdayFormatted = KNJ_EditDate.setDateFormat(db2, null, param._year) + "生";
            }
            _sex = getString("SEX", baseRow);
            _finishDate = KNJ_EditDate.setDateFormat(db2, KNJ_EditDate.h_format_JP_M(db2, getString("FINISH_DATE", baseRow)), param._year);
            _installationDiv = getString("INSTALLATION_DIV", baseRow);
            _jName = getString("J_NAME", baseRow);
            _finschoolTypeName = getString("FINSCHOOL_TYPE_NAME", baseRow);
        }
        
        private static String getPrintName(final boolean useRealName, final boolean outputFlg, final String realName, final String name) {
            final String rtn;
            if (useRealName && outputFlg && !StringUtils.isBlank(realName) && !StringUtils.isBlank(name)) {
                rtn = realName + "（" + name + "）";
            } else if (useRealName && !StringUtils.isBlank(realName)) {
                rtn = realName;
            } else {
                rtn = name;
            }
            return StringUtils.defaultString(rtn);
        }
        
        public int getYearBegin() {
            return null == _entDate ? 0 : getNendo(getCalendarOfDate(_entDate));
        }

        public int getYearEnd(final Param param) {
            return Math.min(Integer.parseInt(param._year), null == _grdDate ? 9999 : getNendo(getCalendarOfDate(_grdDate)));
        }

        /**
         * 生徒情報を得る
         */
        public static PersonalInfo load(final DB2UDB db2, final Param param, final Student student, final Map staffMstMap, final String comebackDate) {
            final Map entGrdHistRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, sql_state(param, student._schregno, comebackDate), null));
            
            final Map baseRow = KnjDbUtils.lastRow(KnjDbUtils.query(db2, sql_info_reg(param, student._schregno, comebackDate), null));
            
            final PersonalInfo personalInfo = new PersonalInfo(db2, param, baseRow, student, entGrdHistRow);

            personalInfo._addressList = Address.load(db2, param, false, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._guardianAddressList = Address.load(db2, param, true, student, personalInfo._entDate, personalInfo._grdDate);
            personalInfo._comebackDate = comebackDate;
            personalInfo._gakusekiList = Gakuseki.load(db2, param, staffMstMap, student, personalInfo._grdDate);
            personalInfo._transferInfoList = loadTransferList(db2, param, student, personalInfo._entDate, personalInfo._grdDate);
            return personalInfo;
        }
        
        /**
         * 住所履歴を得る
         */
        public static List loadTransferList(final DB2UDB db2, final Param param, final Student student, final String startDate, final String endDate) {
            final List transferList = new ArrayList();
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * ");
            sql.append(" FROM SCHREG_TRANSFER_DAT ");
            sql.append(" WHERE SCHREGNO = '" + student._schregno + "' ");
            sql.append(" ORDER BY TRANSFER_SDATE ");
            transferList.addAll((List) KnjDbUtils.query(db2, sql.toString()));
            return transferList;
        }
        
        public static String sql_info_reg(final Param param, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            final String switch6 = "1";
            sql.append("SELECT ");
            sql.append("  T2.BIRTHDAY, T7.ABBV1 AS SEX,");
            sql.append("  T21.BIRTHDAY_FLG, ");
            sql.append("  T1.GRADE, T1.ATTENDNO, T1.ANNUAL,");
            // 課程・学科・コース
            sql.append("  T3.COURSENAME, T4.MAJORNAME, T5.COURSECODENAME,");
            // 卒業情報
            sql.append("  EGHIST.FINISH_DATE,");
            sql.append("  FIN_S.FINSCHOOL_NAME AS J_NAME,");
            sql.append("  NM_MST.NAME1 AS INSTALLATION_DIV,");
            sql.append("  VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,");
            sql.append("  (CASE WHEN T11.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_REAL_NAME, ");
            sql.append("  T11.NAME_OUTPUT_FLG, ");
            sql.append("  (CASE WHEN T26.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS USE_GUARD_REAL_NAME, ");
            sql.append("  T26.GUARD_NAME_OUTPUT_FLG, ");
            sql.append("  T1.SCHREGNO ");
            sql.append("FROM ");
            // 学籍情報
            sql.append("  (   SELECT     * ");
            sql.append("  FROM       SCHREG_REGD_DAT T1 ");
            sql.append("  WHERE      T1.SCHREGNO = '" + schregno + "' AND T1.YEAR = '" + param._year + "' ");
            if (switch6.equals("1")) { // 学期を特定
                sql.append("  AND T1.SEMESTER = '" + param._gakki + "' ");
            } else {
                // 最終学期
                sql.append("  AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SCHREG_REGD_DAT WHERE SCHREGNO = '" + schregno + "' AND YEAR = '" + param._year + "')");
            }
            sql.append("  ) T1 ");
            sql.append("INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ");
            // 基礎情報
            sql.append("INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            sql.append("LEFT JOIN NAME_MST T7 ON T7.NAMECD1 = 'Z002' AND T7.NAMECD2 = T2.SEX ");
            sql.append("LEFT JOIN ");
            if (null != comebackDate) {
                sql.append(" SCHREG_ENT_GRD_HIST_COMEBACK_DAT EGHIST ON EGHIST.COMEBACK_DATE = '" + comebackDate + "' AND ");
            } else {
                sql.append(" SCHREG_ENT_GRD_HIST_DAT EGHIST ON ");
            }
            sql.append("    EGHIST.SCHREGNO = T1.SCHREGNO AND EGHIST.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            sql.append("LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = EGHIST.FINSCHOOLCD ");
            sql.append("LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ");
            sql.append("LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ");
            // 課程、学科、コース
            sql.append("LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ");
            sql.append("LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ");
            sql.append("LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR AND VALUE(T5.COURSECODE, '0000') = VALUE(T1.COURSECODE, '0000')");
            // 生徒住所
            sql.append("LEFT JOIN SCHREG_NAME_SETUP_DAT T11 ON T11.SCHREGNO = T2.SCHREGNO AND T11.DIV = '02' ");
            sql.append("LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ");
            
            sql.append("LEFT JOIN GUARDIAN_NAME_SETUP_DAT T26 ON T26.SCHREGNO = T2.SCHREGNO AND T26.DIV = '02' ");
            return sql.toString();
        }
        
        private static String sql_state(final Param param, final String schregno, final String comebackDate) {
            final StringBuffer sql = new StringBuffer();
            sql.append(" SELECT ");
            sql.append("    FISCALYEAR(ENT_DATE) AS ENT_YEAR, ");
            sql.append("    ENT_DATE, ");
            sql.append("    ENT_REASON, ");
            sql.append("    ENT_SCHOOL, ");
            sql.append("    ENT_ADDR, ");
            sql.append("    ENT_DIV, ");
            sql.append("    T3.NAME1 AS ENT_DIV_NAME, ");
            sql.append("    FISCALYEAR(GRD_DATE) AS GRD_YEAR, ");
            sql.append("    GRD_DATE, ");
            sql.append("    GRD_REASON, ");
            sql.append("    GRD_SCHOOL, ");
            sql.append("    GRD_ADDR, ");
            sql.append("    GRD_NO, ");
            sql.append("    GRD_DIV, ");
            sql.append("    T4.NAME1 AS GRD_DIV_NAME, ");
            sql.append("    T1.CURRICULUM_YEAR, ");
            sql.append("    T1.TENGAKU_SAKI_ZENJITU, ");
            sql.append("    T1.NYUGAKUMAE_SYUSSIN_JOUHOU ");
            sql.append(" FROM ");
            if (null != comebackDate) {
                sql.append("    SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1 ");
            } else {
                sql.append("    SCHREG_ENT_GRD_HIST_DAT T1 ");
            }
            sql.append("    LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ");
            sql.append("    LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ");
            sql.append(" WHERE ");
            sql.append("    T1.SCHREGNO = '" + schregno + "' AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            if (null != comebackDate) {
                sql.append("    AND T1.COMEBACK_DATE = '" + comebackDate + "' ");
            }
            return sql.toString();
        }

        public Address getStudentAddressMax() {
            return _addressList == null || _addressList.isEmpty() ? null : (Address) _addressList.get(0);
        }
    }
    
    /**
     * 在籍データ
     */
    private static class Gakuseki {
        private static Map hmap = null;
        
        final int _i;
        final String _year;
        final String _grade;
        final int _gradeCd;
        final String _hrname;
        final String _attendno;
        final String _nendo;
        final Staff _principal;
        final Staff _principal1;
        final Staff _principal2;
        final Staff _staff1;
        final Staff _staff2;
//        final String _staffSeq;
//        final String _principalSeq;
//        final String _kaizanFlg;
        
        public Gakuseki(
                final int i,
                final String year,
                final String grade,
                final int gradeCd,
                final String hrname,
                final String attendno,
                final String nendo,
                final Staff principal,
                final Staff principal1,
                final Staff principal2,
                final Staff staff1,
                final Staff staff2,
//                final String chageOpiSeq,
//                final String lastOpiSeq,
//                final String flg,
                final String chageStampNo,
                final String lastStampNo,
                final String lastStampNo1) {
            _i = i;
            _year = year;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrname = hrname;
            _attendno = attendno;
            _nendo = nendo;
//            _staffSeq = chageOpiSeq;
//            _principalSeq = lastOpiSeq;
//            _kaizanFlg = flg;
            _principal = principal;
            _principal1 = principal1;
            _principal2 = principal2;
            _staff1 = staff1;
            _staff2 = staff2;
        }
        
        /**
         * 在籍データのリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List load(final DB2UDB db2, final Param param, final Map staffMstMap, final Student student, final String grdDate) {
            final List gakusekiList = new ArrayList();
            try {
                if (hmap == null) {
                    hmap = KNJ_Get_Info.getMapForHrclassName(db2); // 表示用組
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_2 error!", ex);
            } finally {
                db2.commit();
            }
            
            final String sql = sqlSchGradeRec(param, student, grdDate);
            
            for (final Iterator it = KnjDbUtils.query(db2, sql).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                
                final String year = getString("YEAR", row);
                final String grade = getString("GRADE", row);
                final String hrClass = getString("HR_CLASS", row);
                final String attendno = getString("ATTENDNO", row);
//                final String principalstaffcd = getString("PRINCIPALSTAFFCD", row);
                final String principalStaffcd1 = getString("PRINCIPALSTAFFCD1", row);
                final String principalStaffcd2 = getString("PRINCIPALSTAFFCD2", row);

                final String principalname = getString("PRINCIPALNAME", row);

                final String staffcd1 = getString("STAFFCD1", row);
                final String staffcd2 = getString("STAFFCD2", row);
                final String staff1FromDate = getString("STAFF1_FROM_DATE", row);
                final String staff2FromDate = getString("STAFF2_FROM_DATE", row);
                final String staff1ToDate = getString("STAFF1_TO_DATE", row);
                final String staff2ToDate = getString("STAFF2_TO_DATE", row);
                final String principal1FromDate = getString("PRINCIPAL1_FROM_DATE", row);
                final String principal1ToDate = getString("PRINCIPAL1_TO_DATE", row);
                final String principal2FromDate = getString("PRINCIPAL2_FROM_DATE", row);
                final String principal2ToDate = getString("PRINCIPAL2_TO_DATE", row);
                
//                final String chageOpiSeq = getString("CHAGE_OPI_SEQ", row);
//                final String lastOpiSeq = getString("LAST_OPI_SEQ", row);
//                final String flg = getString("FLG", row);
                final String chageStampNo = getString("CHAGE_STAMP_NO", row);
                final String lastStampNo = getString("LAST_STAMP_NO", row);
                final String lastStampNo1 = getString("LAST_STAMP_NO1", row);
                final int i = param.getGradeCd(getString("YEAR", row), grade); // 学年
                final int gradeCd = param.getGradeCd(getString("YEAR", row), grade); // 学年
                
                String hrname = null;
                if ("1".equals(param._useSchregRegdHdat)) {
                    hrname = getString("HR_CLASS_NAME1", row);
                } else if ("0".equals(param._useSchregRegdHdat)) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrClass, hmap);
                }
                if (hrname == null) {
                    hrname = KNJ_EditEdit.Ret_Num_Str(hrClass);
                }
                final String nendo = KNJ_EditDate.setNendoFormat(db2, KNJ_EditDate.gengou(db2, Integer.parseInt(year)) + "年度", param._year);
                
                final Staff principal = new Staff(year, new StaffMst(null, principalname, null, null, null), null, null, lastStampNo);
                final Staff principal1 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd1), principal1FromDate, principal1ToDate, lastStampNo1);
                final Staff principal2 = new Staff(year, StaffMst.get(staffMstMap, principalStaffcd2), principal2FromDate, principal2ToDate, null);
                final Staff staff1 = new Staff(year, StaffMst.get(staffMstMap, staffcd1), staff1FromDate, staff1ToDate, chageStampNo);
                final Staff staff2 = new Staff(year, StaffMst.get(staffMstMap, staffcd2), staff2FromDate, staff2ToDate, null);
                
                final Gakuseki gakuseki = new Gakuseki(i, year, grade, gradeCd, hrname, attendno, nendo,
                        principal, principal1, principal2, staff1, staff2,
                        //chageOpiSeq, lastOpiSeq, flg,
                        chageStampNo, lastStampNo, lastStampNo1);
                gakusekiList.add(gakuseki);

            }
            return gakusekiList;
        }
        
        /**
         * @return 学籍履歴のＳＱＬ文を戻します。
         */
        private static String sqlSchGradeRec(final Param param, final Student student, final String grdDate) {
            final String certifKind = "108";
            final StringBuffer stb = new StringBuffer();
            // 印鑑関連 1
            stb.append(" WITH T_INKAN AS ( ");
            stb.append("     SELECT ");
            stb.append("         MAX(STAMP_NO) AS STAMP_NO, ");
            stb.append("         STAFFCD ");
            stb.append("     FROM ");
            stb.append("         ATTEST_INKAN_DAT ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD ");
            
            stb.append(" ), YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MAX(SEMESTER) AS SEMESTER ");
            stb.append("     FROM SCHREG_REGD_DAT ");
            stb.append("     WHERE SCHREGNO = '" + student._schregno + "' ");
            stb.append("     GROUP BY YEAR ");
            if (null != grdDate) {
                stb.append("     UNION ALL ");
                stb.append("     SELECT ");
                stb.append("     T1.YEAR, ");
                stb.append("     T1.SEMESTER ");
                stb.append("     FROM SCHREG_REGD_DAT T1 ");
                stb.append("     INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR  ");
                stb.append("         AND T2.SEMESTER = T1.SEMESTER ");
                stb.append("         AND '" + grdDate + "' BETWEEN T2.SDATE AND T2.EDATE ");
                stb.append("     WHERE T1.SCHREGNO = '" + student._schregno + "' ");
            }

            stb.append(" ), MIN_YEAR_SEMESTER AS ( ");
            stb.append("     SELECT ");
            stb.append("     YEAR, ");
            stb.append("     MIN(SEMESTER) AS SEMESTER ");
            stb.append("     FROM YEAR_SEMESTER ");
            stb.append("     GROUP BY YEAR ");

            stb.append(" ), T_TEACHER AS ( ");
            stb.append("     SELECT ");
            stb.append("         STAFFCD, ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         FROM_DATE, ");
            stb.append("         MIN(TO_DATE) AS TO_DATE ");
            stb.append("     FROM ");
            stb.append("         STAFF_CLASS_HIST_DAT ");
            stb.append("     WHERE ");
            stb.append("         TR_DIV = '1' ");
            stb.append("     GROUP BY ");
            stb.append("         STAFFCD, YEAR, GRADE, HR_CLASS, FROM_DATE ");
            stb.append(" ), T_MINMAX_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         YEAR, ");
            stb.append("         GRADE, ");
            stb.append("         HR_CLASS, ");
            stb.append("         MAX(FROM_DATE) AS MAX_FROM_DATE, ");
            stb.append("         MIN(FROM_DATE) AS MIN_FROM_DATE ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER ");
            stb.append("     GROUP BY ");
            stb.append("         YEAR, GRADE, HR_CLASS ");
            stb.append(" ), T_TEACHER_MIN_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MIN_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MIN_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), T_TEACHER_MAX_FROM_DATE AS ( ");
            stb.append("     SELECT ");
            stb.append("         MIN(STAFFCD) AS STAFFCD, T1.FROM_DATE AS MAX_FROM_DATE, T1.YEAR, T1.GRADE, T1.HR_CLASS ");
            stb.append("     FROM ");
            stb.append("         T_TEACHER T1 ");
            stb.append("         INNER JOIN T_MINMAX_DATE T2 ON T2.MAX_FROM_DATE = T1.FROM_DATE ");
            stb.append("            AND T2.YEAR = T1.YEAR ");
            stb.append("            AND T2.GRADE = T1.GRADE ");
            stb.append("            AND T2.HR_CLASS = T1.HR_CLASS ");
            stb.append("     GROUP BY ");
            stb.append("         T1.YEAR, T1.GRADE, T1.HR_CLASS, T1.FROM_DATE ");
            stb.append(" ), REGD AS ( ");
            stb.append("      SELECT  T1.YEAR, T1.SEMESTER, T1.GRADE, T1.ANNUAL, T1.HR_CLASS, T1.ATTENDNO, T1.SCHREGNO");
            stb.append("      FROM    SCHREG_REGD_DAT T1");
            stb.append("      WHERE   T1.SCHREGNO = '" + student._schregno + "' ");
            stb.append("          AND T1.YEAR <= '" + param._year + "'");
            stb.append("          AND T1.SEMESTER = (SELECT SEMESTER FROM  MIN_YEAR_SEMESTER WHERE YEAR = T1.YEAR) ");
            stb.append(" ), PRINCIPAL_HIST AS ( ");
            stb.append("     SELECT ");
            stb.append("         T2.YEAR, T1.FROM_DATE, T1.TO_DATE, T1.STAFFCD, ROW_NUMBER() OVER(PARTITION BY T2.YEAR ORDER BY T1.FROM_DATE) AS ORDER ");
            stb.append("     FROM ");
            stb.append("         STAFF_PRINCIPAL_HIST_DAT T1 ,REGD T2 ");
            stb.append("     WHERE ");
            stb.append("         T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("         AND FISCALYEAR(T1.FROM_DATE) <= T2.YEAR AND T2.YEAR <=  FISCALYEAR(VALUE(T1.TO_DATE, '9999-12-31')) ");
            stb.append(" ), YEAR_PRINCIPAL AS ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR ");
            stb.append("         ,T2.STAFFCD AS PRINCIPALSTAFFCD1, T2.FROM_DATE AS PRINCIPAL1_FROM_DATE, T2.TO_DATE AS PRINCIPAL1_TO_DATE ");
            stb.append("         ,T3.STAFFCD AS PRINCIPALSTAFFCD2, T3.FROM_DATE AS PRINCIPAL2_FROM_DATE, T3.TO_DATE AS PRINCIPAL2_TO_DATE ");
            stb.append("     FROM ( ");
            stb.append("       SELECT YEAR, MIN(ORDER) AS FIRST, MAX(ORDER) AS LAST FROM PRINCIPAL_HIST GROUP BY YEAR ");
            stb.append("      ) T1 ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T2 ON T2.YEAR = T1.YEAR AND T2.ORDER = T1.LAST ");
            stb.append("      INNER JOIN PRINCIPAL_HIST T3 ON T3.YEAR = T1.YEAR AND T3.ORDER = T1.FIRST ");
            stb.append(" ) ");
            
            stb.append(" SELECT ");
            stb.append("    T1.YEAR ");
            stb.append("   ,T1.GRADE ");
            stb.append("   ,T1.HR_CLASS ");
            stb.append("   ,T1.ATTENDNO ");
            stb.append("   ,T1.ANNUAL ");
            stb.append("   ,T3.HR_NAME ");
            if ("1".equals(param._useSchregRegdHdat)) {
                stb.append("         ,T3.HR_CLASS_NAME1");
            }
            stb.append("   ,T4.STAFFCD AS STAFFCD1 ");
            stb.append("   ,T9.STAFFCD AS STAFFCD2 ");
            stb.append("   ,T10.FROM_DATE AS STAFF1_FROM_DATE, T10.TO_DATE AS STAFF1_TO_DATE ");
            stb.append("   ,T11.FROM_DATE AS STAFF2_FROM_DATE, T11.TO_DATE AS STAFF2_TO_DATE ");
            stb.append("   ,T6.REMARK7 AS PRINCIPALSTAFFCD, T6.PRINCIPAL_NAME AS PRINCIPALNAME ");
            stb.append("   ,T13.STAFFCD AS PRINCIPALSTAFFCD1 ");
            stb.append("   ,T14.STAFFCD AS PRINCIPALSTAFFCD2 ");
            stb.append("   ,T12.PRINCIPAL1_FROM_DATE, T12.PRINCIPAL1_TO_DATE ");
            stb.append("   ,T12.PRINCIPAL2_FROM_DATE, T12.PRINCIPAL2_TO_DATE ");

            // 印鑑関連 2
            stb.append("   ,ATTEST.CHAGE_OPI_SEQ ");
            stb.append("   ,ATTEST.LAST_OPI_SEQ ");
            stb.append("   ,ATTEST.FLG ");
            stb.append("   ,IN1.STAMP_NO AS CHAGE_STAMP_NO ");
            stb.append("   ,IN2.STAMP_NO AS LAST_STAMP_NO ");
            stb.append("   ,IN21.STAMP_NO AS LAST_STAMP_NO1 ");
            stb.append("   ,IN22.STAMP_NO AS LAST_STAMP_NO2 ");
            
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER ");
            stb.append("                              AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MAX_FROM_DATE T10A ON T10A.YEAR = T1.YEAR ");
            stb.append("          AND T10A.GRADE = T1.GRADE AND T10A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T10 ON T10.STAFFCD = T10A.STAFFCD ");
            stb.append("          AND T10.FROM_DATE = T10A.MAX_FROM_DATE AND T10.YEAR = T1.YEAR AND T10.GRADE = T1.GRADE AND T10.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER_MIN_FROM_DATE T11A ON T11A.YEAR = T1.YEAR ");
            stb.append("          AND T11A.GRADE = T1.GRADE AND T11A.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN T_TEACHER T11 ON T11.STAFFCD = T11A.STAFFCD ");
            stb.append("          AND T11.FROM_DATE = T11A.MIN_FROM_DATE AND T11.YEAR = T1.YEAR AND T11.GRADE = T1.GRADE AND T11.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST T4 ON T4.STAFFCD = T10.STAFFCD ");
            stb.append(" LEFT JOIN STAFF_MST T9 ON T9.STAFFCD = T11.STAFFCD ");
            
            stb.append(" LEFT JOIN CERTIF_SCHOOL_DAT T6 ON T6.YEAR = T1.YEAR ");
            stb.append("      AND T6.CERTIF_KINDCD = '" + certifKind + "'");
            
            // 印鑑関連 3
            stb.append(" LEFT JOIN T_INKAN IN1 ON IN1.STAFFCD = T10.STAFFCD ");
            stb.append(" LEFT JOIN T_INKAN IN2 ON IN2.STAFFCD = T6.REMARK7 ");
            stb.append(" LEFT JOIN ( ");
            stb.append("     SELECT ");
            stb.append("         T1.YEAR, ");
            stb.append("         T1.SCHREGNO, ");
            stb.append("         T1.CHAGE_OPI_SEQ, ");
            stb.append("         T1.LAST_OPI_SEQ, ");
            stb.append("         L1.FLG ");
            stb.append("     FROM ");
            stb.append("         ATTEST_OPINIONS_WK T1 ");
            stb.append("         LEFT JOIN ATTEST_OPINIONS_UNMATCH L1 ");
            stb.append("                ON L1.YEAR = T1.YEAR ");
            stb.append("               AND L1.SCHREGNO = T1.SCHREGNO ");
            stb.append("      ) ATTEST ON ATTEST.YEAR = T1.YEAR AND ATTEST.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN YEAR_PRINCIPAL T12 ON T12.YEAR = T1.YEAR ");
            stb.append(" LEFT JOIN STAFF_MST T13 ON T13.STAFFCD = T12.PRINCIPALSTAFFCD1 ");
            stb.append(" LEFT JOIN STAFF_MST T14 ON T14.STAFFCD = T12.PRINCIPALSTAFFCD2 ");
            stb.append(" LEFT JOIN T_INKAN IN21 ON IN21.STAFFCD = T13.STAFFCD ");
            stb.append(" LEFT JOIN T_INKAN IN22 ON IN22.STAFFCD = T14.STAFFCD ");
            
            stb.append(" ORDER BY T1.HR_CLASS ");
            return stb.toString();
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフマスタ>>。
     */
    private static class StaffMst {
        /**pkg*/ static StaffMst Null = new StaffMst(null, null, null, null, null);
        final String _staffcd;
        final String _name;
        final String _kana;
        final String _nameReal;
        final String _kanaReal;
        private final Map _yearStaffNameSetUp;
        public StaffMst(final String staffcd, final String name, final String kana, final String nameReal, final String kanaReal) {
            _staffcd = staffcd;
            _name = name;
            _kana = kana;
            _nameReal = nameReal;
            _kanaReal = kanaReal;
            _yearStaffNameSetUp = new HashMap();
        }
        public boolean isPrintNameBoth(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            if (null != nameSetup) {
                return "1".equals(nameSetup.get("NAME_OUTPUT_FLG"));
            }
            return false;
        }
        public boolean isPrintNameReal(final String year) {
            final Map nameSetup = (Map) _yearStaffNameSetUp.get(year);
            return null != nameSetup;
        }
        
        public List<String> getNameLine(final String year) {
            final String[] name;
            if (isPrintNameBoth(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    if (StringUtils.isBlank(_name)) {
                        name = new String[]{_nameReal};
                    } else {
                        final String n = "（" + _name + "）";
                        if ((null == _nameReal ? "" : _nameReal).equals(_name)) {
                            name =  new String[]{_nameReal};
                        } else if (KNJ_EditEdit.getMS932ByteLength(_nameReal + n) > 26) {
                            name =  new String[]{_nameReal, n};
                        } else {
                            name =  new String[]{_nameReal + n};
                        }
                    }
                }
            } else if (isPrintNameReal(year)) {
                if (StringUtils.isBlank(_nameReal)) {
                    name = new String[]{_name};
                } else {
                    name = new String[]{_nameReal};
                }
            } else {
                name = new String[]{_name};
            }
            return Arrays.asList(name);
        }

        public static StaffMst get(final Map<String, StaffMst>  staffMstMap, final String staffcd) {
            if (null == staffMstMap || null == staffMstMap.get(staffcd)) {
                return Null;
            }
            return staffMstMap.get(staffcd);
        }

        public static Map<String, StaffMst> load(final DB2UDB db2, final String year) {
            final Map<String, StaffMst> rtn = new HashMap();

            final String sql1 = "SELECT * FROM STAFF_MST ";
            for (final Iterator it = KnjDbUtils.query(db2, sql1).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                final String staffcd = (String) m.get("STAFFCD");
                final String name = (String) m.get("STAFFNAME");
                final String kana = (String) m.get("STAFFNAME_KANA");
                final String nameReal = (String) m.get("STAFFNAME_REAL");
                final String kanaReal = (String) m.get("STAFFNAME_KANA_REAL");
                
                final StaffMst s = new StaffMst(staffcd, name, kana, nameReal, kanaReal);
                
                rtn.put(s._staffcd, s);
            }
            
            final String sql2 = "SELECT STAFFCD, YEAR, NAME_OUTPUT_FLG FROM STAFF_NAME_SETUP_DAT WHERE YEAR <= '" + year + "' AND DIV = '02' ";
            for (final Iterator it = KnjDbUtils.query(db2, sql2).iterator(); it.hasNext();) {
                final Map m = (Map) it.next();
                if (null == rtn.get(getString("STAFFCD", m))) {
                    continue;
                }
                final StaffMst s = (StaffMst) rtn.get(getString("STAFFCD", m));
                
                final Map nameSetupDat = new HashMap();
                nameSetupDat.put("NAME_OUTPUT_FLG", m.get("NAME_OUTPUT_FLG"));
                s._yearStaffNameSetUp.put(m.get("YEAR"), nameSetupDat);
            }
            return rtn;
        }

        public String toString() {
            return "StaffMst(staffcd=" + _staffcd + ", name=" + _name + ", nameSetupDat=" + _yearStaffNameSetUp + ")";
        }
    }

    // --- 内部クラス -------------------------------------------------------
    /**
     * <<スタッフクラス>>。
     */
    private static class Staff {
        /**pkg*/ static Staff Null = new Staff(null, StaffMst.Null, null, null, null);
        final String _year;
        final StaffMst _staffMst;
        final String _dateFrom;
        final String _dateTo;
        final String _stampNo;
        public Staff(final String year, final StaffMst staffMst, final String dateFrom, final String dateTo, final String stampNo) {
            _year = year;
            _staffMst = staffMst;
            _dateFrom = dateFrom;
            _dateTo = dateTo;
            _stampNo = stampNo;
        }
        
        public String getNameString() {
            final StringBuffer stb = new StringBuffer();
            final List name = _staffMst.getNameLine(_year);
            for (int i = 0; i < name.size(); i++) {
                if (null == name.get(i)) continue;
                stb.append(name.get(i));
            }
            return stb.toString();
        }

        public List getNameBetweenLine() {
            final String fromDate = toYearDate(_dateFrom, _year);
            final String toDate = toYearDate(_dateTo, _year);
            final String between = StringUtils.isBlank(fromDate) && StringUtils.isBlank(toDate) ? "" : "(" + jpMonthName(fromDate) + "\uFF5E" + jpMonthName(toDate) + ")";
            
            final List rtn;
            if (KNJ_EditEdit.getMS932ByteLength(getNameString() + between) > 26) {
                rtn = Arrays.asList(new String[]{getNameString(), between});
            } else {
                rtn = Arrays.asList(new String[]{getNameString() + between});
            }
            return rtn;
        }
        
        private String toYearDate(final String date, final String year) {
            if (null == date) {
                return null;
            }
            final String sdate = year + "-04-01";
            final String edate = String.valueOf(Integer.parseInt(year) + 1) + "-03-31";
            if (date.compareTo(sdate) <= 0) {
                return sdate;
            } else if (date.compareTo(edate) >= 0) {
                return edate;
            }
            return date;
        }

        private String jpMonthName(final String date) {
            if (StringUtils.isBlank(date)) {
                return "";
            }
            return new SimpleDateFormat("M月").format(java.sql.Date.valueOf(date));
        }

        public String toString() {
            return "Staff(year=" + _year + ", staffMst=" + _staffMst + ", dateFrom=" + _dateFrom + ", dateTo=" + _dateTo + ", stampNo="+ _stampNo + ")";
        }
    }
    
    /**
     * 出欠データ
     */
    private static class Attend {
        final String _year;
        final int _g;
        final String _lesson;
        final String _suspendMourning;
        final String _abroad;
        final String _requirePresent;
        final String _present;
        final String _absent;
        final String _late;
        final String _early;
        
        public Attend(
                final String year, 
                final int g,
                final String lesson, 
                final String suspendMourning,
                final String abroad,
                final String requirePresent,
                final String present,
                final String absent,
                final String late,
                final String early) {
            _year = year;
            _g = g;
            _lesson = lesson;
            _suspendMourning = suspendMourning;
            _abroad = abroad;
            _requirePresent = requirePresent;
            _present = present;
            _absent = absent;
            _late = late;
            _early = early;
        }
        
        /**
         *  年次ごとの出欠データのリストを得る
         */
        public static List load(final DB2UDB db2, final Param param, final Student student) {
            final List attendRecordList = new ArrayList();
            final String psKey = "PS_ATTEND";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql = getAttendSql(param);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
            } catch (Exception ex) {
                log.error("printSvfAttendRecord error!", ex);
            } finally {
                db2.commit();
            }
            
            for (final Iterator it = KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] { student._schregno, param._year, }).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String year = getString("YEAR", row);
                if (null == year) {
                    continue;
                }
                final int g = KNJA133J_0.getG(param, year, getString("ANNUAL", row));
                final String lesson = getString("LESSON", row);
                final String suspendMourning = getString("SUSPEND_MOURNING", row);
                final String abroad = getString("ABROAD", row);
                final String requirePresent = getString("REQUIREPRESENT", row);
                final String present = getString("PRESENT", row);
                final String absent = getString("ABSENT", row);
                final String late = getString("LATE", row);
                final String early = getString("EARLY", row);
                
                final Attend attendRecord = new Attend(year, g, lesson, suspendMourning, abroad, requirePresent, present, absent, late, early);
                attendRecordList.add(attendRecord);
            }

            return attendRecordList;
        }
        
        /**
         *  priparedstatement作成  出欠の記録
         *  SEM_OFFDAYS='1'の場合、休学日数は「授業日数」「要出席日数」「欠席日数」に含める。
         */
        private static String getAttendSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        SCHREG_ATTENDREC_DAT ");
            stb.append("   WHERE ");
            stb.append("        SCHREGNO = ? ");
            stb.append("        AND YEAR <= ? ");
            stb.append("   GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR, ");
            stb.append("        T1.ANNUAL, ");
            stb.append(        "VALUE(CLASSDAYS,0) AS CLASSDAYS, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            if (param._definecode.schoolmark.substring(0, 1).equals("K")) {
                stb.append("              THEN VALUE(CLASSDAYS,0) - VALUE(ABROAD,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) - VALUE(ABROAD,0) ");
            } else {
                stb.append("              THEN VALUE(CLASSDAYS,0) ");
                stb.append("              ELSE VALUE(CLASSDAYS,0) - VALUE(OFFDAYS,0) ");
            }
            stb.append(             "END AS LESSON, ");
            stb.append(        "VALUE(SUSPEND,0) + VALUE(MOURNING,0) AS SUSPEND_MOURNING, ");
            stb.append(        "VALUE(SUSPEND,0) AS SUSPEND, ");
            stb.append(        "VALUE(MOURNING,0) AS MOURNING, ");
            stb.append(        "VALUE(ABROAD,0) AS ABROAD, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(REQUIREPRESENT,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(REQUIREPRESENT,0) ");
            stb.append(             "END AS REQUIREPRESENT, ");
            stb.append(        "VALUE(PRESENT,0) AS PRESENT, ");
            stb.append(        "CASE WHEN S1.SEM_OFFDAYS = '1' ");
            stb.append(             "THEN VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) + VALUE(OFFDAYS,0) ");
            stb.append(             "ELSE VALUE(SICK,0) + VALUE(ACCIDENTNOTICE,0) + VALUE(NOACCIDENTNOTICE,0) ");
            stb.append(             "END AS ABSENT, ");
            stb.append(        "VALUE(LATE,0) AS LATE, ");
            stb.append(        "VALUE(EARLY,0) AS EARLY ");
            stb.append("FROM    SCHREG_ATTENDREC_DAT T1 ");
            stb.append(        "INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR AND T2.SCHREGNO = T1.SCHREGNO AND T2.ANNUAL = T1.ANNUAL ");
            stb.append(        "LEFT JOIN (SELECT SCHREGNO, YEAR, SUM(LATE) AS LATE, SUM(EARLY) AS EARLY ");
            stb.append(        "           FROM ATTEND_SEMES_DAT ");
            stb.append(        "           GROUP BY SCHREGNO, YEAR ");
            stb.append(        "           ) L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR ");
            stb.append(        "LEFT JOIN SCHOOL_MST S1 ON S1.YEAR = T1.YEAR ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append(        " AND S1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            return stb.toString();
        }
    }
    
    /**
     * 住所データ
     */
    private static class Address {
        final String _issuedate;
        final String _address1;
        final String _address2;
        final String _zipCd;
        final boolean _isPrintAddr2;

        private Address(final String issuedate, final String addr1, final String addr2, final String zip, final boolean isPrintAddr2) {
            _issuedate = issuedate;
            _address1 = addr1;
            _address2 = addr2;
            _zipCd = zip;
            _isPrintAddr2 = isPrintAddr2;
        }
        
        /**
         * 住所履歴を得る
         */
        public static List load(final DB2UDB db2, final Param param, final boolean isGuardian, final Student student, final String startDate, final String endDate) {
            final List addressRecordList = new ArrayList();
            final String sql = isGuardian ? sqlAddress(true, startDate, endDate) : sqlAddress(false, startDate, endDate);
            
            for (final Iterator it = KnjDbUtils.query(db2, sql, new String[] { student._schregno, param._year, }).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String issuedate = getString("ISSUEDATE", row);
                final String address1 = getString("ADDR1", row);
                final String address2 = getString("ADDR2", row);
                final boolean isPrintAddr2 = "1".equals(getString("ADDR_FLG", row));
                final String zipCd = getString("ZIPCD", row);
                
                final Address addressRecord = new Address(issuedate, address1, address2, zipCd, isPrintAddr2);
                addressRecordList.add(addressRecord);
            }
            return addressRecordList;
        }
        
        public static String sqlAddress(final boolean isGuardianAddress, final String startDate, final String endDate) {
            
            StringBuffer stb = new StringBuffer();
            if (isGuardianAddress) {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.GUARD_ADDR1 AS ADDR1, ");
                stb.append("       T1.GUARD_ADDR2 AS ADDR2, ");
                stb.append("       T1.GUARD_ZIPCD AS ZIPCD, ");
                stb.append("       T1.GUARD_ADDR_FLG AS ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       GUARDIAN_ADDRESS_DAT T1  ");
            } else {
                stb.append(" SELECT  ");
                stb.append("       T1.ISSUEDATE, ");
                stb.append("       T1.ADDR1, ");
                stb.append("       T1.ADDR2, ");
                stb.append("       T1.ZIPCD, ");
                stb.append("       T1.ADDR_FLG, ");
                stb.append("       T1.SCHREGNO  ");
                stb.append(" FROM  ");
                stb.append("       SCHREG_ADDRESS_DAT T1  ");
            }
            stb.append("WHERE  ");
            stb.append("       T1.SCHREGNO = ?  ");
            stb.append("       AND FISCALYEAR(ISSUEDATE) <= ?  ");
            if (null != startDate && null != endDate) {
                stb.append("       AND (ISSUEDATE BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR VALUE(EXPIREDATE, '9999-12-31') BETWEEN '" + startDate + "' AND '" + endDate + "' ");
                stb.append("          OR ISSUEDATE <= '" + startDate + "' AND '" + endDate + "' <= VALUE(EXPIREDATE, '9999-12-31') ");
                stb.append("          OR '" + startDate + "' <= ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') <= '" + endDate + "' ) ");
            } else if (null != startDate && null == endDate) {
                stb.append("       AND ('" + startDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR '" + startDate + "' <= ISSUEDATE) ");
            } else if (null == startDate && null != endDate) {
                stb.append("       AND ('" + endDate + "' BETWEEN ISSUEDATE AND VALUE(EXPIREDATE, '9999-12-31') OR ISSUEDATE <= '" + endDate + "') ");
            }
            stb.append("ORDER BY  ");
            stb.append("       ISSUEDATE DESC ");
            return stb.toString();
        }
        public String toString() {
            return "AddressRec(" + _issuedate + "," + _address1 + " " + _address2 + ")";
        }
    }
    
//    /**
//     * 所見データ
//     */
//    private static class HtrainremarkHdat {
//        String _totalstudyact;
//        String _totalstudyval;
//        String _detail2HDatSeq001Remark1;
//        String _detail2HDatSeq002Remark1;
//        
//        public static HtrainremarkHdat load(final DB2UDB db2, final Param param, final String schregno) {
//            HtrainremarkHdat htrainremarkHdat = new HtrainremarkHdat();
//            final String psKey = "PS_HRHD";
//            try {
//                if (null == param._psMap.get(psKey)) {
//                    final StringBuffer stb = new StringBuffer();
//                    stb.append("SELECT ");
//                    stb.append("    TOTALSTUDYACT ");
//                    stb.append("   ,TOTALSTUDYVAL ");
//                    stb.append("FROM    HTRAINREMARK_HDAT T1 ");
//                    stb.append("WHERE   SCHREGNO = ? ");
//
//                    param._psMap.put(psKey, db2.prepareStatement(stb.toString()));
//                }
//                
//            } catch (Exception ex) {
//                log.error("printSvfDetail_1 error!", ex);
//            } finally {
//                db2.commit();
//            }
//            for (final Iterator it = DB.query(db2, param._psMap.get(psKey), new String[] { schregno, }).iterator(); it.hasNext();) {
//                final Map row = (Map) it.next();
//
//                htrainremarkHdat._totalstudyact = getString("TOTALSTUDYACT", row);
//                htrainremarkHdat._totalstudyval = getString("TOTALSTUDYVAL", row); 
//            }
////            PreparedStatement ps = null;
////            ResultSet rs = null;
////            if (param.isTokubetsuShien()) {
////                int p;
////                try {
////                    final String psKey2 = "PS_HRHD_DET2";
////                    if (null == param._psMap.get(psKey2)) {
////                        
////                        final StringBuffer stb = new StringBuffer();
////                        stb.append("SELECT ");
////                        stb.append("    REMARK1 ");
////                        stb.append("FROM    HTRAINREMARK_DETAIL2_HDAT T1 ");
////                        stb.append("WHERE   SCHREGNO = ? ");
////                        stb.append("    AND HTRAIN_SEQ = ? ");
////
////                        param._psMap.put(psKey2, db2.prepareStatement(stb.toString()));
////                    }
////                    ps = param._psMap.get(psKey2);
////                    p = 0;
////                    ps.setString(++p, schregno);
////                    ps.setString(++p, "001");
////
////                    rs = ps.executeQuery();
////                    if (rs.next()) {
////                        htrainremarkHdat._detail2HDatSeq001Remark1 = rs.getString("REMARK1");
////                    }
////                } catch (Exception ex) {
////                    log.error("printSvfDetail_1 error!", ex);
////                } finally {
////                    DbUtils.closeQuietly(rs);
////                    db2.commit();
////                }
////                try {
////                    p = 0;
////                    ps.setString(++p, schregno);
////                    ps.setString(++p, "002");
////                    rs = ps.executeQuery();
////                    if (rs.next()) {
////                        htrainremarkHdat._detail2HDatSeq002Remark1 = rs.getString("REMARK1");
////                    }
////                } catch (Exception ex) {
////                    log.error("printSvfDetail_1 error!", ex);
////                } finally {
////                    DbUtils.closeQuietly(rs);
////                    db2.commit();
////                }
////            }
//            return htrainremarkHdat;
//        }
//    }
    
    /**
     * 総合的な学習の時間の記録・外国語活動の記録
     */
    private static class HTrainRemarkDat {
        final String _year;
        int _g;
        String _totalstudyact;
        String _totalstudyval;
        String _specialActRemark;
        String _totalRemark;
        String _attendrecRemark;
        String _viewremark;
        String _detail2DatSeq001Remark1;
        String _detailTrainRef1;
        String _detailTrainRef2;
        String _detailTrainRef3;
        public HTrainRemarkDat(
                final String year) {
            _year = year;
        }
        
        public static List<HTrainRemarkDat> load(final DB2UDB db2, final Param param, final Student student) {
            final List<HTrainRemarkDat> htrainRemarkDatList = new ArrayList();
            final String psKey = "PS_HRD";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql = getHtrainremarkDatSql();
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                db2.commit();
            }
            
            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] { param._year, student._schregno, })) {

                final String year = getString("YEAR", row);
                if (null == year) {
                    continue;
                }
                final String totalstudyact = getString("TOTALSTUDYACT", row);
                final String totalstudyval = getString("TOTALSTUDYVAL", row);
                final String specialActRemark = getString("SPECIALACTREMARK", row);
                final String totalRemark = getString("TOTALREMARK", row);
                final String attendrecRemark = getString("ATTENDREC_REMARK", row);
                final String viewremark = getString("VIEWREMARK", row);
                
                final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
                htrainremarkDat._g = KNJA133J_0.getG(param, year, getString("ANNUAL", row));
                htrainremarkDat._totalstudyact = totalstudyact;
                htrainremarkDat._totalstudyval = totalstudyval;
                htrainremarkDat._specialActRemark = specialActRemark;
                htrainremarkDat._totalRemark = totalRemark;
                htrainremarkDat._attendrecRemark = attendrecRemark;
                htrainremarkDat._viewremark = viewremark;

                htrainRemarkDatList.add(htrainremarkDat);
            }

//            if ("1".equals(param._train_ref_1_2_3_use_J)) {
//                final String psKey1 = "PS_HRD_DET";
//                try {
//                    if (null == param._psMap.get(psKey1)) {
//                        final String sql = getHtrainremarkDetailDatSql();
//                        param._psMap.put(psKey1, db2.prepareStatement(sql));
//                    }
//                } catch (Exception ex) {
//                    log.error("exception!", ex);
//                }
//                
//                for (final Iterator it = DB.query(db2, param._psMap.get(psKey1), new String[] { param._year, student._schregno, }).iterator(); it.hasNext();) {
//                    final Map row = (Map) it.next();
//                    final String year = getString("YEAR", row);
//                    if (null == year) {
//                        continue;
//                    }
//                    if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
//                        final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
//                        htrainremarkDat._g = KNJA133J_0.getG(param, year, getString("ANNUAL", row));
//                        htrainRemarkDatList.add(htrainremarkDat);
//                    }
//                    final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
//                    htrainremarkDat._detailTrainRef1 = getString("TRAIN_REF1", row);
//                    htrainremarkDat._detailTrainRef2 = getString("TRAIN_REF2", row);
//                    htrainremarkDat._detailTrainRef3 = getString("TRAIN_REF3", row);
//                }
//            }
//            PreparedStatement ps = null;
//            ResultSet rs = null;
//            if (param.isTokubetsuShien()) {
//                try {
//                    final String psKey2 = "PS_HRD_DET2";
//                    if (null == param._psMap.get(psKey2)) {
//                        final String sql = getHtrainremarkDetail2HDatSql();
//                        param._psMap.put(psKey2, db2.prepareStatement(sql));
//                    }
//                    ps = param._psMap.get(psKey2);
//                    int p = 0;
//                    ps.setString(++p, param._year);
//                    ps.setString(++p, student._schregno);
//                    ps.setString(++p, "001");
//
//                    rs = ps.executeQuery();
//                    while (rs.next()) {
//                        final String year = rs.getString("YEAR");
//                        if (null == year) {
//                            continue;
//                        }
//                        if (null == getHTrainRemarkDat(htrainRemarkDatList, year))  {
//                            final HTrainRemarkDat htrainremarkDat = new HTrainRemarkDat(year);
//                            htrainremarkDat._g = KNJA133J_0.getG(param, year, rs.getString("ANNUAL"));
//                            htrainRemarkDatList.add(htrainremarkDat);
//                        }
//                        final HTrainRemarkDat htrainremarkDat = getHTrainRemarkDat(htrainRemarkDatList, year);
//                        htrainremarkDat._detail2DatSeq001Remark1 = rs.getString("REMARK1");
//                    }
//                } catch (Exception ex) {
//                    log.error("exception!", ex);
//                } finally {
//                    DbUtils.closeQuietly(rs);
//                    db2.commit();
//                }
//            }
            return htrainRemarkDatList;
        }
        
        private static String getHtrainremarkDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        YEAR <= ? ");
            stb.append("        AND SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T1.ANNUAL ");
            stb.append("       ,TOTALSTUDYACT ");
            stb.append("       ,TOTALSTUDYVAL ");
            stb.append("       ,SPECIALACTREMARK ");
            stb.append("       ,TOTALREMARK ");
            stb.append("       ,ATTENDREC_REMARK ");
            stb.append("       ,VIEWREMARK ");
            stb.append("       ,BEHAVEREC_REMARK ");
            stb.append("FROM    HTRAINREMARK_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("ORDER BY ");
            stb.append("    T1.YEAR, T1.ANNUAL ");
            return stb.toString();
        }
        
        private static String getHtrainremarkDetailDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,TRAIN_REF1 ");
            stb.append("       ,TRAIN_REF2 ");
            stb.append("       ,TRAIN_REF3 ");
            stb.append("FROM HTRAINREMARK_DETAIL_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            return stb.toString();
        }
        
        private static String getHtrainremarkDetail2HDatSql() {
            StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(YEAR) AS YEAR, SCHREGNO, ANNUAL ");
            stb.append("    FROM ");
            stb.append("        HTRAINREMARK_DAT T1 ");
            stb.append("    WHERE ");
            stb.append("        T1.YEAR <= ? ");
            stb.append("        AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        SCHREGNO, ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT  T1.YEAR ");
            stb.append("       ,T2.ANNUAL ");
            stb.append("       ,REMARK1 ");
            stb.append("FROM HTRAINREMARK_DETAIL2_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.YEAR = T1.YEAR ");
            stb.append("        AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("WHERE ");
            stb.append("    T1.HTRAIN_SEQ = ? ");
            return stb.toString();
        }
    }
    
    /**
     * 行動の記録・特別活動の記録 
     */
    private static class ActRecord {
        final String _year;
        final String _annual;
        final String _record;
        final String _code;
        final String _div;
        public ActRecord(
                final String year,
                final String annual,
                final String record,
                final String code,
                final String div) {
            _year = year;
            _annual = annual;
            _record = record;
            _code = code;
            _div = div;
        }
        
        /**
         *  SVF-FORM 印刷処理 明細
         *  行動の記録・特別活動の記録
         */
        public static List load(final DB2UDB db2, final Param param, final Student student) {
            final List actList = new ArrayList();
//            if (null == param.gakushu && null == param.koudo) {
//                return actList;
//            }
            final String psKey = "PS_ACT";
            try {
                if (null == param._psMap.get(psKey)) {
                    param._psMap.put(psKey, db2.prepareStatement(getActRecordSql(param)));
                }
            } catch (Exception ex) {
                log.error("printSvfActRecord error!", ex);
            }
            
            final PreparedStatement ps = param._psMap.get(psKey);
            for (final Iterator it = KnjDbUtils.query(db2, ps, new String[] { student._schregno, }).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                final String year = getString("YEAR", row);
                final String record = getString("RECORD", row);
                final String annual = getString("ANNUAL", row);;
                final String code = getString("CODE", row);
                final String div = getString("DIV", row);
                
                final ActRecord act = new ActRecord(year, annual, record, code, div);
                actList.add(act);
            }

            return actList;
        }
        
        /**
         *  priparedstatement作成  行動の記録・特別活動の記録
         */
        private static String getActRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD_YEAR AS ( ");
            stb.append("    SELECT ");
            stb.append("        MAX(T1.YEAR) AS YEAR, T1.SCHREGNO, T1.ANNUAL ");
            stb.append("    FROM ");
            stb.append("        BEHAVIOR_DAT T1 ");
            stb.append("    WHERE   T1.YEAR <= '" + param._year + "' ");
            stb.append("          AND T1.SCHREGNO = ? ");
            stb.append("    GROUP BY ");
            stb.append("        T1.SCHREGNO, T1.ANNUAL ");
            stb.append(" ) ");
            stb.append("SELECT ");
            stb.append("     T1.YEAR ");
            stb.append("    ,T1.DIV ");
            stb.append("    ,T1.CODE ");
            stb.append("    ,T1.ANNUAL ");
            stb.append("    ,T1.RECORD ");
            stb.append("FROM    BEHAVIOR_DAT T1 ");
            stb.append("INNER JOIN REGD_YEAR T2 ON T2.SCHREGNO = T1.SCHREGNO ");
            stb.append("    AND T2.ANNUAL = T1.ANNUAL ");
            stb.append("    AND T2.YEAR = T1.YEAR ");
            return stb.toString();
        }
    }
    
    /**
     * 観点の教科
     */
    private static class ClassView {
        final String _classcd;  //教科コード
        final String _classname;  //教科名称
        final String _electdiv;
        final List<ViewSubclass> _viewSubclassList;
        
        public ClassView(
                final String classcd, 
                final String classname, 
                final String electdiv
        ) {
            _classcd = classcd;
            _classname = classname;
            _electdiv = electdiv;
            _viewSubclassList = new ArrayList();
        }
        
        public int getViewNum() {
            int c = 0;
            for (final ViewSubclass viewSubclass : _viewSubclassList) {
                c += viewSubclass._viewList.size();
            }
            return c;
        }
        
        // 教科名のセット
        private String setClassname(final String classname, final Param param) {
            if (classname == null) {
                return "";
            }
            final int viewnum = getViewNum();
            if (viewnum == 0) {
                return classname;
            }
            final int newviewnum;
            if (classname.length() <= viewnum && !"1".equals(param._seitoSidoYorokuCyugakuKantenNoBlank)) {
                newviewnum = viewnum + 1;  // 教科間の観点行に１行ブランクを挿入
            } else {
                newviewnum = viewnum;
            }
            final String newclassname;
            
            if (classname.length() < newviewnum) {
                final int i = (newviewnum - classname.length()) / 2;
                String space = "";
                for (int j = 0; j < i; j++) {
                    space = " " + space;
                }  // 教科名のセンタリングのため、空白を挿入
                newclassname = space + classname;
            } else {
                newclassname = classname;
            }
            return newclassname;
        }
        
        public String toString() {
            return "ViewClass(" + _classcd + ":" + _classname + " e = " + _electdiv + ")";
        }
        
        private static ClassView getClassView(final List classViewList, final String classcd, final String classname, final String electdiv) {
            if (null == classcd) {
                return null;
            }
            ClassView classView = null;
            for (final Iterator it = classViewList.iterator(); it.hasNext();) {
                final ClassView classView0 = (ClassView) it.next();
                if (classView0._classcd.equals(classcd) && classView0._classname.equals(classname) && classView0._electdiv.equals(electdiv)) {
                    classView = classView0;
                    break;
                }
            }
            return classView;
        }
        
        /**
         * 観点のリストを得る
         * @param db2
         * @param param
         * @param schregno
         * @return
         */
        public static List load(final DB2UDB db2, final Param param, final Student student) {
            final List classViewList = new ArrayList();
            final String psKey = "PS_VIEW";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql = getViewRecordSql(param);
                    log.debug(" sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }
            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            }
            
            for (final Map row : KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] { student._schregno, student._schregno, })) {

                //教科コードの変わり目
                final String year = getString("YEAR", row);
                if (null == year) {
                    continue;
                }
                final String classcd = getString("CLASSCD", row);
                final String classname = getString("CLASSNAME", row);
                final String curriculumcd = getString("CURRICULUM_CD", row);
                final String subclasscd = getString("SUBCLASSCD", row);
                final String subclassname = getString("SUBCLASSNAME", row);
                final String viewcd = getString("VIEWCD", row);
                final String viewname = getString("VIEWNAME", row);
                final String status = getString("STATUS", row);
                final String electdiv = getString("ELECTDIV", row);
                final int g = param.getGradeCd(year, getString("GRADE", row)); // 学年
                final String grade = getString("GRADE", row);
                
                ClassView classView = getClassView(classViewList, classcd, classname, electdiv);
                if (null == classView) {
                    classView = new ClassView(classcd, classname, electdiv);
                    classViewList.add(classView);
                }
                ViewSubclass viewSubclass = ViewSubclass.getViewSubclass(classView._viewSubclassList, subclasscd);
                if (null == viewSubclass) {
                    viewSubclass = new ViewSubclass(curriculumcd, subclasscd, subclassname);
                    classView._viewSubclassList.add(viewSubclass);
                }
                View view = View.getView(viewSubclass._viewList, viewcd);
                if (null == view) {
                    view = new View(viewcd, viewname);
                    viewSubclass._viewList.add(view);
                }
                view._viewMap.put(year, new ViewStatus(curriculumcd, status, year, g, grade));
            }

            return classViewList;
        }
        
        /**
         *  priparedstatement作成  成績データ（観点）
         */
        private static String getViewRecordSql(final Param param) {
            
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //観点の表
            stb.append("VIEW_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("      ,CLASSCD ");
                stb.append("      ,SCHOOL_KIND ");
                stb.append("      ,CURRICULUM_CD ");
            }
            stb.append("     ,VIEWCD ");
            stb.append("     ,YEAR ");
            stb.append("     ,STATUS ");
            stb.append("  FROM ");
            stb.append("     JVIEWSTAT_SUB_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("     T1.SCHREGNO = ? ");
            stb.append("    AND T1.YEAR <= '" + param._year + "' ");
            stb.append("    AND T1.SEMESTER = '9' ");
            stb.append("    AND SUBSTR(T1.VIEWCD,3,2) <> '99' ");
            stb.append(") ");
            
            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT  YEAR ");
            stb.append(         ",GRADE  ");
            stb.append("  FROM    SCHREG_REGD_DAT T1  ");
            stb.append("  WHERE   SCHREGNO = ?  ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <='" + param._year + "' ");
            stb.append("                 GROUP BY GRADE)  ");
            stb.append("  GROUP BY YEAR,GRADE  ");
            stb.append(") ");
            
            //メイン表
            stb.append("SELECT ");
            stb.append("    T2.YEAR ");
            stb.append("   ,T2.GRADE ");
            stb.append("   ,VALUE(T3.ELECTDIV, '0') AS ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("   ,T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSCD ");
                stb.append("   ,T2.CURRICULUM_CD ");
                stb.append("   ,T2.SUBCLASSCD AS SUBCLASSCD ");
            } else {
                stb.append("   ,T3.CLASSCD");
                stb.append("   ,'' AS CURRICULUM_CD");
                stb.append("   ,T2.SUBCLASSCD");
            }
            stb.append("   ,CASE WHEN T3.CLASSORDERNAME1 IS NOT NULL THEN T3.CLASSORDERNAME1 ELSE T3.CLASSNAME END AS CLASSNAME ");
            stb.append("   ,CASE WHEN T4.SUBCLASSORDERNAME1 IS NOT NULL THEN T4.SUBCLASSORDERNAME1 ELSE T4.SUBCLASSNAME END AS SUBCLASSNAME");
            stb.append("   ,CASE WHEN T3.SHOWORDER IS NOT NULL THEN T3.SHOWORDER ELSE -1 END AS SHOWORDERCLASS ");
            stb.append("   ,T2.VIEWCD ");
            stb.append("   ,T2.VIEWNAME ");
            stb.append("   ,T1.STATUS ");
            stb.append("FROM  ( SELECT DISTINCT ");
            stb.append("            W2.YEAR ");
            stb.append("          , W2.GRADE ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          , W1.CLASSCD ");
                stb.append("          , W1.SCHOOL_KIND ");
                stb.append("          , W1.CURRICULUM_CD ");
            }
            stb.append("          , W1.SUBCLASSCD ");
            stb.append("          , W1.VIEWCD ");
            stb.append("          , VIEWNAME ");
            stb.append("          , CASE WHEN W1.SHOWORDER IS NOT NULL THEN W1.SHOWORDER ELSE -1 END AS SHOWORDERVIEW ");
            stb.append("        FROM    JVIEWNAME_SUB_MST W1 ");
            stb.append("                INNER JOIN JVIEWNAME_SUB_YDAT W3 ON W3.SUBCLASSCD = W1.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("          AND W3.CLASSCD = W1.CLASSCD ");
                stb.append("          AND W3.SCHOOL_KIND = W1.SCHOOL_KIND ");
                stb.append("          AND W3.CURRICULUM_CD = W1.CURRICULUM_CD ");
            }
            stb.append("          AND W3.VIEWCD = W1.VIEWCD ");
            stb.append("               INNER JOIN SCHREG_DATA W2 ON W2.YEAR = W3.YEAR ");
            stb.append("        WHERE W1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            stb.append("      ) T2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T2.CLASSCD ");
                stb.append("  AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ");
            } else {
                stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = SUBSTR(T2.SUBCLASSCD,1,2)  ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T2.SUBCLASSCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("  AND T4.CLASSCD = T2.CLASSCD ");
                stb.append("  AND T4.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("  AND T4.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN VIEW_DATA T1 ON T1.YEAR = T2.YEAR ");
            stb.append("    AND T1.VIEWCD = T2.VIEWCD  ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    AND T1.CLASSCD = T2.CLASSCD ");
                stb.append("    AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ");
                stb.append("    AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ");
            }
            stb.append("    AND T1.SUBCLASSCD = T2.SUBCLASSCD  ");
            stb.append("ORDER BY ");
            stb.append("    VALUE(SHOWORDERCLASS, -1), ");
            stb.append("    VALUE(T3.ELECTDIV, '0'), ");
            stb.append("    T3.CLASSCD, ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T3.SCHOOL_KIND, ");
            }
            stb.append("    T2.SUBCLASSCD, ");
            stb.append("    VALUE(T2.SHOWORDERVIEW, -1), ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" T2.CURRICULUM_CD, "); // 教育課程の昇順に取得（同一の観点コードの場合、観点名称は教育課程の小さいほうを表示）
            }
            stb.append("    T2.VIEWCD, ");
            stb.append("    T2.GRADE ");
            return stb.toString();
        }
    }
    
    /**
     * 観点科目データ
     */
    private static class ViewSubclass {
        final String _curriculumcd;
        final String _subclasscd;  //科目コード
        final String _subclassname;
        final List<View> _viewList = new ArrayList();
        
        public ViewSubclass(
                final String curriculumcd,
                final String subclasscd,
                final String subclassname
        ) {
            _curriculumcd = curriculumcd;
            _subclasscd = subclasscd;
            _subclassname = subclassname;
        }
        
        private static ViewSubclass getViewSubclass(final List viewSubclassList, final String subclasscd) {
            ViewSubclass subclassView = null;
            for (final Iterator it = viewSubclassList.iterator(); it.hasNext();) {
                final ViewSubclass viewSubclass0 = (ViewSubclass) it.next();
                if (viewSubclass0._subclasscd.equals(subclasscd)) {
                    subclassView = viewSubclass0;
                    break;
                }
            }
            return subclassView;
        }
        
        public String toString() {
            return "Subclass(" + _subclasscd + ":" + _subclassname.toString() + ")";
        }
    }
    
    private static class View {
        final String _viewcd;  //観点コード
        final String _viewname;  //観点コード
        final Map<String, ViewStatus> _viewMap = new HashMap();
        private View(
                final String viewcd, 
                final String viewname
        ) {
            _viewcd = viewcd;
            _viewname = viewname;
        }
        
        private static View getView(final List<View> viewList, final String viewcd) {
            View view = null;
            for (final View view0 : viewList) {
                if (view0._viewcd.equals(viewcd)) {
                    view = view0;
                    break;
                }
            }
            return view;
        }
        
        public String toString() {
            return "View(" + _viewcd + ":" + _viewMap.toString() + ")";
        }
    }
    
    /**
     * 観点データ
     */
    private static class ViewStatus {
        final String _curriculumcd;
        final String _status; //観点
        final String _year;
        final int _g; // 学年
        final String _grade; // 学年
        
        public ViewStatus(
                final String curriculumcd,
                final String status,
                final String year,
                final int g,
                final String grade
        ) {
            _curriculumcd = curriculumcd;
            _status = status;
            _year = year;
            _g = g;
            _grade = grade;
        }
        
        public String toString() {
            return "(" + _year + "/" + _curriculumcd + ":" + StringUtils.defaultString(_status, " ") + ")";
        }
    }
    
    /**
     * 評定データ
     */
    private static class ValueRecord {
        final String _year;
        final int _g;
        final String _grade;
        final String _classCd;
        final String _curriculumCd;
        final String _subclassCd;
        final String _electDiv;
        final String _className;
        final String _subclassName;
        final String _value; //評定
        public ValueRecord(
                final String year,
                final int g, 
                final String grade,
                final String classCd,
                final String curriculumCd,
                final String subclassCd,
                final String electDiv, 
                final String className, 
                final String subclassName, 
                final String value) {
            _year = year;
            _g = g;
            _grade = grade;
            _classCd = classCd;
            _curriculumCd = curriculumCd;
            _subclassCd = subclassCd;
            _electDiv = electDiv;
            _className = className;
            _subclassName = subclassName;
            _value = value;
        }
        public String toString() {
            return "(" + _classCd + ": " + _curriculumCd + ":" + _subclassCd + ", " + _className + ":" + _subclassName + ")";
        }
        
        public static List load(final DB2UDB db2, final Param param, final Student student) {
            final List valueRecordList = new ArrayList();
            final String psKey = "PS_VALUE";
            try {
                if (null == param._psMap.get(psKey)) {
                    final String sql = getValueRecordSql(param);
                    log.debug(" sql = " + sql);
                    param._psMap.put(psKey, db2.prepareStatement(sql));
                }

            } catch (Exception ex) {
                log.error("printSvfDetail_1 error!", ex);
            }
            
            for (final Iterator it = KnjDbUtils.query(db2, param._psMap.get(psKey), new String[] { student._schregno, student._schregno, }).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();

                //教科コードの変わり目
                final String year = getString("YEAR", row);
                if (null == year) {
                    continue;
                }
                final int g = param.getGradeCd(year, getString("GRADE", row)); // 学年
                final String grade = getString("GRADE", row);
                final String electDiv = getString("ELECTDIV", row);
                final String classCd = getString("CLASSCD", row);
                final String curriculumCd = getString("CURRICULUM_CD", row);
                final String subclassCd = getString("SUBCLASSCD", row);
                final String className = getString("CLASSNAME", row);
                final String subclassName = getString("SUBCLASSNAME", row);
                //評定出力
                final String value = getString("VALUE", row);
                
                final ValueRecord valueRecord = new ValueRecord(year, g, grade, classCd, curriculumCd, subclassCd, electDiv, className, subclassName, value);
                valueRecordList.add(valueRecord);
            }
            
            return valueRecordList;
        }
        
        /**
         *  priparedstatement作成  成績データ（評定）
         */
        private static String getValueRecordSql(final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append("WITH ");
            //評定の表
            stb.append(" VALUE_DATA AS( ");
            stb.append("   SELECT ");
            stb.append("        ANNUAL ");
            stb.append("       ,CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("       ,SCHOOL_KIND ");
                stb.append("       ,CURRICULUM_CD ");
            }
            stb.append("       ,SUBCLASSCD ");
            stb.append("       ,YEAR ");
            stb.append("       ,VALUATION AS VALUE ");
            stb.append("   FROM ");
            stb.append("       SCHREG_STUDYREC_DAT T1 ");
            stb.append("   WHERE ");
            stb.append("       T1.SCHREGNO = ? ");
            stb.append("       AND T1.YEAR <= '" + param._year + "' ");
            if (param._hasSCHOOL_MST_SCHOOL_KIND) {
                stb.append("       AND T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            }
            stb.append(" ) ");
            
            //学籍の表
            stb.append(",SCHREG_DATA AS( ");
            stb.append("  SELECT ");
            stb.append("      YEAR ");
            stb.append("     ,ANNUAL  ");
            stb.append("     ,GRADE  ");
            stb.append("  FROM ");
            stb.append("     SCHREG_REGD_DAT T1 ");
            stb.append("  WHERE ");
            stb.append("      SCHREGNO = ? ");
            stb.append("      AND YEAR IN (SELECT  MAX(YEAR)  ");
            stb.append("                 FROM    SCHREG_REGD_DAT  ");
            stb.append("                 WHERE   SCHREGNO = T1.SCHREGNO ");
            stb.append("                     AND YEAR <= '" + param._year + "' ");
            stb.append("                 GROUP BY GRADE) ");
            stb.append("  GROUP BY ");
            stb.append("      YEAR ");
            stb.append("      ,ANNUAL ");
            stb.append("      ,GRADE ");
            stb.append(") ");
            
            //メイン表
            stb.append("SELECT ");
            stb.append("     T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,VALUE(T3.ELECTDIV, '0') AS ELECTDIV ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND AS CLASSCD ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
                stb.append("    ,'' AS CURRICULUM_CD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD, T5.SUBCLASSCD) AS SUBCLASSCD ");
            stb.append("    ,MAX(VALUE(T3.CLASSORDERNAME1, T3.CLASSNAME)) AS CLASSNAME ");
            stb.append("    ,MAX(VALUE(T6.SUBCLASSORDERNAME1, T6.SUBCLASSNAME, T4.SUBCLASSORDERNAME1, T4.SUBCLASSNAME)) AS SUBCLASSNAME ");
            stb.append("    ,MAX(VALUE(T3.SHOWORDER, -1)) AS SHOWORDERCLASS ");
            stb.append("    ,MAX(T5.VALUE) AS VALUE ");
            stb.append("FROM  SCHREG_DATA T2 ");
            stb.append("INNER JOIN VALUE_DATA T5 ON T5.YEAR = T2.YEAR ");
            stb.append("       AND T5.ANNUAL = T2.ANNUAL ");
            stb.append("INNER JOIN CLASS_MST T3 ON T3.CLASSCD = T5.CLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T4.CLASSCD = T5.CLASSCD ");
                stb.append(" AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ");
                stb.append(" AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ");
            }
            stb.append("LEFT JOIN SUBCLASS_MST T6 ON T6.SUBCLASSCD = T4.SUBCLASSCD2 ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append(" AND T6.CLASSCD = T4.CLASSCD ");
                stb.append(" AND T6.SCHOOL_KIND = T4.SCHOOL_KIND ");
                stb.append(" AND T6.CURRICULUM_CD = T4.CURRICULUM_CD ");
            }
            stb.append("GROUP BY ");
            stb.append("    T2.YEAR ");
            stb.append("    ,T2.GRADE ");
            stb.append("    ,VALUE(T3.ELECTDIV, '0') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD, T5.SUBCLASSCD) ");
            stb.append("ORDER BY ");
            stb.append("    SHOWORDERCLASS ");
            stb.append("    ,VALUE(T3.ELECTDIV, '0') ");
            if ("1".equals(param._useCurriculumcd)) {
                stb.append("    ,T5.CLASSCD || '-' || T5.SCHOOL_KIND ");
                stb.append("    ,T5.CURRICULUM_CD ");
            } else {
                stb.append("    ,T5.CLASSCD ");
            }
            stb.append("    ,VALUE(T6.SUBCLASSCD, T5.SUBCLASSCD) ");
            stb.append("    ,T2.GRADE ");
            return stb.toString();
        }
    }
    
    /**
     * 様式
     */
    private static abstract class KNJA133J_0 {
        
        private Param _param;
        
        KNJA133J_0(final Param param) {
            _param = param;
        }
        
        protected Param param() {
            return _param;
        }
        
        protected static void printSvfRenban(final Vrw32alp svf, final String field, final List list) {
            if (null != list) {
                for (int i = 0 ; i < list.size(); i++) {
                    svf.VrsOutn(field, i + 1, (String) list.get(i));
                }
            }
        }
        
        protected static List<String> retDividString(final String targetsrc, final int dividlen, final int dividnum, final Param param) {
            final List<String> lines = retDividString(targetsrc, dividlen, param);
            if (lines.size() > dividnum) {
                return lines.subList(0, dividnum);
            }
            return lines;
        }

        protected static List<String> retDividString(String targetsrc, final int dividlen, final Param param) {
            if (targetsrc == null) {
                return Collections.EMPTY_LIST;
            }
            return KNJ_EditKinsoku.getTokenList(targetsrc, dividlen);
        }
        
        protected void setForm1(final Vrw32alp svf, final String formname, final int n) {
            log.info(" set form = " + formname);
            svf.VrSetForm(formname, n);
        }
        
        /**
         * 名前を印字する
         * @param svf
         * @param name 名前
         * @param fieldData フィールドのデータ
         */
        protected void printName(
                final Vrw32alp svf, 
                final String name,
                final SvfFieldData fieldData) {
            final double charSize = KNJSvfFieldModify.getCharSize(name, fieldData);
            svf.VrAttribute(fieldData._fieldName, "Size=" + charSize);
            svf.VrAttribute(fieldData._fieldName, "Y=" + (int) KNJSvfFieldModify.getYjiku(0, charSize, fieldData));
            svf.VrsOut(fieldData._fieldName, name);
        }
        
        public static int getG(final Param param, final String year, final String annual) {
            int rtn = param.getGradeCd(year, annual);
            if (rtn == -1) {
                if (NumberUtils.isDigits(annual)) {
                    return Integer.parseInt(annual);
                }
            }
            return rtn;
        }
        
        public abstract int getPrintPage(final Student student);

        public static int getPrintPageDefault(final Student student, final Param param) {
            final List personalInfoList = student.getPrintSchregEntGrdHistList(param);
            return personalInfoList.size();
        }

        /**
         *  SVF-FORM 印刷処理
         */
        public abstract int printSvf(final Vrw32alp svf, final Student student);
    }
    
    /**
     * SVF フィールドのデータ (文字の大きさ調整に使用)
     */
    private static class SvfFieldData {
        private final String _fieldName; // フィールド名
        private final int _posx1;   // フィールド左端のX
        private final int _posx2;   // フィールド右端のX
        private final int _height;  // フィールドの高さ(ドット)
        private final int _minnum;  // 最小設定文字数
        private final int _maxnum;  // 最大設定文字数
        private final int _ystart;  // フィールド上端のY
        public SvfFieldData(
                final String fieldName,
                final int posx1,
                final int posx2,
                final int height,
                final int minnum,
                final int maxnum,
                final int ystart) {
            _fieldName = fieldName;
            _posx1 = posx1;
            _posx2 = posx2;
            _height = height;
            _minnum = minnum;
            _maxnum = maxnum;
            _ystart = ystart;
        }
        public int getWidth() {
            return _posx2 - _posx1;
        }
        public String toString() {
            return "[SvfFieldData: fieldname = " + _fieldName + " width = "+ (_posx2 - _posx1) + " , height = " + _height + " , ystart = " + _ystart + " , minnum = " + _minnum + " , maxnum = " + _maxnum + "]";
        }
    }
    
    private static class KNJSvfFieldModify {

        private static final Log log = LogFactory.getLog(KNJSvfFieldModify.class);

        /**
         * 中央割付フィールドで文字の大きさ調整による中心軸のずれ幅の値を得る
         * @param posx1 フィールドの左端X
         * @param posx2 フィールドの右端X
         * @param num フィールド指定の文字数
         * @param charSize 変更後の文字サイズ
         * @return ずれ幅の値
         */
        public static int getModifiedCenteringOffset(final int posx1, final int posx2, final int num, double charSize) {
            final int maxWidth = getStringLengthPixel(charSize, num); // 文字の大きさを考慮したフィールドの最大幅
            final int offset = (maxWidth / 2) - (posx2 - posx1) / 2 + 10;
            return offset;
        }
        
        private static int getStringLengthPixel(final double charSize, final int num) {
            return charSizeToPixel(charSize) * num / 2;
        }
        
        /**
         *  ポイントの設定
         *  引数について  String str : 出力する文字列
         */
        public static double getCharSize(final String str, final SvfFieldData fieldData) {
            return Math.min(pixelToCharSize(fieldData._height), retFieldPoint(fieldData.getWidth(), getStringByteSize(str, fieldData))); //文字サイズ
        }
        
        /**
         * 文字列のバイト数を得る
         * @param str 文字列
         * @return 文字列のバイト数
         */
        private static int getStringByteSize(final String str, final SvfFieldData fieldData) {
            return Math.min(Math.max(KNJ_EditEdit.getMS932ByteLength(str), fieldData._minnum), fieldData._maxnum);
        }
        
        /**
         * 文字サイズをピクセルに変換した値を得る
         * @param charSize 文字サイズ
         * @return 文字サイズをピクセル(ドット)に変換した値
         */
        public static int charSizeToPixel(final double charSize) {
            return (int) Math.round(charSize / 72 * 400);
        }
        
        /**
         * ピクセルを文字サイズに変換した値を得る
         * @param charSize ピクセル
         * @return ピクセルを文字サイズに変換した値
         */
        public static double pixelToCharSize(final double pixel) {
            return pixel / 400 * 72;
        }
        
        /**
         *  Ｙ軸の設定
         *  引数について  int hnum   : 出力位置(行)
         */
        public static long getYjiku(final int hnum, final double charSize, final SvfFieldData fieldData) {
            long jiku = 0;
            try {
                jiku = retFieldY(fieldData._height, charSize) + fieldData._ystart + fieldData._height * hnum;  //出力位置＋Ｙ軸の移動幅
            } catch (Exception ex) {
                log.error("getYjiku error! jiku = " + jiku, ex);
            }
            return jiku;
        }
        
        /**
         *  文字サイズを設定
         */
        private static double retFieldPoint(final int width, final int num) {
            return (double) Math.round((double) width / (num / 2) * 72 / 400 * 10) / 10;
        }
        
        /**
         *  Ｙ軸の移動幅算出
         */
        private static long retFieldY(final int height, final double charSize) {
            return Math.round(((double) height - charSizeToPixel(charSize)) / 2);
        }
    }
    
    private static class ShokenSize {
        int _mojisu;
        int _gyo;
        
        ShokenSize(final int mojisu, final int gyo) {
            _mojisu = mojisu;
            _gyo = gyo;
        }
        
        private static ShokenSize getShokenSize(final String paramString, final int mojisuDefault, final int gyoDefault) {
            final int mojisu = ShokenSize.getParamSizeNum(paramString, 0);
            final int gyo = ShokenSize.getParamSizeNum(paramString, 1);
            if (-1 == mojisu || -1 == gyo) {
                return new ShokenSize(mojisuDefault, gyoDefault);
            }
            return new ShokenSize(mojisu, gyo);
        }
        
        /**
         * "[w] * [h]"サイズタイプのパラメータのwもしくはhを整数で返す
         * @param param サイズタイプのパラメータ文字列
         * @param pos split後のインデクス (0:w, 1:h)
         * @return "[w] * [h]"サイズタイプのパラメータのwもしくはhの整数値
         */
        private static int getParamSizeNum(final String param, final int pos) {
            int num = -1;
            if (StringUtils.isBlank(param)) {
                return num;
            }
            final String[] nums = StringUtils.split(StringUtils.replace(param, "+", " "), " * ");
            if (StringUtils.isBlank(param) || !(0 <= pos && pos < nums.length)) {
                num = -1;
            } else {
                try {
                    num = Integer.valueOf(nums[pos]).intValue();
                } catch (Exception e) {
                    log.error("Exception!", e);
                }
            }
            return num;
        }
        
        public String toString() {
            return "ShokenSize(" + _mojisu + ", " + _gyo + ")";
        }
    }
    
    private static class Param {
        
        final static String SCHOOL_KIND = "J";
        
        final String _year;
        final String _gakki;
        final String _gradeHrclass;
        final String _useSchregRegdHdat;
//        final int _seitoSidoYorokuCyugakuKirikaeNendo;
//        final String _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear;
        final String _output;
        final String[] _categorySelected;
//        final String _inei;
        final Map _paramMap;
        
//        final String seito;
//        final String gakushu;
//        final String koudo;
        
//        final String _simei;
//        final String _schzip;
//        final String _schoolzip;
//        final String _colorPrint;
//        final String _documentroot;
        final String _useCurriculumcd;
        final String _seitoSidoYorokuCyugakuKantenNoBlank;
        
        final KNJDefineCode _definecode; // 各学校における定数等設定
        
        final String _imagePath;
        final String _extension;
        
        private String _schoolName1;
        private String _certifSchoolName;
        private String _schoolZipCd;
        private String _schoolAddr1;
        private String _schoolAddr2;
        
        /** 生年月日に西暦を使用するか */
        final boolean _isSeireki;
        
        final Map<String, String> _gradeCdMap;
        
        final Map<String, PreparedStatement> _psMap = new HashMap();
        
        final boolean _isKumamoto;
        final boolean _isKaijyo;
        final boolean _isKinJunior;
        final boolean _isOomiya;
        final boolean _isChiben; // 智辯和歌山/五條/奈良カレッジ
        final boolean _isMeiji;
        final boolean _isBunkyo;
        final boolean _isSundaikoufu;
        final boolean _isMusashinohigasi;
        
//        /** 卒業した学校の設立区分を表示するか */
//        private boolean _isInstallationDivPrint;
//
//        final String _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J;
//        final String _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J;

        final boolean _hasSchregEntGrdHistComebackDat;
        final boolean _hasAftGradCourseDat;
        final boolean _hasSCHOOL_MST_SCHOOL_KIND;
//        /** FINSCHOOL_MST.FINSCHOOL_TYPE(名称マスタ「L019」、「中学校」「小学校」等)を表示しない */
//        final String _notPrintFinschooltypeName;
//        
//        final Map _slashViewFieldIndexMapList;
//        final Map _slashValueFieldIndexMapList;
//
//        final String _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J;
//        final String _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J;
//        final String _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J;
//        final String _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J;
//        final String _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J;
//        final String _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J;
//        
//        final String _train_ref_1_2_3_use_J;
//        final String _train_ref_1_2_3_field_size_J;
//        final String _train_ref_1_2_3_gyo_size_J;
//        private Integer[] _train_ref_1_2_3_field_size_JMojisu = {new Integer(-1), new Integer(-1), new Integer(-1)};
        
        final List _d065Name1List;
        final Map _d001Abbv1Map;

        public Param(final HttpServletRequest request, final DB2UDB db2, final Map paramMap) {
            
            _year = request.getParameter("YEAR"); // 年度
            _gakki = request.getParameter("GAKKI"); // 学期
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS"); // 学年・組
            _categorySelected = request.getParameterValues("category_selected");
            _output = request.getParameter("OUTPUT");
            
//            seito = request.getParameter("seito");
//            gakushu = request.getParameter("gakushu");
//            koudo = request.getParameter("koudo");
            
//            _simei = request.getParameter("simei"); // 漢字名出力
//            _schzip = request.getParameter("schzip");
//            _schoolzip = request.getParameter("schoolzip");
//            _colorPrint = request.getParameter("color_print");

//            _documentroot = request.getParameter("DOCUMENTROOT"); // 陰影保管場所
            _useSchregRegdHdat = request.getParameter("useSchregRegdHdat");
            _useCurriculumcd = request.getParameter("useCurriculumcd");
//            _seitoSidoYorokuCyugakuKirikaeNendoForRegdYear = request.getParameter("seitoSidoYorokuCyugakuKirikaeNendoForRegdYear");
//            _seitoSidoYorokuCyugakuKirikaeNendo = NumberUtils.isDigits(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) ? Integer.parseInt(request.getParameter("seitoSidoYorokuCyugakuKirikaeNendo")) : 0;
            _seitoSidoYorokuCyugakuKantenNoBlank = request.getParameter("seitoSidoYorokuCyugakuKantenNoBlank");
//            if ("1".equals(request.getParameter("seitoSidoYorokuPrintInei"))) {
//                _inei = "2";
//            } else {
//                _inei = "".equals(request.getParameter("INEI")) ? null : request.getParameter("INEI");
//            }
            _paramMap = paramMap;
            
            _definecode = new KNJDefineCode(); // 各学校における定数等設定
            _definecode.setSchoolCode(db2, _year);
            
//            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
//            _imagePath = null == returnval ? null : returnval.val4; // 写真データ格納フォルダ
//            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _imagePath = "image/stamp";
            _extension = "bmp";
            
            _hasSchregEntGrdHistComebackDat = KnjDbUtils.setTableColumnCheck(db2, "SCHREG_ENT_GRD_HIST_COMEBACK_DAT", null);
            _hasAftGradCourseDat = KnjDbUtils.setTableColumnCheck(db2, "AFT_GRAD_COURSE_DAT", null);
            _hasSCHOOL_MST_SCHOOL_KIND = KnjDbUtils.setTableColumnCheck(db2, "SCHOOL_MST", "SCHOOL_KIND");

            loadSchoolInfo(db2);
            
            _isSeireki = KNJ_EditDate.isSeireki(db2);
            
            _gradeCdMap = getGradeCdMap(db2);
            
            final String z010Name1 = KnjDbUtils.getOne(KnjDbUtils.query(db2, "SELECT NAMESPARE2,NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00'"));
            log.info(" z010 = " + z010Name1);
            _isKumamoto = "kumamoto".equals(z010Name1);
            _isKaijyo = "kaijyo".equals(z010Name1);
            _isKinJunior = "KINJUNIOR".equals(z010Name1);
            _isOomiya = "oomiya".equals(z010Name1);
            _isChiben = "CHIBEN".equals(z010Name1);
            _isMeiji = "meiji".equals(z010Name1);
            _isBunkyo = "bunkyo".equals(z010Name1);
            _isSundaikoufu = "sundaikoufu".equals(z010Name1);
            _isMusashinohigasi = "musashinohigashi".equals(z010Name1);
//
//            _isInstallationDivPrint = !("KINDAI".equals(z010Name1) || "KINJUNIOR".equals(z010Name1) || _isKumamoto);

//            _slashViewFieldIndexMapList = getSlashViewIndexList(db2);
//            _slashValueFieldIndexMapList = getSlashValueIndexMapList(db2);
            
//            _HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYACT_SIZE_J");
//            _HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J                      = request.getParameter("HTRAINREMARK_DAT_TOTALSTUDYVAL_SIZE_J");
//            _HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J                   = request.getParameter("HTRAINREMARK_DAT_SPECIALACTREMARK_SIZE_J");
//            _HTRAINREMARK_DAT_VIEWREMARK_SIZE_J                         = request.getParameter("HTRAINREMARK_DAT_VIEWREMARK_SIZE_J");
//            _HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J                 = request.getParameter("HTRAINREMARK_DAT_INDEPENDENT_REMARK_SIZE_J");
//            _HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J             = request.getParameter("HTRAINREMARK_HDAT_ENT_DISABILITY_REMARK_SIZE_J");
//            _HTRAINREMARK_DAT_TOTALREMARK_SIZE_J                        = request.getParameter("HTRAINREMARK_DAT_TOTALREMARK_SIZE_J");
//            _HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J                   = request.getParameter("HTRAINREMARK_DAT_ATTENDREC_REMARK_SIZE_J");
//            _notPrintFinschooltypeName = request.getParameter("notPrintFinschooltypeName");
            
//            _train_ref_1_2_3_use_J = request.getParameter("train_ref_1_2_3_use_J");
//            _train_ref_1_2_3_field_size_J = request.getParameter("train_ref_1_2_3_field_size_J");
//            _train_ref_1_2_3_gyo_size_J = request.getParameter("train_ref_1_2_3_gyo_size_J");
//            if ("1".equals(_train_ref_1_2_3_use_J)) {
//                final String[] mojisu;
//                final Integer _14 = new Integer(14);
//                if (!StringUtils.isBlank(_train_ref_1_2_3_field_size_J)) {
//                    mojisu = StringUtils.split(_train_ref_1_2_3_field_size_J, "-");
//                } else {
//                    mojisu = new String[] {_14.toString(), _14.toString(), _14.toString()};
//                }
//                _train_ref_1_2_3_field_size_JMojisu = new Integer[] {getIdxInteger(mojisu, 0, _14), getIdxInteger(mojisu, 1, _14), getIdxInteger(mojisu, 2, _14)};
//            }
            
            _d065Name1List = getD065Name1List(db2);
            _d001Abbv1Map = getD001Abbv1Map(db2);
        }
        
        public void setPs(final DB2UDB db2, final String psKey, final String sql) {
            try {
                _psMap.put(psKey, db2.prepareStatement(sql));
            } catch (Exception e) {
                log.fatal("exception!", e);
            }
        }
        
        public void closeStatementQuietly() {
            for (final PreparedStatement ps : _psMap.values()) {
                DbUtils.closeQuietly(ps);
            }
        }
        
        private static Integer getIdxInteger(final String[] nums, final int idx, final Integer def) {
            return nums.length < idx || !NumberUtils.isDigits(nums[idx]) ? def : Integer.valueOf(nums[idx]);
        }
        
        protected static String loadCertifSchoolName(final DB2UDB db2, final Param param) {
            final String certifKindCd = "108";
            final String sql = "SELECT SCHOOL_NAME FROM CERTIF_SCHOOL_DAT WHERE YEAR = '" + param._year + "' AND CERTIF_KINDCD = '" + certifKindCd + "' ";
            final String rtn = KnjDbUtils.getOne(KnjDbUtils.query(db2, sql));
            
            log.debug("CERTIF_SCHOOL_DAT の学校名称=[" + rtn + "]");
            return rtn;
        }
        
        /**
         * 学校データを得る
         */
        private void loadSchoolInfo(final DB2UDB db2) {
            _certifSchoolName = loadCertifSchoolName(db2, this);
            
            String sql = null;
            try {
                
                final Map paramMap = new HashMap();
                if (_hasSCHOOL_MST_SCHOOL_KIND) {
                    paramMap.put("schoolMstSchoolKind", Param.SCHOOL_KIND);
                }
                sql = new KNJ_SchoolinfoSql("10000").pre_sql(paramMap);
            } catch (Throwable e) {
                log.warn("old KNJ_SchoolinfoSql.");
                sql = new KNJ_SchoolinfoSql("10000").pre_sql();
            }
			final String sql1 = sql;
            for (final Iterator it = KnjDbUtils.query(db2, sql1, new String[] { _year, _year, }).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                _schoolName1 = getString("SCHOOLNAME1", row);
                _schoolAddr1 = getString("SCHOOLADDR1", row);
                _schoolZipCd = getString("SCHOOLZIPCD", row);
                _schoolAddr2 = getString("SCHOOLADDR2", row);
            }
        }

//        /**
//         * 帳票作成JAVAクラスをＬＩＳＴへ格納 NO001 Build
//         */
//        private List getKnja133List() {
//            final List rtnList = new ArrayList();
//            rtnList.add(new KNJA133J_1(this)); // 様式１（学籍に関する記録）
//            rtnList.add(new KNJA133J_3(this)); // 様式２（指導に関する記録）
//            rtnList.add(new KNJA133J_4(this)); // 様式３
//            return rtnList;
//        }
        
        protected List<String> getSchregnoList(final DB2UDB db2) {
            final List<String> schregnoList;
            if ("2".equals(_output)) {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT T1.SCHREGNO ");
                stb.append(" FROM SCHREG_REGD_DAT T1 ");
                stb.append(" INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
                stb.append(" WHERE T1.YEAR = '" + _year + "' ");
                stb.append("   AND T1.SEMESTER = '" + _gakki + "' ");
                stb.append("   AND T1.GRADE || T1.HR_CLASS IN " + SQLUtils.whereIn(true, _categorySelected) + " ");
                stb.append("ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ");
                
                schregnoList = KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, stb.toString()), "SCHREGNO");
            } else {
                schregnoList = new ArrayList();
                schregnoList.addAll(Arrays.asList(_categorySelected));
            }
            return schregnoList;
        }
        
//        public String getImageFilePath(final String filename) {
//            String ret = null;
//            try {
//                if (null != _documentroot && null != _imagePath && null != _extension) {
//                    // 写真データ存在チェック
//                    final String path = _documentroot + "/" + _imagePath + "/" + filename + "." + _extension;
//                    final File file = new File(path);
//                    if (file.exists()) {
//                        ret = path;
//                    }
//                }
//            } catch (Exception ex) {
//                log.error("getDocumentroot error!", ex);
//            }
//            return ret;
//        }
        
        public int getGradeCd(final String year, final String grade) {
            final String gradeCd = _gradeCdMap.get(year + grade);
            return NumberUtils.isNumber(gradeCd) ? Integer.parseInt(gradeCd) : -1;
        }
        
//        /**
//         * 写真データ格納フォルダの取得 --NO001
//         */
//        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
//            KNJ_Control.ReturnVal returnval = null;
//            try {
//                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
//                returnval = imagepath_extension.Control(db2);
//            } catch (Exception ex) {
//                log.error("getDocumentroot error!", ex);
//            }
//            return returnval;
//        }
        
        private Map getGradeCdMap(final DB2UDB db2) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT ");
            stb.append("     * ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_GDAT T1 ");
            stb.append(" WHERE ");
            stb.append("     T1.SCHOOL_KIND = '" + Param.SCHOOL_KIND + "' ");
            
            final Map gdatMap = new HashMap();
            for (final Iterator it = KnjDbUtils.query(db2, stb.toString()).iterator(); it.hasNext();) {
                final Map row = (Map) it.next();
                final String year = getString("YEAR", row);
                final String grade = getString("GRADE", row);
                gdatMap.put(year + grade, getString("GRADE_CD", row));
            }
            return gdatMap;
        }
        
//        private boolean isSlashView(final String subclasscd, final String grade, final int line) {
//            final boolean isSlash = getMappedList(getMappedMap(_slashViewFieldIndexMapList, subclasscd), grade).contains(String.valueOf(line));
//            //log.debug(" isSlashView = " + isSlash + " : " + subclasscd + " : " + grade + " : " + line + " / " + _slashViewFieldIndexMapList);
//            return isSlash;
//        }
//
//        private boolean isSlashValue(final String subclasscd, final String grade) {
//            final boolean isSlash = getMappedList(_slashValueFieldIndexMapList, subclasscd).contains(grade);
//            //log.debug(" isSlashValue = " + isSlash + " : " + subclasscd + " : " + grade + " / " + _slashValueFieldIndexMapList);
//            return isSlash;
//        }
//
//        /**
//         * スラッシュを表示する観点フィールド(科目、学年、行)のリスト 
//         */
//        private Map getSlashViewIndexList(final DB2UDB db2) {
//            Map map = new HashMap();
//            String sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN, NAME3 AS LINE FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A036' ";
//            for (final Iterator it = DB.query(db2, sql).iterator(); it.hasNext();) {
//                final Map row = (Map) it.next();
//                if (null != getString("SUBCLASSCD", row) && null != getString("GAKUNEN", row) && NumberUtils.isDigits(getString("LINE", row))) {
//                    getMappedList(getMappedMap(map, getString("SUBCLASSCD", row)), getString("GAKUNEN", row)).add(getString("LINE", row));
//                }
//            }
//            return map;
//        }
//        
//        /**
//         * スラッシュを表示する評定フィールド(科目、学年)のリスト 
//         */
//        private Map getSlashValueIndexMapList(final DB2UDB db2) {
//            Map map = new HashMap();
//            String sql = "SELECT NAME1 AS SUBCLASSCD, NAME2 AS GAKUNEN FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'A037' ";
//            for (final Iterator it = DB.query(db2, sql).iterator(); it.hasNext();) {
//                final Map row = (Map) it.next();
//                if (null != getString("SUBCLASSCD", row) && null != getString("GAKUNEN", row)) {
//                    getMappedList(map, getString("SUBCLASSCD", row)).add(getString("GAKUNEN", row));
//                }
//            }
//            return map;
//        }
        
        private List getD065Name1List(final DB2UDB db2) {
            final String sql = " SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D065' AND NAME1 IS NOT NULL ";
            return KnjDbUtils.getColumnDataList(KnjDbUtils.query(db2, sql), "NAME1");
        }
        
        private Map getD001Abbv1Map(final DB2UDB db2) {
            final String sql = " SELECT NAMECD2, ABBV1 FROM NAME_MST WHERE NAMECD1 = 'D001' AND ABBV1 IS NOT NULL ";
            return KnjDbUtils.getColumnValMap(KnjDbUtils.query(db2, sql), "NAMECD2", "ABBV1");
        }
    }
}
