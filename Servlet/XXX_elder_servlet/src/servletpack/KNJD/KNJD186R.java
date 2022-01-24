/*
 * $Id: ff661f4842404e8b02ec727a6c255ac0a7c90168 $
 *
 * 作成日: 2015/08/04
 * 作成者: maesiro
 *
 * Copyright(C) 2004-2006 ALP Okinawa Co.,Ltd. All rights reserved.
 */
package servletpack.KNJD;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import servletpack.KNJZ.detail.KNJDefineSchool;
import servletpack.KNJZ.detail.KNJServletUtils;
import servletpack.KNJZ.detail.KNJ_Control;
import servletpack.KNJZ.detail.KNJ_EditDate;
import servletpack.KNJZ.detail.KNJ_EditEdit;
import servletpack.KNJZ.detail.KNJ_EditKinsoku;
import servletpack.KNJZ.detail.dao.AttendAccumulate;

/**
 * 洛南高校　高校通知票
 */
public class KNJD186R {

    private static final Log log = LogFactory.getLog(KNJD186R.class);

    private boolean _hasData;

    private static String SEMEALL = "9";
    private static String GAKUNENHYOKA_TESTCD = "9990008";
    private static String HYOTEI_TESTCD = "9990009";

    private Param _param;

    /**
     * @param request リクエスト
     * @param response レスポンス
     */
    public void svf_out(
            final HttpServletRequest request,
            final HttpServletResponse response
    ) throws Exception {

        final Vrw32alp svf = new Vrw32alp();
        DB2UDB db2 = null;
        try {
            response.setContentType("application/pdf");

            svf.VrInit();
            svf.VrSetSpoolFileStream(response.getOutputStream());

            db2 = new DB2UDB(request.getParameter("DBNAME"), "db2inst1", "db2inst1", DB2UDB.TYPE2);
            db2.open();

            _param = createParam(db2, request);

            _hasData = false;

            printMain(db2, svf);
        } catch (final Exception e) {
            log.error("Exception:", e);
        } finally {

            if (!_hasData) {
                svf.VrSetForm("MES001.frm", 0);
                svf.VrsOut("note", "note");
                svf.VrEndPage();
            }

            if (null != db2) {
                db2.commit();
                db2.close();
            }
            svf.VrQuit();
        }
    }

    private void printMain(final DB2UDB db2, final Vrw32alp svf) {

        for (final Iterator sit = Student.getStudentList(db2, _param).iterator(); sit.hasNext();) {
            final Student student = (Student) sit.next();

            log.info(" schregno = " + student._schregno);

            svf.VrSetForm("KNJD186R_1.frm", 1);
            if (_param._isHyousiPrint) {
                printHyoshi(svf, student);
            }
            svf.VrEndPage();

            svf.VrSetForm("KNJD186R_2.frm", 4);
            printMain(svf, student);

            _hasData = true;
        }
    }

    // 表紙印刷
    private void printHyoshi(final Vrw32alp svf, final Student student) {
        svf.VrsOut("NENDO", _param._nendo); // 年度
        if (null != _param._schoolLogoImagePath) {
            svf.VrsOut("SCHOOL_LOGO", _param._schoolLogoImagePath); //
        }
        svf.VrsOut("HR_NAME", "第 " + StringUtils.defaultString(_param._gradeCd, " ") + " 学年　" + StringUtils.defaultString(student._hrClassName1) + " 組　" + student.getAttendno() + " 番"); // 年組番
        final String printName = "1".equals(student._useRealName) ? StringUtils.defaultString(student._realName, student._name) : student._name;
        final int stdKeta = getMS932ByteLength(printName);
        svf.VrsOut("NAME" + (stdKeta <= 20 ? "1" : stdKeta <= 30 ? "2" : "3"), printName); // 氏名
        final int stfKeta = getMS932ByteLength(student._staffname);
        svf.VrsOut("TEACHER_NAME" + (stfKeta <= 20 ? "1" : stfKeta <= 30 ? "2" : "3"), student._staffname); // 氏名
        if (null != student._staffname2) {
            svf.VrAttribute("STAFF2_LINE", "UnderLine=(0,2,1),Keta=21");
            final int substfKeta = getMS932ByteLength(student._staffname2);
            svf.VrsOut("TEACHER_NAME2_" + (substfKeta <= 20 ? "1" : substfKeta <= 30 ? "2" : "3"), student._staffname2); // 氏名
        }
        svf.VrsOut("SCHOOL_NAME", _param._certifSchoolSchoolName); // 学校名
        svf.VrEndPage();
    }

    private String ltrim(final String s) {
        if (null == s) {
            return "";
        }
        final StringBuffer stb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            final char ch = s.charAt(i);
            if (ch != ' ' && ch != '　') {
                stb.append(s.substring(i));
                break;
            }
        }
        return stb.toString();
    }


    // 生徒内容印刷
    private void printMain(final Vrw32alp svf, final Student student) {

        svf.VrsOut("TITLE", _param._nendo + "　" + ltrim(_param._certifSchoolSchoolName) + "　通知簿");
        //svf.VrsOut("ATTEND_NO", student.getAttendno()); // 出席番号
        svf.VrsOut("HR_NAME", "第 " + StringUtils.defaultString(_param._gradeCd, " ") + " 学年　" + StringUtils.defaultString(student._hrClassName1) + " 組　" + student.getAttendno() + " 番"); // 年組番
        svf.VrsOut("NAME" + (getMS932ByteLength(student._name) > 30 ? "2" : "1"), student._name); // 氏名
        final int stfKeta = getMS932ByteLength(student._staffname);
        svf.VrsOut("TEACHER_NAME" + (stfKeta <= 20 ? "1" : stfKeta <= 30 ? "2" : "3"), student._staffname); // 氏名
        if (null != student._staffname2) {
            final int substfKeta = getMS932ByteLength(student._staffname2);
            svf.VrsOut("TEACHER_NAME2_" + (substfKeta <= 20 ? "1" : substfKeta <= 30 ? "2" : "3"), student._staffname2); // 氏名
            svf.VrsOut("TEACHER_NAME_MARU2", "○"); // 学期名
            svf.VrsOut("TEACHER_NAME_IN2", "印"); // 学期名
        }
        if (SEMEALL.equals(_param._semester)) {
            if (student.isPass(_param)) {
                svf.VrsOut("JUDGE", _param._is3nen ? "卒業認定" : "進級認定"); // 判定
            }
        }

        for (int semes = 1; semes <= 3; semes++) {
            final String semestername = _param.getSemesterName(semes == 3 ? SEMEALL : String.valueOf(semes));
            for (int i = 1; i <= 4; i++) {
                svf.VrsOutn("SEMESTER" + String.valueOf(i), semes, semestername); // 学期
            }
        }

        for (int semesi = 0; semesi < _param.getTargetSemes().length; semesi++) {
            final String semester = _param.getTargetSemes()[semesi];
            final String semeAllIf3 = "3".equals(semester) ? SEMEALL : semester;
            final int semesline = semesi + 1;

            // 通信欄
            String communication = StringUtils.defaultString(toString(student._communicationMap.get(semester)));
            String kaigyo = "";
            if (SEMEALL.equals(semeAllIf3)) {
                checkKaikin(student, _param);
                String text = "";
                if (student._isKaikinAll) {
                    text = "三ヵ年皆勤";
                } else if (student._isKaikin){
                    text = "一ヵ年皆勤";
                }
                if (!StringUtils.isBlank(text) && !StringUtils.isBlank(communication)) {
                    kaigyo = "\n";
                }
                communication = text + kaigyo + communication;
            }
            final ShokenSize comSize = ShokenSize.getShokenSize(_param._HREPORTREMARK_DAT_COMMUNICATION_SIZE_H, 27, 4);
            final List token = getTokenList(communication, comSize._mojisu * 2);
            for (int i = 0; i < Math.min(token.size(), comSize._gyo); i++) {
                svf.VrsOutn("COMM" + String.valueOf(i + 1), semesline, toString(token.get(i)));
            }

            // 出欠
            final Map attendSemes = getMappedMap(student._attendMap, _param._year + "-" + semeAllIf3);
            svf.VrsOutn("PRESENT", semesline, toString(attendSemes.get("LESSON"))); // 授業日数
            svf.VrsOutn("SUSPEND", semesline, toString(add(add(toString(attendSemes.get("SUSPEND")), toString(attendSemes.get("VIRUS"))), toString(attendSemes.get("MOURNING"))))); // 出停忌引
            svf.VrsOutn("SICK", semesline, toString(attendSemes.get("SICK_ONLY"))); // 病気
            svf.VrsOutn("NOTICE", semesline, toString(attendSemes.get("NOTICE_ONLY"))); // 事故
            svf.VrsOutn("LATE", semesline, toString(attendSemes.get("LATE"))); // 遅刻
            svf.VrsOutn("EARLY", semesline, toString(attendSemes.get("EARLY"))); // 早退
            svf.VrsOutn("ABSENCE", semesline, toString(attendSemes.get("M_KEKKA_JISU"))); // 欠席
        }

        // 科目
        int classcdi = 0;
        final int max = 22;
        int count = 0;
        final List classCdGroupList = getClassCdGroupList(_param, student._subclassList);
        for (int cli = 0; cli < classCdGroupList.size(); cli++) {
            final Map sameClasscdMap = (Map) classCdGroupList.get(cli);

            classcdi = ((Integer) sameClasscdMap.get("CLSIDX")).intValue();
            final List subclassList = getMappedList(sameClasscdMap, "SUBCLASSLIST");
            final String classname = StringUtils.defaultString((String) sameClasscdMap.get("CLASSNAME"));
            final int keta;
            if (subclassList.size() == 1) {
                keta = getMS932ByteLength(classname) <= 4 ? 4 : getMS932ByteLength(classname) > 8 ? 10 : getMS932ByteLength(classname) > 6 ? 8 : 6;
            } else {
                keta = 6;
            }
            final List classnameSplitList = centering(classname, keta * subclassList.size(), keta);

            for (int subi = 0; subi < subclassList.size(); subi++) {

                final Subclass subclass = (Subclass) subclassList.get(subi);

                if (subi < classnameSplitList.size()) {
                    final String classnameSplit = toString(classnameSplitList.get(subi));
                    //log.info(" split = [" + classnameSplit + "]");
                    if (getMS932ByteLength(classnameSplit) <= 4) {
                        svf.VrsOut("CLASS_NAME1", classnameSplit); // 教科名
                    } else if (getMS932ByteLength(classnameSplit) > 8) {
                        svf.VrsOut("CLASS_NAME4", classnameSplit); // 教科名
                    } else if (getMS932ByteLength(classnameSplit) > 6) {
                        svf.VrsOut("CLASS_NAME3", classnameSplit); // 教科名
                    } else {
                        svf.VrsOut("CLASS_NAME2", classnameSplit); // 教科名
                    }
                }

                svf.VrsOut("GRP1", String.valueOf(classcdi)); // グループコード
                if (null != subclass._subclassname) {
                    if (subclass._subclassname.length() <= 5) {
                        svf.VrsOut("SUBCLASS_NAME1", subclass._subclassname); // 科目名
                    } else if (subclass._subclassname.length() <= 7) {
                        svf.VrsOut("SUBCLASS_NAME2", subclass._subclassname); // 科目名
                    } else if (subclass._subclassname.length() <= 9) {
                        svf.VrsOut("SUBCLASS_NAME3", subclass._subclassname); // 科目名
                    } else {
                        svf.VrsOut("SUBCLASS_NAME3_1", subclass._subclassname.substring(0, 9)); // 科目名
                        svf.VrsOut("SUBCLASS_NAME3_2", subclass._subclassname.substring(9)); // 科目名
                    }
                }

                for (int tii = 0; tii < _param._testitemList.size(); tii++) {
                    final Testitem testitem = (Testitem) _param._testitemList.get(tii);

                    if (null == testitem._semester || Integer.parseInt(testitem._semester) > Integer.parseInt(_param._semester)) {
                        continue;
                    }

                    final Score s = (Score) getMappedMap(student._scoreMap, testitem._semester + testitem._testcd).get(subclass._subclasscd);
                    if (null != s) {
                        if (GAKUNENHYOKA_TESTCD.equals(testitem._semester + testitem._testcd) && "1".equals(s._score)) {
                            // 学年評価は1を表示しない
                        } else {
                            svf.VrsOut("VALUE" + String.valueOf(tii + 1), s._score); // 評価
                        }
                    }
                    final BigDecimal kekka = (BigDecimal) getMappedMap(student._attendSubclassMap, subclass._subclasscd).get(_param._year + "-" + testitem._semester);
                    if (null != kekka) {
                        if (kekka.doubleValue() != 0.0) {
                            svf.VrsOut("KEKKA" + String.valueOf(tii + 1), kekka.toString()); // 欠課
                        }
                    }
                }

                svf.VrEndRecord();
                count += 1;
            }
        }
        if (count == 0 || count % max != 0) {
            // 空行追加
            for (int i = 0; i < max - (count % max); i++) {
                svf.VrsOut("GRP1", String.valueOf(classcdi + i + 1)); // グループコード
                svf.VrEndRecord();
            }
        }
    }

    private static List centering(final String s, final int keta, final int colKeta) {
        final StringBuffer stb = new StringBuffer();
        if (null == s) {
            stb.append(StringUtils.repeat(" ", keta));
        } else {
            final int sketa = getMS932ByteLength(s);
            final int spKeta = (keta - sketa) / 2 - (((keta - sketa) / 2 % 2) == 0 ? 0 : 1);
            final String sp1 = StringUtils.repeat(" ", spKeta);
            final String sp2 = StringUtils.repeat(" ", keta - getMS932ByteLength(sp1) - sketa);
            stb.append(sp1);
            stb.append(s);
            stb.append(sp2);

            //log.info(" keta = " + keta + ", s = " + s + ", sketa = " + sketa + ", " + sp1.length() + ", " + sp2.length());
        }
        final List tokenList = new ArrayList();
        for (final Iterator it = getTokenList(stb.toString(), colKeta).iterator(); it.hasNext();) {
            String token = (String) it.next();
            if (getMS932ByteLength(token) < colKeta) { // スペースを挿入した後に半角分スペースがあまって隣の列とはなれて見える現象への対応
                if (token.startsWith(" ")) {
                    token = StringUtils.repeat(" ", colKeta - getMS932ByteLength(token)) + token;
                }
            }
            tokenList.add(StringUtils.replace(token, "  ", "　"));
        }
        return tokenList;
    }

    private static List getClassCdGroupList(final Param param, final List subclassList) {
        final List rtn = new ArrayList();
        String classkey = null;
        Map current = null;
        for (final Iterator it = subclassList.iterator(); it.hasNext();) {
            final Subclass subclass = (Subclass) it.next();
            if (subclass._subclasscd.startsWith("9")) {
                continue;
            }
            if (param._d026List.contains(subclass._subclasscd)) {
                continue;
            }
            if (null == current || !subclass._classkey.equals(classkey)) {
                current = new HashMap();
                current.put("CLASSKEY", subclass._classkey);
                current.put("CLSIDX", new Integer(rtn.size()));
                current.put("CLASSNAME", subclass._classname);
                rtn.add(current);
                classkey = subclass._classkey;
            }
            getMappedList(current, "SUBCLASSLIST").add(subclass);
        }
        return rtn;
    }
    
    private static Map addAttendMap(final Map m1, final Map m2) {
        if (null == m1 || m1.isEmpty()) {
            return m2;
        }
        if (null == m2 || m2.isEmpty()) {
            return m1;
        }
        final Map rtn = new HashMap();
        final String[] fields = {"LESSON", "VIRUS", "SICK_ONLY", "NOTICE_ONLY", "LATE", "EARLY", "M_KEKKA_JISU", };
        for (int i = 0; i < fields.length; i++) {
            rtn.put(fields[i], add((String) m1.get(fields[i]), (String) m2.get(fields[i])));
        }
        return rtn;
    }

    private static void checkKaikin(final Student student, final Param param) {
        if (param._is3nen && null != student._regdList) {
            student._isKaikinAll = true;
            final Set keySet = new HashSet();
            keySet.add(param._year);
            for (final Iterator it = student._regdList.iterator(); it.hasNext();) {
                final Regd regd = (Regd) it.next();
                keySet.add(regd._year);
            }
            Map total = null;
            for (final Iterator yit = keySet.iterator(); yit.hasNext();) {
                final String kYear = (String) yit.next();
                final Map attendSemes = getMappedMap(student._attendMap, kYear + "-" + SEMEALL);
                total = addAttendMap(total, attendSemes);
                total.put("YEAR", "9999");
            }
            if (student._isKaikinAll) {
                if (!isKaikin(param, student, total)) {
                    student._isKaikinAll = false;
                }
            }
        }
        if (!student._isKaikinAll) {
            final Map attendSemes = getMappedMap(student._attendMap, param._year + "-" + SEMEALL);
            attendSemes.put("YEAR", param._year);
            if (isKaikin(param, student, attendSemes)) {
                student._isKaikin = true;
            }
        }
    }

    private static boolean isKaikin(final Param param, final Student student, final Map attendMap) {
        final int lesson = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("LESSON")), "0"));
//      final int suspendMourning = Integer.parseInt(StringUtils.defaultString(add(toString(attendMap.get("SUSPEND")), toString(attendMap.get("MOURNING"))), "0"));
      final int virus = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("VIRUS")), "0"));
//      final int mourning = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("MOURNING")), "0"));
      final int sick = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("SICK_ONLY")), "0"));
      final int notice = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("NOTICE_ONLY")), "0"));
      final int late = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("LATE")), "0"));
      final int early = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("EARLY")), "0"));
      final int mKekkaJisu = Integer.parseInt(StringUtils.defaultString(toString(attendMap.get("M_KEKKA_JISU")), "0"));
      boolean rtn = true;
      if (lesson <= 0 || virus > 0 || sick + notice > param._kKesseki || late > param._kChikoku || early > param._kSoutai || late + early > param._kChikokusoutai || mKekkaJisu > param._kKekka) {
          rtn = false;
      }
      log.info(" " + rtn + ":" + student._schregno + ":" + student._attendno + " = " + attendMap.get("YEAR") + ", lesson = " + lesson + ", sick = " + (sick + notice) + ", late = " + late + ", early = " + early + ", mKekka = " + mKekkaJisu);
      return rtn;
    }

    /**
     *  文字数を取得
     */
    private static int getMS932ByteLength(final String str) {
        return KNJ_EditEdit.getMS932ByteLength(str);
    }

    private static List getTokenList(final String s, final int keta) {
        return KNJ_EditKinsoku.getTokenList(s, keta);
    }

    private static String toString(final Object o) {
        return null == o ? null : o.toString();
    }

    private static String mkString(final List textList, final String comma1) {
        final StringBuffer stb = new StringBuffer();
        String comma = "";
        for (final Iterator it = textList.iterator(); it.hasNext();) {
            final String text = (String) it.next();
            if (StringUtils.isBlank(text)) {
                continue;
            }
            stb.append(comma).append(text);
            comma = comma1;
        }
        return stb.toString();
    }

    private static Map getMappedMap(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new TreeMap());
        }
        return (Map) map.get(key1);
    }

    private static List getMappedList(final Map map, final String key1) {
        if (!map.containsKey(key1)) {
            map.put(key1, new ArrayList());
        }
        return (List) map.get(key1);
    }

    private static String sishagonyu(final BigDecimal bd) {
        if (null == bd) {
            return null;
        }
        return bd.setScale(1, BigDecimal.ROUND_HALF_UP).toString();
    }

    /**
     * 数値を加算して文字列（両方nullの場合、null）で返す
     * @param num1
     * @param num2
     * @return
     */
    private static String add(String num1, String num2) {
        if (NumberUtils.isNumber(num2)) {
            if (NumberUtils.isNumber(num1)) {
                num1 = new BigDecimal(num1).add(new BigDecimal(num2)).toString();
            } else {
                num1 = num2;
            }
        }
        return num1;
    }

    private static Integer toInteger(final String v) {
        if (NumberUtils.isDigits(v)) {
            return Integer.valueOf(v);
        }
        return null;
    }

    private static Map rsToMap(final ResultSet rs) throws SQLException {
        final Map map = new TreeMap();
        final ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            final String field = meta.getColumnName(i);
            final String data = rs.getString(field);
            map.put(field, data);
        }
        return map;
    }

    private static class Score {
        final String _subclasscd;
        final String _score;
        final BigDecimal _avg;

        Score(
                final String subclasscd,
                final String score,
                final BigDecimal avg) {
            _subclasscd = subclasscd;
            _score = score;
            _avg = avg;
        }
    }

    private static class Subclass {
        final String _classkey;
        final String _subclasscd;
        final String _classname;
        final String _subclassname;
        final String _subclassabbv;
//        final String _requireFlg;
        public Subclass(final String classkey, final String subclasscd, final String classname, final String subclassname, final String subclassabbv, final String requireFlg) {
            _classkey = classkey;
            _subclasscd = subclasscd;
            _classname = classname;
            _subclassname = subclassname;
            _subclassabbv = subclassabbv;
//            _requireFlg = requireFlg;
        }
        private static Subclass getSubclass(final String subclasscd, final List subclassList) {
            for (final Iterator it = subclassList.iterator(); it.hasNext();) {
                final Subclass subclass = (Subclass) it.next();
                if (subclasscd.equals(subclass._subclasscd)) {
                    return subclass;
                }
            }
            return null;
        }
    }

    private static class Student {
        final String _grade;
        final String _hrClass;
        final String _hrName;
        final String _hrClassName1;
        final String _staffname;
        final String _staffname2;
        final String _attendno;
        final String _schregno;
        final String _name;
        final String _realName;
        final String _useRealName;
        final List _subclassList = new ArrayList();
        final Map _scoreMap = new HashMap();
        final Map _attendMap = new HashMap();
        final Map _attendSubclassMap = new HashMap();
        final Map _attendSubclassAbsenceHighMap = new HashMap();
        final Map _communicationMap = new HashMap();
        List _regdList = null;
        boolean _isKaikinAll; // 3年間皆勤
        boolean _isKaikin;

        Student(
            final String grade,
            final String hrClass,
            final String hrName,
            final String hrClassName1,
            final String staffname,
            final String staffname2,
            final String attendno,
            final String schregno,
            final String name,
            final String realName,
            final String useRealName
        ) {
            _grade = grade;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrClassName1 = hrClassName1;
            _staffname = staffname;
            _staffname2 = staffname2;
            _attendno = attendno;
            _schregno = schregno;
            _name = name;
            _realName = realName;
            _useRealName = useRealName;
        }

        // 評定=1がないかつ欠課数上限値オーバーの科目がないなら合格
        public boolean isPass(final Param param) {
            boolean rtn = true;
            final Map subclassHyoteiMap = getMappedMap(_scoreMap, GAKUNENHYOKA_TESTCD);
            for (final Iterator it = subclassHyoteiMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclasscd = (String) e.getKey();
                final Score score = (Score) e.getValue();
                if ("1".equals(score._score)) {
                    log.info("no pass schregno = " + _schregno + ", hyotei = " + score._score + " , subclasscd = " + subclasscd);
                    rtn = false;
                }
            }
            //log.info(" student = " + _schregno + ", map = " + _attendSubclassAbsenceHighMap);
            for (final Iterator it = _attendSubclassMap.entrySet().iterator(); it.hasNext();) {
                final Map.Entry e = (Map.Entry) it.next();
                final String subclasscd = (String) e.getKey();
                final BigDecimal kekka = (BigDecimal) ((Map) e.getValue()).get(param._year + "-" + SEMEALL);
                if (null == kekka || kekka.doubleValue() <= 0.0) {
                    continue;
                }
                final BigDecimal absenceHigh = (BigDecimal) getMappedMap(_attendSubclassAbsenceHighMap, subclasscd).get(param._year + "-" + SEMEALL);
                if (null == absenceHigh) {
                    continue;
                }
                final boolean isKekkaOver = kekka.doubleValue() > absenceHigh.doubleValue();
                if (isKekkaOver) {
                    log.info(" absencehigh schregno = " + _schregno + ", kekka = " + kekka + ", absenceHigh = " + absenceHigh + ", subclasscd = " + subclasscd + ", isKekkaOver? = " + isKekkaOver);
                    rtn = false;
                }
            }
            return rtn;
        }

        public String getAttendno() {
            return NumberUtils.isDigits(_attendno) ? String.valueOf(Integer.parseInt(_attendno)) : StringUtils.defaultString(_attendno);
        }

        public static void setHreportremarkCommunication(final Param param, final DB2UDB db2, final List studentList) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" SELECT SEMESTER, COMMUNICATION ");
            stb.append(" FROM HREPORTREMARK_DAT ");
            stb.append(" WHERE YEAR = '" + param._year + "' ");
            stb.append("   AND SEMESTER <= '" + param._semester + "' ");
            stb.append("   AND SCHREGNO = ? ");

            PreparedStatement ps = null;
            try {
                ps = db2.prepareStatement(stb.toString());

                for (final Iterator it = studentList.iterator(); it.hasNext();) {
                    final Student student = (Student) it.next();
                    ps.setString(1, student._schregno);
                    ResultSet rs = ps.executeQuery();
                    while (rs.next()) {
                        student._communicationMap.put(rs.getString("SEMESTER"), rs.getString("COMMUNICATION"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }

        private static List getStudentList(final DB2UDB db2, final Param param) {
            final StringBuffer stb = new StringBuffer();
            stb.append(" WITH REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("   T1.YEAR, ");
            stb.append("   T1.SEMESTER, ");
            stb.append("   GDAT.GRADE_CD, ");
            stb.append("   GDAT.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.COURSECD, ");
            stb.append("   T1.MAJORCD, ");
            stb.append("   T1.COURSECODE, ");
            stb.append("   HDAT.HR_NAME, ");
            stb.append("   HDAT.HR_CLASS_NAME1, ");
            stb.append("   HRSTF.STAFFNAME, ");
            stb.append("   HRSTF2.STAFFNAME AS STAFFNAME2, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   BASE.NAME, ");
            stb.append("   BASE.REAL_NAME, ");
            stb.append("   CASE WHEN T7.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS USE_REAL_NAME ");
            stb.append(" FROM SCHREG_REGD_DAT T1 ");
            stb.append(" INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = T1.SCHREGNO ");
            stb.append(" INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ");
            stb.append("     AND GDAT.GRADE = T1.GRADE ");
            stb.append(" INNER JOIN SCHREG_REGD_HDAT HDAT ON HDAT.YEAR = T1.YEAR ");
            stb.append("     AND HDAT.SEMESTER = T1.SEMESTER ");
            stb.append("     AND HDAT.GRADE = T1.GRADE ");
            stb.append("     AND HDAT.HR_CLASS = T1.HR_CLASS ");
            stb.append(" LEFT JOIN STAFF_MST HRSTF ON HRSTF.STAFFCD = HDAT.TR_CD1 ");
            stb.append(" LEFT JOIN STAFF_MST HRSTF2 ON HRSTF2.STAFFCD = HDAT.TR_CD2 ");
            stb.append(" LEFT JOIN SCHREG_NAME_SETUP_DAT T7 ON T7.SCHREGNO = T1.SCHREGNO AND T7.DIV = '03' ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
//            stb.append("     AND T1.GRADE || T1.HR_CLASS = '" + param._gradeHrclass + "' ");
            stb.append("     AND T1.SCHREGNO IN " + SQLUtils.whereIn(true, param._categorySelected) + " ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("   T1.GRADE_CD, ");
            stb.append("   T1.GRADE_NAME1, ");
            stb.append("   T1.GRADE, ");
            stb.append("   T1.HR_CLASS, ");
            stb.append("   T1.HR_NAME, ");
            stb.append("   T1.HR_CLASS_NAME1, ");
            stb.append("   T1.STAFFNAME, ");
            stb.append("   T1.STAFFNAME2, ");
            stb.append("   T1.ATTENDNO, ");
            stb.append("   T1.SCHREGNO, ");
            stb.append("   T1.NAME, ");
            stb.append("   T1.REAL_NAME, ");
            stb.append("   T1.USE_REAL_NAME, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND AS CLASSKEY, ");
            stb.append("   T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASSCD, ");
            stb.append("   VALUE(CLM.CLASSORDERNAME2, CLM.CLASSNAME) AS CLASSNAME, ");
            stb.append("   VALUE(SUBM.SUBCLASSORDERNAME2, SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ");
            stb.append("   SUBM.SUBCLASSABBV, ");
//            stb.append("   CRE.REQUIRE_FLG, ");
            stb.append("   TREC.VALUE_DI, ");
            stb.append("   TREC.SEMESTER || TREC.TESTKINDCD || TREC.TESTITEMCD || TREC.SCORE_DIV AS SEM_TESTCD, ");
            stb.append("   TREC.SCORE, ");
            stb.append("   TRANK.AVG ");
            stb.append(" FROM REGD T1 ");
            stb.append(" LEFT JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ");
            stb.append("     AND T2.SEMESTER <= T1.SEMESTER ");
            stb.append("     AND T2.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN CHAIR_DAT T3 ON T3.YEAR = T2.YEAR ");
            stb.append("     AND T3.SEMESTER = T2.SEMESTER ");
            stb.append("     AND T3.CHAIRCD = T2.CHAIRCD ");
            stb.append("     AND T3.CLASSCD < '90' ");
            stb.append(" LEFT JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T3.CLASSCD ");
            stb.append("     AND SUBM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND SUBM.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND SUBM.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append(" LEFT JOIN CLASS_MST CLM ON CLM.CLASSCD = T3.CLASSCD ");
            stb.append("     AND CLM.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append(" LEFT JOIN RECORD_SCORE_DAT TREC ON TREC.YEAR = T3.YEAR ");
            stb.append("     AND TREC.SEMESTER <= '" + param._semester + "' ");
            stb.append("     AND  (TREC.SEMESTER <> '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '08' ");
            stb.append("        OR TREC.SEMESTER  = '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '08' ");
            stb.append("        OR TREC.SEMESTER  = '9' AND TREC.TESTKINDCD = '99' AND TREC.TESTITEMCD = '00' AND TREC.SCORE_DIV = '09') ");
            stb.append("     AND TREC.CLASSCD = T3.CLASSCD ");
            stb.append("     AND TREC.SCHOOL_KIND = T3.SCHOOL_KIND ");
            stb.append("     AND TREC.CURRICULUM_CD = T3.CURRICULUM_CD ");
            stb.append("     AND TREC.SUBCLASSCD = T3.SUBCLASSCD ");
            stb.append("     AND TREC.SCHREGNO = T1.SCHREGNO ");
            stb.append(" LEFT JOIN RECORD_RANK_SDIV_DAT TRANK ON TRANK.YEAR = TREC.YEAR ");
            stb.append("     AND TRANK.SEMESTER = TREC.SEMESTER ");
            stb.append("     AND TRANK.TESTKINDCD = TREC.TESTKINDCD ");
            stb.append("     AND TRANK.TESTITEMCD = TREC.TESTITEMCD ");
            stb.append("     AND TRANK.SCORE_DIV = TREC.SCORE_DIV ");
            stb.append("     AND TRANK.CLASSCD = TREC.CLASSCD ");
            stb.append("     AND TRANK.SCHOOL_KIND = TREC.SCHOOL_KIND ");
            stb.append("     AND TRANK.CURRICULUM_CD = TREC.CURRICULUM_CD ");
            stb.append("     AND TRANK.SUBCLASSCD = TREC.SUBCLASSCD ");
            stb.append("     AND TRANK.SCHREGNO = TREC.SCHREGNO ");
            stb.append(" ORDER BY ");
            stb.append("     GRADE, ");
            stb.append("     HR_CLASS, ");
            stb.append("     ATTENDNO, ");
            stb.append("     VALUE(CLM.SHOWORDER3, 99), ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND, ");
            stb.append("     VALUE(SUBM.SHOWORDER3, 99), ");
            stb.append("     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ");

            final List studentList = new ArrayList();
            PreparedStatement ps = null;
            ResultSet rs = null;
            final Map studentMap = new HashMap();
            try {
                final String sql = stb.toString();
                log.info(" sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    if (null == schregno) {
                        continue;
                    }

                    if (null == studentMap.get(schregno)) {
                        final String grade = rs.getString("GRADE");
                        final String hrClass = rs.getString("HR_CLASS");
                        final String hrName = rs.getString("HR_NAME");
                        final String hrClassName1 = rs.getString("HR_CLASS_NAME1");
                        final String staffname = rs.getString("STAFFNAME");
                        final String staffname2 = rs.getString("STAFFNAME2");

                        String attendno = rs.getString("ATTENDNO");
                        attendno = null == attendno || !NumberUtils.isDigits(attendno) ? "" : String.valueOf(Integer.valueOf(attendno));
                        final String name = rs.getString("NAME");
                        final String realName = rs.getString("REAL_NAME");
                        final String useRealName = rs.getString("USE_REAL_NAME");

                        final Student student = new Student(grade, hrClass, hrName, hrClassName1, staffname, staffname2, attendno, schregno, name, realName, useRealName);

                        studentList.add(student);
                        studentMap.put(schregno, student);
                    }
                    final Student student = (Student) studentMap.get(schregno);

                    final String subclasscd = rs.getString("SUBCLASSCD");
                    if (null == subclasscd) {
                        continue;
                    }

                    if (null == Subclass.getSubclass(subclasscd, student._subclassList)) {
                        final String classkey = rs.getString("CLASSKEY");
                        final String classname = rs.getString("CLASSNAME");
                        final String subclassname = rs.getString("SUBCLASSNAME");
                        final String subclassabbv = rs.getString("SUBCLASSABBV");
                        final String requireFlg = null; // rs.getString("REQUIRE_FLG");

                        final Subclass subclass = new Subclass(classkey, subclasscd, classname, subclassname, subclassabbv, requireFlg);
                        student._subclassList.add(subclass);
                    }

                    final String semtestcd = rs.getString("SEM_TESTCD");

                    if (null != semtestcd) {
                        final String score = null != rs.getString("VALUE_DI") ? rs.getString("VALUE_DI") : rs.getString("SCORE");

                        final Score s = new Score(subclasscd, score, rs.getBigDecimal("AVG"));

                        getMappedMap(student._scoreMap, semtestcd).put(subclasscd, s);
                    }

                }
            } catch (Exception ex) {
                log.fatal("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }

            loadAttendance(db2, param, param._year, param._semester, param._date, studentMap, new HashMap(param._attendParamMap));
            if (SEMEALL.equals(param._semester) && param._is3nen) {
                Regd.getRegdList(db2, param, studentMap);

                final Map regdMap = Regd.getRegdMap(studentList);
                for (final Iterator it = regdMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final String key = (String) e.getKey();
                    final String[] split = StringUtils.split(key, "-");
                    final String date = String.valueOf(Integer.parseInt(split[0]) + 1) + "-03-31";
                    loadAttendance(db2, param, split[0], "9", date, studentMap, new HashMap(param._attendParamMap));
                }
            }

            Student.setHreportremarkCommunication(param, db2, studentList);

            return studentList;
        }

        private static void loadAttendance(
                final DB2UDB db2,
                final Param param,
                final String year,
                final String semester,
                final String date,
                final Map studentMap,
                final Map attendParamMap
        ) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                final String sql = AttendAccumulate.getAttendSemesSql(year, semester, null, date, attendParamMap);
                //log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        final Map semes = getMappedMap(student._attendMap, year + "-" + rs.getString("SEMESTER"));

                        semes.put("LESSON", rs.getString("LESSON"));
                        semes.put("MLESSON", rs.getString("MLESSON"));
                        semes.put("SUSPEND", rs.getString("SUSPEND"));
                        semes.put("MOURNING", rs.getString("MOURNING"));
                        semes.put("SICK_ONLY", rs.getString("SICK_ONLY"));
                        semes.put("NOTICE_ONLY", rs.getString("NOTICE_ONLY"));
                        semes.put("SICK_NOTICE", add(rs.getString("SICK_ONLY"), rs.getString("NOTICE_ONLY")));
                        semes.put("PRESENT", rs.getString("PRESENT"));
                        semes.put("VIRUS", rs.getString("VIRUS"));
                        semes.put("LATE", rs.getString("LATE"));
                        semes.put("EARLY", rs.getString("EARLY"));
                        semes.put("M_KEKKA_JISU", rs.getString("M_KEKKA_JISU"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }

            try {
                final String sql = AttendAccumulate.getAttendSubclassSql(year, semester, null, date, param._attendParamMap);
                //log.debug(" attend sql = " + sql);
                ps = db2.prepareStatement(sql);

                for (final Iterator it = studentMap.entrySet().iterator(); it.hasNext();) {
                    final Map.Entry e = (Map.Entry) it.next();
                    final Student student = (Student) e.getValue();

                    ps.setString(1, student._schregno);
                    rs = ps.executeQuery();

                    while (rs.next()) {
                        getMappedMap(student._attendSubclassMap, rs.getString("SUBCLASSCD")).put(year + "-" + rs.getString("SEMESTER"), "1".equals(rs.getBigDecimal("IS_COMBINED_SUBCLASS")) ? rs.getBigDecimal("REPLACE_SICK") : rs.getBigDecimal("SICK2"));
                        getMappedMap(student._attendSubclassAbsenceHighMap, rs.getString("SUBCLASSCD")).put(year + "-" + rs.getString("SEMESTER"), rs.getBigDecimal("ABSENCE_HIGH"));
                    }
                    DbUtils.closeQuietly(rs);
                }
            } catch (Exception e) {
                log.error("Exception", e);
            } finally {
                DbUtils.closeQuietly(ps);
                db2.commit();
            }
        }
    }

    private static class Regd {
        final Student _student;
        final String _year;
        final String _semester;
        final String _grade;
        final String _gradeCd;
        final String _hrClass;
        final String _hrName;
        final String _hrNameAbbv;
        final String _attendNo;

        public Regd(
                final Student student,
                final String year,
                final String semester,
                final String grade,
                final String gradeCd,
                final String hrClass,
                final String hrName,
                final String hrNameAbbv,
                final String attendNo) {
            _student = student;
            _year = year;
            _semester = semester;
            _grade = grade;
            _gradeCd = gradeCd;
            _hrClass = hrClass;
            _hrName = hrName;
            _hrNameAbbv = hrNameAbbv;
            _attendNo = attendNo;
        }

        private static Map getRegdMap(final List studentList) {
            final Map map = new HashMap();
            for (final Iterator it = studentList.iterator(); it.hasNext();) {
                final Student student = (Student) it.next();
                if (null == student._regdList) {
                    log.warn(" regdList empty : " + student._schregno);
                    continue;
                }
                for (final Iterator git = student._regdList.iterator(); git.hasNext();) {
                    final Regd regd = (Regd) git.next();
                    final String key = regd._year + "-" + regd._semester + "-" + regd._grade + "-" + regd._hrClass;
                    if (null == map.get(key)) {
                        map.put(key, new ArrayList());
                    }
                    ((List) map.get(key)).add(regd);
                }
            }
            return map;
        }

        private static void getRegdList(final DB2UDB db2, final Param param, final Map studentMap) {
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                // HRの生徒を取得
                final String sql = sqlSchregRegdDat(param);
                log.info("schreg_regd_dat sql = " + sql);
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String schregno = rs.getString("SCHREGNO");
                    final Student student = (Student) studentMap.get(schregno);
                    if (null == student) {
                        continue;
                    }
                    if (null == student._regdList) {
                        student._regdList = new ArrayList();
                    }
                    student._regdList.add(new Regd(student, rs.getString("YEAR"), rs.getString("SEMESTER"), rs.getString("GRADE"), rs.getString("GRADE_CD"), rs.getString("HR_CLASS"), rs.getString("HR_NAME"), rs.getString("HR_NAMEABBV"), rs.getString("ATTENDNO")));
                }
            } catch (Exception e) {
                log.fatal("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
        }

        /** 学生を得るSQL */
        private static String sqlSchregRegdDat(final Param param) {
            StringBuffer stb = new StringBuffer();

            stb.append(" WITH T_REGD0 AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, YEAR, MAX(SEMESTER) AS SEMESTER FROM SCHREG_REGD_DAT GROUP BY SCHREGNO, YEAR) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR < '" + param._year + "' ");
            stb.append(" ), T_REGD AS ( ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T1.YEAR, ");
            stb.append("     T1.SEMESTER, ");
            stb.append("     T1.GRADE, ");
            stb.append("     T1.HR_CLASS, ");
            stb.append("     T1.ATTENDNO ");
            stb.append(" FROM ");
            stb.append("     T_REGD0 T1");
            stb.append("     INNER JOIN (SELECT SCHREGNO, GRADE, MAX(YEAR) AS YEAR FROM T_REGD0 GROUP BY SCHREGNO, GRADE) T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ");
            stb.append("     INNER JOIN (SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '" + param._year + "' AND GRADE = '" + param._grade + "') T3 ON T3.SCHOOL_KIND = T4.SCHOOL_KIND ");
            stb.append(" ) ");
            stb.append(" SELECT ");
            stb.append("     T1.SCHREGNO, ");
            stb.append("     T2.NAME, ");
            stb.append("     T2.SEX, ");
            stb.append("     T5.YEAR, ");
            stb.append("     T5.SEMESTER, ");
            stb.append("     T5.GRADE, ");
            stb.append("     REGDG.GRADE_CD, ");
            stb.append("     T5.HR_CLASS, ");
            stb.append("     T5.ATTENDNO, ");
            stb.append("     T3.HR_NAME, ");
            stb.append("     T3.HR_NAMEABBV ");
            stb.append(" FROM ");
            stb.append("     SCHREG_REGD_DAT T1");
            stb.append("     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ");
            stb.append("     LEFT JOIN T_REGD T5 ON T5.SCHREGNO = T1.SCHREGNO ");
            stb.append("     LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T5.YEAR AND REGDG.GRADE = T5.GRADE ");
            stb.append("     INNER JOIN SCHREG_REGD_HDAT T3 ON ");
            stb.append("         T5.YEAR = T3.YEAR ");
            stb.append("         AND T5.SEMESTER = T3.SEMESTER ");
            stb.append("         AND T5.GRADE = T3.GRADE ");
            stb.append("         AND T5.HR_CLASS = T3.HR_CLASS ");
            stb.append(" WHERE ");
            stb.append("     T1.YEAR = '" + param._year + "' ");
            stb.append("     AND T1.SEMESTER = '" + param.getRegdSemester() + "' ");
            stb.append("     AND T1.GRADE = '" + param._grade + "' ");
            stb.append("     AND T1.HR_CLASS = '" + param._gradeHrclass.substring(2) + "' ");
            stb.append(" ORDER BY ");
            stb.append("     T1.YEAR, T1.SEMESTER, T1.GRADE, T1.HR_CLASS, T1.ATTENDNO, T5.YEAR DESC ");
            return stb.toString();
        }
    }

    private static class Testitem {
        final String _semester;
        final String _testcd;
        final String _semestername;
        final String _testitemname;
        public Testitem(final String semester, final String testcd, final String semestername, final String testitemname) {
            _semester = semester;
            _testcd = testcd;
            _testitemname = testitemname;
            _semestername = semestername;
        }

//        public static Testitem getTestItem(final String semTestcd, final List testitemList) {
//            for (final Iterator it = testitemList.iterator(); it.hasNext();) {
//                final Testitem ti = (Testitem) it.next();
//                if ((ti._semester + ti._testcd).equals(semTestcd)) {
//                    return ti;
//                }
//            }
//            return null;
//        }

        public static List getTestitemList(final DB2UDB db2, final Param param) {
            String sql = "";
            sql += " SELECT T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, T2.SEMESTERNAME, T1.TESTITEMNAME ";
            sql += " FROM TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
            sql += " INNER JOIN SEMESTER_MST T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER ";
            sql += " WHERE ";
            sql += "   T1.YEAR = '" + param._year + "' ";
            sql += "   AND T1.TESTKINDCD = '99' ";
            sql += " ORDER BY T1.SEMESTER, T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV ";
            Map cdMap = new HashMap();
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String testcd = rs.getString("TESTCD");
                    final String semestername = rs.getString("SEMESTERNAME");
                    final String testitemname = rs.getString("TESTITEMNAME");
                    final Testitem testitem = new Testitem(semester, testcd, semestername, testitemname);
                    cdMap.put(semester + testcd, testitem);
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            final String[] cds = {"1990008", "2990008", GAKUNENHYOKA_TESTCD};
            List rtn = new ArrayList();
            for (int i = 0; i < cds.length; i++) {
                Testitem testitem = (Testitem) cdMap.get(cds[i]);
                if (null == testitem) {
                    testitem = new Testitem(cds[i].substring(0, 1), cds[i].substring(1), null, null);
                }
                rtn.add(testitem);
            }
            return rtn;
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
    }

    /** パラメータ取得処理 */
    private Param createParam(final DB2UDB db2, final HttpServletRequest request) throws Exception {
        log.fatal("$Revision: 64081 $");
        KNJServletUtils.debugParam(request, log);
        final Param param = new Param(db2, request);
        return param;
    }

    /** パラメータクラス */
    private static class Param {
        final String _year;
        final String _semester;
        final String _paramSemester;
        final String _ctrlSemester;
        final String _grade;
        final String _gradeHrclass;
        final String[] _categorySelected;
        final String _ctrlDate;
        final String _date;
        final String _documentRoot;
        final String _imagePath;
        final String _extension;
        final String _outputDate;
        final String _schoolLogoImagePath;
        final boolean _isHyousiPrint;
        final int _kChikoku;
        final int _kChikokusoutai;
        final int _kSoutai;
        final int _kKekka;
        final int _kKesseki;
        final String _nendo;

        final List _testitemList;
        final String _gradeCd;
        final boolean _is3nen;

        final String _schoolKind;
        final String _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H;

        private KNJDefineSchool _definecode;  //各学校における定数等設定のクラス
        private Map _semesterNameMap;
        private String _useClassDetailDat;
        private List _d026List = new ArrayList();

        private String _certifSchoolSchoolName;
//        private String _certifSchoolJobName;
//        private String _certifSchoolPrincipalName;
        private String _certifSchoolHrJobName;

        private final Map _attendParamMap;

        Param(final DB2UDB db2, final HttpServletRequest request) throws SQLException {
            _year = request.getParameter("CTRL_YEAR");
            _paramSemester = request.getParameter("SEMESTER");
            _definecode = createDefineCode(db2);
            if (String.valueOf(_definecode.semesdiv).equals(_paramSemester)) {
                _semester = "9"; // 最終学期は学年末
            } else {
                _semester = _paramSemester;
            }
            _useClassDetailDat = request.getParameter("useClassDetailDat");
            _ctrlSemester = request.getParameter("CTRL_SEMESTER");
            _gradeHrclass = request.getParameter("GRADE_HR_CLASS");
            _grade = _gradeHrclass.substring(0, 2);
            _categorySelected = request.getParameterValues("category_selected");
            _isHyousiPrint = "1".equals(request.getParameter("HYOUSI"));
            _kChikoku = NumberUtils.isDigits(request.getParameter("KCHIKOKU")) ? Integer.parseInt(request.getParameter("KCHIKOKU")) : 999;
            _kChikokusoutai = NumberUtils.isDigits(request.getParameter("KCHIKOKU_SOUTAI")) ? Integer.parseInt(request.getParameter("KCHIKOKU_SOUTAI")) : 999;
            _kSoutai = NumberUtils.isDigits(request.getParameter("KSOUTAI")) ? Integer.parseInt(request.getParameter("KSOUTAI")) : 999;
            _kKekka = NumberUtils.isDigits(request.getParameter("KKEKKA")) ? Integer.parseInt(request.getParameter("KKEKKA")) : 999;
            _kKesseki = NumberUtils.isDigits(request.getParameter("KKESSEKI")) ? Integer.parseInt(request.getParameter("KKESSEKI")) : 999;
            _nendo = KNJ_EditDate.gengou(db2, Integer.parseInt(_year)) + "年度";

            _ctrlDate = request.getParameter("CTRL_DATE");
            _date = request.getParameter("DATE");
            _documentRoot = request.getParameter("DOCUMENTROOT");
            _HREPORTREMARK_DAT_COMMUNICATION_SIZE_H = request.getParameter("HREPORTREMARK_DAT_COMMUNICATION_SIZE_H");

            final KNJ_Control.ReturnVal returnval = getDocumentroot(db2);
            _imagePath = null == returnval ? null : returnval.val4;
            _extension = null == returnval ? null : returnval.val5; // 写真データの拡張子
            _schoolLogoImagePath = getImageFilePath();
            log.debug(" schoolLogoImagePath = " + _schoolLogoImagePath);
            _outputDate = request.getParameter("OUTPUT_DATE");

            _schoolKind = getSchoolKind(db2);
            _testitemList = Testitem.getTestitemList(db2, this);

            loadSemester(db2, _year);
            loadNameMstD026(db2);
            setCertifSchoolDat(db2);
            _gradeCd = getGradeCd(db2, _grade);
            _is3nen = NumberUtils.isDigits(_gradeCd) && Integer.parseInt(_gradeCd) == 3;

            _attendParamMap = new HashMap();
            _attendParamMap.put("DB2UDB", db2);
            _attendParamMap.put("HttpServletRequest", request);
            _attendParamMap.put("schregno", "?");
            _attendParamMap.put("useCurriculumcd", "1");
            _attendParamMap.put("useTestCountflg", "TESTITEM_MST_COUNTFLG_NEW_SDIV");
            _attendParamMap.put("absenceDiv", "2");
        }
        
        
        private void loadNameMstD026(final DB2UDB db2) {
            
            final StringBuffer sql = new StringBuffer();
            if ("1".equals(_useClassDetailDat)) {
                final String field = "SUBCLASS_REMARK" + (SEMEALL.equals(_semester) ? "4" : String.valueOf(Integer.parseInt(_semester)));
                sql.append(" SELECT CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD FROM SUBCLASS_DETAIL_DAT ");
                sql.append(" WHERE YEAR = '" + _year + "' AND SUBCLASS_SEQ = '007' AND " + field + " = '1'  ");
            } else {
                final String field = SEMEALL.equals(_semester) ? "NAMESPARE1" : "ABBV" + _semester;
                sql.append(" SELECT NAME1 AS SUBCLASSCD FROM V_NAME_MST ");
                sql.append(" WHERE YEAR = '" + _year + "' AND NAMECD1 = 'D026' AND " + field + " = '1'  ");
            }
            
            PreparedStatement ps = null;
            ResultSet rs = null;
            _d026List.clear();
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String subclasscd = rs.getString("SUBCLASSCD");
                    _d026List.add(subclasscd);
                }
            } catch (Exception e) {
                log.error("exception!", e);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
            log.info("非表示科目:" + _d026List);
        }

        public String getSemesterName(final String semester) {
            return (String) _semesterNameMap.get(semester);
        }

        public String getImageFilePath() {
            final String path = _documentRoot + "/" + (null == _imagePath || "".equals(_imagePath) ? "" : _imagePath + "/") + "SCHOOL_LOGO_H." + _extension;
            if (new java.io.File(path).exists()) {
                return path;
            }
            log.warn(" path not exists: " + path);
            return null;
        }

        /**
         * 写真データ格納フォルダの取得 --NO001
         */
        private KNJ_Control.ReturnVal getDocumentroot(final DB2UDB db2) {
            KNJ_Control.ReturnVal returnval = null;
            try {
                KNJ_Control imagepath_extension = new KNJ_Control(); // 取得クラスのインスタンス作成
                returnval = imagepath_extension.Control(db2);
            } catch (Exception ex) {
                log.error("getDocumentroot error!", ex);
            }
            return returnval;
        }

        public String getRegdSemester() {
            return SEMEALL.equals(_semester) ? _ctrlSemester : _semester;
        }

        private String getSchoolKind(final DB2UDB db2) {
            String sql = "";
            sql += " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT ";
            sql += " WHERE ";
            sql += "   YEAR = '" + _year + "' ";
            sql += "   AND GRADE = '" + _grade + "' ";
            String rtn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    rtn = rs.getString("SCHOOL_KIND");
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            }
            return rtn;
        }

        /*
         *  クラス内で使用する定数設定
         */
        private KNJDefineSchool createDefineCode(
                final DB2UDB db2
        ) {
            final KNJDefineSchool definecode = new KNJDefineSchool();
            definecode.defineCode(db2, _year);         //各学校における定数等設定
            //log.debug("semesdiv=" + definecode.semesdiv + "   absent_cov=" + definecode.absent_cov + "   absent_cov_late=" + definecode.absent_cov_late);
            return definecode;
        }

        /**
         * 年度の開始日を取得する
         */
        private void loadSemester(final DB2UDB db2, final String year) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            _semesterNameMap = new HashMap();
            try {
                final String sql;
                sql = "select"
                    + "   SEMESTER,"
                    + "   SEMESTERNAME,"
                    + "   SDATE"
                    + " from"
                    + "   SEMESTER_MST"
                    + " where"
                    + "   YEAR='" + year + "'"
                    + " order by SEMESTER"
                ;

                ps = db2.prepareStatement(sql);
                rs = ps.executeQuery();
                while (rs.next()) {
                    final String semester = rs.getString("SEMESTER");
                    final String name = rs.getString("SEMESTERNAME");
                    _semesterNameMap.put(semester, name);
                }
            } catch (final Exception ex) {
                log.error("テスト項目のロードでエラー", ex);
            } finally {
                db2.commit();
                DbUtils.closeQuietly(null, ps, rs);
            }
        }

        private void setCertifSchoolDat(final DB2UDB db2) {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT SCHOOL_NAME, JOB_NAME, PRINCIPAL_NAME, REMARK2, REMARK4, REMARK5 FROM CERTIF_SCHOOL_DAT ");
            sql.append(" WHERE YEAR = '" + _year + "' AND CERTIF_KINDCD = '104' ");
            log.debug("certif_school_dat sql = " + sql.toString());
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                ps = db2.prepareStatement(sql.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    _certifSchoolSchoolName = rs.getString("SCHOOL_NAME");
//                    _certifSchoolJobName = rs.getString("JOB_NAME");
//                    _certifSchoolPrincipalName = rs.getString("PRINCIPAL_NAME");
                    _certifSchoolHrJobName = rs.getString("REMARK2");
                }
            } catch (Exception ex) {
                log.error("exception!", ex);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            _certifSchoolSchoolName = StringUtils.defaultString(_certifSchoolSchoolName);
//            _certifSchoolJobName = StringUtils.defaultString(_certifSchoolJobName, "学校長");
//            _certifSchoolPrincipalName = StringUtils.defaultString(_certifSchoolPrincipalName);
            _certifSchoolHrJobName = StringUtils.defaultString(_certifSchoolHrJobName, "担任");
        }

        private static String hankakuToZenkaku(final String str) {
            if (null == str) {
                return null;
            }
            final String[] nums = new String[]{"０", "１", "２", "３", "４", "５", "６", "７", "８", "９"};
            final StringBuffer stb = new StringBuffer();
            for (int i = 0; i < str.length(); i++) {
                final String s = String.valueOf(str.charAt(i));
                if (NumberUtils.isDigits(s)) {
                    final int j = Integer.parseInt(s);
                    stb.append(nums[j]);
                } else {
                    stb.append(s);
                }
            }
            return stb.toString();
        }

        private String getGradeCd(final DB2UDB db2, final String grade) {
            String gradeCd = "　";
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                final StringBuffer stb = new StringBuffer();
                stb.append(" SELECT ");
                stb.append("     * ");
                stb.append(" FROM ");
                stb.append("     SCHREG_REGD_GDAT T1 ");
                stb.append(" WHERE ");
                stb.append("     T1.YEAR = '" + _year + "' ");
                stb.append("     AND T1.GRADE = '" + grade + "' ");
                ps = db2.prepareStatement(stb.toString());
                rs = ps.executeQuery();
                if (rs.next()) {
                    final String tmp = rs.getString("GRADE_CD");
                    if (NumberUtils.isDigits(tmp)) {
                        gradeCd = hankakuToZenkaku(String.valueOf(Integer.parseInt(tmp)));
                    }
                }
            } catch (SQLException e) {
                log.error("exception!", e);
            } finally {
                DbUtils.closeQuietly(null, ps, rs);
                db2.commit();
            }
            return gradeCd;
        }

        public String[] getTargetSemes() {
            List semesters = new ArrayList();
            String[] s = {"1", "2", "3"};
            for (int i = 0; i < s.length; i++) {
                if (_semester.compareTo(s[i]) >= 0) {
                    semesters.add(s[i]);
                }
            }
            String[] rtn = new String[semesters.size()];
            semesters.toArray(rtn);
            return rtn;
        }
    }
}

// eof

